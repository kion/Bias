/**
 * Created on Mar 15, 2008
 */
package bias.extension;

/**
 * @author kion
 */
public abstract class ObservableTransferExtension extends TransferExtension {
    
    public ObservableTransferExtension(byte[] settings) {
        super(settings);
    }

    private TransferProgressListener listener;

    public void setListener(TransferProgressListener listener) {
        this.listener = listener;
    }
    
    public void unsetListener(TransferProgressListener listener) {
        this.listener = null;
    }
    
    protected void fireOnProgressEvent(long transferredBytesNum, long elapsedTime){
        if (listener != null) {
            listener.onProgress(transferredBytesNum, elapsedTime);
        }
    };
    
}
