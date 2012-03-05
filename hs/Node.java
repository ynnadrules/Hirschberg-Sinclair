package hs;

import java.lang.Thread.State;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * The Node class represents a single node in the topology. It has
 * server and client capabilites and registers itself through the Ringer
 * Service.
 * 
 * 
 * @author Daniel Ruiz
 */
public class Node extends UnicastRemoteObject implements NodeService, Runnable, 
        Loggable  {
   
    private static NodeService node = null;
    private static RingerService ringer = null;
    
    private NodeService left;
    private NodeService right;
    
    private long pid;
    private long leaderPid;
    
    private boolean participating = false;
    private boolean isLeader = false;
    private boolean hasLeader = false;
    
    private boolean announcedAsLeader = false;
    
    private int phase = 0;
    private int replies = 0;
    
    private boolean activated = false;
    
    
    private Queue<Probe> messages;
    
    private Random rand = new Random();
    
    private Thread me;
    
    private Report report;
    
    private Node() throws RemoteException {
        super();
        
        rand.setSeed( Calendar.getInstance().getTimeInMillis() );
        
        pid = rand.nextLong();
        
        messages = new LinkedList<Probe>();
        report = new Report( String.format( "Node-%x", pid ) );
    }
    
    private static Node newInstance() throws RemoteException {
        Node instance = new Node();
        
        instance.me = new Thread( instance );
        instance.me.setName( instance.logIdent() );
        
        return instance;
    }
    
    @Override
    public synchronized void activate() {
        if( me.getState() == State.NEW ) {
            me.start();
            activated = true;
            Logger.debug( "I have been activated", this );
        }
    }
    
    @Override
    public long pid() { return pid; }
    
    @Override
    public NodeService left() throws RemoteException { return left; }
    
    @Override
    public NodeService right() throws RemoteException { return right; }
    
    @Override
    public void left( NodeService n ) throws RemoteException {
        left = n;
        if( n != null && ( n.right() == null || n.right().pid() != pid ) )
            n.right( (NodeService)this );
    }
    
    @Override
    public void right( NodeService n ) throws RemoteException {
        right = n;
        if( n != null && ( n.left() == null || n.left().pid() != pid ) )
            n.left( (NodeService)this );
    }
    
    @Override
    public void send( Probe probe ) throws RemoteException {
        Logger.debug( String.format( "Just got a probe(%x) of type: %s", 
                probe.id, probe.type ), this );
        if( me.getState() == State.NEW )
            activate();
        
        messages.add( probe );
    }
    
    @Override
    public  Probe receive() throws RemoteException {
        //Logger.debug( "I am trying to read in a message", this );
        return messages.poll();
    }
    
    private void send( NodeService n, Probe p ) throws RemoteException {
        n.send( p );
        report.msg();
    }
    
    @Override
    public void probe( MessageType type, Direction direction ) 
            throws RemoteException {
        Probe probe = new Probe( pid, type, phase );
        
        Logger.debug( String.format( "I'm about to probe(%x)", probe.id ), this );
        
        if( direction == Direction.BOTH || direction == Direction.LEFT ) {
        
            probe.direction = Direction.LEFT;
            Logger.debug( "before send to left", this );
            send( left, probe );
            Logger.debug( "after send to left", this );
        }
        
        if( direction == Direction.BOTH || direction == Direction.RIGHT ) {
        
            probe.direction = Direction.RIGHT;
            Logger.debug( "before send to right", this );
            send( right, probe );
            Logger.debug( "after send to right", this );
        }
        
        Logger.debug( String.format( "Ok, I probed(%x)", probe.id ), this );
        
    }
    
    private  void forward( Probe probe ) throws RemoteException {
        if( probe.direction == Direction.RIGHT )
            send( right, probe );
        else 
            send( left, probe );
    }
    
    private  void reply( Probe probe ) throws RemoteException {
        probe.type = MessageType.REPLY;
                        
        if( probe.direction == Direction.RIGHT ) {
            probe.direction = Direction.LEFT;
            send( left, probe );
        } else {
            probe.direction = Direction.RIGHT;
            send( right, probe );
        }
    }
    
    private  void processProbe( Probe probe ) 
            throws RemoteException {
        
        report.rcv(); // report a recv'd msg
        
        Logger.debug( String.format( "I am processing probe(%x) "
                + "now", probe.id ), this );
        
        probe.last_pid = pid;
        
        probe.hops++;
        
        switch( probe.type ) {
            case ELECTION:
                Logger.debug( String.format( "It's an Election probe "
                        + "originally from Node-%x", probe.src_pid ), this );
                if( probe.src_pid > pid ) {
                    
                    int maxHops = (int)Math.pow( 2, probe.phase );
                    
                    if( probe.hops < maxHops ) {

                        Logger.debug( String.format( "Probe max hops (2^%d): "
                                + "%d, it's at %d hops so far...fowarding to "
                                + "the %s", probe.phase, maxHops, 
                                probe.hops, probe.direction ), this );
                        forward( probe );

                    } else if( probe.hops == maxHops ) {
                        Logger.debug( String.format( "Probe(%x) has reached "
                                + "it's max hops(%d), sending a reply "
                                + "back", probe.id, probe.hops ), this);
                        
                        reply( probe );
                        
                    } 
                    
                } else if( probe.src_pid == pid ) {
                    
                    if( hasLeader ) {
                        Logger.debug( String.format( "I already have a "
                                + "leader: Node-%x", leaderPid ), this );
                        break;
                    }
                    
                    isLeader = true;
                    hasLeader = true;
                    leaderPid = pid;
                    Logger.debug( "Annoucing myself as winner", this );
                    
                    probe( MessageType.ANNOUNCEMENT, Direction.RIGHT );
                    
                } else Logger.debug( String.format( "Swallowed probe(%x) "
                        + "because I'm a greater node than Node-%x", 
                        probe.id, probe.src_pid ), this );
            break;
                
            case REPLY:
                Logger.debug( String.format( "It's an Reply probe for "
                        + "Node-%x", probe.src_pid ), this );
                if( probe.src_pid != pid ) {
                    
                    forward( probe );
                   
                    
                } else {
                    replies++;
                    if( replies >= 2 ) {
                        phase++;
                        
                        Logger.debug( "I am entering phase " + phase, this );
                        
                        probe( MessageType.ELECTION, Direction.BOTH );
                        
                        replies = 0;
                    }
                }
                
            break;
                
            case ANNOUNCEMENT:
                Logger.debug( String.format( "It's an Announcement probe "
                        + "from Node-%x", probe.src_pid ), this );
                if( !hasLeader && probe.src_pid != pid ) {
                   
                    forward( probe );
                    
                    leaderPid = probe.src_pid;
                    hasLeader = true;   
                } else announcedAsLeader = true;
                
            break;
        }
        
        Logger.debug( String.format( "I finished processing probe(%x) "
                + "now", probe.id ), this );
        
    }
    
    @Override
    public void run() {
        
        if( Thread.currentThread() != me ) return;
        
        Logger.debug( "I am running. ", this );
        
        if( !participating ) {
            try {
                probe( MessageType.ELECTION, Direction.BOTH );
            } catch( RemoteException re ) {
                Logger.error( "The service failed: " + re, this );
            }
            participating = true;
            Logger.debug( "I am now participating", this );
        }
        
        Probe p;
        while( true ) {
            
           if( isLeader && announcedAsLeader )
                break;
           
            try {
                if( (p = receive()) != null ) {
                    processProbe( p );
                    
                    if( hasLeader && !isLeader )
                        break;
                }
								// if you are running over 128 nodes, a lil sleep helped my system cope
                //Thread.sleep( 5000 );
            } catch( RemoteException re ) {
                Logger.error( "The service failed in run(): " + re, this );
                re.printStackTrace();
            } 

 
        }
        
        me.interrupt();
        
        finish();
        
    }
    
    public void finish() {
        try {
            if( !isLeader )
                Logger.info( String.format( "Node-%x has chosen leader: "
                        + "Node-%x", pid, leaderPid ), this );
            else
                Logger.info( "I have conquered all...I am leader", this );
            
            Logger.info( "Reporting to Ringer", this );
            
            Node.ringer.report( report );
            
            Thread.sleep( 1000 );
            me.join();
            Thread.sleep( 1000 );
            
            
            
        } catch( InterruptedException ie ) {
        } catch( RemoteException re ) {
            Logger.error( "The service failed in finish(): " + re, this );
        }
            
        System.exit(0);
    }
    
    @Override
    public String logIdent() {
        return String.format( "Node-%x", pid );
    }
    
    public static void main( String[] args ) {
        node = null;
        try {
            
           node = Node.newInstance();
            
           Logger.info( "node created with pid: " + String.format( "%x", 
                   node.pid() ), (Loggable)node );
           
           ringer = (RingerService)Naming.lookup( "Ringer" );
           
           ringer.registerNode( node );
            
        } catch( NotBoundException nbe ) {
            Logger.error( "A remote object was not bound: " + nbe, 
                    (Loggable)node );
        } catch( UnknownHostException uhe ) {
            Logger.error( "Host not recognized: " + uhe, (Loggable)node );
        } catch( RemoteException re ) {
            Logger.error( "Error starting service: " + re.getMessage(), 
                    (Loggable)node );
        } catch( MalformedURLException mal ) {
            Logger.error( "URL is possibly malformed: " + mal, (Loggable)node );
        }
        
    }
    
}
