package csc.rm.rmi;

import csc.rm.util.PropertiesUtil;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述: RMI Handle服务工厂类
 * Created by zhaodk on 2018/2/27.
 */
public class RmiHandleFactory {

    private final static Map<String, Remote> RMI_HANDLE_MAP = new HashMap<>();

    private RmiHandleFactory() {

    }

    /**
     * 获取RMI Handle
     *
     * @param rmiURI
     * @return
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws NotBoundException
     */
    public static synchronized Remote getRempte(String rmiURI) throws MalformedURLException, RemoteException, NotBoundException {
        if (RMI_HANDLE_MAP.get(rmiURI) == null) {
            return registry(rmiURI);
        } else {
            return RMI_HANDLE_MAP.get(rmiURI);
        }
    }

    /**
     * 重新获取RMI Handle
     *
     * @param rmiURI
     * @return
     * @throws RemoteException
     * @throws NotBoundException
     * @throws MalformedURLException
     */
    public static synchronized Remote registry(String rmiURI) throws RemoteException, NotBoundException, MalformedURLException {
        Remote remote = Naming.lookup(rmiURI);
        RMI_HANDLE_MAP.put(rmiURI, remote);
        return remote;
    }
}
