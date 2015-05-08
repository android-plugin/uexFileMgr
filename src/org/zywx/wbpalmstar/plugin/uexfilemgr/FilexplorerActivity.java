package org.zywx.wbpalmstar.plugin.uexfilemgr;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.base.cache.MyAsyncTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class FilexplorerActivity extends Activity implements OnItemClickListener, OnClickListener {
	public static final String F_INTENT_KEY_RETURN_EXPLORER_PATH = "returnExplorerPath";
	public static final String F_INTENT_KEY_RETURN_PATH_LIST = "pathList";
	public static final String F_INTENT_KEY_MULTI_FLAG = "flag";
	private static final String TAG = "FilexplorerActicity";
	private ListView lv_fileList;
	private TextView tv_filePath;
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
	private TranslateAnimation popUpAnim;
	private TranslateAnimation pushDownAnim;
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
						if (startFile.getAbsolutePath().contains(SDCARD_PATH)) {
							currentFile = startFile;
						} else {
							Toast.makeText(this,
									finder.getString("plugin_file_input_path_is_not_valid_path_redirect_to_sdcard"),
									Toast.LENGTH_LONG).show();
							currentFile = new File(sdPath);
						}
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
		tv_filePath = (TextView) findViewById(finder.getId("plugin_file_tv_file_path"));
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

		DecelerateInterpolator interpolator = new DecelerateInterpolator();
		popUpAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		popUpAnim.setDuration(200);
		popUpAnim.setInterpolator(interpolator);

		pushDownAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
		pushDownAnim.setDuration(200);
		pushDownAnim.setInterpolator(interpolator);
		strConfirm = finder.getString("confirm");
		strCancel = finder.getString("cancel");
		strPrompt = finder.getString("prompt");
	}

	// 点击按钮的操作
	@Override
	public void onClick(View v) {
		if (v == btnBack) {
			backToParent();
		} else if (v == btnCancel) {
			if (fileListAdapter.isMultiSelectMode()) {// 处于多选模式，取消多选模式
				btnCancel.setBackgroundDrawable(multiSelectDrawable);
				btnCancel.setText("");
				fileListAdapter.setMultiSelectMode(false);
				layoutBottom.setVisibility(View.GONE);
				layoutBottom.startAnimation(pushDownAnim);
			} else {// 处于普通模式,进入多选模式
				fileListAdapter.setMultiSelectMode(true);
				btnCancel.setBackgroundDrawable(cancelDrawable);
				btnCancel.setText(strCancel);
				layoutBottom.setVisibility(View.VISIBLE);
				btnCancelAll.setEnabled(false);
				btnSelectAll.setEnabled(true);
				btnSelectConfirm.setText(strConfirm);
				layoutBottom.startAnimation(popUpAnim);
			}
		} else if (v == btnSelectAll) {
			fileListAdapter.setAllItemsSelect(true);
			notifyItemSelectChanged();
		} else if (v == btnCancelAll) {
			fileListAdapter.setAllItemsSelect(false);
			notifyItemSelectChanged();
		} else if (v == btnSelectConfirm) {
			ArrayList<FileBean> arrayList = fileListAdapter.getTotalSelectedList();
			String json = combinationJson(arrayList);
			final Intent intent = new Intent(getIntent().getAction());
			intent.putExtra(F_INTENT_KEY_RETURN_EXPLORER_PATH, json);
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
					intent.putExtra(F_INTENT_KEY_RETURN_EXPLORER_PATH, clickedFile.getAbsolutePath());
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
			confirmExit(strPrompt, finder.getString("plugin_file_are_you_sure_to_exit_file_explorer"));
		} else {
			openDirectory(parent, false);
		}
	}

	public void openDirectory(final File file, final boolean enterOrBack) {
		new MyAsyncTask() {

			public void handleOnPreLoad(MyAsyncTask task) {
				progressDialog = ProgressDialog.show(FilexplorerActivity.this, null,
						finder.getString("plugin_file_now_loading_folder"), false, false);
			};

			protected Object doInBackground(Object... params) {
				return fileDao.getFileList(file);
			};

			@SuppressWarnings("unchecked")
			public void handleOnCompleted(MyAsyncTask task, Object result) {
				progressDialog.dismiss();
				if (result == null) {
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
				currentFile = file;
				tv_filePath.setText(currentFile.getAbsolutePath());
				tvTitle.setText(currentFile.getName());
				notifyItemSelectChanged();
				if (currentFile.getAbsolutePath().equals(SDCARD_PATH)) {
					btnBack.setVisibility(View.INVISIBLE);
				} else {
					btnBack.setVisibility(View.VISIBLE);
				}
				if (enterOrBack) {// 进入下一层文件夹
					lv_fileList.setSelection(0);
				} else {// 退回上一层文件夹
					if (!historyPostionStack.empty()) {
						int lastPostion = historyPostionStack.pop();
						if (lastPostion >= 0 && lastPostion < fileListAdapter.getCount()) {
							lv_fileList.setSelection(lastPostion);
						}
					}
				}
			};
		}.execute(new Object[] {});
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
		fileListAdapter.clear();
		super.onDestroy();
	}

	private String combinationJson(ArrayList<FileBean> list) {
		JSONObject json = new JSONObject();
		int index = 0;
		for (FileBean b : list) {
			try {
				json.put(String.valueOf(index++), b.getFile().getAbsolutePath());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return json.toString();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

}