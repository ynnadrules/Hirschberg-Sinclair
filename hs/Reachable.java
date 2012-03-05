
package hs;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Daniel Ruiz
 */
public interface Reachable extends Remote {
    
    public static final String HOST_NAME = "localhost";
    public static final int PORT = 1099;
    
    public String reachMeAt() throws RemoteException;
    
}
