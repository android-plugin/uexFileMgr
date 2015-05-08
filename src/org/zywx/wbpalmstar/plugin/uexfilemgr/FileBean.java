package org.zywx.wbpalmstar.plugin.uexfilemgr;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.ResoureFinder;
import android.content.Context;

public class FileBean {

	public static final String TAG = "FileBean";
	public static final String KEY_ICON = "ICON";
	public static final String KEY_NAME = "NAME";
	public static final String KEY_SIZE = "SIZE";
	public static final String KEY_FILE = "FILE";

	private static final String FOLDER = "文件夹";
	private int resourceId;
	private File file;
	public String fileSize = "";
	public String lastModifyDate = "";
	private Context context;
	private ResoureFinder finder;

	public FileBean(Context context, File file) {
		if (file == null) {
			throw new NullPointerException("file can not be null.......");
		}
		this.context = context;
		finder = ResoureFinder.getInstance();
		resourceId = finder.getDrawableId(context, "plugin_file_unknown");
		this.file = file;
		try {
			setResourceId();
			if (file.isDirectory()) {
				this.fileSize = FOLDER;
			} else {
				this.fileSize = FileUtility.getFileInfoBySize(this.file.length());
			}
			this.lastModifyDate = formatFileModifyTime(this.file.lastModified());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public File getFile() {
		return file;
	}

	public int getResourceId() {
		return resourceId;
	}

	private void setResourceId() throws SecurityException {
		if (file == null) {
			return;
		}
		String name = file.getName();
		if (name == null || name.length() == 0) {
			return;
		}
		if (file.isDirectory()) {
			final String[] array = file.list();
			if (array == null || array.length == 0) {
				resourceId = finder.getDrawableId(context, "plugin_file_emptyfolder");
			} else {
				resourceId = finder.getDrawableId(context, "plugin_file_folder");
			}
			return;
		}
		final String suffix = name.substring(name.lastIndexOf(".") + 1, name.length());
		if (suffix.equalsIgnoreCase("jpg") || suffix.equalsIgnoreCase("png") || suffix.equalsIgnoreCase("bmp")
				|| suffix.equalsIgnoreCase("gif")) {
			resourceId = finder.getDrawableId(context, "plugin_file_photo");
		} else if (suffix.equalsIgnoreCase("mp3") || suffix.equalsIgnoreCase("aac") || suffix.equalsIgnoreCase("wav")
				|| suffix.equalsIgnoreCase("wma") || suffix.equalsIgnoreCase("amr")) {
			resourceId = finder.getDrawableId(context, "plugin_file_music");
		} else if (suffix.equalsIgnoreCase("apk")) {
			resourceId = finder.getDrawableId(context, "plugin_file_apk");
		} else if (suffix.equalsIgnoreCase("3gp") || suffix.equalsIgnoreCase("mp4") || suffix.equalsIgnoreCase("wmv")
				|| suffix.equalsIgnoreCase("avi") || suffix.equalsIgnoreCase("mov") || suffix.equalsIgnoreCase("mkv")
				|| suffix.equalsIgnoreCase("m4v") || suffix.equalsIgnoreCase("rm") || suffix.equalsIgnoreCase("rmvb")
				|| suffix.equalsIgnoreCase("flv")) {
			resourceId = finder.getDrawableId(context, "plugin_file_video");
		} else if (suffix.equalsIgnoreCase("txt") || suffix.equalsIgnoreCase("xml")) {
			this.resourceId = finder.getDrawableId(context, "plugin_file_txt");
		} else if (suffix.equalsIgnoreCase("doc") || suffix.equalsIgnoreCase("docx")) {
			resourceId = finder.getDrawableId(context, "plugin_file_doc");
		} else if (suffix.equalsIgnoreCase("xls") || suffix.equalsIgnoreCase("xlsx")) {
			resourceId = finder.getDrawableId(context, "plugin_file_excel");
		} else if (suffix.equalsIgnoreCase("ppt") || suffix.equalsIgnoreCase("pptx")) {
			resourceId = finder.getDrawableId(context, "plugin_file_ppt");
		} else if (suffix.equalsIgnoreCase("pdf")) {
			resourceId = finder.getDrawableId(context, "plugin_file_pdf");
		} else if (suffix.equalsIgnoreCase("zip") || suffix.equalsIgnoreCase("rar") || suffix.equalsIgnoreCase("7z")) {
			resourceId = finder.getDrawableId(context, "plugin_file_zip");
		} else {
			resourceId = finder.getDrawableId(context, "plugin_file_unknown");
		}
	}

	public int getResIdByFilePath(String filePath) {
		int resId = finder.getDrawableId(context, "plugin_file_unknown");
		if (filePath == null) {
			return resId;
		}
		final File currentFile = new File(filePath);
		try {
			if (currentFile == null || !currentFile.exists()) {
				return resId;
			}
			if (currentFile.isDirectory()) {
				final String[] array = currentFile.list();
				if (array == null || array.length == 0) {
					resId = finder.getDrawableId(context, "plugin_file_emptyfolder");
				} else {
					resId = finder.getDrawableId(context, "plugin_file_folder");
				}
				return resId;
			}
		} catch (SecurityException e) {
			BDebug.e(TAG, "getResIdByFilePath() " + filePath + " can not access!+ msg:" + e.getMessage());
		}

		final String name = currentFile.getName();
		final String suffix = name.substring(name.lastIndexOf(".") + 1, name.length());
		if (suffix.equalsIgnoreCase("jpg") || suffix.equalsIgnoreCase("png") || suffix.equalsIgnoreCase("bmp")
				|| suffix.equalsIgnoreCase("gif")) {
			resId = finder.getDrawableId(context, "plugin_file_photo");
		} else if (suffix.equalsIgnoreCase("mp3") || suffix.equalsIgnoreCase("aac") || suffix.equalsIgnoreCase("wav")
				|| suffix.equalsIgnoreCase("wma") || suffix.equalsIgnoreCase("amr")) {
			resId = finder.getDrawableId(context, "plugin_file_music");
		} else if (suffix.equalsIgnoreCase("apk")) {
			resId = finder.getDrawableId(context, "plugin_file_apk");
		} else if (suffix.equalsIgnoreCase("3gp") || suffix.equalsIgnoreCase("mp4") || suffix.equalsIgnoreCase("wmv")
				|| suffix.equalsIgnoreCase("avi") || suffix.equalsIgnoreCase("mov") || suffix.equalsIgnoreCase("mkv")
				|| suffix.equalsIgnoreCase("m4v") || suffix.equalsIgnoreCase("rm") || suffix.equalsIgnoreCase("rmvb")
				|| suffix.equalsIgnoreCase("flv")) {
			resId = finder.getDrawableId(context, "plugin_file_video");
		} else if (suffix.equalsIgnoreCase("txt") || suffix.equalsIgnoreCase("xml")) {
			resId = finder.getDrawableId(context, "plugin_file_txt");
		} else if (suffix.equalsIgnoreCase("doc") || suffix.equalsIgnoreCase("docx")) {
			resId = finder.getDrawableId(context, "plugin_file_doc");
		} else if (suffix.equalsIgnoreCase("xls") || suffix.equalsIgnoreCase("xlsx")) {
			resId = finder.getDrawableId(context, "plugin_file_excel");
		} else if (suffix.equalsIgnoreCase("ppt") || suffix.equalsIgnoreCase("pptx")) {
			resId = finder.getDrawableId(context, "plugin_file_ppt");
		} else if (suffix.equalsIgnoreCase("pdf")) {
			resId = finder.getDrawableId(context, "plugin_file_pdf");
		} else if (suffix.equalsIgnoreCase("zip") || suffix.equalsIgnoreCase("rar") || suffix.equalsIgnoreCase("7z")) {
			resId = finder.getDrawableId(context, "plugin_file_zip");
		} else {
			resId = finder.getDrawableId(context, "plugin_file_unknown");
		}
		return resId;
	}

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private String formatFileModifyTime(long milliSeconds) {
		return sdf.format(new Date(milliSeconds));
	}

	@Override
	public int hashCode() {
		return file.getAbsolutePath().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof FileBean)) {
			return false;
		}
		FileBean other = (FileBean) o;
		return file.getAbsolutePath().equals(other.getFile().getAbsolutePath());
	}
}