/**
 * Created on Jun 6, 2017
 */
package bias.event;

/**
 * @author kion
 */
public interface HideAppWindowEventListener extends EventListener {

    /**
     * Called whenever appropriate event happens.
     */
    public void onEvent(HideAppWindowEvent e) throws Throwable;

}
