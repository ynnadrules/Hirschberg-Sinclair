
package hs;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Daniel Ruiz
 */
public class ReportSet extends Report implements Loggable {

    private Set<Report> reports;
    
    private int totalSentCount = 0;
    private int totalReceiveCount = 0;
    
    private double sentAverage = 0.0;
    private double receiveAverage = 0.0;
    
    private boolean available = false;
    
    private final int MAX_REPORTS;
    
    public ReportSet( String a, int maxReports ) {
        super( a );
        reports = new HashSet<Report>();
        MAX_REPORTS = maxReports;
    }
    
    public void add( Report report ) {
        
        if( available && reports.size() < MAX_REPORTS )
            available = false;
        
        reports.add(report);
        
        if( reports.size() == MAX_REPORTS )
            prepare();
    }
    
    public int size() { return reports.size(); }
    
    private void prepare() {
        
        totalSentCount = 0;
        totalReceiveCount = 0;

        sentAverage = 0.0;
        receiveAverage = 0.0;
        
        for( Report r : reports ) {
            totalSentCount += r.sentMessageCount();
            totalReceiveCount += r.receiveMessageCount();
        } 
        
        sentAverage = (double)totalSentCount / reports.size();
        receiveAverage = (double)totalSentCount / reports.size();
        
        available = true;
    }
    
    public boolean available() { return available; }
    
    @Override
    public String logIdent() {
        return String.format( "%s ReportSet", author );
    }
    
    @Override
    public String summary() {
        if( !available )
             prepare();
        String summary = String.format( "Report for %s\n", author );
        
        summary += "===================================\n";
                
        summary += String.format( "Total Nodes: %d\n", reports.size() );
        
        for( Report r : reports ) {
            summary += r.summary();
        }
        
        summary += "===================================\n";
        
        summary += String.format( "%d total sent messages from all nodes"
                + "\n", totalSentCount );
        summary += String.format( "%d total received messages from all nodes"
                + "\n", totalReceiveCount );
        summary += String.format( "%f sent messages average\n", 
                (double)totalSentCount/reports.size() );
        summary += String.format( "%f received messages average\n",
                (double)totalReceiveCount/reports.size() );
        
        return summary;

    }
    
    @Override
    public String raw() {
        if( !available )
             prepare();
        return String.format( "%s %d %f %f\n", author, reports.size(),
                sentAverage, receiveAverage );
    } 
}
