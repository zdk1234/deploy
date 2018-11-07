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
    private List<String> addedFileList = new ArrayList<>();

    /**
     * 修改的文件
     */
    private List<String> diffFileList = new ArrayList<>();

    /**
     * 删除的文件
     */
    private List<String> deletedFileList = new ArrayList<>();

    public FileModel() {
    }

    public FileModel(List<String> addedFileList, List<String> diffFileList, List<String> deletedFileList) {
        this.addedFileList = addedFileList;
        this.diffFileList = diffFileList;
        this.deletedFileList = deletedFileList;
    }

    public boolean isChange() {
        boolean f = addedFileList.isEmpty() && diffFileList.isEmpty() && deletedFileList.isEmpty();
        return !f;
    }

    public void addFile(String file) {
        this.addedFileList.add(file);
    }

    public void addDiffFile(String file) {
        this.diffFileList.add(file);
    }

    public void addDeletedFile(String file) {
        this.deletedFileList.add(file);
    }

    public List<String> getAddedFileList() {
        return addedFileList;
    }

    public List<String> getDiffFileList() {
        return diffFileList;
    }

    public List<String> getDeletedFileList() {
        return deletedFileList;
    }
}
