
package hs;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.security.AccessControlException;

/**
 *
 * @author Daniel Ruiz
 */
public class Report implements Loggable, Serializable {
    
    protected String author;
    
    private int sentMessageCount = 0;
    private int receiveMessageCount = 0;
    
    
    public Report( String a ) {
        author = a;
    }
    
    public void msg() { sentMessageCount++; }
    public void rcv() { receiveMessageCount++; }
    
    public String author() { return author; }
    
    public int sentMessageCount() { return sentMessageCount; }
    public int receiveMessageCount() { return receiveMessageCount; }
    
    public String summary() { 
        return String.format(
          "Report for %s - Total Messages Sent: %d | " +
          "Total Received Messages: %d\n",
          author,
          sentMessageCount,
          receiveMessageCount
        );
    }
    
    public String raw() {
        return String.format( "%s %d %d\n", author, sentMessageCount, receiveMessageCount );
    }
    
    @Override
    public String logIdent() {
        return String.format( "%s Reporter", author );
    }
    
    public void export( String filename ) {
        
        if( filename == null ) filename = author;
        
        try {
            FileWriter fstream = new FileWriter( filename, true );
            BufferedWriter out = new BufferedWriter( fstream );
            
            out.write( raw() );
            
            out.close();
        } catch( IOException ioe ) {
            Logger.error( "There was an error writing to the file: " + ioe, this );
        } catch( AccessControlException ace ) {
						Logger.error( "There was a problem with writing to the file. You probably have a " +
							"bad java security policy file. Check it and make sure that writing to this " + 
							"directory is permitted: " + ace, this );
				}
        
    }
   
    
    
    
}
