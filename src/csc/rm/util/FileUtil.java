package csc.rm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 功能描述:
 * Created by zhaodengke on 2018/11/6.
 */
public class FileUtil {

    private static MessageDigest messageDigest;

    static {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private FileUtil() {

    }

    /**
     * 递归获取文件夹下全部文件
     *
     * @param target
     * @return
     */
    public static List<File> getFileList(File target) {
        final List<File> fileList = new ArrayList<>();
        if (target.isFile()) {
            fileList.add(target);
        } else if (target.isDirectory()) {
            final File[] files = target.listFiles();
            if (files != null && files.length != 0) {
                Arrays.stream(files).forEach(file -> {
                    if (file.isDirectory()) {
                        fileList.addAll(getFileList(file));
                    } else {
                        fileList.add(file);
                    }
                });
            }
        }
        return fileList;
    }

    /**
     * 判断两个文件是否一样
     *
     * @param source
     * @param target
     * @return
     * @throws IOException
     */
    public static boolean isSmaeFile(File source, File target) throws IOException {
        if (source == null || target == null) {
            throw new IllegalArgumentException("比较文件不能为空");
        }
        return source.length() == target.length() && Objects.equals(getFileMD5(source), getFileMD5(target));
    }

    /**
     * 获取文件MD5
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String getFileMD5(File file) throws IOException {
        if (messageDigest == null) {
            throw new IllegalStateException("MessageDigest 初始化异常");
        }

        if (file == null) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer, 0, 1024)) > 0) {
                messageDigest.update(buffer, 0, length);
            }
            return new BigInteger(1, messageDigest.digest()).toString(16);
        }
    }
}
