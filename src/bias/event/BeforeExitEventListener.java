/**
 * Created on Feb 25, 2008
 */
package bias.event;

/**
 * @author kion
 */
public interface BeforeExitEventListener extends EventListener {

    /**
     * Called whenever appropriate event happens.
     */
    public void onEvent() throws Throwable;

}
