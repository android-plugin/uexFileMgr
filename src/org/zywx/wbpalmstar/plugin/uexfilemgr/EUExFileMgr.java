package org.zywx.wbpalmstar.plugin.uexfilemgr;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

public class EUExFileMgr extends EUExBase {

	public static final String tag = "uexFileMgr_";
	private static final String F_CALLBACK_NAME_CREATEFILE = "uexFileMgr.cbCreateFile";
	private static final String F_CALLBACK_NAME_CREATEDIR = "uexFileMgr.cbCreateDir";
	private static final String F_CALLBACK_NAME_GETFILECREATETIME = "uexFileMgr.cbGetFileCreateTime";
    private static final String F_CALLBACK_NAME_RENAMEFILE = "uexFileMgr.cbRenameFile";
	private static final String F_CALLBACK_NAME_OPENFILE = "uexFileMgr.cbOpenFile";
	private static final String F_CALLBACK_NAME_ISFILEEXISTBYPATH = "uexFileMgr.cbIsFileExistByPath";
	private static final String F_CALLBACK_NAME_ISFILEEXISTBYID = "uexFileMgr.cbIsFileExistById";
	private static final String F_CALLBACK_NAME_GETFILETYPEBYPATH = "uexFileMgr.cbGetFileTypeByPath";
	private static final String F_CALLBACK_NAME_GETFILETYPEBYID = "uexFileMgr.cbGetFileTypeById";
	public static final String F_CALLBACK_NAME_EXPLORER = "uexFileMgr.cbExplorer";
	public static final String F_CALLBACK_NAME_MULTI_EXPLORER = "uexFileMgr.cbMultiExplorer";
	private static final String F_CALLBACK_NAME_READFILE = "uexFileMgr.cbReadFile";
	private static final String F_CALLBACK_NAME_GETFILESIZE = "uexFileMgr.cbGetFileSize";
	private static final String F_CALLBACK_NAME_GETFILEPATH = "uexFileMgr.cbGetFilePath";
	private static final String F_CALLBACK_NAME_GETFILEREALPATH = "uexFileMgr.cbGetFileRealPath";
	private static final String F_CALLBACK_NAME_GETREADEROFFSET = "uexFileMgr.cbGetReaderOffset";
	private static final String F_CALLBACK_NAME_READPERCENT = "uexFileMgr.cbReadPercent";
	private static final String F_CALLBACK_NAME_READNEXT = "uexFileMgr.cbReadNext";
	private static final String F_CALLBACK_NAME_READPRE = "uexFileMgr.cbReadPre";
	private static final String  F_CALLBACK_NAME_CREATESECURE =  "uexFileMgr.cbCreateSecure";
	private static final String  F_CALLBACK_NAME_OPENSECURE = "uexFileMgr.cbOpenSecure";
	

	private static final String F_CALLBACK_NAME_DELETEFILEBYPATH = "uexFileMgr.cbDeleteFileByPath";
	private static final String F_CALLBACK_NAME_DELETEFILEBYID = "uexFileMgr.cbDeleteFileByID";

	public static final int F_FILE_OPEN_MODE_READ = 0x1;
	public static final int F_FILE_OPEN_MODE_WRITE = 0x2;
	public static final int F_FILE_OPEN_MODE_NEW = 0x4;

	public static final int F_FILE_WRITE_MODE_RESTORE = 0x0;
	public static final int F_FILE_WRITE_MODE_APPEND = 0x1;

	public static final int F_TYPE_FILE = 0;
	public static final int F_TYPE_DIR = 1;

	public static final int F_STATE_CREATE = 0;
	public static final int F_STATE_OPEN = 1;

	public static final int F_ACT_REQ_CODE_UEX_FILE_MULTI_EXPLORER = 8;
	public static final int F_ACT_REQ_CODE_UEX_FILE_EXPLORER = 3;

	private HashMap<Integer, EUExFile> objectMap = new HashMap<Integer, EUExFile>();
	Context m_context;
	private ResoureFinder finder;

	public EUExFileMgr(Context context, EBrowserView view) {
		super(context, view);
		m_context = context;
		finder = ResoureFinder.getInstance(mContext);
	}

	native static int encode_by_userkey();

	private boolean testFileError(int type, int inOpCode, String path) {
		switch (type) {
		case EUExFile.F_ERROR_CREATEFILE:
			errorCallback(
					inOpCode,
					EUExCallback.F_E_UEXFILEMGR_CREATEFILE_6,
					ResoureFinder.getInstance().getString(mContext,
							"error_parameter"));
			return true;
		case EUExFile.F_ERROR_FILE_NOT_EXIST:
			errorCallback(
					inOpCode,
					EUExCallback.F_E_UEXFILEMGR_OPENFILE_2,
					ResoureFinder.getInstance().getString(mContext,
							"error_file_does_not_exist"));
			return true;
		}
		return false;
	}

	private boolean testNull(WWidgetData wgtData, String path, int inOpCode) {

		if (path == null || path.length() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 创建一个文件对象
	 * @return 文件对象
	 */

	public void createFile(String[] parm) {
		if (parm.length != 2) {
			return;
		}
		String inOpCode = parm[0], inPath = parm[1];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		if (objectMap.containsKey(Integer.parseInt(inOpCode))) {
			jsCallback(F_CALLBACK_NAME_CREATEFILE, Integer.parseInt(inOpCode),
					EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
			return;
		}
		if (testNull(mBrwView.getCurrentWidget(), inPath,
				Integer.parseInt(inOpCode))) {
			jsCallback(F_CALLBACK_NAME_CREATEFILE, Integer.parseInt(inOpCode),
					EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
			return;
		}
		inPath = BUtility.makeRealPath(
				BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
				mBrwView.getCurrentWidget().m_widgetPath,
				mBrwView.getCurrentWidget().m_wgtType);
		EUExFile uexFile = new EUExFile(F_TYPE_FILE, inPath,
				F_FILE_OPEN_MODE_NEW, mContext,null);
		if (testFileError(uexFile.m_errorType, Integer.parseInt(inOpCode),
				inPath)) {
			return;
		}
		objectMap.put(Integer.parseInt(inOpCode), uexFile);
		jsCallback(F_CALLBACK_NAME_CREATEFILE, Integer.parseInt(inOpCode),
				EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
	}

	/**
	 * 创建个文件夹对象
	 * @return 文件夹对象
	 */
	public void createDir(String[] parm) {
		if (parm.length != 2) {
			return;
		}
		String inOpCode = parm[0], inPath = parm[1];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		if (objectMap.containsKey(Integer.parseInt(inOpCode))) {
			jsCallback(F_CALLBACK_NAME_CREATEDIR, Integer.parseInt(inOpCode),
					EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
			return;
		}
		if (testNull(mBrwView.getCurrentWidget(), inPath,
				Integer.parseInt(inOpCode))) {
			jsCallback(F_CALLBACK_NAME_CREATEDIR, Integer.parseInt(inOpCode),
					EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
			return;
		}
		inPath = BUtility.makeRealPath(
				BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
				mBrwView.getCurrentWidget().m_widgetPath,
				mBrwView.getCurrentWidget().m_wgtType);
		EUExFile uexFile = new EUExFile(F_TYPE_DIR, inPath,
				F_FILE_OPEN_MODE_NEW, mContext,null);
		if (testFileError(uexFile.m_errorType, Integer.parseInt(inOpCode),
				inPath)) {
			return;
		}
		objectMap.put(Integer.parseInt(inOpCode), uexFile);
		jsCallback(F_CALLBACK_NAME_CREATEDIR, Integer.parseInt(inOpCode),
				EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
	}

	/**
	 * 打开一个文件
	 * @return 文件对象
	 */

	public void openFile(String[] parm) {
		if (parm.length != 3) {
			return;
		}
		String inOpCode = parm[0], inPath = parm[1], inMode = parm[2];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		if (testNull(mBrwView.getCurrentWidget(), inPath,
				Integer.parseInt(inOpCode))) {
			jsCallback(F_CALLBACK_NAME_OPENFILE, Integer.parseInt(inOpCode),
					EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
			return;
		}
		EUExFile uexFile = objectMap.get(Integer.parseInt(inOpCode));
		if (uexFile == null) {
			inPath = BUtility.makeRealPath(
					BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
					mBrwView.getCurrentWidget().m_widgetPath,
					mBrwView.getCurrentWidget().m_wgtType);
			uexFile = new EUExFile(F_TYPE_FILE, inPath,
					Integer.parseInt(inMode), mContext,null);
			if (testFileError(uexFile.m_errorType, Integer.parseInt(inOpCode),
					inPath)) {
				return;
			}
			uexFile.m_state = F_STATE_OPEN;
			objectMap.put(Integer.parseInt(inOpCode), uexFile);
			jsCallback(F_CALLBACK_NAME_OPENFILE, Integer.parseInt(inOpCode),
					EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
		} else {
			if (uexFile.m_state == F_STATE_CREATE) {
				uexFile.m_state = F_STATE_OPEN;
			} else {
				jsCallback(F_CALLBACK_NAME_OPENFILE,
						Integer.parseInt(inOpCode), EUExCallback.F_C_INT,
						EUExCallback.F_C_FAILED);
			}
		}

	}

	/**
	 * 删除一个文件
	 * @return (true-成功;false-失败)
	 */

	public void deleteFileByPath(String[] parm) {
		if (parm.length != 1) {
			return;
		}
		String inPath = parm[0];
		if (testNull(mBrwView.getCurrentWidget(), inPath, 0)) {
			jsCallback(F_CALLBACK_NAME_DELETEFILEBYPATH, 0,
					EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
			return;
		}
		inPath = BUtility.makeRealPath(
				BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
				mBrwView.getCurrentWidget().m_widgetPath,
				mBrwView.getCurrentWidget().m_wgtType);
		try {
			File file = new File(inPath);
			if (file.exists()) {
				deleteFile(file);
				jsCallback(F_CALLBACK_NAME_DELETEFILEBYPATH, 0,
						EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
			} else {
				jsCallback(F_CALLBACK_NAME_DELETEFILEBYPATH, 0,
						EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);

			}
		} catch (SecurityException e) {
			Toast.makeText(
					m_context,
					ResoureFinder.getInstance().getString(mContext,
							"error_no_permisson_RW"), Toast.LENGTH_SHORT)
					.show();
		}

	}

	/**
	 * 删除一个文件
	 * 
	 */

	public void deleteFileByID(String[] parm) {
		if (parm.length != 1) {
			return;
		}
		String inOpCode = parm[0];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		EUExFile object = objectMap.get(Integer.parseInt(inOpCode));
		try {
			if (object != null) {

				String path = object.getFilePath();

				if (path != null) {

					File file = new File(path);
					if (file.exists()) {
						deleteFile(file);
						jsCallback(F_CALLBACK_NAME_DELETEFILEBYID,
								Integer.parseInt(inOpCode),
								EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
					} else {
						jsCallback(F_CALLBACK_NAME_DELETEFILEBYID,
								Integer.parseInt(inOpCode),
								EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
					}

				} else {
					jsCallback(F_CALLBACK_NAME_DELETEFILEBYID,
							Integer.parseInt(inOpCode), EUExCallback.F_C_INT,
							EUExCallback.F_C_FAILED);
				}

			} else {
				jsCallback(F_CALLBACK_NAME_DELETEFILEBYID,
						Integer.parseInt(inOpCode), EUExCallback.F_C_INT,
						EUExCallback.F_C_FAILED);
			}
		} catch (SecurityException e) {
			Toast.makeText(
					m_context,
					ResoureFinder.getInstance().getString(mContext,
							"error_no_permisson_RW"), Toast.LENGTH_SHORT)
					.show();
		}

	}

	private void deleteFile(File file) {
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					this.deleteFile(files[i]);
				}
			}
			file.delete();
		}
	}

	/**
	 * 通过路径判断文件是否存在
	 * 
	 */
	public void isFileExistByPath(String[] parm) {
		String inOpCode = null;
		String inPath = null;
		if (parm.length == 1) {
			inPath = parm[0];
			inOpCode = "0";
		} else if (parm.length == 2) {
			inOpCode = parm[0];
			inPath = parm[1];
		}
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		if (testNull(mBrwView.getCurrentWidget(), inPath, 0)) {
			errorCallback(
					0,
					EUExCallback.F_E_UEXFILEMGR_ISFILEEXISTBYPATH_1,
					ResoureFinder.getInstance().getString(mContext,
							"error_parameter"));
			return;
		}
		inPath = BUtility.makeRealPath(
				BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
				mBrwView.getCurrentWidget().m_widgetPath,
				mBrwView.getCurrentWidget().m_wgtType);
		try {
			if (inPath.startsWith("/")) {
				File file = new File(inPath);
				if (file.exists()) {
					jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYPATH,
							Integer.parseInt(inOpCode), EUExCallback.F_C_INT,
							EUExCallback.F_C_TRUE);
				} else {
					jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYPATH,
							Integer.parseInt(inOpCode), EUExCallback.F_C_INT,
							EUExCallback.F_C_FALSE);
				}
			} else {
				InputStream is = null;
				try {
					is = mContext.getAssets().open(inPath);
					if (is == null) {
						jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYPATH,
								Integer.parseInt(inOpCode),
								EUExCallback.F_C_INT, EUExCallback.F_C_FALSE);
					} else {
						jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYPATH,
								Integer.parseInt(inOpCode),
								EUExCallback.F_C_INT, EUExCallback.F_C_TRUE);
					}
				} catch (IOException e) {
					jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYPATH,
							Integer.parseInt(inOpCode), EUExCallback.F_C_INT,
							EUExCallback.F_C_FALSE);
					e.printStackTrace();
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					is = null;
				}
			}
		} catch (SecurityException e) {
			Toast.makeText(
					m_context,
					ResoureFinder.getInstance().getString(mContext,
							"error_no_permisson_RW"), Toast.LENGTH_SHORT)
					.show();
		}

	}

	/**
	 * 通过文件id判断文件是否存在
	 * 
	 */
	public void isFileExistByID(String[] parm) {
		if (parm.length != 1) {
			return;
		}
		String inOpCode = parm[0];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		EUExFile object = objectMap.get(Integer.parseInt(inOpCode));
		try {
			if (object != null) {
				String filePath = object.getFilePath();
				if (filePath.startsWith("/")) {
					File file = new File(object.getFilePath());
					if (file.exists()) {
						jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYID, 0,
								EUExCallback.F_C_INT, EUExCallback.F_C_TRUE);
					} else {
						jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYID, 0,
								EUExCallback.F_C_INT, EUExCallback.F_C_FALSE);
					}
				} else {
					InputStream is = null;
					try {
						is = mContext.getAssets().open(filePath);
						if (is == null) {
							jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYID, 0,
									EUExCallback.F_C_INT,
									EUExCallback.F_C_FALSE);
						} else {
							jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYID, 0,
									EUExCallback.F_C_INT, EUExCallback.F_C_TRUE);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYID, 0,
								EUExCallback.F_C_INT, EUExCallback.F_C_FALSE);
						e.printStackTrace();
					} finally {
						if (is != null) {
							try {
								is.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						is = null;
					}
				}
			} else {
				errorCallback(
						0,
						EUExCallback.F_E_UEXFILEMGR_ISFILEEXISTBYID_1,
						ResoureFinder.getInstance().getString(mContext,
								"error_parameter"));
			}
		} catch (SecurityException e) {
			Toast.makeText(
					m_context,
					ResoureFinder.getInstance().getString(mContext,
							"error_no_permisson_RW"), Toast.LENGTH_SHORT)
					.show();
		}

	}

	/**
	 * 根据路径判断文件类型
	 * 
	 */

	public void getFileTypeByPath(String[] parm) {
		if (parm.length != 1) {
			return;
		}
		String inPath = parm[0];
		if (testNull(mBrwView.getCurrentWidget(), inPath, 0)) {
			errorCallback(
					0,
					EUExCallback.F_E_UEXFILEMGR_GETFILETYPEBYPATH_1,
					ResoureFinder.getInstance().getString(mContext,
							"error_parameter"));
			return;
		}
		inPath = BUtility.makeRealPath(
				BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
				mBrwView.getCurrentWidget().m_widgetPath,
				mBrwView.getCurrentWidget().m_wgtType);
		try {
			File file = new File(inPath);
			int resValue = EUExCallback.F_C_File;
			if (file.isDirectory()) {
				resValue = EUExCallback.F_C_Folder;
			}

			jsCallback(F_CALLBACK_NAME_GETFILETYPEBYPATH, 0,
					EUExCallback.F_C_INT, resValue);
		} catch (SecurityException e) {
			Toast.makeText(
					m_context,
					ResoureFinder.getInstance().getString(mContext,
							"error_no_permisson_RW"), Toast.LENGTH_SHORT)
					.show();
		}

	}

	/**
	 * 根据文件id判断文件类型
	 * 
	 */
	public void getFileTypeByID(String[] parm) {
		if (parm.length != 1) {
			return;
		}
		String inOpCode = parm[0];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		EUExFile object = objectMap.get(Integer.parseInt(inOpCode));
		try {
			if (object != null) {

				File file = new File(object.getFilePath());

				int resValue = 0;
				if (file.isDirectory()) {
					resValue = 1;
				}

				jsCallback(F_CALLBACK_NAME_GETFILETYPEBYID, 0,
						EUExCallback.F_C_INT, resValue);
			}
		} catch (SecurityException e) {
			Toast.makeText(
					m_context,
					ResoureFinder.getInstance().getString(mContext,
							"error_no_permisson_RW"), Toast.LENGTH_SHORT)
					.show();
		}

	}

	public void explorer(String[] parm) {
		String inPath = parm[0];

		try {
			Intent intent = new Intent(mContext, FilexplorerActivity.class);
			if (inPath == null || inPath.length() == 0) {
				inPath = BUtility.getSdCardRootPath();
			}
			final String fullPath = BUtility.getFullPath(
					mBrwView.getCurrentUrl(), inPath);
			if (BUtility.isSDcardPath(fullPath)) {
				String realPath = BUtility.getSDRealPath(fullPath,
						mBrwView.getCurrentWidget().m_widgetPath,
						mBrwView.getCurrentWidget().m_wgtType);
				if (realPath != null && realPath.length() > 0) {
					try {
						realPath = URLDecoder.decode(realPath, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					File destFile = new File(realPath.replace(
							BUtility.F_FILE_SCHEMA, ""));
					if (destFile.exists() && destFile.isDirectory()) {
						intent.setData(Uri.fromFile(destFile));
						intent.putExtra(
								FilexplorerActivity.F_INTENT_KEY_MULTI_FLAG,
								false);
						startActivityForResult(intent,
								F_ACT_REQ_CODE_UEX_FILE_EXPLORER);
					} else {
						errorCallback(0,
								EUExCallback.F_E_UEXFILEMGR_EXPLORER_2,
								finder.getString("error_parameter"));
					}
				} else {
					errorCallback(0, EUExCallback.F_E_UEXFILEMGR_EXPLORER_2,
							finder.getString("error_parameter"));
				}
			} else {
				errorCallback(0, EUExCallback.F_E_UEXFILEMGR_EXPLORER_6,
						finder.getString("error_sdcard_is_not_available"));
			}
		} catch (SecurityException e) {
			Toast.makeText(m_context,
					finder.getString("error_no_permisson_RW"),
					Toast.LENGTH_SHORT).show();
		}

	}

	public void multiExplorer(String[] parm) {
		String inPath = parm[0];
		try {
			Intent intent = new Intent(mContext, FilexplorerActivity.class);
			if (inPath == null || inPath.length() == 0) {
				inPath = BUtility.getSdCardRootPath();
			}
			final String fullPath = BUtility.getFullPath(
					mBrwView.getCurrentUrl(), inPath);
			if (BUtility.isSDcardPath(fullPath)) {
				String realPath = BUtility.getSDRealPath(fullPath,
						mBrwView.getCurrentWidget().m_widgetPath,
						mBrwView.getCurrentWidget().m_wgtType);
				if (realPath != null && realPath.length() > 0) {
					try {
						realPath = URLDecoder.decode(realPath, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					File destFile = new File(realPath.replace(
							BUtility.F_FILE_SCHEMA, ""));
					if (destFile.exists() && destFile.isDirectory()) {
						intent.setData(Uri.fromFile(destFile));
						intent.putExtra(
								FilexplorerActivity.F_INTENT_KEY_MULTI_FLAG,
								true);
						startActivityForResult(intent,
								F_ACT_REQ_CODE_UEX_FILE_MULTI_EXPLORER);
					} else {
						errorCallback(0,
								EUExCallback.F_E_UEXFILEMGR_EXPLORER_2,
								finder.getString("error_parameter"));
					}
				} else {
					errorCallback(0, EUExCallback.F_E_UEXFILEMGR_EXPLORER_2,
							finder.getString("error_parameter"));
				}
			} else {
				errorCallback(0, EUExCallback.F_E_UEXFILEMGR_EXPLORER_6,
						finder.getString("error_sdcard_is_not_available"));
			}
		} catch (SecurityException e) {
			Toast.makeText(m_context,
					finder.getString("error_no_permisson_RW"),
					Toast.LENGTH_SHORT).show();
		}
	}

	public void seekFile(String[] parm) {
		if (parm.length != 2) {
			return;
		}
		String inOpCode = parm[0], inPos = parm[1];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		EUExFile object = objectMap.get(Integer.parseInt(inOpCode));
		if (object != null) {
			if (!object.seek(inPos)) {
				errorCallback(
						Integer.parseInt(inOpCode),
						EUExCallback.F_E_UEXFILEMGR_SEEKFILE_1,
						ResoureFinder.getInstance().getString(mContext,
								"error_parameter"));
			}
		}
	}

	public void seekBeginOfFile(String[] parm) {

		String inOpCode = parm[0];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		EUExFile object = objectMap.get(Integer.parseInt(inOpCode));
		if (object != null) {
			if (!object.seekBegin()) {
				errorCallback(
						Integer.parseInt(inOpCode),
						EUExCallback.F_E_UEXFILEMGR_SEEKBEGINOFFILE_1,
						ResoureFinder.getInstance().getString(mContext,
								"error_parameter"));
			}

		}
	}

	public void seekEndOfFile(String[] parm) {

		String inOpCode = parm[0];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		EUExFile object = objectMap.get(Integer.parseInt(inOpCode));
		if (object != null) {
			if (!object.seekEnd()) {
				errorCallback(
						Integer.parseInt(inOpCode),
						EUExCallback.F_E_UEXFILEMGR_SEEKENDOFFILE_1,
						ResoureFinder.getInstance().getString(mContext,
								"error_parameter"));
			}

		}
	}

	public void writeFile(String[] parm) {
		if (parm.length != 3) {
			return;
		}
		String inOpCode = parm[0], inMode = parm[1], inData = parm[2];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		EUExFile object = objectMap.get(Integer.parseInt(inOpCode));
		if (object != null) {
			object.write(inData, Integer.parseInt(inMode));

		} else {
			errorCallback(
					Integer.parseInt(inOpCode),
					EUExCallback.F_E_UEXFILEMGR_WRITEFILE_1,
					ResoureFinder.getInstance().getString(mContext,
							"error_parameter"));
		}
	}

	public void readFile(String[] parm) {
		if (parm.length != 2) {
			return;
		}
		String inOpCode = parm[0], inLen = parm[1];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		EUExFile object = objectMap.get(Integer.parseInt(inOpCode));
		if (object != null) {
			String resString = object.read(Integer
					.parseInt(inLen));
					if(TextUtils.isEmpty(resString)){
						jsCallback(F_CALLBACK_NAME_READFILE, Integer.parseInt(inOpCode),
								EUExCallback.F_C_TEXT, "");
					}else{
						jsCallback(F_CALLBACK_NAME_READFILE, Integer.parseInt(inOpCode),
								EUExCallback.F_C_TEXT,  BUtility.transcoding(resString));
					}
		} else {
			errorCallback(
					Integer.parseInt(inOpCode),
					EUExCallback.F_E_UEXFILEMGR_READFILE_1,
					ResoureFinder.getInstance().getString(mContext,
							"error_parameter"));
		}
	}

	public void getFileSize(String[] parm) {
		String inOpCode = parm[0];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		EUExFile object = objectMap.get(Integer.parseInt(inOpCode));
		if (object != null) {
			long res = object.getSize();
			jsCallback(F_CALLBACK_NAME_GETFILESIZE, Integer.parseInt(inOpCode),
					EUExCallback.F_C_INT, Integer.parseInt(String.valueOf(res)));
		} else {
			errorCallback(
					Integer.parseInt(inOpCode),
					EUExCallback.F_E_UEXFILEMGR_GETFILESIZE_1,
					ResoureFinder.getInstance().getString(mContext,
							"error_parameter"));
		}

	}

	public void getFilePath(String[] parm) {
		String inOpCode = parm[0];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		EUExFile object = objectMap.get(Integer.parseInt(inOpCode));
		if (object != null) {
			String res = object.getFilePath();
			jsCallback(F_CALLBACK_NAME_GETFILEPATH, Integer.parseInt(inOpCode),
					EUExCallback.F_C_TEXT, res);
		} else {

			errorCallback(
					Integer.parseInt(inOpCode),
					EUExCallback.F_E_UEXFILEMGR_GETFILEPATH_1,
					ResoureFinder.getInstance().getString(mContext,
							"error_parameter"));
		}

	}

	public void getFileRealPath(String[] parm) {
		boolean flag = false;
		String inPath = parm[0];
		if(inPath.startsWith("res://")) {
			flag = true;
		}
		inPath = BUtility.makeRealPath(
				BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
				mBrwView.getCurrentWidget().m_widgetPath,
				mBrwView.getCurrentWidget().m_wgtType);
		if(flag) {
			inPath = "file:///android_asset/" + inPath;
			flag = false;
		}
		jsCallback(F_CALLBACK_NAME_GETFILEREALPATH, 0, EUExCallback.F_C_TEXT,
				inPath);

	}

	public void closeFile(String[] parm) {
		String inOpCode = parm[0];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		EUExFile object = objectMap.remove(Integer.parseInt(inOpCode));
		if (object != null) {
			object.close();
		} else {
			errorCallback(
					Integer.parseInt(inOpCode),
					EUExCallback.F_E_UEXFILEMGR_CLOSEFILE_1,
					ResoureFinder.getInstance().getString(mContext,
							"error_parameter"));
		}

	}

	public void getReaderOffset(String[] parm) {
		String inOpCode = parm[0];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		EUExFile object = objectMap.get(Integer.parseInt(inOpCode));
		if (object != null) {
			long res = object.getreaderOffset();

			jsCallback(F_CALLBACK_NAME_GETREADEROFFSET,
					Integer.parseInt(inOpCode), EUExCallback.F_C_INT,
					Integer.parseInt(String.valueOf(res)));
		} else {
			errorCallback(
					Integer.parseInt(inOpCode),
					EUExCallback.F_E_UEXFILEMGR_GETREADEROFFSET_1,
					ResoureFinder.getInstance().getString(mContext,
							"error_parameter"));
		}

	}

	public void readPercent(String[] parm) {
		if (parm.length != 3) {
			return;
		}
		String inOpCode = parm[0], inPercent = parm[1], inLen = parm[2];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		EUExFile object = objectMap.get(Integer.parseInt(inOpCode));
		if (object != null) {
			String res = object.readerPercent(Integer.parseInt(inPercent),
					Integer.parseInt(inLen));

			jsCallback(F_CALLBACK_NAME_READPERCENT, Integer.parseInt(inOpCode),
					EUExCallback.F_C_TEXT, res);

		}

	}

	public void readNext(String[] parm) {
		if (parm.length != 2) {
			return;
		}
		String inOpCode = parm[0], inLen = parm[1];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		EUExFile object = objectMap.get(Integer.parseInt(inOpCode));
		if (object != null) {
			String res = object.readerNext(Integer.parseInt(inLen));

			jsCallback(F_CALLBACK_NAME_READNEXT, Integer.parseInt(inOpCode),
					EUExCallback.F_C_TEXT, res);

		}

	}

	public void readPre(String[] parm) {
		if (parm.length != 2) {
			return;
		}
		String inOpCode = parm[0], inLen = parm[1];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		EUExFile object = objectMap.get(Integer.parseInt(inOpCode));
		if (object != null) {
			String res = object.readerPre(Integer.parseInt(inLen));

			jsCallback(F_CALLBACK_NAME_READPRE, Integer.parseInt(inOpCode),
					EUExCallback.F_C_TEXT, res);

		}

	}

	public void createSecure(String[] parm) {
		if (parm.length != 3) {
			return;
		}

		String inOpCode = parm[0], inPath = parm[1],inKey =  parm[2];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		if (objectMap.containsKey(Integer.parseInt(inOpCode))) {
			jsCallback(F_CALLBACK_NAME_CREATESECURE, Integer.parseInt(inOpCode),
					EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
			return;
		}
		if (testNull(mBrwView.getCurrentWidget(), inPath,
				Integer.parseInt(inOpCode))) {
			jsCallback(F_CALLBACK_NAME_CREATESECURE, Integer.parseInt(inOpCode),
					EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
			return;
		}
		inPath = BUtility.makeRealPath(
				BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
				mBrwView.getCurrentWidget().m_widgetPath,
				mBrwView.getCurrentWidget().m_wgtType);
		EUExFile uexFile = new EUExFile(F_TYPE_FILE, inPath,
				F_FILE_OPEN_MODE_NEW, mContext,inKey);
		if (testFileError(uexFile.m_errorType, Integer.parseInt(inOpCode),
				inPath)) {
			return;
		}
		objectMap.put(Integer.parseInt(inOpCode), uexFile);
		jsCallback(F_CALLBACK_NAME_CREATESECURE, Integer.parseInt(inOpCode),
				EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
	}

	public void openSecure(String[] parm){
		if (parm.length != 4) {
			return;
		}

		String inOpCode = parm[0], inPath = parm[1], inMode = parm[2],inKey = parm[3];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		if (testNull(mBrwView.getCurrentWidget(), inPath,
				Integer.parseInt(inOpCode))) {
			jsCallback(F_CALLBACK_NAME_OPENSECURE, Integer.parseInt(inOpCode),
					EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
			return;
		}
		EUExFile uexFile = objectMap.get(Integer.parseInt(inOpCode));
		if (uexFile == null) {
			inPath = BUtility.makeRealPath(
					BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
					mBrwView.getCurrentWidget().m_widgetPath,
					mBrwView.getCurrentWidget().m_wgtType);
			uexFile = new EUExFile(F_TYPE_FILE, inPath,
					Integer.parseInt(inMode), mContext,inKey);
			if (testFileError(uexFile.m_errorType, Integer.parseInt(inOpCode),
					inPath)) {
				return;
			}
			uexFile.m_state = F_STATE_OPEN;
			objectMap.put(Integer.parseInt(inOpCode), uexFile);
			jsCallback(F_CALLBACK_NAME_OPENSECURE, Integer.parseInt(inOpCode),
					EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
		} else {
			if (uexFile.m_state == F_STATE_CREATE) {
				uexFile.m_state = F_STATE_OPEN;
			} else {
				jsCallback(F_CALLBACK_NAME_OPENSECURE,
						Integer.parseInt(inOpCode), EUExCallback.F_C_INT,
						EUExCallback.F_C_FAILED);
			}
		}
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		JSONObject jobj = new JSONObject();
		try {
			if (requestCode == F_ACT_REQ_CODE_UEX_FILE_EXPLORER) {
				if (resultCode == Activity.RESULT_OK) {

					jobj.put(
							EUExCallback.F_JK_URL,
							data.getStringExtra(FilexplorerActivity.F_INTENT_KEY_RETURN_EXPLORER_PATH));
				} else {
					return;
				}
				jsCallback(EUExFileMgr.F_CALLBACK_NAME_EXPLORER, 0,
						EUExCallback.F_C_TEXT,
						jobj.getString(EUExCallback.F_JK_URL));
			} else if (requestCode == F_ACT_REQ_CODE_UEX_FILE_MULTI_EXPLORER) {

				if (resultCode != Activity.RESULT_OK) {
					return;
				}
				String jsonPath = data
						.getStringExtra(FilexplorerActivity.F_INTENT_KEY_RETURN_EXPLORER_PATH);
				jsCallback(EUExFileMgr.F_CALLBACK_NAME_MULTI_EXPLORER, 0,
						EUExCallback.F_C_JSON, jsonPath);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void getFileCreateTime(String[] params) {
		if (params.length != 2) {
			return;
		}
		String inOpCode = params[0], inPath = params[1];
		if (!BUtility.isNumeric(inOpCode)) {
			return;
		}
		inPath = BUtility.makeRealPath(
				BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
				mBrwView.getCurrentWidget().m_widgetPath,
				mBrwView.getCurrentWidget().m_wgtType);
		String time = FileUtility.getTimeFromSp(mContext, inPath);
		if(time == null || "".equalsIgnoreCase(time)) {
			time = "文件或文件夹不存在";
		}
		jsCallback(F_CALLBACK_NAME_GETFILECREATETIME, Integer.parseInt(inOpCode),
				EUExCallback.F_C_TEXT, time);
	}

	@Override
	public boolean clean() {
		Iterator<Integer> iterator = objectMap.keySet().iterator();
		while (iterator.hasNext()) {
			EUExFile object = objectMap.get(iterator.next());
			object.close();
		}
		objectMap.clear();
		return true;
	}

    public void renameFile(final String[] params) {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String jsonString= params[0];
                String oldFilePath=null;
                String newPath=null;
                JSONObject resultJson=new JSONObject();
                String result="1";
                try {
                    JSONObject jsonObject=new JSONObject(jsonString);
                    oldFilePath=jsonObject.optString("oldFilePath");
                    newPath=jsonObject.optString("newFilePath");
                } catch (JSONException e) {

                }
                if (!TextUtils.isEmpty(oldFilePath)&&!TextUtils.isEmpty(newPath)){
                    oldFilePath = BUtility.makeRealPath(
                            BUtility.makeUrl(mBrwView.getCurrentUrl(), oldFilePath),
                            mBrwView.getCurrentWidget().m_widgetPath,
                            mBrwView.getCurrentWidget().m_wgtType);
                    newPath= BUtility.makeRealPath(
                            BUtility.makeUrl(mBrwView.getCurrentUrl(), newPath),
                            mBrwView.getCurrentWidget().m_widgetPath,
                            mBrwView.getCurrentWidget().m_wgtType);
                    File oldFile=new File(oldFilePath);
                    File newFile=new File(newPath);
                    if (!oldFile.renameTo(newFile)) {
                        result="0";
                    }
                }else{
                    result="0";
                }
                try {
                    resultJson.put("result",result);
                } catch (JSONException e) {
                }
                String js = SCRIPT_HEADER + "if(" + F_CALLBACK_NAME_RENAMEFILE + "){"
                        + F_CALLBACK_NAME_RENAMEFILE + "('" + resultJson.toString() + "');}";
                onCallback(js);
            }
        });

    }


}