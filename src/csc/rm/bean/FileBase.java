package csc.rm.bean;

import java.io.Serializable;

/**
 * 功能描述:
 * Created by zdk on 2018/11/7.
 */
public class FileBase implements Serializable {

    private static final long serialVersionUID = 7741508420601013300L;

    private String filePath;

    private boolean isDirectory = false;

    public FileBase() {
    }

    public FileBase(String filePath, boolean isDirectory) {
        this.filePath = filePath;
        this.isDirectory = isDirectory;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }
}
