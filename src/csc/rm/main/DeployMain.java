package csc.rm.main;

import csc.rm.logic.DeployMonitor;

/**
 * 功能描述:
 * Created by zdk on 2018/11/7.
 */
public class DeployMain {

    /**
     * 监视Main入口
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            DeployMonitor.init();
            DeployMonitor.run();
        } catch (Exception e) {
            DeployMonitor.stop();
        }
    }
}
