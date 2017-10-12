package org.zywx.wbpalmstar.plugin.uexfilemgr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.ResoureFinder;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.DisplayMetrics;

public class FileUtility {

	public static final String FILE_TAG = "file://";
	public static final String MNT_TAG = "/mnt";
	public static final String TAG = "FileUtility";
	private static final String[] projection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
	private static final String selection = MediaStore.Images.Media.DATA + "=? and "
			+ MediaStore.Images.Media.DATE_MODIFIED + "=?";

	/**
	 * 
	 * 
	 * @param size
	 * @return
	 */
	public static String getFileInfoBySize(long size) {
		if (size > 1073741824) {// 1GB
			return new BigDecimal(Double.valueOf(size) / 1073741824).setScale(2, BigDecimal.ROUND_DOWN).toString()
					+ "GB";
		} else if (size > 1048576) {// 1MB
			return new BigDecimal(Double.valueOf(size) / 1048576).setScale(2, BigDecimal.ROUND_DOWN).toString() + "MB";
		} else if (size > 1024) {// 1KB
			return new BigDecimal(Double.valueOf(size) / 1024).setScale(2, BigDecimal.ROUND_DOWN).toString() + "KB";
		} else {
			return size + "B";
		}
	}
	
	public static void storageCreateTime2Sp(Context context, String path) {
		SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SharedPreferences sp = context.getSharedPreferences("InitTime", Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		edit.putString(path, sdf.format(new Date()));
		edit.commit();
	}
	
	public static void storageLastTime2Sp(Context context, String path, long time) {
		SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SharedPreferences sp = context.getSharedPreferences("InitTime", Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		edit.putString(path, sdf.format(new Date(time)));
		edit.commit();
	}
	
	public static String getTimeFromSp(Context context, String path) {
		SharedPreferences sp = context.getSharedPreferences("InitTime", Context.MODE_PRIVATE);
		return sp.getString(path, "");
	}

	public static int getThumbnailSize(Activity activity) {
		final DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		switch (dm.densityDpi) {
		case DisplayMetrics.DENSITY_HIGH:
			return 72;
		case DisplayMetrics.DENSITY_LOW:
			return 36;
		case DisplayMetrics.DENSITY_MEDIUM:
			return 48;
		case 320:
			return 96;
		}
		return 48;
	}

	/**
	 * 获取图片的缩略图
	 * 
	 * @param destSize
	 * @param filePath
	 * @return
	 * @throws OutOfMemoryError
	 */
	public static Bitmap getPictureThumbnail(int destSize, String filePath) throws OutOfMemoryError {
		long start = System.currentTimeMillis();
		if (filePath == null || filePath.length() == 0) {
			return null;
		}
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;//
		Bitmap source = BitmapFactory.decodeFile(filePath, options);//
		final int srcHeight = options.outHeight;//
		final int srcWidth = options.outWidth;//
		if (srcHeight <= 0 || srcWidth <= 0) {//
			return null;
		}
		float scaleRate = 1;//
		if (srcHeight > srcWidth) {//
			scaleRate = srcHeight / destSize;
		} else {//
			scaleRate = srcWidth / destSize;
		}
		scaleRate = scaleRate > 1 ? scaleRate : 1;
		options.inJustDecodeBounds = false;
		options.inSampleSize = (int) scaleRate;
		options.inDither = false;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(filePath));
			source = BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);
			if (source != null) {
				final int width = source.getWidth();
				final int height = source.getHeight();
				if (width > height) {
					source = Bitmap.createBitmap(source, (width - height) / 2, 0, height, height);
				} else {
					source = Bitmap.createBitmap(source, 0, (height - width) / 2, width, width);
				}
			}
		} catch (Exception e) {
			BDebug.e(TAG, "getPictureThumbnail() ERROR:" + e.getMessage());
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (source != null) {
			BDebug.log("CostTime:" + (System.currentTimeMillis() - start) + "ms" + " bitmapBytes:"
					+ source.getRowBytes() * source.getHeight());
		}
		return source;
	}

	public static Bitmap getSystemPictureThumbnail(Context context, String filePath) {
		if (Build.VERSION.SDK_INT < 8) {
			return null;
		}
		String inPath = filePath.replace(FILE_TAG, "").replace(MNT_TAG, "");
		inPath = MNT_TAG + inPath;
		long imageId = -1;
		File file = new File(inPath);
		if (file.exists() && file.isFile()) {
			Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					projection, selection, new String[] { inPath, String.valueOf(file.lastModified()) }, null);
			if (cursor != null) {
				if (cursor.moveToNext()) {
					int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
					if (columnIndex != -1) {
						imageId = cursor.getLong(columnIndex);
					}
				}
				cursor.close();
			}
		}
		if (imageId == -1) {
			return null;
		}
		Bitmap bg = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), imageId,
				Images.Thumbnails.MICRO_KIND, null);
		return bg;
	}

	/**
	 * 获取音乐的封面专辑
	 * 
	 * @param filePath
	 * @param activity
	 * @return
	 */
	public static Bitmap getMusicAlbum(String filePath, Activity activity) throws OutOfMemoryError {
		String inPath = filePath.replace(FILE_TAG, "");
		final Cursor c = activity.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID }, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		int albumId = -1;
		while (c.moveToNext()) {
			String currentPath = c.getString(0);// "_data"
			currentPath = currentPath.replace(FILE_TAG, "");
			if (inPath.equals(currentPath)) {
				albumId = c.getInt(1);// "album_art"
				break;
			}
		}
		c.close();
		if (albumId == -1) {
			return null;
		}
		final String uriAlbums = "content://media/external/audio/albums/";
		final Cursor albumCursor = activity.getContentResolver().query(
				Uri.parse(uriAlbums + Integer.toString(albumId)),
				new String[] { MediaStore.Audio.AlbumColumns.ALBUM_ART }, null, null, null);
		String albumPath = null;
		if (albumCursor.moveToNext()) {
			albumPath = albumCursor.getString(0);
		}
		albumCursor.close();
		return getPictureThumbnail(getThumbnailSize(activity), albumPath);
	}

	/**
	 * 获取视频缩略图
	 * 
	 * @param activity
	 * @param filePath
	 * @return
	 */
	public static Bitmap getVideoThumbnail(Activity activity, String filePath) throws OutOfMemoryError {
		long start = System.currentTimeMillis();
		if (Build.VERSION.SDK_INT < 8) {
			BDebug.w(TAG, "getVideoThumbnail() API Level less than 8 (Android2.2), do not support!");
			return null;
		}
		String inPath = filePath.replace(FILE_TAG, "").replace(MNT_TAG, "");
		String videoId = null;
		Cursor cursor = activity.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID }, null, null, null);
		while (cursor.moveToNext()) {
			String currentPath = cursor.getString(0);// "_data"
			currentPath = currentPath.replace(FILE_TAG, "").replace(MNT_TAG, "");
			if (inPath.equals(currentPath)) {
				videoId = cursor.getInt(1) + "";// "_id"
				break;
			}
		}
		cursor.close();
		if (videoId == null) {
			return null;
		}
		Bitmap bg = MediaStore.Video.Thumbnails.getThumbnail(activity.getContentResolver(), Long.parseLong(videoId),
				Images.Thumbnails.MICRO_KIND, null);

		if (bg == null) {
			BDebug.e(TAG, "getVideoThumbnail can not get Video Thumbnail:@ " + filePath);
			return null;
		}
		Bitmap hover = BitmapFactory.decodeResource(activity.getResources(),
				ResoureFinder.getInstance().getDrawableId(activity, "plugin_file_video_icon_hover"));
		if (hover == null) {
			return null;
		}
		int bgWidth = bg.getWidth();
		int bgHeight = bg.getHeight();
		// 裁剪成正方形
		if (bgWidth > bgHeight) {
			bg = Bitmap.createBitmap(bg, (bgWidth - bgHeight) / 2, 0, bgHeight, bgHeight);
		} else {
			bg = Bitmap.createBitmap(bg, 0, (bgHeight - bgWidth) / 2, bgWidth, bgWidth);
		}
		bgWidth = bg.getWidth();
		bgHeight = bg.getHeight();
		// 缩小hover为bg的一半大小
		hover = Bitmap.createScaledBitmap(hover, bgWidth / 2, bgHeight / 2, false);
		Bitmap newbmp = Bitmap.createBitmap(bgWidth, bgHeight, Config.ARGB_8888);
		Canvas cv = new Canvas(newbmp);
		cv.drawBitmap(bg, 0, 0, null);// 在 0，0坐标开始画入bg
		cv.drawBitmap(hover, bgWidth / 4, bgHeight / 4, null);// hover居中
		// save all clip
		cv.save(Canvas.ALL_SAVE_FLAG);// 保存
		// store
		cv.restore();// 存储
		if (!bg.isRecycled()) {
			bg.recycle();
		}
		if (!hover.isRecycled()) {
			hover.recycle();
		}
		BDebug.i(TAG, "getVideoThumbnail bitmap:" + newbmp + " costTime:" + (System.currentTimeMillis() - start) + "ms");
		return newbmp;
	}

	/*
	 * 采用了新的办法获取APK图标，之前的失败是因为android中存在的一个BUG,通过 appInfo.publicSourceDir =
	 * apkPath;来修正这个问题，详情参见:
	 * http://code.google.com/p/android/issues/detail?id=9151
	 */
	public static Bitmap getApkIcon(Context context, String apkPath) {
		Bitmap bitmap = null;
		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
		if (info != null) {
			ApplicationInfo appInfo = info.applicationInfo;
			appInfo.sourceDir = apkPath;
			appInfo.publicSourceDir = apkPath;
			try {
				Drawable icon = appInfo.loadIcon(pm);
				bitmap = ((BitmapDrawable) icon).getBitmap();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}

    /**
     * 获取指定文件大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static long getFileSize(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        return fis.available();
    }
    /**
     * 获取指定文件夹大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static long getFileSizes(File file) throws Exception {
        long size = 0;
        File list[] = file.listFiles();
        for (int i = 0; i < list.length; i++) {
            if (list[i].isDirectory()) {
                size = size + getFileSizes(list[i]);
            } else {
                size = size + getFileSize(list[i]);
            }
        }
        return size;
    }

    /**
     * 转换文件大小,指定转换的类型
     *
     * @param fileS
     * @param sizeType
     * @return
     */
    public static double formetFileSize(long fileS, String sizeType) {
        DecimalFormat df = new DecimalFormat("#.00");
        double fileSizeLong;
        if (sizeType.equals(JsConst.SIZE_TYPE_B)){
            fileSizeLong = Double.valueOf(df.format((double) fileS));
        }else if(sizeType.equals(JsConst.SIZE_TYPE_KB)){
            fileSizeLong = Double.valueOf(df.format((double) fileS / 1024));
        }else if(sizeType.equals(JsConst.SIZE_TYPE_MB)) {
            fileSizeLong = Double.valueOf(df.format((double) fileS / 1048576));
        }else if(sizeType.equals(JsConst.SIZE_TYPE_GB)){
            fileSizeLong = Double.valueOf(df
                    .format((double) fileS / 1073741824));
        }else{
            fileSizeLong = Double.valueOf(df.format((double) fileS));
        }
        return fileSizeLong;
    }

    /**
     * @return widget目录是否拷贝完成到沙箱目录
     */
    public static boolean getIsCopyAssetsFinish(Context context) {
       boolean isCopyAssetsFinish = false;
       try {
            String javaName = "org.zywx.wbpalmstar.base.BUtility";
            Class c = Class.forName(javaName, true, context.getClassLoader());
            Method m = c.getMethod("getIsCopyAssetsFinish");
            if (null != m) {
                Object object = m.invoke(c, new Object[]{null});
                if (null != object) {
                    isCopyAssetsFinish = ((Boolean) object).booleanValue();
                }
            }
        } catch (Exception e) {
        }
        return isCopyAssetsFinish;
    }
}