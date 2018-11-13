package csc.rm.logic;

import csc.rm.bean.CompareableFileBean;
import csc.rm.bean.FileBase;
import csc.rm.bean.FileModel;
import csc.rm.rmi.RmiFileTransfer;
import csc.rm.rmi.RmiHandleFactory;
import csc.rm.rmi.RmiService;
import csc.rm.util.FileUtil;
import csc.rm.util.LoggerUtil;
import csc.rm.util.PropertiesUtil;

import java.io.*;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 功能描述:
 * Created by zhaodengke on 2018/11/6.
 */
public class DeployMonitor {

    private static final LoggerUtil LOGGER = new LoggerUtil(DeployMonitor.class);

    private static Map<String, CompareableFileBean> FILE_MAP = new LinkedHashMap<>();

    private static Executor executor = Executors.newSingleThreadExecutor();

    /**
     * 获取本地需要监视的文件夹路径
     */
    private static String sourcePath;

    private static boolean inited = false;

    private static Thread monitorThread;

    private static String rmiUri = PropertiesUtil.getValue("rmi.uri") + ":" + PropertiesUtil.getValue("rmi.port") + "/" + PropertiesUtil.getValue("rmi.context");

    private static boolean isSynchronizeAll = Boolean.valueOf(PropertiesUtil.getValue("monitor.synchronizeall"));

    private static boolean isSynchronize24 = Boolean.valueOf(PropertiesUtil.getValue("monitor.synchronize.24hours"));

    private static AtomicBoolean runSwitch = new AtomicBoolean(true);

    private DeployMonitor() {

    }

    public static void init() throws Exception {
        sourcePath = PropertiesUtil.getValue("monitor.sourcepath");
        if (Objects.equals(sourcePath, "")) {
            throw new IllegalStateException("conf/config.properties -> monitor.sourcepath 配置监视目录为空");
        }
        FILE_MAP = getFiles(sourcePath, true);

        LOGGER.info("<初始化> RMI服务端地址:" + rmiUri);

        if (isSynchronizeAll || isSynchronize24) {
            // 第一次启动同步最后修改时间为24小时以内的文件
            if (isSynchronizeAll) {
                LOGGER.info("<初始化> 正在同步" + sourcePath + "中全部文件...");
            } else {
                LOGGER.info("<初始化> 正在同步" + sourcePath + "中24小时以内修改的文件...");
            }
            long now = System.currentTimeMillis() / 1000 - 86400000;
            RmiFileTransfer rmiFileTransfer = new RmiFileTransfer();
            Map<String, byte[]> dataMap = new HashMap<>();
            FileModel fileModel = new FileModel();
            for (Map.Entry<String, CompareableFileBean> entry : FILE_MAP.entrySet()) {
                String key = entry.getKey();
                CompareableFileBean cfb = entry.getValue();
                if (!Objects.equals(new File(sourcePath).getAbsolutePath(), key)) {
                    File file = cfb.getFile();
                    long modifiedTime = file.lastModified() / 1000;
                    if (isSynchronizeAll || (modifiedTime > now && isSynchronize24)) {
                        fileModel.addFile(new FileBase(key, file.isDirectory()));
                        if (!file.isDirectory()) {
                            try (InputStream is = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(is)) {
                                byte[] bytes = bis.readAllBytes();
                                dataMap.put(key, bytes);
                            }
                        }
                    }
                }
            }
            if (fileModel.isChange()) {
                rmiFileTransfer.setFileModel(fileModel);
                rmiFileTransfer.setDataMap(dataMap);
                rmiFileTransfer.setSourcePath(sourcePath);
                sendRmiFileTransfer(rmiFileTransfer);
            }
            LOGGER.info("<初始化> 同步成功");
        }

        monitorThread = new Thread(DeployMonitor::monitor);
        monitorThread.setDaemon(true);
        inited = true;
        LOGGER.info("<初始化> 启动完成");
    }

    /**
     * 启动监视线程
     */
    public static void run() {
        if (!inited) {
            throw new IllegalStateException("初始化未正确完成,无法启动监视");
        }
        executor.execute(monitorThread);
    }

    /**
     * 关闭监视器
     */
    public static void stop() {
        runSwitch.set(false);
    }

    /**
     * 监视逻辑
     */
    private static void monitor() {
        while (runSwitch.get()) {
            RmiFileTransfer rmiFileTransfer = new RmiFileTransfer();
            try {
                final Map<String, CompareableFileBean> fileMap = getFiles(sourcePath, false);

                FileModel fileModel = new FileModel();

                // 获取删除的文件
                FILE_MAP.forEach((key, cfb) -> {
                    if (!fileMap.containsKey(key)) {
                        File file = cfb.getFile();
                        fileModel.addDeletedFile(new FileBase(file.getAbsolutePath(), file.isDirectory()));
                    }
                });

                fileMap.forEach((path, cfb) -> {
                    File file = cfb.getFile();
                    boolean exists = file.exists();
                    if (exists) {
                        if (!file.isDirectory()) {
                            try {
                                String fileMD5 = FileUtil.getFileMD5(file);
                                cfb.setMD5(fileMD5);
                            } catch (FileNotFoundException e) {
                                fileModel.addDeletedFile(new FileBase(path, false));
                            } catch (IOException e) {
                                LOGGER.error(e);
                            }
                        }
                    } else {
                        fileModel.addDeletedFile(new FileBase(path, false));
                    }
                });

                List<FileBase> deletedFileList = fileModel.getDeletedFileList();

                // 获取新增&修改的文件
                for (Map.Entry<String, CompareableFileBean> entry : fileMap.entrySet()) {
                    // 当前文件夹快照
                    String key = entry.getKey();
                    CompareableFileBean compareableFileBean = entry.getValue();
                    File file = compareableFileBean.getFile();

                    if (deletedFileList.stream().noneMatch(fileBase -> Objects.equals(fileBase.getFilePath(), key))) {
                        if (FILE_MAP.containsKey(key)) {
                            if (!file.isDirectory()) {
                                CompareableFileBean cfb = FILE_MAP.get(key);
                                if (cfb != null) {
                                    boolean isSameFile = Objects.equals(cfb.getMD5(), compareableFileBean.getMD5());
                                    if (!isSameFile) {
                                        fileModel.addDiffFile(new FileBase(file.getAbsolutePath(), false));
                                    }
                                }
                            }
                        } else {
                            fileModel.addFile(new FileBase(file.getAbsolutePath(), file.isDirectory()));
                        }
                    }
                }

                List<FileBase> addedFileList = fileModel.getAddedFileList();
                List<FileBase> diffFileList = fileModel.getDiffFileList();

                if (fileModel.isChange()) {
                    final StringBuilder builder = new StringBuilder();
                    if (!addedFileList.isEmpty()) {
                        builder.append("新增:\n");
                        addedFileList.forEach(fileBase -> builder.append("\t").append(fileBase.toString()).append("\n"));
                    }
                    if (diffFileList.size() != 0) {
                        builder.append("修改:\n");
                        diffFileList.forEach(fileBase -> builder.append("\t").append(fileBase.toString()).append("\n"));
                    }
                    if (deletedFileList.size() != 0) {
                        builder.append("删除:\n");
                        deletedFileList.forEach(fileBase -> builder.append("\t").append(fileBase.toString()).append("\n"));
                    }
                    LOGGER.info(builder.toString());

                    rmiFileTransfer.setFileModel(fileModel);
                    rmiFileTransfer.setSourcePath(sourcePath);

                    Map<String, byte[]> dataMap = new HashMap<>();
                    addedFileList.addAll(diffFileList);
                    addedFileList.stream().filter(fileBase -> !fileBase.isDirectory()).forEach(fileBase -> {
                        try (InputStream is = new FileInputStream(new File(fileBase.getFilePath())); BufferedInputStream bis = new BufferedInputStream(is)) {
                            byte[] bytes = bis.readAllBytes();
                            dataMap.put(fileBase.getFilePath(), bytes);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    rmiFileTransfer.setDataMap(dataMap);

                    sendRmiFileTransfer(rmiFileTransfer);
                    FILE_MAP = new HashMap<>(fileMap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * RMI发送文件数据
     *
     * @param rmiFileTransfer
     */
    private static void sendRmiFileTransfer(RmiFileTransfer rmiFileTransfer) throws Exception {
        RmiService rmiService;
        try {
            rmiService = (RmiService) RmiHandleFactory.getRempte(rmiUri);
            rmiService.getRmiFileTransfer(rmiFileTransfer);
        } catch (RemoteException e) {
            if (e instanceof ConnectException) {
                rmiService = (RmiService) RmiHandleFactory.registry(rmiUri);
                rmiService.getRmiFileTransfer(rmiFileTransfer);
            } else {
                throw e;
            }
        }
    }

    /**
     * 获取监视文件夹的全部File实例
     *
     * @param path
     * @return
     */
    private static Map<String, CompareableFileBean> getFiles(String path, boolean isMD5Set) throws IOException {
        File folder = new File(path);
        if (!folder.exists()) {
            throw new IllegalStateException("检索文件夹不存在:" + folder.getAbsolutePath());
        }
        if (!folder.isDirectory()) {
            throw new IllegalStateException("检索目标:" + folder.getAbsolutePath() + "不是一个文件夹");
        }
        List<File> fileList = FileUtil.getFileList(folder);
        Map<String, CompareableFileBean> tMap = new LinkedHashMap<>();
        for (File file : fileList) {
            String md5 = null;
            if (!file.isDirectory() && isMD5Set) {
                md5 = FileUtil.getFileMD5(file);
            }
            tMap.put(file.getAbsolutePath(), new CompareableFileBean(file, md5));
        }
        return tMap;
    }
}
