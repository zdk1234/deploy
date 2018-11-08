package csc.rm.util;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java.util.Properties;

public class LoggerUtil {

    private static Properties properties = new Properties();
    private Logger logger = null;

    static {
        DOMConfigurator.configure("conf/log4j.xml");
    }

    public LoggerUtil(Class<? extends Object> _clazz) {
        logger = Logger.getLogger(_clazz);
    }

    public LoggerUtil(String name) {
        logger = Logger.getLogger(name);
    }


    /**
     * Debug 输出
     *
     * @param clazz   目标.Class
     * @param message 输出信息
     */
    public static void debug(Class<? extends Object> clazz, String message) {
        Logger logger = Logger.getLogger(clazz);
        logger.debug(message);
    }

    /**
     * Error 输出
     *
     * @param clazz   目标.Class
     * @param message 输出信息
     * @param e       异常类
     */
    public static void error(Class<? extends Object> clazz, String message, Exception e) {
        Logger logger = Logger.getLogger(clazz);
        if (null == e) {
            logger.error(message);
            return;
        }
        logger.error(message, e);
    }

    /**
     * Error 输出
     *
     * @param clazz   目标.Class
     * @param message 输出信息
     */
    public static void error(Class<? extends Object> clazz, String message) {
        error(clazz, message, null);
    }

    public void debug(Object _msg) {
        logger.debug(_msg);
    }

    public void info(Object _msg) {
        logger.info(_msg);
    }

    public void error(Object _msg) {
        logger.error(_msg);
    }
}
