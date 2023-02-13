package org.zywx.wbpalmstar.plugin.uexfilemgr;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

/**
 * File Description: 自定义ListView
 * <p>
 * Created by sandy with Email: sandy1108@163.com at Date: 2023/2/12.
 */
public class FileListView extends ListView {

    private OnItemContentClickListener mOnItemContentClickListener;

    public FileListView(Context context) {
        super(context);
    }

    public FileListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FileListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnItemContentClickListener(OnItemContentClickListener listener) {
        mOnItemContentClickListener = listener;
    }

    public OnItemContentClickListener getOnItemContentClickListener() {
        return mOnItemContentClickListener;
    }

    public interface OnItemContentClickListener {
        void onContentItemClick(FileListView listView, View view, int position);
    }
}
