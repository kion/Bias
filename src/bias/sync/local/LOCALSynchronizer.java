/**
 * Created on Oct 24, 2007
 */
package bias.sync.local;

import java.io.File;

import bias.Preferences;
import bias.sync.Synchronizer;
import bias.utils.FSUtils;

/**
 * @author kion
 */
public class LOCALSynchronizer extends Synchronizer {

    private String syncDirPath = Preferences.getInstance().localSyncDirPath;

    /* (non-Javadoc)
     * @see bias.sync.Synchronizer#checkOut(java.lang.String)
     */
    @Override
    protected byte[] checkOut(String filePath) throws Exception {
        File syncDir = new File(syncDirPath);
        if (syncDir.isDirectory()) {
            File file = new File(syncDir, filePath);
            if (file.exists()) {
                return FSUtils.readFile(file);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see bias.sync.Synchronizer#commit(java.lang.String, byte[])
     */
    @Override
    protected void commit(String filePath, byte[] data) throws Exception {
        File syncDir = new File(syncDirPath);
        File file = new File(syncDir, filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        FSUtils.writeFile(file, data);
    }

    /* (non-Javadoc)
     * @see bias.sync.Synchronizer#delete(java.lang.String)
     */
    @Override
    protected void delete(String filePath) throws Exception {
        File syncDir = new File(syncDirPath);
        File file = new File(syncDir, filePath);
        if (file.exists()) {
            file.delete();
        }
    }

}
