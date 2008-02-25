/**
 * Created on Feb 25, 2008
 */
package bias.event;

/**
 * @author kion
 */
public class SaveEvent {
    
    private boolean beforeExit;

    public SaveEvent(boolean beforeExit) {
        this.beforeExit = beforeExit;
    }

    public boolean isBeforeExit() {
        return beforeExit;
    }

    public void setBeforeExit(boolean beforeExit) {
        this.beforeExit = beforeExit;
    }
    
}
