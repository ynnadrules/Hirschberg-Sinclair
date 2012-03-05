
package hs;

/**
 *
 * @author Daniel Ruiz
 */
public enum MessageType {
    ELECTION,
    REPLY,
    ANNOUNCEMENT;

    @Override
    public String toString() {
        String ret = super.toString();
        switch( this ) {
            case ELECTION:
                ret = "Election";
                break;
            case REPLY:
                ret = "Reply";
                break;
            case ANNOUNCEMENT:
                ret = "Announcement";
                break;
        }
        return ret; 
    }
}
