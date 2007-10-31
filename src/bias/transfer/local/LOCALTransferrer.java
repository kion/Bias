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
    protected void doExport(byte[] data, Properties settings) throws Exception {
        String filePath = settings.getProperty(Constants.TRANSFER_PROPERTY_FILEPATH);
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
        FSUtils.writeFile(file, data);
    }

    /* (non-Javadoc)
     * @see bias.transfer.Transferrer#doImport(java.util.Properties)
     */
    @Override
    protected byte[] doImport(Properties settings) throws Exception {
        String filePath = settings.getProperty(Constants.TRANSFER_PROPERTY_FILEPATH);
        File file = new File(filePath);
        return FSUtils.readFile(file);
    }

}
