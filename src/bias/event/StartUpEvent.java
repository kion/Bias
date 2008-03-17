/**
 * Created on Mar 17, 2008
 */
package bias.event;

/**
 * @author kion
 */
public class StartUpEvent {
    
    private boolean isStartedHidden;

    public StartUpEvent(boolean isStartedHidden) {
        this.isStartedHidden = isStartedHidden;
    }

    public boolean isStartedHidden() {
        return isStartedHidden;
    }

    public void setStartedHidden(boolean isStartedHidden) {
        this.isStartedHidden = isStartedHidden;
    }
    
}
