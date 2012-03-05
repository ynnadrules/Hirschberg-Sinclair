
package hs;

/**
 *
 * @author Daniel Ruiz
 */
public enum Direction {
    LEFT,
    RIGHT,
    BOTH;
    
    @Override
    public String toString() {
        String ret = super.toString();
        switch( this ) {
            case LEFT:
                ret = "Left";
                break;
            case RIGHT:
                ret = "Right";
                break;
            case BOTH:
                ret = "Both";
                break;
        }
        return ret; 
    }
}
