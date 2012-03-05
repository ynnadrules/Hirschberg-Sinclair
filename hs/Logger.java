
package hs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
/**
 *
 * @author Daniel Ruiz
 */
public class Logger {
    
    private static enum LogType {
        INFO,
        WARN,
        DEBUG,
        ERROR
    };
    
    private static DateFormat dateFormat = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );

    public static void info( String msg, Loggable obj ) {
        String info = String.format(																														/* a small hack down below */
            "[INFO][%1$tH:%1$tM:%1$tS:%1$tL] - %2$s: %3$s%n", Calendar.getInstance().getTime(), (obj != null ) ? obj.logIdent():"Unknown", msg );
        
        write( info, LogType.INFO );
    }
    
    public static void warn( String msg, Loggable obj ) {
        String info = String.format(																														/* a small hack down below */
            "[WARN][%1$tH:%1$tM:%1$tS:%1$tL] - %2$s: %3$s%n", Calendar.getInstance().getTime(), (obj != null ) ? obj.logIdent():"Unknown", msg );
        
        write( info, LogType.WARN );
    }
    
    public static void debug( String msg, Loggable obj ) {
        String info = String.format(																														/* a small hack down below */
            "[DEBUG][%1$tH:%1$tM:%1$tS:%1$tL] - %2$s: %3$s%n", Calendar.getInstance().getTime(), (obj != null ) ? obj.logIdent():"Unknown", msg );
        
        write( info, LogType.DEBUG );
    }
    
    public static void error( String msg, Loggable obj ) {
        String info = String.format(																														/* a small hack down below */
            "[ERROR][%1$tH:%1$tM:%1$tS:%1$tL] - %2$s: %3$s%n", Calendar.getInstance().getTime(), (obj != null ) ? obj.logIdent():"Unknown", msg );
        
        write( info, LogType.ERROR );
    }

    
    private static void write( String msg, LogType type ) {
        switch( type ) {
            case INFO:
            case WARN:
            case DEBUG:
                System.out.println( msg );
            break;
            case ERROR:
                System.err.println( msg );
            break;
        }
    }

}
