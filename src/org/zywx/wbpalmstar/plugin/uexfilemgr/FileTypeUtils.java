package org.zywx.wbpalmstar.plugin.uexfilemgr;

import android.webkit.MimeTypeMap;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * https://github.com/nbsp-team/MaterialFilePicker
 *
 * Created by nickolay on 25.10.15.
 */

public class FileTypeUtils {

    public enum FileType {
        DIRECTORY(EUExUtil.getResDrawableID("plugin_file_ic_folder_gray_48dp"), EUExUtil.getResStringID
                ("plugin_file_type_directory)")),
        DOCUMENT(EUExUtil.getResDrawableID("plugin_file_ic_document_box"), EUExUtil.getResStringID("plugin_file_type_document)")),
        CERTIFICATE(EUExUtil.getResDrawableID("plugin_file_ic_certificate_box"), EUExUtil.getResStringID("plugin_file_type_certificate"), "cer", "der", "pfx", "p12", "arm", "pem"),
        DRAWING(EUExUtil.getResDrawableID("plugin_file_ic_drawing_box"), EUExUtil.getResStringID("plugin_file_type_drawing"), "ai", "cdr", "dfx", "eps", "svg", "stl", "wmf", "emf", "art", "xar"),
        EXCEL(EUExUtil.getResDrawableID("plugin_file_ic_excel_box"), EUExUtil.getResStringID("plugin_file_type_excel"), "xls", "xlk", "xlsb", "xlsm", "xlsx", "xlr", "xltm", "xlw", "numbers", "ods", "ots"),
        IMAGE(EUExUtil.getResDrawableID("plugin_file_ic_image_box"), EUExUtil.getResStringID("plugin_file_type_image"), "bmp", "gif", "ico", "jpeg", "jpg", "pcx", "png", "psd", "tga", "tiff", "tif", "xcf"),
        MUSIC(EUExUtil.getResDrawableID("plugin_file_ic_music_box"), EUExUtil.getResStringID("plugin_file_type_music"), "aiff", "aif", "wav", "flac", "m4a", "wma", "amr", "mp2", "mp3", "wma", "aac", "mid", "m3u"),
        VIDEO(EUExUtil.getResDrawableID("plugin_file_ic_video_box"), EUExUtil.getResStringID("plugin_file_type_video"), "avi", "mov", "wmv", "mkv", "3gp", "f4v", "flv", "mp4", "mpeg", "webm"),
        PDF(EUExUtil.getResDrawableID("plugin_file_ic_pdf_box"), EUExUtil.getResStringID("plugin_file_type_pdf"), "pdf"),
        POWER_POINT(EUExUtil.getResDrawableID("plugin_file_ic_powerpoint_box"), EUExUtil.getResStringID("plugin_file_type_power_point"), "pptx", "keynote", "ppt", "pps", "pot", "odp", "otp"),
        WORD(EUExUtil.getResDrawableID("plugin_file_ic_word_box"), EUExUtil.getResStringID("plugin_file_type_word"), "doc", "docm", "docx", "dot", "mcw", "rtf", "pages", "odt", "ott"),
        ARCHIVE(EUExUtil.getResDrawableID("plugin_file_ic_zip_box"), EUExUtil.getResStringID("plugin_file_type_archive"), "cab", "7z", "alz", "arj", "bzip2", "bz2", "dmg", "gzip", "gz", "jar", "lz", "lzip", "lzma", "zip", "rar", "tar", "tgz"),
        APK(EUExUtil.getResDrawableID("plugin_file_ic_apk_box"), EUExUtil.getResStringID("plugin_file_type_apk"), "apk");

        private int icon;
        private int description;
        private String[] extensions;

        FileType(int icon, int description, String... extensions) {
            this.icon = icon;
            this.description = description;
            this.extensions = extensions;
        }

        public String[] getExtensions() {
            return extensions;
        }

        public int getIcon() {
            return icon;
        }

        public int getDescription() {
            return description;
        }
    }

    private static Map<String, FileType> fileTypeExtensions = new HashMap<String, FileType>();

    static {
        for (FileType fileType : FileType.values()) {
            for (String extension : fileType.getExtensions()) {
                fileTypeExtensions.put(extension, fileType);
            }
        }
    }

    public static FileType getFileType(File file) {
        if (file.isDirectory()) {
            return FileType.DIRECTORY;
        }

        FileType fileType = fileTypeExtensions.get(getExtension(file.getName()));
        if (fileType != null) {
            return fileType;
        }

        return FileType.DOCUMENT;
    }

    public static String getExtension(String fileName) {
        String encoded;
        try {
            encoded = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            encoded = fileName;
        }
        return MimeTypeMap.getFileExtensionFromUrl(encoded).toLowerCase();
    }
}
