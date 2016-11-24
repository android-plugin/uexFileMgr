package org.zywx.wbpalmstar.plugin.uexfilemgr;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.ResoureFinder;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

public class FilexplorerActivity extends Activity implements OnItemClickListener, OnClickListener {
    public static final String F_INTENT_KEY_RETURN_EXPLORER_PATH = "returnExplorerPath";
    public static final String F_INTENT_KEY_RETURN_PATH_LIST = "pathList";
    public static final String F_INTENT_KEY_MULTI_FLAG = "flag";
    private static final String TAG = "FilexplorerActicity";
    private ListView lv_fileList;
    private File currentFile;
    private static final String SDCARD_PATH = "/sdcard";
    private ProgressDialog progressDialog;
    private FileListAdapter fileListAdapter;
    private Button btnBack;
    private Button btnCancel;
    private Button btnSelectAll;
    private Button btnCancelAll;
    private Button btnSelectConfirm;
    private LinearLayout layoutBottom;
    private TextView tvTitle;
    private FileDao fileDao = null;
    private Stack<Integer> historyPostionStack;
    private boolean canMultiSelected = true;
    private Animator popUpAnim;
    private Animator pushDownAnim;
    private Drawable cancelDrawable;
    private Drawable multiSelectDrawable;
    private String strConfirm;
    private String strCancel;
    private String strPrompt;
    private ResoureFinder finder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finder = ResoureFinder.getInstance(this);
        final String sdPath = FileDao.getSDcardPath();
        if (sdPath == null) {// SD卡不存在
            alertMessage(finder.getString("plugin_file_can_not_mount_sdcard"), true);
            return;
        }
        final Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            canMultiSelected = intent.getBooleanExtra(F_INTENT_KEY_MULTI_FLAG, false);
            final String startFilePath = intent.getData().getPath();
            if (startFilePath == null) {
                currentFile = new File(sdPath);
            } else {
                final File startFile = new File(startFilePath);
                if (startFile.exists()) {
                    if (startFile.isDirectory()) {
                        currentFile = startFile;
                    } else {
                        Toast.makeText(this,
                                finder.getString("plugin_file_input_path_is_not_valid_path_redirect_to_sdcard"),
                                Toast.LENGTH_LONG).show();
                        currentFile = new File(sdPath);
                    }
                } else {
                    Toast.makeText(this,
                            finder.getString("plugin_file_target_directory_is_not_exist_auto_redirect_to_sdcard"),
                            Toast.LENGTH_LONG).show();
                    currentFile = new File(sdPath);
                }
            }
        } else {
            currentFile = new File(sdPath);
        }
        fileDao = new FileDao(this);
        historyPostionStack = new Stack<Integer>();
        setContentView(finder.getLayoutId("plugin_file_main"));
        initViews();
        historyPostionStack.push(0);
        openDirectory(currentFile, true);
    }

    private void initViews() {
        multiSelectDrawable = finder.getDrawable("plugin_file_btn_multiselect_bg_selector");
        cancelDrawable = finder.getDrawable("plugin_file_btn_cancel_bg_selector");
        btnBack = (Button) findViewById(finder.getId("plugin_file_top_back"));
        btnBack.setOnClickListener(this);
        btnCancel = (Button) findViewById(finder.getId("plugin_file_top_cancel"));
        if (canMultiSelected) {
            btnCancel.setOnClickListener(this);
            btnCancel.setVisibility(View.VISIBLE);
        } else {
            btnCancel.setVisibility(View.GONE);
        }
        tvTitle = (TextView) findViewById(finder.getId("plugin_file_top_title"));
        lv_fileList = (ListView) findViewById(finder.getId("plugin_file_lv_file_list"));
        lv_fileList.setOnItemClickListener(this);
        lv_fileList.setItemsCanFocus(false);
        layoutBottom = (LinearLayout) findViewById(finder.getId("plugin_file_bottom_layout"));

        btnSelectAll = (Button) findViewById(finder.getId("plugin_file_bottom_btn_select_all"));
        btnSelectAll.setOnClickListener(this);

        btnCancelAll = (Button) findViewById(finder.getId("plugin_file_bottom_btn_cancel_all"));
        btnCancelAll.setOnClickListener(this);

        btnSelectConfirm = (Button) findViewById(finder.getId("plugin_file_bottom_btn_select_confirm"));
        btnSelectConfirm.setOnClickListener(this);

        strConfirm = finder.getString("confirm");
        strCancel = finder.getString("cancel");
        strPrompt = finder.getString("prompt");

        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        layoutBottom.measure(w,h);
    }

    public void startPushAnim(){
        //消失动画
        pushDownAnim = ObjectAnimator.ofFloat(layoutBottom,"translationY",0,layoutBottom.getHeight());
        pushDownAnim.setDuration(200);
        pushDownAnim.setInterpolator(new DecelerateInterpolator());
        pushDownAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                layoutBottom.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        pushDownAnim.start();
    }

    public void startPopAnim(){
        //出现动画
        popUpAnim = ObjectAnimator.ofFloat(layoutBottom,"translationY",layoutBottom.getMeasuredHeight(),0);
        popUpAnim.setDuration(200);
        popUpAnim.setInterpolator(new DecelerateInterpolator());
        popUpAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                layoutBottom.setTranslationY(layoutBottom.getMeasuredHeight());
                layoutBottom.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        popUpAnim.start();
    }

    // 点击按钮的操作
    @Override
    public void onClick(View v) {
        if (v == btnBack) {
            backToParent();
        } else if (v == btnCancel) {
            if (fileListAdapter.isMultiSelectMode()) {// 处于多选模式，取消多选模式
                btnCancel.setText(finder.getString("plugin_file_multi_file"));
                fileListAdapter.setMultiSelectMode(false);
                startPushAnim();
            } else {// 处于普通模式,进入多选模式
                fileListAdapter.setMultiSelectMode(true);
                btnCancel.setText(strCancel);
                btnCancelAll.setEnabled(false);
                btnSelectAll.setEnabled(true);
                btnSelectConfirm.setText(strConfirm);
                startPopAnim();
            }
        } else if (v == btnSelectAll) {
            fileListAdapter.setAllItemsSelect(true);
            notifyItemSelectChanged();
        } else if (v == btnCancelAll) {
            fileListAdapter.setAllItemsSelect(false);
            notifyItemSelectChanged();
        } else if (v == btnSelectConfirm) {
            ArrayList<FileBean> arrayList = fileListAdapter.getTotalSelectedList();
            final Intent intent = new Intent(getIntent().getAction());
            intent.putExtra(F_INTENT_KEY_RETURN_EXPLORER_PATH, getFileList(arrayList));
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    /**
     * 点击ListView子项事件处理函数
     */
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == lv_fileList) {
            final File clickedFile = fileListAdapter.getItem(position).getFile();// 获得被点击的FileBean对象
            if (clickedFile.isDirectory()) {// 点击了文件夹
                int firstVisablePostion = lv_fileList.getFirstVisiblePosition();
                historyPostionStack.push(firstVisablePostion);
                openDirectory(clickedFile, true);
            } else {// 点击了文件
                // 多选模式
                if (fileListAdapter.isMultiSelectMode()) {
                    boolean isChecked = fileListAdapter.getItemSelectState(position);
                    fileListAdapter.setItemSelectState(position, !isChecked);
                    notifyItemSelectChanged();
                } else {// 点选模式
                    BDebug.i(TAG, "Explorer Return Path:" + clickedFile.getAbsolutePath());
                    final Intent intent = new Intent(getIntent().getAction());
                    if (canMultiSelected){
                        ArrayList<String> paths=new ArrayList<String>();
                        paths.add(clickedFile.getAbsolutePath());
                        intent.putStringArrayListExtra(F_INTENT_KEY_RETURN_EXPLORER_PATH, paths);
                    }else{
                        intent.putExtra(F_INTENT_KEY_RETURN_EXPLORER_PATH, clickedFile.getAbsolutePath());
                    }
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }
    }

    private void notifyItemSelectChanged() {
        // 当前列表选中项个数
        int currentSelectCount = fileListAdapter.getSelectedCount();
        if (currentSelectCount > 0) {
            btnCancelAll.setEnabled(true);
        } else {
            btnCancelAll.setEnabled(false);
        }
        // 全部选中之后全选按钮设置为disable
        if (currentSelectCount == fileListAdapter.getSelectableCount()) {
            btnSelectAll.setEnabled(false);
        } else {
            btnSelectAll.setEnabled(true);
        }
        int totalCount = fileListAdapter.getTotalSelectedCount();
        btnSelectConfirm.setText(strConfirm + "(" + totalCount + ")");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backToParent();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void backToParent() {
        final File parent = currentFile.getParentFile();
        if (currentFile.getAbsolutePath().equals(SDCARD_PATH) || parent == null) {
            FilexplorerActivity.this.finish();
        } else {
            openDirectory(parent, false);
        }
    }

    public class LoadFileTask extends AsyncTask<Object, Integer, Object>{

        private File mFile;
        private boolean mEnterOrBack;

        public LoadFileTask(File file, boolean enterOrBack){
            mFile=file;
            mEnterOrBack=enterOrBack;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Object doInBackground(Object[] params) {
            return fileDao.getFileList(mFile);
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result == null) {
                if (historyPostionStack.isEmpty()){
                    finish();
                    return;
                }
                historyPostionStack.pop();
                Toast.makeText(FilexplorerActivity.this, finder.getString("plugin_file_can_not_open_this_folder"),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            ArrayList<FileBean> fileList = (ArrayList<FileBean>) result;
            if (fileListAdapter == null) {
                fileListAdapter = new FileListAdapter(FilexplorerActivity.this, fileList, lv_fileList);
                lv_fileList.setAdapter(fileListAdapter);
            } else {
                fileListAdapter.reload(fileList);
            }
            currentFile = mFile;
            tvTitle.setText(currentFile.getAbsolutePath());
            notifyItemSelectChanged();
            if (currentFile.getAbsolutePath().equals(SDCARD_PATH)) {
                btnBack.setVisibility(View.INVISIBLE);
            } else {
                btnBack.setVisibility(View.VISIBLE);
            }
            if (mEnterOrBack) {// 进入下一层文件夹
                lv_fileList.setSelection(0);
            } else {// 退回上一层文件夹
                if (!historyPostionStack.empty()) {
                    int lastPostion = historyPostionStack.pop();
                    if (lastPostion >= 0 && lastPostion < fileListAdapter.getCount()) {
                        lv_fileList.setSelection(lastPostion);
                    }
                }
            }
        }
    }

    public void openDirectory(final File file, final boolean enterOrBack) {
       new LoadFileTask(file,enterOrBack).execute();
    }



    private void alertMessage(String msg, final boolean exitOnClicked) {
        new AlertDialog.Builder(this).setTitle(strPrompt).setMessage(msg).setCancelable(false)
                .setPositiveButton(strConfirm, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (exitOnClicked) {
                            FilexplorerActivity.this.finish();
                        }
                    }
                }).show();

    }

    private void confirmExit(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(false)
                .setPositiveButton(strConfirm, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FilexplorerActivity.this.finish();
                    }
                }).setNegativeButton(strCancel, null).show();
    }

    @Override
    protected void onDestroy() {
        if (fileListAdapter != null) {
            fileListAdapter.clear();
        }
        super.onDestroy();
    }

    private ArrayList<String> getFileList(ArrayList<FileBean> list) {
        ArrayList<String> strings=new ArrayList<String>();
        int index = 0;
        for (FileBean b : list) {
            strings.add(b.getFile().getAbsolutePath());
        }
        return strings;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}