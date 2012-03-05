/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hs;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Random;

/**
 *
 * @author Daniel Ruiz
 */
public class Probe implements Serializable {
        
    public long src_pid;
    public long last_pid;
    public long id;
    public int phase = 0;
    public int hops = 0;
    
    private Random rand = new Random();
    
    public MessageType type;
    public Direction direction;

    public Probe( long pid, MessageType msgtype, int phase ) {
        src_pid = pid;
        last_pid = pid;
        type = msgtype;
        
        this.phase = phase;
        
        rand.setSeed( Calendar.getInstance().getTimeInMillis() );
        id = rand.nextLong();
    }
    
}
