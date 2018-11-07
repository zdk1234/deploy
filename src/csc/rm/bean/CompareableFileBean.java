package csc.rm.bean;

import java.io.File;

/**
 * 功能描述:
 * Created by zdk on 2018/11/7.
 */
public class CompareableFileBean {

    private File file;

    private String MD5;

    public CompareableFileBean() {
    }

    public CompareableFileBean(File file, String MD5) {
        this.file = file;
        this.MD5 = MD5;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getMD5() {
        return MD5;
    }

    public void setMD5(String MD5) {
        this.MD5 = MD5;
    }
}
