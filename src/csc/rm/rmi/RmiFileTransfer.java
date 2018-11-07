package csc.rm.rmi;

import csc.rm.bean.FileModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述:
 * Created by zdk on 2018/11/7.
 */
public class RmiFileTransfer implements Serializable {

    private static final long serialVersionUID = -7669925635714569018L;
    private FileModel fileModel;

    private Map<String, byte[]> dataMap = new HashMap<>();

    public FileModel getFileModel() {
        return fileModel;
    }

    public void setFileModel(FileModel fileModel) {
        this.fileModel = fileModel;
    }

    public Map<String, byte[]> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, byte[]> dataMap) {
        this.dataMap = dataMap;
    }
}
