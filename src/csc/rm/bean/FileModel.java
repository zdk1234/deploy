package csc.rm.bean;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述:
 * Created by zhaodengke on 2018/11/6.
 */
public class FileModel implements Serializable {

    /**
     * 新增的文件
     */
    private List<File> addedFileList = new ArrayList<>();

    /**
     * 修改的文件
     */
    private List<File> diffFileList = new ArrayList<>();

    /**
     * 删除的文件
     */
    private List<File> deletedFileList = new ArrayList<>();

    public FileModel() {
    }

    public FileModel(List<File> addedFileList, List<File> diffFileList, List<File> deletedFileList) {
        this.addedFileList = addedFileList;
        this.diffFileList = diffFileList;
        this.deletedFileList = deletedFileList;
    }

    public void clear() {
        this.addedFileList = new ArrayList<>();
        this.diffFileList = new ArrayList<>();
        this.deletedFileList = new ArrayList<>();
    }

    public void addFile(File file) {
        this.addedFileList.add(file);
    }

    public void addDiffFile(File file) {
        this.diffFileList.add(file);
    }

    public void addDeletedFile(File file) {
        this.deletedFileList.add(file);
    }

    public List<File> getAddedFileList() {
        return addedFileList;
    }

    public List<File> getDiffFileList() {
        return diffFileList;
    }

    public List<File> getDeletedFileList() {
        return deletedFileList;
    }
}
