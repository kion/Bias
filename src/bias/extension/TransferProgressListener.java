/**
 * Created on Mar 15, 2008
 */
package bias.extension;


/**
 * @author kion
 */
public interface TransferProgressListener {

    public void onProgress(long transferredBytesNum, long elapsedTime);

}
