/**
 * Created on Feb 25, 2008
 */
package bias.event;

/**
 * @author kion
 */
public interface BeforeSaveEventListener extends EventListener {

    /**
     * Called whenever appropriate event happens.
     */
    public void onEvent(SaveEvent e) throws Throwable;

}
