package org.zywx.wbpalmstar.plugin.uexfilemgr;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import org.zywx.wbpalmstar.base.BDebug;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

public class FileDao {
	public static final int STATE_FAIL = 0;
	public static final int STATE_SUCCESS = 1;
	public static final int STATE_EXIST = 2;
	public static final int STATE_NOT_EXIST = 3;
	private Collator collator;
	private Context context;

	public FileDao(Context context) {
		this.context = context;
		collator = Collator.getInstance(Locale.CHINA);
	}

	public static String getSDcardPath() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return "/sdcard/";
		}
		return null;
	}

	/**
	 * 获得SD卡总空间字节数 -1为SD卡不可用
	 * 
	 * @return
	 */
	public long getSDcardTotalSpace() {
		String sdPath = getSDcardPath();
		if (sdPath != null) {
			StatFs fs = new StatFs(sdPath);
			return fs.getBlockSize() * fs.getBlockCount();
		} else {
			return -1L;
		}
	}

	/**
	 * 获得SD卡可用字节数
	 * 
	 * @return
	 */
	public long getSDcardFreeSpace() {
		final String sdPath = getSDcardPath();
		if (sdPath != null) {
			final StatFs fs = new StatFs(sdPath);
			return 1L * fs.getBlockSize() * fs.getAvailableBlocks();
		} else {
			return -1L;
		}
	}

	public ArrayList<FileBean> getFileList(File path) {
		if (path == null || !path.exists() || path.isFile()) {
			return null;
		}
		ArrayList<FileBean> arrayList = null;
		try {
			final File[] fileArray = path.listFiles();
			if (fileArray == null) {
				return null;
			}
			int length = fileArray.length;
			arrayList = new ArrayList<FileBean>(length);
			for (int i = 0; i < length; i++) {
				FileBean bean = new FileBean(context, fileArray[i]);
				arrayList.add(bean);
			}
			Collections.sort(arrayList, new Comparator<FileBean>() {

				public int compare(FileBean file1, FileBean file2) {
					return collator.compare(file1.getFile().getName(), file2.getFile().getName());
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return arrayList;
	}

	public static long getFileSize(File file) {
		long count = 0;
		if (file == null) {
			return 0;
		}
		try {
			if (file.exists()) {
				if (file.isDirectory()) {
					File[] list = file.listFiles();
					if (list != null && list.length > 0) {
						for (File item : list) {
							count += getFileSize(item);
							item = null;
						}
					}
					count += file.length();
					list = null;
				} else {
					count = file.length();
				}
			}
			file = null;
		} catch (Exception e) {
			count = 0;
		}
		return count;
	}

	public boolean createFile(File file) throws IOException {
		boolean isSuccessed = false;
		if (file == null) {
			throw new NullPointerException("file can not be null.........");
		}
		if (!file.exists()) {
			if (file.isDirectory()) {
				isSuccessed = file.mkdir();
			} else {
				isSuccessed = file.createNewFile();
			}
		} else {
			isSuccessed = true;
		}
		return isSuccessed;
	}

	/**
	 * delete a file/folder
	 * 
	 * @param file
	 *            the file object want to be deleted
	 * @return true-->deleted success, false-->deleted fail.
	 */
	public boolean deleteFile(File file) {
		if (file == null) {
			throw new NullPointerException("file can not be null!");
		}
		boolean isDeleted = false;
		try {
			if (file.exists()) {// file exist
				if (file.isDirectory()) {// is a folder
					File[] files = file.listFiles();
					if (files == null || files.length == 0) {// empty folder
						isDeleted = file.delete();
						files = null;
					} else {// contain folder or file
						for (File item : files) {
							if (!deleteFile(item)) {
								item = null;
								break;
							}
							item = null;
						}
						files = null;
						isDeleted = file.delete();
						file = null;
						return isDeleted;
					}
				} else {// is a file
					if (file.canWrite()) {
						isDeleted = file.delete();
					}
				}
			} else {// file not exist!
				isDeleted = true;
			}
		} catch (SecurityException e) {
			BDebug.e("FileDao--->deleteFile():", e.getMessage());
			isDeleted = false;
		}
		return isDeleted;
	}

	public byte[] readFile(File file, int buffSize) {
		byte[] data = null;
		if (file == null) {
			throw new NullPointerException("File can not be null..........");
		}
		if (buffSize <= 0) {
			throw new IllegalArgumentException("buffSize can not less than zero");
		}
		FileInputStream fis = null;
		ByteArrayOutputStream baos = null;
		try {
			fis = new FileInputStream(file);
			baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[buffSize];
			int actualSize = 0;
			while ((actualSize = fis.read(buffer)) != -1) {
				baos.write(buffer, 0, actualSize);
			}
			data = baos.toByteArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return data;
	}

	public boolean writeFile(File target, byte[] data) {
		boolean isSuccessed = false;
		if (target == null || data == null) {
			throw new NullPointerException("params can not be null........");
		}
		if (target.isDirectory()) {
			throw new IllegalArgumentException("target can only be file!...........");
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(target);
			fos.write(data);
			isSuccessed = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return isSuccessed;
	}

	public boolean copyFile(File source, File target) {
		boolean isSuccessed = false;
		if (source == null || target == null) {
			throw new NullPointerException("source file and target file can not be null!......");
		}
		if (source.isDirectory() || target.isDirectory()) {
			throw new IllegalArgumentException("source and target can only be file..........");
		}
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(source);
			fos = new FileOutputStream(target);
			int actualSize = 0;
			byte[] buffer = new byte[getBufferSizeByFileSize(source.length())];
			while ((actualSize = fis.read(buffer)) != -1) {
				fos.write(buffer, 0, actualSize);
			}
			buffer = null;
			isSuccessed = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return isSuccessed;
	}

	public boolean copyDirectory(File source, File target) {
		boolean isSuccessed = false;
		if (source == null || target == null) {
			throw new NullPointerException("source file and target file can not be null!......");
		}
		if (!source.exists() || !target.isDirectory()) {
			throw new IllegalArgumentException("source not exist or target is not a  directory....");
		}
		if (!source.canRead() || !source.canWrite() || !target.canWrite()) {
			throw new SecurityException("file can not read or write.....");
		}
		try {
			if (source.isDirectory()) {// copy folder
				File[] list = source.listFiles();
				for (File item : list) {
					if (item.isDirectory()) {

					} else {

					}
				}

			} else {// copy file
				isSuccessed = copyFile(source, target);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}

		return isSuccessed;
	}

	public int isExistFileInDirectory(File directory, File file) {
		int state = STATE_NOT_EXIST;
		if (directory == null || file == null) {
			throw new NullPointerException("params can no be null......");
		}
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("directory must be a folder........");
		}
		try {
			File[] list = directory.listFiles();
			if (list != null && list.length > 0) {
				for (File item : list) {
					if (item.getName().equals(file.getName())) {
						state = STATE_EXIST;
						break;
					}
				}
			}
		} catch (Exception e) {
			state = STATE_FAIL;// 文件系统无法读写
		}
		return state;
	}

	public static int getBufferSizeByFileSize(long length) {
		if (length < 0) {
			throw new IllegalArgumentException("length can not less than zero.........");
		}
		if (length == 0) {
			return 0;
		}
		if (length > 104857600) {// 100MB
			return 1048576;// 1MB
		}
		if (length > 1048576) {// 1MB
			return 327680;// 32K
		}
		return 4096;// 4K
	}

}