package com.netfinworks.site.core.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netfinworks.common.lang.FileUtil;


/**
 * <p>文件上传工具</p>
 * @author eric
 * @version $Id: FileUploader.java, v 0.1 2013-8-1 下午3:45:22 Exp $
 */
public class FileUploader {
    private static final Logger logger = LoggerFactory.getLogger(FileUploader.class);

    /** 根目录地�?*/
    private String              rootPath;
    /** 子目录地�?*/
    private String              subPath;

    /**
     * 上传文件
     * @param in
     * @param fileName
     * @return
     */
    public String upload(InputStream in, String fileName) throws IllegalAccessException {
        // 创建目标文件
        File targetFile = new File(getFileDir(), FileUtil.getFileName(fileName));
        try {
            // 写文�?
            this.write(in, new FileOutputStream(targetFile));
        } catch (Exception e) {
            logger.error("文件读写异常", e);
            throw new IllegalAccessException("文件读写异常");
        }

        return FileUtil.normalizePath(targetFile.getAbsolutePath());
    }

    /**
     * 文件写入
     * @param in
     * @param out
     * @throws Exception
     */
    private void write(InputStream in, OutputStream out) throws Exception {
        BufferedInputStream inBuffer = null;
        BufferedOutputStream outBuffer = null;
        try {
            inBuffer = new BufferedInputStream(in);
            outBuffer = new BufferedOutputStream(out);

            byte[] bytes = new byte[2048];
            int s = 0;

            while ((s = inBuffer.read(bytes)) != -1) {
                outBuffer.write(bytes, 0, s);
            }
        } finally {
            if (inBuffer != null) {
                try {
                    inBuffer.close();
                } catch (IOException e) {
                    // ignore
                }
            }

            if (outBuffer != null) {
                try {
                    outBuffer.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * 获取文件目录
     * @return
     */
    private File getFileDir() {
        File dir = new File(rootPath + subPath);
        if (dir.exists()) {
            return dir;
        }
        dir.mkdirs();

        return dir;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public void setSubPath(String subPath) {
        this.subPath = subPath;
    }

    /**
     * 创建唯一文件名
     *
     * @param memberId
     * @param fileName
     * @return
     */
    public static String createFileName(String memberId, String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        return memberId + "_" + System.currentTimeMillis()  + suffix;
    }
}
