package org.zywx.wbpalmstar.plugin.uexfilemgr;

import android.app.Activity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.zywx.wbpalmstar.base.ACEImageLoader;
import org.zywx.wbpalmstar.base.ResoureFinder;

import java.util.ArrayList;

/**
 * 文件管理器列表适配器
 * 
 * @ClassName: FileListAdapter
 * @Description: TODO
 * @author fangzhenyu
 * @date 2011-8-23 上午11:48:22
 */
public class FileListAdapter extends BaseAdapter {

	public static final String TAG = "FileListAdapter";
	private ArrayList<FileBean> m_listItems = new ArrayList<FileBean>();
	private LayoutInflater m_inflater;
	private ListView listView;
	private Activity activity;
	private int folderDrawableId;
	private int emptyFolderDrawableId;
	private int apkDrawableId;
	private int musicDrawableId;
	private int videoDrawableId;
	private int photoDrawableId;
	private SparseBooleanArray listSelectStates;
	private boolean multiSelectMode = false;
	private ResoureFinder finder;
	private ArrayList<FileBean> totalSelectedList = new ArrayList<FileBean>();

	public FileListAdapter(Activity context, ArrayList<FileBean> fileListData, ListView listView) {
		finder = ResoureFinder.getInstance(context);
		this.activity = context;
		m_inflater = LayoutInflater.from(context);
		m_listItems = fileListData;
		listSelectStates = new SparseBooleanArray(m_listItems.size());
		this.listView = listView;
		finder = ResoureFinder.getInstance();
		emptyFolderDrawableId = finder.getDrawableId("plugin_file_emptyfolder");
		folderDrawableId = finder.getDrawableId("plugin_file_folder");
		apkDrawableId = finder.getDrawableId("plugin_file_apk");
		musicDrawableId = finder.getDrawableId("plugin_file_music");
		videoDrawableId = finder.getDrawableId("plugin_file_video");
		photoDrawableId = finder.getDrawableId("plugin_file_photo");
		preloadList();
	}

	/**
	 * 是否处于多选模式
	 * 
	 * @return
	 */
	public boolean isMultiSelectMode() {
		return multiSelectMode;
	}

	/**
	 * 设置选择模式
	 * 
	 * @param multiSelect
	 *            true--->多选; false--->单选
	 */
	public void setMultiSelectMode(boolean multiSelect) {
		this.multiSelectMode = multiSelect;
		if (!multiSelect) {
			listSelectStates.clear();
			totalSelectedList.clear();
		}
		notifyDataSetChanged();
	}

	public int getCount() {
		return m_listItems.size();
	}

	/**
	 * 获得当前列表可选中的项的个数(只有文件能选中)
	 * 
	 * @return
	 */
	public int getSelectableCount() {
		int count = 0;
		for (int i = 0, size = getCount(); i < size; i++) {
			if (m_listItems.get(i).getFile().isFile()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 获得当前列表已选中的个数
	 * 
	 * @return
	 */
	public int getSelectedCount() {
		int count = 0;
		for (int i = 0, size = getCount(); i < size; i++) {
			if (listSelectStates.get(i)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 获得所有选中项的个数
	 * 
	 * @return
	 */
	public int getTotalSelectedCount() {
		return totalSelectedList.size();
	}

	/**
	 * 获得当前列表中指定位置的选中状态
	 * 
	 * @param postion
	 * @return true--->被选中; false--->未选中
	 */
	public boolean getItemSelectState(int postion) {
		return listSelectStates.get(postion);
	}

	public void reverseItemSelectState(int postion) {
		boolean isChecked = listSelectStates.get(postion);
		listSelectStates.put(postion, !isChecked);
		notifyDataSetChanged();
	}

	public void setItemSelectState(int postion, boolean state) {
		listSelectStates.put(postion, state);
		FileBean fileBean = m_listItems.get(postion);
		if (fileBean.getFile().isDirectory()) {
			throw new IllegalStateException("Directory can't be selected!");
		}
		if (state) {
			totalSelectedList.add(fileBean);
		} else {
			totalSelectedList.remove(fileBean);
		}
		notifyDataSetChanged();
	}

	public void setAllItemsSelect(boolean select) {
		for (int i = 0, count = getCount(); i < count; i++) {
			FileBean fileBean = getItem(i);
			if (fileBean.getFile().isFile()) {
				listSelectStates.put(i, false);
				totalSelectedList.remove(fileBean);
			}
		}
		if(select){
			for (int i = 0, count = getCount(); i < count; i++) {
				FileBean fileBean = getItem(i);
				if (fileBean.getFile().isFile()) {
					listSelectStates.put(i, select);
					totalSelectedList.add(fileBean);
				}
			}
		}
		notifyDataSetChanged();
	}

	public void clearTotalSelectedList() {
		listSelectStates.clear();
		totalSelectedList.clear();
		notifyDataSetChanged();
	}

	public ArrayList<FileBean> getTotalSelectedList() {
		return totalSelectedList;
	}

	public FileBean getItem(int position) {
		return m_listItems.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	/**
	 * 删除列表所有项
	 */
	public void removeAll() {
		m_listItems.clear();
		listSelectStates.clear();
		notifyDataSetChanged();
	}

	private void initSelectState() {
		for (int i = 0, size = getCount(); i < size; i++) {
			FileBean fileBean = getItem(i);
			if (fileBean.getFile().isFile() && totalSelectedList.contains(fileBean)) {
				listSelectStates.put(i, true);
			}
		}
	}

	public void reload(ArrayList<FileBean> fileListData) {
		if (fileListData == null) {
			throw new NullPointerException("fileListData can'y be null");
		}
		m_listItems.clear();
		listSelectStates.clear();

		m_listItems.addAll(fileListData);
		initSelectState();
		preloadList();
		notifyDataSetChanged();
	}

	/**
	 * 预加载List里面所有项的缩略图
	 */
	public void preloadList() {
		for (FileBean bean : m_listItems) {
			if (bean.getResourceId() == photoDrawableId || bean.getResourceId() == apkDrawableId
					|| bean.getResourceId() == musicDrawableId || bean.getResourceId() == videoDrawableId) {
                final View tagedView = listView.findViewWithTag(bean.getFile().getAbsolutePath());
                if (tagedView != null) {
                    ACEImageLoader.getInstance().displayImage((ImageView) tagedView,bean.getFile().getAbsolutePath());

                }
       		}
		}
	}

	/**
	 * 删除指定位置的项
	 * 
	 * @param postion
	 */
	public void deleteItem(int postion) {
		if (postion >= 0 && postion < m_listItems.size()) {
			m_listItems.remove(postion);
			notifyDataSetChanged();
		}
	}

	// 根据文件路径删除某项
	public void deleteItemByPath(String path) {
		if (path == null || path.length() == 0) {
			return;
		}
		for (FileBean bean : m_listItems) {
			if (bean.getFile().getAbsolutePath().equals(path)) {
				m_listItems.remove(bean);
				break;
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}


	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = m_inflater.inflate(finder.getLayoutId("plugin_file_filelist_item"), null);
			viewHolder = new ViewHolder();
			viewHolder.iconImageView = (ImageView) convertView.findViewById(finder.getId("plugin_file_iv_file_icon"));
			viewHolder.nameTextView = (TextView) convertView.findViewById(finder.getId("plugin_file_tv_file_name"));
			viewHolder.sizeTextView = (TextView) convertView.findViewById(finder.getId("plugin_file_tv_file_size"));
			viewHolder.selectCheckBox = (CheckBox) convertView
					.findViewById(finder.getId("plugin_file_cb_select_state"));
			convertView.setTag(viewHolder);// 缓存视图
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final FileBean bean = m_listItems.get(position);
		final String filePath = bean.getFile().getAbsolutePath();
		viewHolder.nameTextView.setText(bean.getFile().getName());
		viewHolder.sizeTextView.setText(bean.fileSize);
        FileTypeUtils.FileType fileType=FileTypeUtils.getFileType(bean.getFile());
		boolean isFolder = bean.getFile().isDirectory();
		if (multiSelectMode) {// 处于多选模式
			if (isFolder) {
				viewHolder.selectCheckBox.setVisibility(View.INVISIBLE);
			} else {
				viewHolder.selectCheckBox.setVisibility(View.VISIBLE);
				viewHolder.selectCheckBox.setChecked(listSelectStates.get(position));
			}
		} else {
			viewHolder.selectCheckBox.setVisibility(View.GONE);
		}
		// 设置图片链接为imageview 的tag来标记该imageview当前应该显示的图片
		viewHolder.iconImageView.setImageResource(fileType.getIcon());
		return convertView;
	}

	public void clear() {

	}

	@Override
	public int getItemViewType(int position) {
		return super.getItemViewType(position);
	}

	@Override
	public int getViewTypeCount() {
		return super.getViewTypeCount();
	}

	public static class ViewHolder {
		ImageView iconImageView;
		TextView nameTextView;
		TextView sizeTextView;
		CheckBox selectCheckBox;
	}
}