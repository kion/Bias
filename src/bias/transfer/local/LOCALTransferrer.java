/**
 * Created on Oct 31, 2007
 */
package bias.transfer.local;

import java.io.File;
import java.util.Properties;

import bias.Constants;
import bias.transfer.Transferrer;
import bias.utils.FSUtils;

/**
 * @author kion
 */
public class LOCALTransferrer extends Transferrer {

    /* (non-Javadoc)
     * @see bias.transfer.Transferrer#doExport(byte[], java.util.Properties)
     */
    @Override
    public void doExport(byte[] data, Properties options) throws Exception {
        String filePath = options.getProperty(Constants.TRANSFER_OPTION_FILEPATH);
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        } else if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        FSUtils.writeFile(file, data);
    }

    /* (non-Javadoc)
     * @see bias.transfer.Transferrer#doImport(java.util.Properties)
     */
    @Override
    public byte[] doImport(Properties options) throws Exception {
        String filePath = options.getProperty(Constants.TRANSFER_OPTION_FILEPATH);
        File file = new File(filePath);
        return FSUtils.readFile(file);
    }

}
