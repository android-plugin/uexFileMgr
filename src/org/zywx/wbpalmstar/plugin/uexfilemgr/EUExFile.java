package org.zywx.wbpalmstar.plugin.uexfilemgr;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import org.apache.http.util.EncodingUtils;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.ResoureFinder;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class EUExFile {
	private final static int F_PAGE_PRE = 0;
	private final static int F_PAGE_NEXT = 1;
	private final static int F_PAGE_PERCENT = 2;;

	public final static int F_ERROR_CREATEFILE = 1;
	public final static int F_ERROR_FILE_NOT_EXIST = 2;

	public int m_state = EUExFileMgr.F_STATE_CREATE;

	public BufferedWriter m_fout;
	private RandomAccessFile outputStream;
	private long m_offset;
	private long m_preReadsize;
	private Context m_eContext;
	public InputStream m_inputStream;
	public int m_fileType;
	private String m_inPath;
	public int m_errorType = 0;
	private String m_key = null;

	public EUExFile(int fileType, String inPath, int inMode, Context context,
			String inKey) {
		m_inPath = inPath;
		m_key = inKey;
		init(fileType, inPath, inMode, context);
	}

	protected void init(int fileType, String inPath, int inMode, Context context) {
		if (inMode == 8) {
			inMode = 1;
		}
		m_eContext = context;
		m_fileType = fileType;

		try {
			switch (inMode) {
			case EUExFileMgr.F_FILE_OPEN_MODE_NEW:
			case EUExFileMgr.F_FILE_OPEN_MODE_WRITE:
			case EUExFileMgr.F_FILE_OPEN_MODE_WRITE
					| EUExFileMgr.F_FILE_OPEN_MODE_NEW:
			case EUExFileMgr.F_FILE_OPEN_MODE_NEW
					| EUExFileMgr.F_FILE_OPEN_MODE_READ:
			case EUExFileMgr.F_FILE_OPEN_MODE_READ
					| EUExFileMgr.F_FILE_OPEN_MODE_WRITE:
				File file = new File(inPath);
				if (m_fileType == EUExFileMgr.F_TYPE_DIR) {
					if (!file.exists()) {
						file.mkdirs();
						FileUtility.storageCreateTime2Sp(context, inPath);
					}else {
						FileUtility.storageLastTime2Sp(context, inPath, file.lastModified());
					}
					return;
				}

				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				if (!file.exists()) {
					file.createNewFile();
					FileUtility.storageCreateTime2Sp(context, inPath);
				}else {
					FileUtility.storageLastTime2Sp(context, inPath, file.lastModified());
				}
				break;
			case EUExFileMgr.F_FILE_OPEN_MODE_READ:
				if (inPath.startsWith("/")) {
					File readFile = new File(inPath);
					if (!readFile.exists()) {
						m_errorType = F_ERROR_FILE_NOT_EXIST;
						return;
					}
					m_inputStream = new FileInputStream(readFile);
				} else {
					m_inputStream = m_eContext.getAssets().open(inPath);
				}
				break;
			}

		} catch (SecurityException e) {
			close();
			e.printStackTrace();
			m_errorType = F_ERROR_CREATEFILE;
			Toast.makeText(
					context,
					ResoureFinder.getInstance().getString(m_eContext,
							"error_no_permisson_RW"), Toast.LENGTH_SHORT)
					.show();
		} catch (Exception e) {
			close();
			e.printStackTrace();
			m_errorType = F_ERROR_CREATEFILE;
		}
	}

	/**
	 * 文件指针跳到指定的字节数
	 * 
	 * @param pos
	 *            字节数
	 */
	protected boolean seek(String pos) {
		try {

			if (m_inputStream != null) {
				m_offset = Long.parseLong(pos);
				m_inputStream.skip(m_offset);
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		return false;
	}

	protected boolean seek(long pos) {
		try {

			if (m_inputStream != null) {
				m_offset = pos;
				m_inputStream.skip(m_offset);
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 文件指针跳到开始位置
	 */
	protected boolean seekBegin() {
		return seek("0");
	}

	/**
	 * 文件指针跳到开始最后
	 */
	protected boolean seekEnd() {
		return seek("" + getSize());
	}

	/**
	 * 向文件中写入字符串
	 * 
	 * @param data
	 *            字符串
	 * @return 是否写入成功
	 */
	protected boolean write(String data, int inMode) {
		if (m_inPath == null || m_inPath.length() == 0) {
			return false;
		}
		try {
			if (m_fout != null) {
				m_fout.close();
				m_fout = null;
			}
			boolean mode = false;
			if (1 == inMode) {
				mode = true;
			} else {
				mode = false;
			}
			File file = new File(m_inPath);
			m_fout = new BufferedWriter(new FileWriter(file, mode));
			if (!TextUtils.isEmpty(m_key)) {
				data = Rc4Encrypt.encry_RC4_string(data, m_key);
			}
			m_fout.write(data);
			m_fout.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}

	protected void write(byte[] data) {

		if (m_inPath == null) {
			return;
		}
		try {
			if (outputStream == null) {
				outputStream = new RandomAccessFile(m_inPath, "rw");
			}
			outputStream.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 读文件
	 * 
	 * @param len
	 *            字节数
	 * @return 字符串
	 */
	protected String read(int len) {
		int newLen = len;
		byte[] buffer = null;
		if (newLen != -1) {
			buffer = new byte[newLen];
		}
		try {
			if (m_inputStream != null) {
				if (newLen == -1) {
					buffer = new byte[m_inputStream.available()];
					m_inputStream.read(buffer);
				} else {
					m_inputStream.read(buffer, 0, newLen);
				}
				if (!TextUtils.isEmpty(m_key)) {
					return Rc4Encrypt.decry_RC4(EncodingUtils.getString(buffer, "UTF-8"), m_key);
				}
				return EncodingUtils.getString(buffer, "UTF-8");
			} else {
				if (m_inPath.startsWith("/")) {
					File readFile = new File(m_inPath);
					if (!readFile.exists()) {
						m_errorType = F_ERROR_FILE_NOT_EXIST;
						return null;
					}
					m_inputStream = new FileInputStream(readFile);
				} else {
					m_inputStream = m_eContext.getAssets().open(m_inPath);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 文件的大小
	 * 
	 * @return 文件的大小
	 */
	protected long getSize() {
		try {
			if (m_inputStream != null) {
				return m_inputStream.available();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 得到文件在sdcard的绝对路径
	 * 
	 * @return
	 */
	protected String getFilePath() {
		return m_inPath;
	}

	/**
	 * 关闭文件
	 */
	protected void close() {

		try {
			if (m_fout != null) {
				m_fout.close();
				m_fout = null;
			}
			if (m_inputStream != null) {
				m_inputStream.close();
				m_inputStream = null;
			}
			if (outputStream != null) {
				outputStream.close();
				outputStream = null;
			}
		} catch (IOException e) {
			m_fout = null;
			m_inputStream = null;
			e.printStackTrace();
		}

	}

	private String readerFlip(int type, int len) {

		if (len < 3) {
			return null;
		}
		if (type == F_PAGE_PRE) {

			if (m_offset == 0) {
				return null;
			}
			m_offset = m_offset - len - m_preReadsize;
		}
		long fileLen = getSize();
		if (fileLen == 0) {
			return null;
		}

		if (m_offset >= fileLen) {
			return null;
		}
		if (type == F_PAGE_PERCENT) {
			if (fileLen - m_offset <= 3) {
				m_offset = fileLen - 3;
			}
		}

		if (m_offset < 0) {
			m_offset = 0;
		}
		if (m_offset > 0) {
			seek(m_offset);
		}
		int spaceTimes = 0;
		byte[] bytes = null;
		try {
			byte[] space = "<br/>&nbsp;&nbsp;".getBytes("utf-8");
			ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
			int ch = 0;
			int i = 0;
			if (m_inputStream == null) {
				return null;
			}
			while ((ch = m_inputStream.read()) != -1 && i < len) {
				if (ch == 13) {
					if (i + 17 < len) {
						bytestream.write(space);
						i += 17;
						spaceTimes++;
						continue;
					} else {
						len = i;
						break;
					}

				} else if (ch == 10) {
					continue;
				}
				bytestream.write(ch);
				i++;
			}
			bytes = bytestream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close();
		}

		byte[] resByte = cut(type, bytes, len, spaceTimes);

		return BUtility.transcoding(new String(resByte));
	}

	/**
	 * 电子书 偏移量
	 * 
	 * @return
	 */
	protected long getreaderOffset() {
		return m_offset;
	}

	/**
	 * 电子书 读百分比
	 * 
	 * @param percent
	 *            百分数 （整数）
	 * @param len
	 *            读的字节数
	 * @return 内容
	 */
	protected String readerPercent(int percent, int len) {
		if (m_inputStream == null) {
			init(EUExFileMgr.F_TYPE_FILE, m_inPath, 1, m_eContext);
		}
		m_offset = percent * getSize() / 100;
		return readerFlip(F_PAGE_PERCENT, len);
	}

	/**
	 * 电子书 下一页
	 * 
	 * @param len
	 *            读的字节数
	 * @return 内容
	 */
	protected String readerNext(int len) {
		if (m_inputStream == null) {
			init(EUExFileMgr.F_TYPE_FILE, m_inPath, 1, m_eContext);
		}
		return readerFlip(F_PAGE_NEXT, len);
	}

	/**
	 * 电子书 上一页
	 * 
	 * @param len
	 * @return
	 */
	protected String readerPre(int len) {
		if (m_inputStream == null) {
			init(EUExFileMgr.F_TYPE_FILE, m_inPath, 1, m_eContext);
		}
		return readerFlip(F_PAGE_PRE, len);
	}

	private byte[] cut(int type, byte[] bytes, int bytesCount, int spaceTimes) {

		int endIndex = bytes.length - 1;
		int times = 1;
		boolean isString = testNewString(1, bytes, endIndex, times);
		while (!isString) {
			times++;
			endIndex = endIndex - 1;
			isString = testNewString(1, bytes, endIndex, times);

		}
		if (times > 2) {
			endIndex += 3;
		} else {
			endIndex += 1;
		}
		m_offset += endIndex - 15 * spaceTimes;

		for (int i = endIndex; i < bytes.length; i++) {
			bytes[i] = 0;
		}
		// 头 byte
		int headIndex = 0;
		times = 1;
		isString = testNewString(0, bytes, headIndex, times);
		while (!isString) {
			times++;
			if (times == 4) {
				headIndex = 0;
			}
			headIndex = headIndex + 1;
			isString = testNewString(0, bytes, headIndex, times);

		}

		if (times == 3) {
			char[] chars = new String(bytes, headIndex, 1).toCharArray();
			if (chars[0] >= 0 && chars[0] < 127) {

				headIndex = 2;
			} else {
				headIndex = 0;
			}
		}

		byte[] newByte = new byte[endIndex - headIndex];
		System.arraycopy(bytes, headIndex, newByte, 0, newByte.length);
		m_preReadsize = endIndex - 15 * spaceTimes - headIndex;
		return newByte;

	}

	private boolean testNewString(int type, byte[] bytes, int index, int times) {
		try {
			if (times < 3) {
				char[] chars = new String(bytes, index, 1).toCharArray();
				if (chars[0] >= 0 && chars[0] < 127) {
					return true;
				}
			} else if (times == 3) {
				char[] chars = new String(bytes, index, 1).toCharArray();
				if (chars[0] >= 0 && chars[0] < 127) {
					return true;
				}
				if (type == 0) {
					index = 0;
				}
				String newString = new String(bytes, index, 3, "utf-8");
				boolean ishan = isMessyCode(newString);
				return !ishan;
			} else {
				String newString = new String(bytes, index, 3, "utf-8");
				boolean ishan = isMessyCode(newString);
				return !ishan;
			}
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return false;
	}

	/**
	 * 判断是否为乱码
	 * 
	 * @param str
	 * @return
	 */
	private boolean isMessyCode(String str) {
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			// 当从Unicode编码向某个字符集转换时，如果在该字符集中没有对应的编码，则得到0x3f（即问号字符?）
			// 从其他字符集向Unicode编码转换时，如果这个二进制数在该字符集中没有标识任何的字符，则得到的结果是0xfffd
			if ((int) c == 0xfffd) {
				// 存在乱码
				return true;
			}
		}
		return false;
	}
}