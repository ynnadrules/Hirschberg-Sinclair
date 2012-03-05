/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hs;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Random;

/**
 *  The Ringer exports a remote method that nodes can use 
 *  to register themselves into the Ring.
 * 
 *  The Ringer joins the nodes together and then chooses a random
 *  node to begin the leader election process
 * 
 * @author Daniel Ruiz
 */
public class Ringer extends UnicastRemoteObject implements Remote, Loggable, 
        Reachable, RingerService {
    
    private static Ringer ringer;
    
    public final int MAX_NODES;
    
    private LinkedList<NodeService> ring;
    
    private ReportSet reportSet;
    
    private final String SERVICE_NAME = "Ringer";
    
    private static final String EXPORT_FILE = "stats.txt";
    
    public Ringer( int maxNodes ) throws RemoteException {
        super();
        ring = new LinkedList<NodeService>();
        reportSet = new ReportSet( SERVICE_NAME, maxNodes );
        MAX_NODES = maxNodes;
    }
    
    @Override
    public String logIdent() {
        return SERVICE_NAME;
    }
    
    @Override
    public String reachMeAt() {
        return logIdent();
    }
    
    @Override
    public synchronized void registerNode( NodeService node )
            throws RemoteException,
            NotBoundException, UnknownHostException, MalformedURLException
            
    {
        
        if( ring.size() < MAX_NODES ) {

            node.left( ring.peekLast() );
            node.right( ring.peekFirst() );
            
            ring.add( node );
            Logger.info( String.format( "Node-%x registered with registry "
                    + "(%d spots left, current ring size: %d)", 
                    node.pid(), MAX_NODES - ring.size(), ring.size() ), this );
            
        } 
        
        if( ring.size() == MAX_NODES ) {
            init();
        }
        
    }
    
    private void init() throws RemoteException {
        try {
            Thread.sleep( 10000 );
        } catch( InterruptedException ie ) {
            Logger.error( "init() sleep messed up: " + ie, this );
        }
        
        Random rand = new Random();
        rand.setSeed( Calendar.getInstance().getTimeInMillis() );
        
        NodeService n = ring.get( rand.nextInt( ring.size() ) );
        n.activate();
        
        Logger.info( String.format( "Node-%x has been chosen for "
                + "activation", n.pid() ), this );
    }
    
    @Override
    public void report( Report report ) throws RemoteException {
        reportSet.add( report );
        
        if( reportSet.size() == MAX_NODES && reportSet.available() ) {
            new Thread( new Runnable() {
                @Override
                public void run() {
                    prepareReport();
                }
            }).start();
        }
    }
    
    private void prepareReport() {
        Logger.info( "Releasing report...", this );
        
        System.out.println( reportSet.summary() );

        reportSet.export( String.format( "%s", EXPORT_FILE ) );
        
        shutdown();
    }
    
    private void shutdown() {
        try {
            Naming.unbind( ringer.reachMeAt() );
            UnicastRemoteObject.unexportObject(ringer, true);
            Logger.info( "Ringer is shutting down", this);
        } catch( Exception e ) {
            Logger.error( "Failed to shut down Ringer: " + e, this );
        }
        
        System.exit( 0 );
    }

    public static void main( String[] args ) {
        int max;
        
        System.setSecurityManager( new RMISecurityManager() );
        
        try {
            
            if( args.length >= 1 )
                max = Integer.parseInt( args[0] );
            else
                max = 10;
            
            ringer = new Ringer( max );
            
            LocateRegistry.createRegistry( PORT );

            Logger.info( "Registry created on port " + PORT, ringer );
            
            Naming.rebind( ringer.reachMeAt(), ringer );
            
            Logger.info( "Ringer binded and loaded....", ringer );
            
            Logger.info( "Waiting for " + ringer.MAX_NODES + 
                    " node registrations...", ringer );
            
        } catch( UnknownHostException uhe ) {
            Logger.error( "Host not recognized: " + uhe, ringer );
        } catch( RemoteException re ) {
            Logger.error( "Error starting service: " + re, ringer );
        } catch( Exception e ) {
            Logger.error( "Exception occured: " + e, ringer );
        } finally {
            
        }
    }
}
