package csc.rm.logic;

import csc.rm.bean.FileModel;
import csc.rm.rmi.RmiFileTransfer;
import csc.rm.rmi.RmiHandleFactory;
import csc.rm.rmi.RmiService;
import csc.rm.util.FileUtil;
import csc.rm.util.PropertiesUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 功能描述:
 * Created by zhaodengke on 2018/11/6.
 */
public class DeployMonitor {

    private static Map<String, File> FILE_MAP = new LinkedHashMap<>();

    private static Executor executor = Executors.newSingleThreadExecutor();

    /**
     * 获取本地需要监视的文件夹路径
     */
    private static String sourcePath;

    private static boolean inited = false;

    private static Thread monitorThread;

    private static String rmiUri = PropertiesUtil.getValue("rmi.uri");

    private static AtomicBoolean runSwitch = new AtomicBoolean(true);

    private DeployMonitor() {

    }

    public static void init() {
        sourcePath = PropertiesUtil.getValue("monitor.sourcepath");
        if (Objects.equals(sourcePath, "")) {
            throw new IllegalStateException("conf/config.properties -> monitor.sourcepath 配置监视目录为空");
        }
        FILE_MAP = getFiles(sourcePath);
        monitorThread = new Thread(DeployMonitor::monitor);
        monitorThread.setDaemon(true);
        inited = true;
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
            try {
                final Map<String, File> fileMap = getFiles(sourcePath);
                FileModel fileModel = new FileModel();

                // 获取新增&修改的文件
                for (Map.Entry<String, File> entry : fileMap.entrySet()) {
                    String key = entry.getKey();
                    File file = entry.getValue();
                    if (FILE_MAP.containsKey(key)) {
                        if (!file.isDirectory()) {
                            boolean isSameFile = FileUtil.isSmaeFile(file, FILE_MAP.get(key));
                            if (!isSameFile) {
                                fileModel.addDiffFile(file.getAbsolutePath());
                            }
                        }
                    } else {
                        fileModel.addFile(file.getAbsolutePath());
                    }
                }

                // 获取删除的文件
                FILE_MAP.forEach((key, file) -> {
                    if (!fileMap.containsKey(key)) {
                        fileModel.addDeletedFile(file.getAbsolutePath());
                    }
                });

                //TODO: 调用RMI接口告知 fileModel 并让client端读取变动文件
                List<String> addedFileList = fileModel.getAddedFileList();
                List<String> diffFileList = fileModel.getDiffFileList();
                List<String> deletedFileList = fileModel.getDeletedFileList();

                if (addedFileList.size() != 0) {
                    System.out.println("新增:" + addedFileList);
                }
                if (diffFileList.size() != 0) {
                    System.out.println("修改:" + diffFileList);
                }
                if (deletedFileList.size() != 0) {
                    System.out.println("删除:" + deletedFileList);
                }

                if (fileModel.isChange()) {
                    RmiFileTransfer rmiFileTransfer = new RmiFileTransfer();
                    rmiFileTransfer.setFileModel(fileModel);

                    Map<String, byte[]> dataMap = new HashMap<>();
                    addedFileList.stream().map(File::new).filter(file -> !file.isDirectory()).forEach(file -> {
                        try (InputStream is = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(is)) {
                            byte[] bytes = bis.readAllBytes();
                            dataMap.put(file.getAbsolutePath(), bytes);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    rmiFileTransfer.setDataMap(dataMap);

                    RmiService rmiService = (RmiService) RmiHandleFactory.getRempte(rmiUri);
                    rmiService.getRmiFileTransfer(rmiFileTransfer);
                }

                FILE_MAP = new HashMap<>(fileMap);
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取监视文件夹的全部File实例
     *
     * @param path
     * @return
     */
    private static Map<String, File> getFiles(String path) {
        File folder = new File(path);
        if (!folder.exists()) {
            throw new IllegalStateException("检索文件夹不存在:" + folder.getAbsolutePath());
        }
        if (!folder.isDirectory()) {
            throw new IllegalStateException("检索目标:" + folder.getAbsolutePath() + "不是一个文件夹");
        }
        List<File> fileList = FileUtil.getFileList(folder);
        Map<String, File> tMap = new LinkedHashMap<>();
        fileList.forEach(file -> tMap.put(file.getAbsolutePath(), file));
        return tMap;
    }
}
