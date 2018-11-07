package csc.rm.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述:
 * Created by zhaodengke on 2018/11/6.
 */
public class FileModel implements Serializable {

    private static final long serialVersionUID = 6780835632676467975L;

    /**
     * 新增的文件
     */
    private List<FileBase> addedFileList = new ArrayList<>();

    /**
     * 修改的文件
     */
    private List<FileBase> diffFileList = new ArrayList<>();

    /**
     * 删除的文件
     */
    private List<FileBase> deletedFileList = new ArrayList<>();

    public FileModel() {
    }

    public FileModel(List<FileBase> addedFileList, List<FileBase> diffFileList, List<FileBase> deletedFileList) {
        this.addedFileList = addedFileList;
        this.diffFileList = diffFileList;
        this.deletedFileList = deletedFileList;
    }

    public boolean isChange() {
        boolean f = addedFileList.isEmpty() && diffFileList.isEmpty() && deletedFileList.isEmpty();
        return !f;
    }

    public void addFile(FileBase file) {
        this.addedFileList.add(file);
    }

    public void addDiffFile(FileBase file) {
        this.diffFileList.add(file);
    }

    public void addDeletedFile(FileBase file) {
        this.deletedFileList.add(file);
    }

    public List<FileBase> getAddedFileList() {
        return addedFileList;
    }

    public List<FileBase> getDiffFileList() {
        return diffFileList;
    }

    public List<FileBase> getDeletedFileList() {
        return deletedFileList;
    }
}
