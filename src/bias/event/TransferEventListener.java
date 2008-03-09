/**
 * Created on Mar 10, 2008
 */
package bias.event;

/**
 * @author kion
 */
public interface TransferEventListener extends EventListener {

    /**
     * Called whenever appropriate event happens.
     */
    public void onEvent(TransferEvent e) throws Throwable;
    
}
