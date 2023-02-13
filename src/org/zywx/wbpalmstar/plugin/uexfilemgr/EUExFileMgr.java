package org.zywx.wbpalmstar.plugin.uexfilemgr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.plugin.uexfilemgr.vo.CopyVO;
import org.zywx.wbpalmstar.plugin.uexfilemgr.vo.CreateVO;
import org.zywx.wbpalmstar.plugin.uexfilemgr.vo.CreateWithPasswordVO;
import org.zywx.wbpalmstar.plugin.uexfilemgr.vo.FileSizeDataVO;
import org.zywx.wbpalmstar.plugin.uexfilemgr.vo.OpenVO;
import org.zywx.wbpalmstar.plugin.uexfilemgr.vo.OpenWithPasswordVO;
import org.zywx.wbpalmstar.plugin.uexfilemgr.vo.ResultFileSizeVO;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
    private static final String F_CALLBACK_NAME_WRITEFILE = "uexFileMgr.cbWriteFile";
    private static final String F_CALLBACK_NAME_GETFILESIZE = "uexFileMgr.cbGetFileSize";
    private static final String F_CALLBACK_NAME_GETFILEPATH = "uexFileMgr.cbGetFilePath";
    private static final String F_CALLBACK_NAME_GETFILEREALPATH = "uexFileMgr.cbGetFileRealPath";
    private static final String F_CALLBACK_NAME_GETREADEROFFSET = "uexFileMgr.cbGetReaderOffset";
    private static final String F_CALLBACK_NAME_READPERCENT = "uexFileMgr.cbReadPercent";
    private static final String F_CALLBACK_NAME_READNEXT = "uexFileMgr.cbReadNext";
    private static final String F_CALLBACK_NAME_READPRE = "uexFileMgr.cbReadPre";
    private static final String F_CALLBACK_NAME_CREATESECURE = "uexFileMgr.cbCreateSecure";
    private static final String F_CALLBACK_NAME_OPENSECURE = "uexFileMgr.cbOpenSecure";
    private static final String F_CALLBACK_NAME_GETFILELISTBYPATH = "uexFileMgr.cbGetFileListByPath";

    private static final String F_CALLBACK_NAME_DELETEFILEBYPATH = "uexFileMgr.cbDeleteFileByPath";
    private static final String F_CALLBACK_NAME_DELETEFILEBYID = "uexFileMgr.cbDeleteFileByID";
    private static final String F_CALLBACK_NAME_SEARCH = "uexFileMgr.cbSearch";
    private static final String F_CALLBACK_NAME_COPYFILE = "uexFileMgr.cbCopyFile";
    private static final String F_CALLBACK_NAME_GETFILEHASHVALUE = "uexFileMgr.cbGetFileHashValue";


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
    public static final String RES_ROOT = "widget/wgtRes";
    private static final String BUNDLE_DATA = "data";
    private static final int MSG_SEARCH = 1;
    private static final int CALLBACK_FILE_SIZE_TO_JS = 111;
    public static final String INVALID_ID="-1";
    private static int sCurrentId=0;

    private HashMap<String, EUExFile> objectMap = new HashMap<String, EUExFile>();
    private HashMap<String, String> copyMap = new HashMap<String, String>();
    Context m_context;
    private ResoureFinder finder;

    private int mExplorerCallbackId = -1;//选择文件的回调Id

    public EUExFileMgr(Context context, EBrowserView view) {
        super(context, view);
        m_context = context;
        finder = ResoureFinder.getInstance(mContext);
    }

    native static int encode_by_userkey();

    private boolean testFileError(int type, String inOpCode, String path) {
        switch (type) {
            case EUExFile.F_ERROR_CREATEFILE:
                errorCallback(inOpCode,
                        EUExCallback.F_E_UEXFILEMGR_CREATEFILE_6,
                        ResoureFinder.getInstance().getString(mContext,
                                "error_parameter"));
                return true;
            case EUExFile.F_ERROR_FILE_NOT_EXIST:
                errorCallback(inOpCode,
                        EUExCallback.F_E_UEXFILEMGR_OPENFILE_2,
                        ResoureFinder.getInstance().getString(mContext,
                                "error_file_does_not_exist"));
                return true;
        }
        return false;
    }

    private boolean testNull(WWidgetData wgtData, String path) {

        if (path == null || path.length() == 0) {
            return true;
        }
        return false;
    }

    /**
     * 创建一个文件对象
     *
     * @return 文件对象
     */

    public boolean createFile(String[] parm) {
        if (parm.length != 2) {
            return false;
        }
        String inOpCode = parm[0], inPath = parm[1];
//		if (!BUtility.isNumeric(inOpCode)) {
//			return;
//		}
        if (objectMap.containsKey(inOpCode)) {
            jsCallback(F_CALLBACK_NAME_CREATEFILE, inOpCode,
                    EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
            return false;
        }
        if (testNull(mBrwView.getCurrentWidget(), inPath)) {
            jsCallback(F_CALLBACK_NAME_CREATEFILE, inOpCode,
                    EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
            return false;
        }
        inPath = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
        EUExFile uexFile = new EUExFile(F_TYPE_FILE, inPath,
                F_FILE_OPEN_MODE_NEW, mContext, null);
        if (testFileError(uexFile.m_errorType, inOpCode, inPath)) {
            return false;
        }
        objectMap.put(inOpCode, uexFile);
        jsCallback(F_CALLBACK_NAME_CREATEFILE, inOpCode,
                EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
        return true;
    }

    public String create(String[] params){
        CreateVO createVO=DataHelper.gson.fromJson(params[0],CreateVO.class);
        String opId=createVO.id;
        if (opId.equals(INVALID_ID)){
            opId=generateId();
        }
        boolean result=createFile(new String[]{opId,createVO.path});
        return result?opId:null;
    }


    private String generateId(){
        sCurrentId++;
        return String.valueOf(sCurrentId);
    }

    /**
     * 创建个文件夹对象
     *
     * @return 文件夹对象
     */
    public boolean createDir(String[] parm) {
        if (parm.length < 2) {
            return false;
        }
        String inOpCode = parm[0], inPath = parm[1];
//		if (!BUtility.isNumeric(inOpCode)) {
//			return;
//		}
        if (objectMap.containsKey(inOpCode)) {
            jsCallback(F_CALLBACK_NAME_CREATEDIR, inOpCode,
                    EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
            return false;
        }
        if (testNull(mBrwView.getCurrentWidget(), inPath)) {
            jsCallback(F_CALLBACK_NAME_CREATEDIR, inOpCode,
                    EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
            return false;
        }
        inPath = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
        EUExFile uexFile = new EUExFile(F_TYPE_DIR, inPath,
                F_FILE_OPEN_MODE_NEW, mContext, null);
        if (testFileError(uexFile.m_errorType, inOpCode,
                inPath)) {
            return false;
        }
        objectMap.put(inOpCode, uexFile);
        jsCallback(F_CALLBACK_NAME_CREATEDIR, inOpCode,
                EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
        return true;
    }

    public boolean mkdir(String[] params){
        OpenVO openVO=DataHelper.gson.fromJson(params[0],OpenVO.class);
        return createDir(new String[]{generateId(),openVO.path});
    }

    /**
     * 打开一个文件
     *
     * @return 文件对象
     */

    public boolean openFile(String[] parm) {
        if (parm.length != 3) {
            return false;
        }
        String inOpCode = parm[0], inPath = parm[1], inMode = parm[2];
//		if (!BUtility.isNumeric(inOpCode)) {
//			return;
//		}
        if (testNull(mBrwView.getCurrentWidget(), inPath)) {
            jsCallback(F_CALLBACK_NAME_OPENFILE, inOpCode,
                    EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
            return false;
        }
        EUExFile uexFile = objectMap.get(inOpCode);
        if (uexFile == null) {
            inPath = BUtility.makeRealPath(
                    BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
                    mBrwView.getCurrentWidget().m_widgetPath,
                    mBrwView.getCurrentWidget().m_wgtType);
            uexFile = new EUExFile(F_TYPE_FILE, inPath,
                    Integer.parseInt(inMode), mContext, null);
            if (testFileError(uexFile.m_errorType, inOpCode, inPath)) {
                return false;
            }
            uexFile.m_state = F_STATE_OPEN;
            objectMap.put(inOpCode, uexFile);
            jsCallback(F_CALLBACK_NAME_OPENFILE, inOpCode,
                    EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
            return true;
        } else {
            if (uexFile.m_state == F_STATE_CREATE) {
                uexFile.m_state = F_STATE_OPEN;
            } else {
                jsCallback(F_CALLBACK_NAME_OPENFILE, inOpCode,
                        EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
            }
            return false;
        }
    }

    public String open(String[] params){
        OpenVO openVO=DataHelper.gson.fromJson(params[0],OpenVO.class);
        if (openVO.id==null){
            openVO.id=generateId();
        }
        boolean result=openFile(new String[]{openVO.id,openVO.path, String.valueOf(openVO.mode)});
        return result?openVO.id:null;
    }

    /**
     * 删除一个文件
     *
     * @return (true-成功;false-失败)
     */

    public boolean deleteFileByPath(String[] parm) {
        if (parm.length != 1) {
            return false;
        }
        String inPath = parm[0];
        if (testNull(mBrwView.getCurrentWidget(), inPath)) {
            jsCallback(F_CALLBACK_NAME_DELETEFILEBYPATH, 0,
                    EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
            return false;
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
                return true;
            } else {
                jsCallback(F_CALLBACK_NAME_DELETEFILEBYPATH, 0,
                        EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
                return false;
            }
        } catch (SecurityException e) {
            Toast.makeText(
                    m_context,
                    ResoureFinder.getInstance().getString(mContext,
                            "error_no_permisson_RW"), Toast.LENGTH_SHORT)
                    .show();
        }
        return false;
    }

    /**
     * 删除一个文件
     */

    public boolean deleteFileByID(String[] parm) {
        if (parm.length != 1) {
            return false;
        }
        String inOpCode = parm[0];
//		if (!BUtility.isNumeric(inOpCode)) {
//			return;
//		}
        EUExFile object = objectMap.get(inOpCode);
        try {
            if (object != null) {

                String path = object.getFilePath();

                if (path != null) {

                    File file = new File(path);
                    if (file.exists()) {
                        deleteFile(file);
                        jsCallback(F_CALLBACK_NAME_DELETEFILEBYID,
                                inOpCode, EUExCallback.F_C_INT,
                                EUExCallback.F_C_SUCCESS);
                        return true;
                    } else {
                        jsCallback(F_CALLBACK_NAME_DELETEFILEBYID,
                                inOpCode, EUExCallback.F_C_INT,
                                EUExCallback.F_C_FAILED);
                        return false;
                    }

                } else {
                    jsCallback(F_CALLBACK_NAME_DELETEFILEBYID,
                            inOpCode, EUExCallback.F_C_INT,
                            EUExCallback.F_C_FAILED);
                    return false;
                }

            } else {
                jsCallback(F_CALLBACK_NAME_DELETEFILEBYID,
                        inOpCode, EUExCallback.F_C_INT,
                        EUExCallback.F_C_FAILED);
                return false;
            }
        } catch (SecurityException e) {
            Toast.makeText(
                    m_context,
                    ResoureFinder.getInstance().getString(mContext,
                            "error_no_permisson_RW"), Toast.LENGTH_SHORT)
                    .show();
        }
        return false;

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
     */
    public boolean isFileExistByPath(String[] parm) {
        String inOpCode = null;
        String inPath = null;
        if (parm.length == 1) {
            inPath = parm[0];
            inOpCode = "0";
        } else if (parm.length == 2) {
            inOpCode = parm[0];
            inPath = parm[1];
        }
//		if (!BUtility.isNumeric(inOpCode)) {
//			return;
//		}
        if (testNull(mBrwView.getCurrentWidget(), inPath)) {
            errorCallback(0, EUExCallback.F_E_UEXFILEMGR_ISFILEEXISTBYPATH_1,
                    ResoureFinder.getInstance().getString(mContext,
                            "error_parameter"));
            return false;
        }
        inPath = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
        try {
            if (inPath.startsWith("/")) {
                File file = new File(inPath);
                if (file.exists()) {
                    jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYPATH, inOpCode,
                            EUExCallback.F_C_INT, EUExCallback.F_C_TRUE);
                    return true;
                } else {
                    jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYPATH, inOpCode,
                            EUExCallback.F_C_INT, EUExCallback.F_C_FALSE);
                    return false;
                }
            } else {
                InputStream is = null;
                try {
                    is = mContext.getAssets().open(inPath);
                    if (is == null) {
                        jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYPATH, inOpCode,
                                EUExCallback.F_C_INT, EUExCallback.F_C_FALSE);
                        return false;
                    } else {
                        jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYPATH, inOpCode,
                                EUExCallback.F_C_INT, EUExCallback.F_C_TRUE);
                        return true;
                    }
                } catch (IOException e) {
                    jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYPATH, inOpCode,
                            EUExCallback.F_C_INT, EUExCallback.F_C_FALSE);
                    e.printStackTrace();
                    return false;
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
        return false;
    }

    /**
     * 通过文件id判断文件是否存在
     */
    public boolean isFileExistByID(String[] parm) {
        if (parm.length != 1) {
            return false;
        }
        String inOpCode = parm[0];
//		if (!BUtility.isNumeric(inOpCode)) {
//			return;
//		}
        EUExFile object = objectMap.get(inOpCode);
        try {
            if (object != null) {
                String filePath = object.getFilePath();
                if (filePath.startsWith("/")) {
                    File file = new File(object.getFilePath());
                    if (file.exists()) {
                        jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYID, 0,
                                EUExCallback.F_C_INT, EUExCallback.F_C_TRUE);
                        return true;
                    } else {
                        jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYID, 0,
                                EUExCallback.F_C_INT, EUExCallback.F_C_FALSE);
                        return false;
                    }
                } else {
                    InputStream is = null;
                    boolean exist = false;
                    try {
                        is = mContext.getAssets().open(filePath);
                        if (is == null) {
                            jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYID, 0,
                                    EUExCallback.F_C_INT,
                                    EUExCallback.F_C_FALSE);
                            exist = false;
                        } else {
                            jsCallback(F_CALLBACK_NAME_ISFILEEXISTBYID, 0,
                                    EUExCallback.F_C_INT, EUExCallback.F_C_TRUE);
                            exist = true;
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
                    return exist;
                }
            } else {
                errorCallback(0, EUExCallback.F_E_UEXFILEMGR_ISFILEEXISTBYID_1,
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
        return false;
    }

    /**
     * 根据路径判断文件类型
     */

    public int getFileTypeByPath(String[] parm) {
        if (parm.length != 1) {
            return -1;
        }
        String inPath = parm[0];
        if (testNull(mBrwView.getCurrentWidget(), inPath)) {
            errorCallback(
                    0,
                    EUExCallback.F_E_UEXFILEMGR_GETFILETYPEBYPATH_1,
                    ResoureFinder.getInstance().getString(mContext,
                            "error_parameter"));
            return -1;
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
            if (!file.exists()) {
                return -1;
            }
            jsCallback(F_CALLBACK_NAME_GETFILETYPEBYPATH, 0,
                    EUExCallback.F_C_INT, resValue);
            return resValue;
        } catch (SecurityException e) {
            Toast.makeText(
                    m_context,
                    ResoureFinder.getInstance().getString(mContext,
                            "error_no_permisson_RW"), Toast.LENGTH_SHORT)
                    .show();
        }
        return -1;
    }

    /**
     * 根据文件id判断文件类型
     */
    public int getFileTypeByID(String[] parm) {
        if (parm.length != 1) {
            return -1;
        }
        String inOpCode = parm[0];
//		if (!BUtility.isNumeric(inOpCode)) {
//			return;
//		}
        EUExFile object = objectMap.get(inOpCode);
        try {
            if (object != null) {

                File file = new File(object.getFilePath());

                int resValue = 0;
                if (file.isDirectory()) {
                    resValue = 1;
                }
                if (!file.exists()) {
                    return -1;
                }

                jsCallback(F_CALLBACK_NAME_GETFILETYPEBYID, 0,
                        EUExCallback.F_C_INT, resValue);
                return resValue;
            }
        } catch (SecurityException e) {
            Toast.makeText(
                    m_context,
                    ResoureFinder.getInstance().getString(mContext,
                            "error_no_permisson_RW"), Toast.LENGTH_SHORT)
                    .show();
        }
        return -1;
    }

    public void explorer(String[] parm) {
        // 判断parm[0]是否为json，是则解析其中的参数，否，则直接认为parm[0]就是inPath
        String inPath = null;
        boolean canDirectorySelected = false;
        try {
            JSONObject jsonParams = new JSONObject(parm[0]);
            inPath = jsonParams.optString("inPath", null);
            canDirectorySelected = jsonParams.optBoolean("canDirectorySelected", false);
        } catch (JSONException e) {
            inPath = parm[0];
        }
        if (parm.length > 1) {
            try {
                mExplorerCallbackId = Integer.valueOf(parm[1]);
            } catch (Exception ignored) {
            }
        } else {
            mExplorerCallbackId = -1;
        }
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
                        intent.putExtra(
                                FilexplorerActivity.F_INTENT_KEY_DIRECTORY_FLAG,
                                canDirectorySelected);
                        startActivityForResult(intent,
                                F_ACT_REQ_CODE_UEX_FILE_EXPLORER);
                    } else {
                        if (mExplorerCallbackId != -1) {
                            callbackToJs(mExplorerCallbackId, false,1);
                        } else {
                            errorCallback(0,
                                    EUExCallback.F_E_UEXFILEMGR_EXPLORER_2,
                                    finder.getString("error_parameter"));
                        }
                    }
                } else {
                    if (mExplorerCallbackId != -1) {
                        callbackToJs(mExplorerCallbackId, false,1);
                    } else {
                        errorCallback(0, EUExCallback.F_E_UEXFILEMGR_EXPLORER_2,
                                finder.getString("error_parameter"));
                    }
                }
            } else {
                if (mExplorerCallbackId != -1) {
                    callbackToJs(mExplorerCallbackId, false,1);
                } else {
                    errorCallback(0, EUExCallback.F_E_UEXFILEMGR_EXPLORER_6,
                            finder.getString("error_sdcard_is_not_available"));
                }
            }
        } catch (SecurityException e) {
            Toast.makeText(m_context,
                    finder.getString("error_no_permisson_RW"),
                    Toast.LENGTH_SHORT).show();
        }

    }

    public void multiExplorer(String[] parm) {
        String inPath = parm[0];
        if (parm.length > 1) {
            try {
                mExplorerCallbackId = Integer.valueOf(parm[1]);
            } catch (Exception ignored) {
            }
        } else {
            mExplorerCallbackId = -1;
        }
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
                        if (mExplorerCallbackId != -1) {
                            callbackToJs(mExplorerCallbackId, false,1);
                        } else {
                            errorCallback(0,
                                    EUExCallback.F_E_UEXFILEMGR_EXPLORER_2,
                                    finder.getString("error_parameter"));
                        }
                    }
                } else {
                    if (mExplorerCallbackId != -1) {
                        callbackToJs(mExplorerCallbackId, false,1);
                    } else {
                        errorCallback(0, EUExCallback.F_E_UEXFILEMGR_EXPLORER_2,
                                finder.getString("error_parameter"));

                    }
                }
            } else {
                if (mExplorerCallbackId != -1) {
                    callbackToJs(mExplorerCallbackId, false,1);
                } else {
                    errorCallback(0, EUExCallback.F_E_UEXFILEMGR_EXPLORER_6,
                            finder.getString("error_sdcard_is_not_available"));
                }
            }
        } catch (SecurityException e) {
            Toast.makeText(m_context,
                    finder.getString("error_no_permisson_RW"),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public long seekFile(String[] parm) {
        if (parm.length != 2) {
            return -1;
        }
        String inOpCode = parm[0], inPos = parm[1];
//		if (!BUtility.isNumeric(inOpCode)) {
//			return;
//		}
        EUExFile object = objectMap.get(inOpCode);
        if (object != null) {
            if (object.seek(inPos)) {
                return object.getreaderOffset();
            } else {
                errorCallback(inOpCode,
                        EUExCallback.F_E_UEXFILEMGR_SEEKFILE_1,
                        ResoureFinder.getInstance().getString(mContext,
                                "error_parameter"));
                return -1;
            }
        } else {
            return -1;
        }
    }

    public long seekBeginOfFile(String[] parm) {

        String inOpCode = parm[0];
//		if (!BUtility.isNumeric(inOpCode)) {
//			return;
//		}
        EUExFile object = objectMap.get(inOpCode);
        if (object != null) {
            if (object.seekBegin()) {
                return object.getreaderOffset();
            } else {
                errorCallback(inOpCode,
                        EUExCallback.F_E_UEXFILEMGR_SEEKBEGINOFFILE_1,
                        ResoureFinder.getInstance().getString(mContext,
                                "error_parameter"));
                return -1;
            }

        }
        return -1;
    }

    public long seekEndOfFile(String[] parm) {

        String inOpCode = parm[0];
//		if (!BUtility.isNumeric(inOpCode)) {
//			return;
//		}
        EUExFile object = objectMap.get(inOpCode);
        if (object != null) {
            if (object.seekEnd()) {
                return object.getreaderOffset();
            } else {
                errorCallback(inOpCode,
                        EUExCallback.F_E_UEXFILEMGR_SEEKENDOFFILE_1,
                        ResoureFinder.getInstance().getString(mContext,
                                "error_parameter"));
                return -1;
            }

        }
        return -1;
    }

    public void writeFile(String[] parm) {
        if (parm.length < 3) {
            return ;
        }
        final String inOpCode = parm[0], inMode = parm[1], inData = parm[2];
        int callbackId=-1;
        if(parm.length>3 ){
            callbackId= Integer.parseInt(parm[3]);
        }
        final EUExFile object = objectMap.get(inOpCode);
        if (object != null) {
            final int finalCallbackId = callbackId;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean result = false;
                    if (object != null) {
                        result = object.write(inData, Integer.parseInt(inMode));
                    }else{
                        BDebug.e(tag, "writeFile: EUExFile object is null cause by being closed probably: inOpCode=" + inOpCode);
                    }
                    if(finalCallbackId !=-1 ){
                        callbackToJs(finalCallbackId,false, result?0:1);
                    }else{
                        if (result) {
                            jsCallback(F_CALLBACK_NAME_WRITEFILE, inOpCode,
                                    EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
                        } else {
                            jsCallback(F_CALLBACK_NAME_WRITEFILE, inOpCode,
                                    EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
                        }
                    }

                }
            }).start();

        } else {
            if (callbackId!=-1){
                callbackToJs(callbackId,false, 1);
            }else{
                errorCallback(inOpCode,
                        EUExCallback.F_E_UEXFILEMGR_WRITEFILE_1,
                        ResoureFinder.getInstance().getString(mContext,
                                "error_parameter"));
            }
          }
    }

    public void readFile(String[] parm) {
        if (parm.length < 2) {
            return;
        }
        final String inOpCode = parm[0];
        final String inLen = parm[1];
        String modeStr = "0";

        if (parm.length > 2) {
            modeStr = parm[2];
        }
        int callbackId=-1;
        if(parm.length>3 ){
            callbackId= Integer.parseInt(parm[3]);
        }
        final EUExFile object = objectMap.get(inOpCode);
        if (object != null) {
            final String finalModeStr = modeStr;
            final int finalCallbackId = callbackId;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String resString = null;
                    if (object != null) {
                        resString = object.read(Integer
                                .parseInt(inLen), Integer.parseInt(finalModeStr));
                    }else{
                        BDebug.e(tag, "readFile: EUExFile object is null cause by being closed probably: inOpCode=" + inOpCode);
                    }
                    String result = TextUtils.isEmpty(resString) ? "" : BUtility.transcoding(resString);
                    if (finalCallbackId !=-1){
                        callbackToJs(finalCallbackId,false, 0,result);
                    }else{
                        jsCallback(F_CALLBACK_NAME_READFILE, inOpCode,
                                EUExCallback.F_C_TEXT, result);
                    }

                }
            }).start();

        } else {
            if (callbackId!=-1){
                callbackToJs(callbackId,false, 1,"");
            }else {
                errorCallback(inOpCode,
                        EUExCallback.F_E_UEXFILEMGR_READFILE_1,
                        ResoureFinder.getInstance().getString(mContext,
                                "error_parameter"));
            }
        }
    }

    public long getFileSize(String[] parm) {
        String inOpCode = parm[0];
//		if (!BUtility.isNumeric(inOpCode)) {
//			return;
//		}
        EUExFile object = objectMap.get(inOpCode);
        if (object != null) {
            long res = object.getSize();
            jsCallback(F_CALLBACK_NAME_GETFILESIZE, inOpCode,
                    EUExCallback.F_C_INT, Integer.parseInt(String.valueOf(res)));
            return res;
        } else {
            errorCallback(inOpCode,
                    EUExCallback.F_E_UEXFILEMGR_GETFILESIZE_1,
                    ResoureFinder.getInstance().getString(mContext,
                            "error_parameter"));
            return -1;
        }
    }

    public String getFilePath(String[] parm) {
        String inOpCode = parm[0];
//		if (!BUtility.isNumeric(inOpCode)) {
//			return;
//		}
        EUExFile object = objectMap.get(inOpCode);
        if (object != null) {
            String res = object.getFilePath();
            jsCallback(F_CALLBACK_NAME_GETFILEPATH, inOpCode,
                    EUExCallback.F_C_TEXT, res);
            return res;
        } else {

            errorCallback(inOpCode,
                    EUExCallback.F_E_UEXFILEMGR_GETFILEPATH_1,
                    ResoureFinder.getInstance().getString(mContext,
                            "error_parameter"));
            return null;
        }

    }

    public String getFileRealPath(String[] parm) {
        boolean flag = false;
        String inPath = parm[0];
        String contentPrefix = "content://" + m_context.getPackageName() + ".sp/";
        if (inPath.startsWith(contentPrefix)) {  // widget加密了的文件路径
            String subString = contentPrefix + "android_asset";
            inPath = inPath.substring(subString.length());
            String sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String sboxPath = m_context.getFilesDir().getAbsolutePath();
            if ((!TextUtils.isEmpty(sdCardPath) && inPath.startsWith(sdCardPath))
                    || (!TextUtils.isEmpty(sboxPath) && inPath.startsWith(sboxPath))) {
                inPath = "file://" + inPath ;
            } else {
                inPath = "file:///android_asset" + inPath ;
            }
        } else {
            if (inPath.startsWith("res://")) {
                flag = true;
            }
            String m_indexUrl = mBrwView.getCurrentWidget().m_indexUrl;
            String boxPathString = BUtility.makeRealPath("box://",
                    mBrwView.getCurrentWidget().m_widgetPath,
                    mBrwView.getCurrentWidget().m_wgtType);
            if (m_indexUrl.contains("widget/plugin/")) {
                String widgetPaTh = "";
                if (checkAppStatus(mContext, mBrwView.getRootWidget().m_appId)
                        && FileUtility.getIsCopyAssetsFinish(mContext))
                    widgetPaTh = (boxPathString + "widget/plugin/"
                            + mBrwView.getCurrentWidget().m_appId + File.separator);
                else {
                    widgetPaTh = ("file:///android_asset/widget/plugin/"
                            + mBrwView.getCurrentWidget().m_appId + File.separator);
                }
                inPath = BUtility.makeRealPath(
                        BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
                        widgetPaTh, mBrwView.getCurrentWidget().m_wgtType);
            } else {
                inPath = BUtility.makeRealPath(
                        BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
                        mBrwView.getCurrentWidget().m_widgetPath,
                        mBrwView.getCurrentWidget().m_wgtType);
            }
        }
        if (flag && inPath != null
                && inPath.startsWith(BUtility.F_Widget_RES_path)) {
            // 判断如果是解析res://协议并且解析出来的路径以widget/wgtRes/开头（即这是一个主应用没有开启增量更新的情况），就认为是assets路径
            inPath = "file:///android_asset/" + inPath;
            flag = false;
        }
        if (parm.length == 2) {
            String inCallBack = "uexFileMgr." + parm[1];
            onCallback(SCRIPT_HEADER + "if(" + inCallBack + "){"
                    + inCallBack + "('" + inPath + "');}");
        } else {
            jsCallback(F_CALLBACK_NAME_GETFILEREALPATH, 0, EUExCallback.F_C_TEXT,
                    inPath);
        }
        return inPath;

    }

    public boolean closeFile(String[] parm) {
        String inOpCode = parm[0];
//		if (!BUtility.isNumeric(inOpCode)) {
//			return;
//		}
        EUExFile object = objectMap.remove(inOpCode);
        if (object != null) {
            object.close();
            return true;
        } else {
            errorCallback(inOpCode,
                    EUExCallback.F_E_UEXFILEMGR_CLOSEFILE_1,
                    ResoureFinder.getInstance().getString(mContext,
                            "error_parameter"));
            return false;
        }
    }

    public long getReaderOffset(String[] parm) {
        String inOpCode = parm[0];
//		if (!BUtility.isNumeric(inOpCode)) {
//			return;
//		}
        EUExFile object = objectMap.get(inOpCode);
        if (object != null) {
            long res = object.getreaderOffset();

            jsCallback(F_CALLBACK_NAME_GETREADEROFFSET,
                    inOpCode, EUExCallback.F_C_INT,
                    Integer.parseInt(String.valueOf(res)));
            return res;
        } else {
            errorCallback(inOpCode,
                    EUExCallback.F_E_UEXFILEMGR_GETREADEROFFSET_1,
                    ResoureFinder.getInstance().getString(mContext,
                            "error_parameter"));
            return -1;
        }

    }

    public void readPercent(String[] parm) {
        String inOpCode = parm[0], inPercent = parm[1], inLen = parm[2];
        int callbackId = -1;
        if (parm.length > 3) {
            callbackId = Integer.parseInt(parm[3]);
        }
        EUExFile object = objectMap.get(inOpCode);
        if (object != null) {
            String res = object.readerPercent(Integer.parseInt(inPercent),
                    Integer.parseInt(inLen));
            if (callbackId != -1) {
                callbackToJs(callbackId, false, 0,res);
            } else {
                jsCallback(F_CALLBACK_NAME_READPERCENT, inOpCode,
                        EUExCallback.F_C_TEXT, res);

            }
        }else{
            if (callbackId != -1) {
                callbackToJs(callbackId, false, 1);
            }
        }

    }

    public void readNext(String[] parm) {
        if (parm.length < 2) {
            return;
        }
        String inOpCode = parm[0], inLen = parm[1];
        int callbackId = -1;
        if (parm.length > 2) {
            callbackId = Integer.parseInt(parm[2]);
        }
        EUExFile object = objectMap.get(inOpCode);
        if (object != null) {
            String res = object.readerNext(Integer.parseInt(inLen));
            if (callbackId == -1) {
                jsCallback(F_CALLBACK_NAME_READNEXT, inOpCode,
                        EUExCallback.F_C_TEXT, res);
            } else {
                callbackToJs(callbackId, false, 0,res);
            }

        }else {
            if (callbackId != -1) {
                callbackToJs(callbackId, false, 1);
            }
        }

    }

    public void readPre(String[] parm) {
        if (parm.length < 2) {
            return;
        }
        String inOpCode = parm[0], inLen = parm[1];
        int callbackId = -1;
        if (parm.length > 2) {
            callbackId = Integer.parseInt(parm[2]);
        }
        EUExFile object = objectMap.get(inOpCode);
        if (object != null) {
            String res = object.readerPre(Integer.parseInt(inLen));
            if (callbackId == -1) {
                jsCallback(F_CALLBACK_NAME_READPRE, inOpCode,
                        EUExCallback.F_C_TEXT, res);
            } else {
                callbackToJs(callbackId, false, 0, res);
            }
        }else{
            if (callbackId != -1) {
                callbackToJs(callbackId, false, 1);
            }
        }

    }

    public boolean createSecure(String[] parm) {
        if (parm.length < 3) {
            return false;
        }

        String inOpCode = parm[0], inPath = parm[1], inKey = parm[2];
//		if (!BUtility.isNumeric(inOpCode)) {
//			return;
//		}
        if (objectMap.containsKey(inOpCode)) {
            jsCallback(F_CALLBACK_NAME_CREATESECURE, inOpCode,
                    EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
            return false;
        }
        if (testNull(mBrwView.getCurrentWidget(), inPath)) {
            jsCallback(F_CALLBACK_NAME_CREATESECURE, inOpCode,
                    EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
            return false;
        }
        inPath = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
        EUExFile uexFile = new EUExFile(F_TYPE_FILE, inPath,
                F_FILE_OPEN_MODE_NEW, mContext, inKey);
        if (testFileError(uexFile.m_errorType, inOpCode, inPath)) {
            return false;
        }
        objectMap.put(inOpCode, uexFile);
        jsCallback(F_CALLBACK_NAME_CREATESECURE, inOpCode,
                EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
        return true;
    }

    public String createWithPassword(String[] params){
        CreateWithPasswordVO createWithPasswordVO=DataHelper.gson
                .fromJson(params[0],CreateWithPasswordVO.class);
        if (createWithPasswordVO.id==null){
            createWithPasswordVO.id=generateId();
        }
        boolean result=createSecure(new String[]{
                createWithPasswordVO.id,
                createWithPasswordVO.path,
                createWithPasswordVO.password,
        });
        return result?createWithPasswordVO.id:null;
    }

    public boolean openSecure(String[] parm) {
        if (parm.length < 4) {
            return false;
        }

        String inOpCode = parm[0], inPath = parm[1], inMode = parm[2], inKey = parm[3];
//		if (!BUtility.isNumeric(inOpCode)) {
//			return;
//		}
        if (testNull(mBrwView.getCurrentWidget(), inPath)) {
            jsCallback(F_CALLBACK_NAME_OPENSECURE, inOpCode,
                    EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
            return false;
        }
        EUExFile uexFile = objectMap.get(inOpCode);
        if (uexFile == null) {
            inPath = BUtility.makeRealPath(
                    BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
                    mBrwView.getCurrentWidget().m_widgetPath,
                    mBrwView.getCurrentWidget().m_wgtType);
            uexFile = new EUExFile(F_TYPE_FILE, inPath,
                    Integer.parseInt(inMode), mContext, inKey);
            if (testFileError(uexFile.m_errorType, inOpCode,
                    inPath)) {
                return false;
            }
            uexFile.m_state = F_STATE_OPEN;
            objectMap.put(inOpCode, uexFile);
            jsCallback(F_CALLBACK_NAME_OPENSECURE, inOpCode,
                    EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
            return true;
        } else {
            if (uexFile.m_state == F_STATE_CREATE) {
                uexFile.m_state = F_STATE_OPEN;
                return true;
            } else {
                jsCallback(F_CALLBACK_NAME_OPENSECURE, inOpCode,
                        EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
                return false;
            }
        }
    }

    public String openWithPassword(String[] params){
        OpenWithPasswordVO openWithPasswordVO=DataHelper.gson.
                fromJson(params[0],OpenWithPasswordVO.class);
        if (openWithPasswordVO.id==null){
            openWithPasswordVO.id=generateId();
        }
        boolean result=openSecure(new String[]{
                openWithPasswordVO.id,
                openWithPasswordVO.path,
                String.valueOf(openWithPasswordVO.mode),
                openWithPasswordVO.password
        });
        return result?openWithPasswordVO.id:null;
    }

    public JSONArray getFileListByPath(String[] params) {
        if (params.length < 1) {
            Log.i("uexFileMgr", "getFileListByPath");
            return null;
        }
        String inPath = params[0];
        if (testNull(mBrwView.getCurrentWidget(), inPath)) {
            jsCallback(F_CALLBACK_NAME_GETFILELISTBYPATH, 0,
                    EUExCallback.F_C_TEXT, "");
            return null;
        }
        inPath = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
        final String dirPath = inPath;

        try {
            File srcFile = new File(dirPath);
            if (!srcFile.exists()) {
                jsCallback(F_CALLBACK_NAME_GETFILELISTBYPATH, 0,
                        EUExCallback.F_C_TEXT, "");
                return null;
            }
            //获取所有文件
            File[] fileList = srcFile.listFiles();
            JSONArray array = new JSONArray();
            String resultJson = "";
            for (int i = 0; i < fileList.length; i++) {
                File fileItem = fileList[i];
                int resValue = EUExCallback.F_C_File;
                if (fileItem.isDirectory()) {
                    resValue = EUExCallback.F_C_Folder;
                }
                Calendar c = Calendar.getInstance();
                Date date = c.getTime();
//                SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HHmmss");
//                String fileCreateTimestamp = df.format(date);
//                Log.e("111",fileCreateTimestamp);
                //String  fileCreateTimestamp = date.toLocaleString();
                long stf = date.getTime();
                String fileCreateTimestamp = String.valueOf(stf);


                File file = new File(fileItem.getAbsolutePath());
                long fileModTimestamp = file.lastModified();//返回文件最后修改时间，是以个long型毫秒数

//                String fileModTimestamp = new SimpleDateFormat("yyyyMMdd hhmmss").format(new Date(time));
//                Log.e("11",fileModTimestamp);

                JSONObject json = new JSONObject();
                json.put("fileName", fileItem.getName());
                json.put("filePath", fileItem.getAbsolutePath());
                json.put("fileType", resValue);
                json.put("fileCreateTimestamp", fileCreateTimestamp);
                json.put("fileModTimestamp", fileModTimestamp);
                array.put(json);
            }
            resultJson = array.toString();
            jsCallback(F_CALLBACK_NAME_GETFILELISTBYPATH, 0,
                    EUExCallback.F_C_JSON, resultJson);
            return array;
        } catch (SecurityException e) {
            Toast.makeText(
                    m_context,
                    ResoureFinder.getInstance().getString(mContext,
                            "error_no_permisson_RW"),
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        JSONObject jobj = new JSONObject();
        try {
            if (requestCode == F_ACT_REQ_CODE_UEX_FILE_EXPLORER) {
                String path=null;
                if (resultCode == Activity.RESULT_OK) {
                    path=data.getStringExtra(FilexplorerActivity.F_INTENT_KEY_RETURN_EXPLORER_PATH);
                    jobj.put(
                            EUExCallback.F_JK_URL,path
                            );
                }
                if (mExplorerCallbackId != -1) {
                    callbackToJs(mExplorerCallbackId, false, path==null?1:0, path);
                } else {
                    jsCallback(EUExFileMgr.F_CALLBACK_NAME_EXPLORER, 0,
                            EUExCallback.F_C_TEXT,
                            path);
                }
            } else if (requestCode == F_ACT_REQ_CODE_UEX_FILE_MULTI_EXPLORER) {
                List<String> paths=null;
                if (resultCode == Activity.RESULT_OK) {
                    paths = (List<String>) data
                            .getSerializableExtra(FilexplorerActivity.F_INTENT_KEY_RETURN_EXPLORER_PATH);
                }
                if (mExplorerCallbackId != -1) {
                    callbackToJs(mExplorerCallbackId, false, paths==null?1:0, paths==null?null:DataHelper.gson
                            .toJsonTree
                            (paths));
                } else {
                    jsCallback(EUExFileMgr.F_CALLBACK_NAME_MULTI_EXPLORER, 0,
                            EUExCallback.F_C_JSON, paths==null?null:paths.toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getFileCreateTime(String[] params) {
        if (params.length < 1) {
            return null;
        }
        String inOpCode=null;
        String inPath=null;
        if (params.length==1){
            inPath=params[0];
        }else if (params.length==2) {
            inOpCode=params[0];
            inPath=params[1];
        }
//		if (!BUtility.isNumeric(inOpCode)) {
//			return;
//		}
        inPath = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), inPath),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
        String time = FileUtility.getTimeFromSp(mContext, inPath);
        if (inOpCode!=null) {
            jsCallback(F_CALLBACK_NAME_GETFILECREATETIME, inOpCode,
                    EUExCallback.F_C_TEXT, time);
        }
        return time;
    }

    @Override
    public boolean clean() {
        Iterator<String> iterator = objectMap.keySet().iterator();
        while (iterator.hasNext()) {
            EUExFile object = objectMap.get(iterator.next());
            object.close();
        }
        objectMap.clear();
        return true;
    }

    public void renameFile(final String[] params) {
        String jsonString = params[0];
        int callbackId = -1;
        if (params.length > 1) {
            callbackId = Integer.parseInt(params[1]);
        }
        String oldFilePath = null;
        String newPath = null;
        JSONObject resultJson = new JSONObject();
        String result = "1";
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            oldFilePath = jsonObject.optString("oldFilePath");
            newPath = jsonObject.optString("newFilePath");
        } catch (JSONException e) {

        }
        if (!TextUtils.isEmpty(oldFilePath) && !TextUtils.isEmpty(newPath)) {
            oldFilePath = BUtility.makeRealPath(
                    BUtility.makeUrl(mBrwView.getCurrentUrl(), oldFilePath),
                    mBrwView.getCurrentWidget().m_widgetPath,
                    mBrwView.getCurrentWidget().m_wgtType);
            newPath = BUtility.makeRealPath(
                    BUtility.makeUrl(mBrwView.getCurrentUrl(), newPath),
                    mBrwView.getCurrentWidget().m_widgetPath,
                    mBrwView.getCurrentWidget().m_wgtType);
            File oldFile = new File(oldFilePath);
            File newFile = new File(newPath);
            if (!oldFile.renameTo(newFile)) {
                result = "0";
            }
        } else {
            result = "0";
        }
        try {
            resultJson.put("result", result);
        } catch (JSONException e) {
        }
        if (callbackId != -1) {
            callbackToJs(callbackId, false, result.equals("1")?0:1, resultJson);
        } else {
            String js = SCRIPT_HEADER + "if(" + F_CALLBACK_NAME_RENAMEFILE + "){"
                    + F_CALLBACK_NAME_RENAMEFILE + "('" + resultJson.toString() + "');}";
            onCallback(js);
        }


    }

    public void search(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_SEARCH;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void searchMsg(final String[] params) throws JSONException {
        if (params.length < 1) {
            Toast.makeText(m_context, finder.getString("plugin_fileMgr_invalid_params"), Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject jsonObject = new JSONObject(params[0]);
        int callbackId = -1;
        if (params.length > 1) {
            callbackId = Integer.parseInt(params[1]);
        }
        String path = jsonObject.getString("path");
        int option = 0;
        JSONArray keywordsArray = null;
        JSONArray suffixes = null;

        if (jsonObject.has("option")) {
            option = jsonObject.getInt("option");
        }
        if (jsonObject.has("flag")){
            option=jsonObject.getInt("flag");
        }
        if (jsonObject.has("keywords")) {
            keywordsArray = jsonObject.getJSONArray("keywords");
        }
        if (jsonObject.has("suffixes")) {
            suffixes = jsonObject.getJSONArray("suffixes");
        }

        final String realPath = BUtility.makeRealPath(
                BUtility.makeUrl(mBrwView.getCurrentUrl(), path),
                mBrwView.getCurrentWidget().m_widgetPath,
                mBrwView.getCurrentWidget().m_wgtType);
        final int optionTemp = option;
        final JSONArray keywordsArrayTemp = keywordsArray;
        final JSONArray suffixesTemp = suffixes;

        final int finalCallbackId = callbackId;
        new Thread(new Runnable() {
            @Override
            public void run() {

                ArrayList resultFiles = searchFile(realPath, optionTemp, keywordsArrayTemp, suffixesTemp);
                if (finalCallbackId != -1) {
                    callbackToJs(finalCallbackId, false,  resultFiles==null?1:0, DataHelper.gson.toJsonTree
                            (resultFiles));
                } else {
                    try {
                        JSONObject jsonObject=new JSONObject();
                        JSONArray jsonArray = new JSONArray();
                        if (resultFiles!=null) {
                            for (int i = 0; i < resultFiles.size(); i++) {
                                jsonArray.put(i, resultFiles.get(i)); //去掉传进来的目录
                            }
                        }
                        jsonObject.put("result", jsonArray);
                        jsonObject.put("isSuccess", resultFiles!=null);
                        String js = SCRIPT_HEADER + "if(" + F_CALLBACK_NAME_SEARCH + "){"
                                + F_CALLBACK_NAME_SEARCH + "('" + jsonObject.toString() + "');}";
                        onCallback(js);
                    }catch (JSONException exception){
                        if (BDebug.DEBUG){
                            exception.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    public void getFileSizeByPath(String[] params) {
        if (params == null || params.length < 1) {
            ResultFileSizeVO result = new ResultFileSizeVO();
            result.setErrorCode(JsConst.RESULT_FILE_SIZE_ERROR_PARAM);
            callBackPluginJs(JsConst.CALLBACK_GET_FILE_SIZE_BY_PATH,
                    DataHelper.gson.toJson(result));
            return;
        }
        int callbackId = -1;
        if (params.length > 1) {
            callbackId = Integer.parseInt(params[1]);
        }
        FileSizeDataVO dataVO = DataHelper.gson.fromJson(params[0], FileSizeDataVO.class);
        if (dataVO.getId()==null){
            dataVO.setId(generateId());
        }
        if (dataVO != null && !TextUtils.isEmpty(dataVO.getPath())) {
            String filePath = BUtility.makeRealPath(
                    BUtility.makeUrl(mBrwView.getCurrentUrl(), dataVO.getPath()),
                    mBrwView.getCurrentWidget().m_widgetPath,
                    mBrwView.getCurrentWidget().m_wgtType);
            String unit = dataVO.getUnit();
            String id = dataVO.getId();
            GetFileSizeAsyncTask task = new GetFileSizeAsyncTask(callbackId);
            task.execute(filePath, unit, id);
        }

    }

    /**
     * 复制一个文件
     *
     * @param parm [0]:inOpCode id;[1]:srcFilePath 源文件路径;[2]:objPath 目标文件夹路径
     * @throws IOException
     */
    public void copyFile(String[] parm) throws IOException {
        if (parm.length < 3) {
            return;
        }
        final String inOpCode = parm[0], srcFilePath = parm[1], objPath = parm[2];
        int callbackId = -1;
        if (parm.length > 3) {
            callbackId = Integer.parseInt(parm[3]);
        }
        boolean flag = false;
        if (srcFilePath.startsWith(BUtility.F_Widget_RES_SCHEMA)) {
            flag = true;
        }
        final String srcFileRealPath = BUtility.makeRealPath(BUtility.makeUrl(mBrwView.getCurrentUrl(), srcFilePath),
                mBrwView.getCurrentWidget().m_widgetPath, mBrwView.getCurrentWidget().m_wgtType);
        Log.i("srcFileRealPath", srcFileRealPath);
        if (srcFileRealPath.startsWith(BUtility.F_Widget_RES_path)) {
            flag = true;
        }
        final File temp = new File(srcFileRealPath);
        final String objRealPath = BUtility.makeRealPath(BUtility.makeUrl(mBrwView.getCurrentUrl(), objPath),
                mBrwView.getCurrentWidget().m_widgetPath, mBrwView.getCurrentWidget().m_wgtType);
        Log.i("objRealPath", objRealPath + temp.getName());


        final boolean finalFlag = flag;
        final int finalCallbackId = callbackId;
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileInputStream fi = null;
                FileOutputStream fo = null;
                FileChannel in = null;
                FileChannel out = null;
                if (finalFlag && srcFileRealPath != null && srcFileRealPath.startsWith(BUtility.F_Widget_RES_path)) {
                    InputStream in_a = null;
                    OutputStream out_a = null;
                    try {
                        in_a = mContext.getAssets().open(srcFileRealPath);
                        int length = in_a.available();
                        File outFile = new File(objRealPath + temp.getName());
                        out_a = new FileOutputStream(outFile);
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in_a.read(buffer)) != -1) {
                            out_a.write(buffer, 0, read);
                        }
                        if (in_a != null) {
                            in_a.close();
                            in_a = null;
                        }
                        if (out_a != null) {
                            out_a.flush();
                            out_a.close();
                            out_a = null;
                        }
                        File copied = new File(objRealPath + temp.getName());
                        if (copied.exists() && copied.length() == length) {
                            if (finalCallbackId != -1) {
                                callbackToJs(finalCallbackId, false, 0);
                            } else {
                                jsCallback(F_CALLBACK_NAME_COPYFILE, inOpCode,
                                        EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
                            }
                        } else {
                            if (finalCallbackId != -1) {
                                callbackToJs(finalCallbackId, false, 1);
                            } else {
                                jsCallback(F_CALLBACK_NAME_COPYFILE, inOpCode,
                                        EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
                            }
                        }
                    } catch (IOException e) {
                        BDebug.e("Failed to copy asset file: ", srcFileRealPath, e);

                        if (finalCallbackId != -1) {
                            callbackToJs(finalCallbackId, false, 1);
                        } else {
                            jsCallback(F_CALLBACK_NAME_COPYFILE, inOpCode,
                                    EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
                        }
                    }
                } else {
                    try {
                        fi = new FileInputStream(srcFileRealPath);
                        fo = new FileOutputStream(objRealPath + temp.getName());
                        in = fi.getChannel();
                        out = fo.getChannel();
                        in.transferTo(0, in.size(), out);
                        if (in != null) {
                            in.close();
                            in = null;
                        }
                        if (out != null) {
                            out.close();
                            out = null;
                        }
                        if (fo != null) {
                            fo.close();
                            fo = null;
                        }
                        if (fi != null) {
                            fi.close();
                            fi = null;
                        }
                        File copied = new File(objRealPath + temp.getName());
                        if (copied.exists() && copied.length() == temp.length()) {
                            if (finalCallbackId != -1) {
                                callbackToJs(finalCallbackId, false, 0);
                            } else {
                                jsCallback(F_CALLBACK_NAME_COPYFILE, inOpCode,
                                        EUExCallback.F_C_INT, EUExCallback.F_C_SUCCESS);
                            }
                        } else {
                            if (finalCallbackId != -1) {
                                callbackToJs(finalCallbackId, false, 1);
                            } else {
                                jsCallback(F_CALLBACK_NAME_COPYFILE, inOpCode,
                                        EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
                            }
                        }
                    } catch (IOException e) {
                        BDebug.e("Failed to copy asset file: ", srcFileRealPath, e);
                        if (finalCallbackId != -1) {
                            callbackToJs(finalCallbackId, false, 1);
                        } else {
                            jsCallback(F_CALLBACK_NAME_COPYFILE, inOpCode,
                                    EUExCallback.F_C_INT, EUExCallback.F_C_FAILED);
                        }
                    }
                }
            }
        }).start();
    }


    public void copy(String[] params){
        CopyVO copyVO=DataHelper.gson.fromJson(params[0],CopyVO.class);
        final String srcFilePath = copyVO.src, objPath = copyVO.target;
        int callbackId = -1;
        if (params.length > 1) {
            callbackId = Integer.parseInt(params[1]);
        }
        boolean flag = false;
        if (srcFilePath.startsWith(BUtility.F_Widget_RES_SCHEMA)) {
            flag = true;
        }
        final String srcFileRealPath = BUtility.makeRealPath(BUtility.makeUrl(mBrwView.getCurrentUrl(), srcFilePath),
                mBrwView.getCurrentWidget().m_widgetPath, mBrwView.getCurrentWidget().m_wgtType);
        Log.i(tag, "srcFileRealPath: " + srcFileRealPath);
        if (srcFileRealPath.startsWith(BUtility.F_Widget_RES_path)) {
            flag = true;
        }
        final String objRealPath = BUtility.makeRealPath(BUtility.makeUrl(mBrwView.getCurrentUrl(), objPath),
                mBrwView.getCurrentWidget().m_widgetPath, mBrwView.getCurrentWidget().m_wgtType);

        final boolean finalFlag = flag;
        final int finalCallbackId = callbackId;
        new Thread(new Runnable() {
            @Override
            public void run() {
                File srcFile = new File(srcFileRealPath);
                File outFile = new File(objRealPath, srcFile.getName());
                Log.i(tag, "outFile: " + outFile.getAbsolutePath());
                FileInputStream fi = null;
                FileOutputStream fo = null;
                FileChannel in = null;
                FileChannel out = null;
                if (finalFlag && srcFileRealPath != null && srcFileRealPath.startsWith(BUtility.F_Widget_RES_path)) {
                    InputStream in_a = null;
                    OutputStream out_a = null;
                    try {
                        in_a = mContext.getAssets().open(srcFileRealPath);
                        int length = in_a.available();
                        out_a = new FileOutputStream(outFile);
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in_a.read(buffer)) != -1) {
                            out_a.write(buffer, 0, read);
                        }
                        in_a.close();
                        in_a = null;
                        out_a.flush();
                        out_a.close();
                        out_a = null;
                        File copied = new File(objRealPath + srcFile.getName());
                        if (copied.exists() && copied.length() == length) {
                            if (finalCallbackId != -1) {
                                callbackToJs(finalCallbackId, false, 0);
                            }
                        } else {
                            if (finalCallbackId != -1) {
                                callbackToJs(finalCallbackId, false, 1);
                            }
                        }
                    } catch (IOException e) {
                        BDebug.e("Failed to copy asset file: ", srcFileRealPath, e);

                        if (finalCallbackId != -1) {
                            callbackToJs(finalCallbackId, false, 1);
                        }
                    }
                } else {
                    // 复制除了assets路径以外的文件的情况
                    try {
                        FileCopier.copyDirectory(srcFile, outFile);
                        if (outFile.exists() && outFile.length() == srcFile.length()) {
                            if (finalCallbackId != -1) {
                                callbackToJs(finalCallbackId, false, 0);
                            }
                        } else {
                            if (finalCallbackId != -1) {
                                callbackToJs(finalCallbackId, false, 1);
                            }
                        }
                    } catch (IOException e) {
                        BDebug.e("Failed to copy asset file: ", srcFileRealPath, e);
                        if (finalCallbackId != -1) {
                            callbackToJs(finalCallbackId, false, 1);
                        }
                    }
                }
            }
        }).start();
    }

    private void callBackPluginJs(String methodName, String jsonData) {
        String js = SCRIPT_HEADER + "if(" + methodName + "){"
                + methodName + "('" + jsonData + "');}";
        onCallback(js);
    }

    private class GetFileSizeAsyncTask extends AsyncTask<String, String, ResultFileSizeVO> {

        int mCallbackId;

        public GetFileSizeAsyncTask(int callbackId) {
            mCallbackId = callbackId;
        }

        @Override
        protected ResultFileSizeVO doInBackground(String... params) {
            String filePath = params[0];
            String unit = params[1];
            String id = params[2];
            ResultFileSizeVO result = new ResultFileSizeVO();
            result.setId(id);
            File file = new File(filePath);
            if (!file.exists()) {
                result.setErrorCode(JsConst.RESULT_FILE_SIZE_FILE_NOT_EXIST);
            } else {
                try {
                    long size;
                    if (file.isDirectory()) {
                        size = FileUtility.getFileSizes(file);
                    } else {
                        size = FileUtility.getFileSize(file);
                    }
                    result.setErrorCode(JsConst.RESULT_FILE_SIZE_SUCCESS);
                    result.setData(FileUtility.formetFileSize(size, unit));
                    result.setUnit(unit);
                } catch (Exception e) {
                    e.printStackTrace();
                    result.setErrorCode(JsConst.RESULT_FILE_SIZE_UNKNOWN_ERROR);
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(ResultFileSizeVO result) {
            if (mCallbackId != -1) {
                callbackToJs(mCallbackId, false, result.getErrorCode(), DataHelper.gson.toJsonTree(result));
            } else {
                if (result != null) {
                    callBackPluginJs(JsConst.CALLBACK_GET_FILE_SIZE_BY_PATH,
                            DataHelper.gson.toJson(result));
                }
            }
        }
    }

    @Override
    public void onHandleMessage(Message message) {
        if (message == null) {
            return;
        }
        Bundle bundle = message.getData();
        switch (message.what) {
            case MSG_SEARCH:
                try {
                    searchMsg(bundle.getStringArray(BUNDLE_DATA));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            default:
                super.onHandleMessage(message);
        }
    }


    public ArrayList<String> searchFile(String path, int option, JSONArray keywords, JSONArray suffixes) {
        try {
            ArrayList<String> fileList = new ArrayList<String>();
            //如果前端路径写的是 res://转换后 路径为widget/wgtRes/ ,如果不把最后一个/去掉，会导致getAssets().list(path)在有文件的情况下返回为空
            if (path.endsWith("/")) {
                path = path.substring(0, path.lastIndexOf("/"));
            }
            String rootPath = path;
            switch (option) {
                case 0: //搜索当前目录下的文件
                    getFilesInCurrentDir(path, fileList, suffixes, keywords, false, false);
                    break;
                case 1:
                    //当suffixes存在时，仅搜索文件
                    if (suffixes != null && suffixes.length() > 0) {
                        getFilesInCurrentDir(path, fileList, suffixes, keywords, false, false);
                    } else { //搜索包含文件夹
                        getFilesInCurrentDir(path, fileList, suffixes, keywords, false, true);
                    }
                    break;
                case 2: //精确匹配 只搜索文件名恰为keyword的文件, 不含文件夹
                    //此时，keywords不能为空
                    if (keywords == null || keywords.length() == 0) {
                        Toast.makeText(m_context, finder.getString("plugin_fileMgr_need_keywords"), Toast.LENGTH_SHORT).show();
                        return null;
                    }
                    getFilesInCurrentDir(path, fileList, suffixes, keywords, true, false);
                    break;
                case 3: //精确匹配，同时包含文件夹
                    if (keywords == null || keywords.length() == 0) {
                        return null;
                    }
                    getFilesInCurrentDir(path, fileList, suffixes, keywords, true, true);
                    break;
                case 4: //递归搜索，只返回文件
                    getAllFiles(path, fileList, suffixes, keywords, false, false);
                    break;
                case 5: //递归搜索，返回结果包含文件和文件夹
                    getAllFiles(path, fileList, suffixes, keywords, false, true);
                    break;
                case 6://递归搜索，返回结果包含文件，且精确匹配
                    if (keywords == null || keywords.length() == 0) {
                        return null;
                    }
                    getAllFiles(path, fileList, suffixes, keywords, true, true);
                    break;
                case 7: //递归搜索，返回结果包含文件，文件夹，且精确匹配
                    if (keywords == null || keywords.length() == 0) {
                        return null;
                    }
                    getAllFiles(path, fileList, suffixes, keywords, true, true);
                    break;
            }
            for (int i = 0; i < fileList.size(); i++) {
                fileList.set(i,fileList.get(i).replace(rootPath + "/", "")); //去掉传进来的目录
            }
            return fileList;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getFilesInCurrentDir(String path, ArrayList<String> fileList, JSONArray suffixes, JSONArray keywords,
                                     Boolean isKeywordMatchExactly, boolean isContainDirectory) throws JSONException, IOException {
        //assets目录下的
        if (path.startsWith(RES_ROOT)) {
            String fileNameList[] = m_context.getResources().getAssets().list(path);
            //如果传进来的就是一个文件, 如果文件满足这个规则，就直接返回这个文件
            if (fileNameList == null || fileNameList.length == 0) {
                if (hasSuffix(path, suffixes) && hasKeywords(path, keywords, isKeywordMatchExactly)) {
                    fileList.add(path);
                }
                return;
            }
            String[] filePaths = m_context.getResources().getAssets().list(path);
            int len = filePaths.length;
            for (int i = 0; i < len; i++) {
                if (hasSuffix(filePaths[i], suffixes) && hasKeywords(path + "/" + filePaths[i], keywords, isKeywordMatchExactly)) {
                    if (isContainDirectory) {
                        fileList.add(path + "/" + filePaths[i]);
                    } else {
                        //如果是文件
                        String[] temp = m_context.getResources().getAssets().list(path + "/" + filePaths[i]);
                        if (temp == null || temp.length == 0) {
                            fileList.add(path + "/" + filePaths[i]);
                        }
                    }
                }
            }
        } else {
            File file = new File(path);
            if (!file.isDirectory()) {
                if (hasSuffix(file.getName(), suffixes) && hasKeywords(file.getAbsolutePath(), keywords, isKeywordMatchExactly)) {
                    fileList.add(file.getAbsolutePath());
                }
                return;
            }
            File[] files = new File(path).listFiles();
            for (File fileTemp : files) {
                if (hasSuffix(fileTemp.getName(), suffixes) && hasKeywords(fileTemp.getAbsolutePath(), keywords, isKeywordMatchExactly)) {
                    if (isContainDirectory) {
                        fileList.add(fileTemp.getAbsolutePath());
                    } else {
                        if (!fileTemp.isDirectory()) {
                            fileList.add(fileTemp.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    public void getAllFiles(String path, ArrayList<String> fileList, JSONArray suffixes, JSONArray keywords,
                            boolean isKeywordMatchExactly, boolean isContainDirectory) throws JSONException, IOException {
        if (path.startsWith(RES_ROOT)) {
            getAllFilesInAssets(path, fileList, suffixes, keywords, isKeywordMatchExactly, isContainDirectory);
        } else {
            getAllFilesInSDCard(new File(path), fileList, suffixes, keywords, isKeywordMatchExactly, isContainDirectory);
        }
    }

    /**
     * 递归遍历某个目录下的文件
     *
     * @param dir                   文件目录
     * @param list                  返回的列表
     * @param suffixes              文件后缀名
     * @param keywords              匹配的关键字
     * @param isKeywordMatchExactly 关键字是否严格匹配
     * @param isContainDirectory    匹配时是否考虑目录
     * @throws JSONException
     */
    public void getAllFilesInSDCard(File dir, ArrayList<String> list, JSONArray suffixes, JSONArray keywords,
                                    boolean isKeywordMatchExactly, boolean isContainDirectory) throws JSONException, IOException {
        if (!dir.isDirectory()) {
            if (hasSuffix(dir.getName(), suffixes) && hasKeywords(dir.getAbsolutePath(), keywords, isKeywordMatchExactly)) {
                list.add(dir.getAbsolutePath());
            }
            return;
        }

        File[] fs = dir.listFiles();
        for (File file : fs) {
            if (file.isDirectory()) {
                if (isContainDirectory) {
                    if (hasSuffix(file.getName(), suffixes) && hasKeywords(file.getAbsolutePath(), keywords, isKeywordMatchExactly)) {
                        list.add(file.getAbsolutePath() + "/");
                    }
                }
                getAllFilesInSDCard(file, list, suffixes, keywords, isKeywordMatchExactly, isContainDirectory);
            } else {
                if (hasSuffix(file.getName(), suffixes) && hasKeywords(file.getAbsolutePath(), keywords, isKeywordMatchExactly)) {
                    list.add(file.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 递归遍历assets下的所有文件
     *
     * @param path                  assets下面的某个文件目录
     * @param list                  符合要求的文件名列表
     * @param suffixes              文件后缀名
     * @param keywords              匹配的关键字
     * @param isKeywordMatchExactly 关键字是否严格匹配
     * @param isContainDirectory    匹配时是否考虑目录
     * @throws JSONException
     * @throws IOException
     */
    public void getAllFilesInAssets(String path, ArrayList<String> list, JSONArray suffixes, JSONArray keywords,
                                    boolean isKeywordMatchExactly, boolean isContainDirectory) throws JSONException, IOException {
        String fileNameList[] = m_context.getResources().getAssets().list(path);
        //如果传进来的就是一个文件, 如果文件满足这个规则，就直接返回这个文件
        if (fileNameList == null || fileNameList.length == 0) {
            if (hasSuffix(path, suffixes) && hasKeywords(path, keywords, false)) {
                list.add(path);
            }
            return;
        }
        for (int i = 0; i < fileNameList.length; i++) {
            String strTemp[] = m_context.getResources().getAssets().list(path + "/" + fileNameList[i]);
            //如果是文件
            if (strTemp == null || strTemp.length == 0) {
                if (hasSuffix(fileNameList[i], suffixes) && hasKeywords(path + "/" + fileNameList[i], keywords, isKeywordMatchExactly)) {
                    list.add(path + "/" + fileNameList[i]);
                }
            } else {
                if (isContainDirectory) {
                    if (hasSuffix(fileNameList[i], suffixes) && hasKeywords(path + "/" + fileNameList[i], keywords, isKeywordMatchExactly)) {
                        list.add(path + "/" + fileNameList[i] + "/");
                    }
                }
                getAllFilesInAssets(path + "/" + fileNameList[i], list, suffixes, keywords, isKeywordMatchExactly, isContainDirectory);
            }
        }

    }

    public boolean hasSuffix(String fileName, JSONArray suffixArray) throws JSONException {
        if (suffixArray == null || suffixArray.length() == 0) {
            return true;
        }
        int len = suffixArray.length();
        for (int i = 0; i < len; i++) {
            if (fileName.endsWith(suffixArray.getString(i))) {
                return true;
            }
        }
        return false;
    }

    //path需要文件的绝对路径，针对assets目录下文件，path 以 widget/wgtRes开头
    public boolean hasKeywords(String path, JSONArray keywords, boolean isExactly) throws JSONException, IOException {
        if (keywords == null || keywords.length() == 0) {
            return true;
        }
        //获取目录或文件的文件名
        String fileName = "";
        if (path.startsWith(RES_ROOT)) {
            String strTemp[] = m_context.getResources().getAssets().list(path);
            //如果是文件
            if (strTemp == null || strTemp.length == 0) {
                fileName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf('.'));
            } else {
                fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
            }
        } else {
            File f = new File(path);
            if (!f.isDirectory()) {
                fileName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf('.'));
            } else {
                fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
            }
        }
        int len = keywords.length();
        for (int i = 0; i < len; i++) {
            if (isExactly) {
                if (fileName.equals(keywords.getString(i))) {
                    return true;
                }
            } else {
                if (fileName.contains(keywords.getString(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkAppStatus(Context inActivity, String appId) {
        try {
            String appstatus = ResoureFinder.getInstance().getString(
                    inActivity, "appstatus");
            byte[] appstatusToByte = PEncryption.hexStringToBinary(appstatus);
            String appstatusDecrypt = new String(PEncryption.os_decrypt(
                    appstatusToByte, appstatusToByte.length, appId));
            String[] appstatuss = appstatusDecrypt.split(",");
            if ((appstatuss == null) || (appstatuss.length == 0)) {
                return false;
            }
            if ("1".equals(appstatuss[9]))
                return true;
        } catch (Exception e) {
            BDebug.w("uexFileMgr_", e.getMessage(), e);
        }
        return false;
    }

    private void jsCallback(String inCallbackName, String inOpCode,
                            int inDataType, int inData) {
        String js = SCRIPT_HEADER + "if(" + inCallbackName + "){"
                + inCallbackName + "('" + inOpCode + "'," + inDataType + ","
                + inData + SCRIPT_TAIL;
        onCallback(js);
    }

    private void jsCallback(String inCallbackName, String inOpCode,
                            int inDataType, String inData) {
        String js = SCRIPT_HEADER + "if(" + inCallbackName + "){"
                + inCallbackName + "('" + inOpCode + "'," + inDataType + ",'"
                + inData + "'" + SCRIPT_TAIL;
        onCallback(js);
    }

    private void errorCallback(String inOpCode, int InErrorCode,
                               String inErrorInfo) {
        String js = SCRIPT_ERROR_HEADER + "'" + inOpCode + "'," + InErrorCode + ",'"
                + inErrorInfo + "'" + SCRIPT_TAIL;
        onCallback(js);
    }

    public void getFileHashValue(String[] params) {
        if (params.length < 1) {
            return;
        }

        int callbackId = -1;
        if (params.length > 1) {
            callbackId = Integer.parseInt(params[1]);
        }
        final String json = params[0];
        final int finalCallbackId = callbackId;
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    String path = jsonObject.getString("path");
                    String algorithm = jsonObject.getString("algorithm");
                    String realPath = BUtility.makeRealPath(path, mBrwView);

                    InputStream fis;
                    if (realPath.startsWith(BUtility.F_Widget_RES_path)) {
                        AssetManager am = m_context.getAssets();
                        fis = am.open(realPath);
                    } else {
                        fis = new FileInputStream(realPath);
                    }

                    byte[] buffer = new byte[1024];
                    MessageDigest complete = MessageDigest.getInstance(algorithm);
                    int numRead;
                    do {
                        numRead = fis.read(buffer);
                        if (numRead > 0) {
                            complete.update(buffer, 0, numRead);
                        }
                    } while (numRead != -1);
                    fis.close();

                    byte[] b = complete.digest();
                    String result = "";
                    for (int i = 0; i < b.length; i++) {
                        result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
                    }
                    if (finalCallbackId != -1) {
                        callbackToJs(finalCallbackId, false, result);
                    } else {
                        jsCallback(F_CALLBACK_NAME_GETFILEHASHVALUE, 0, EUExCallback.F_C_TEXT, result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}