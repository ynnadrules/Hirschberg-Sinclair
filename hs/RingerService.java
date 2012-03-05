package hs;

import java.net.MalformedURLException;
import java.rmi.*;


/**
 *
 * @author Daniel Ruiz
 */
public interface RingerService extends Remote {
    public void registerNode( NodeService n ) throws RemoteException,
            NotBoundException, UnknownHostException, MalformedURLException;
    
    public void report( Report report ) throws RemoteException;
}
