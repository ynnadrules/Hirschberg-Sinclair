/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hs;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Daniel Ruiz
 */
public interface NodeService extends Remote {
    
    public static final String SERVICE_NAME_PREFIX = "Node"; 
    
    public void probe( MessageType type, Direction direction ) throws RemoteException;
    
    public void left( NodeService n ) throws RemoteException;
    public void right( NodeService n ) throws RemoteException;
    
    public NodeService left() throws RemoteException;
    public NodeService right() throws RemoteException;
    
    public void activate() throws RemoteException;
    
    public void send( Probe probe ) throws RemoteException;
    public Probe receive() throws RemoteException;
    
    public long pid() throws RemoteException;
}
