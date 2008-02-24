/**
 * Created on Feb 23, 2008
 */
package bias.event;

/**
 * @author kion
 */
public interface EventListener {
    
    /**
     * Called whenever appropriate event happens.
     */
    public void onEvent() throws Throwable;

}
