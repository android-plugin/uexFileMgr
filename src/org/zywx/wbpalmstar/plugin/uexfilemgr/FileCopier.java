package org.zywx.wbpalmstar.plugin.uexfilemgr;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;


/**
 * File Description:
 * <p>
 * Created by sandy with Email: sandy1108@163.com at Date: 2023/2/12.
 */
public class FileCopier {
    /**
     * 复制目录和文件
     *
     * @param sourceLocation
     * @param targetLocation
     * @throws IOException
     */
    public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            // 如果源文件是目录，则目标也应该是目录
            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }
            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {
            // 如果源文件不是目录，则目标文件也应该是文件
            FileChannel source = null;
            FileChannel target = null;
            try {
                source = new FileInputStream(sourceLocation).getChannel();
                target = new FileOutputStream(targetLocation).getChannel();
                target.transferFrom(source, 0, source.size());
            } finally {
                if (source != null) {
                    source.close();
                }
                if (target != null) {
                    target.close();
                }
            }
        }
    }
}
