package csc.rm.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 功能描述:
 * Created by zdk on 2018/11/7.
 */
public interface RmiService extends Remote {

    int getRmiFileTransfer(RmiFileTransfer rmiFileTransfer) throws RemoteException;

}
