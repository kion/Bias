/**
 * Created on Oct 31, 2007
 */
package bias.transfer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import bias.Constants;

/**
 * @author kion
 */
public abstract class Transferrer {

    /**
     * Available transfer types
     */
    public static enum TRANSFER_TYPE {
        LOCAL,
        FTP
    }
    
    private static Map<TRANSFER_TYPE, Transferrer> instances = new HashMap<TRANSFER_TYPE, Transferrer>();
    
    @SuppressWarnings("unchecked")
    public static Transferrer getInstance(TRANSFER_TYPE type) throws Exception {
        Transferrer instance = null;
        if (type != null) {
            instance = instances.get(type);
            if (instance == null) {
                String transferrerClassPackageName = Transferrer.class.getPackage().getName() + Constants.PACKAGE_PATH_SEPARATOR + type.name().toLowerCase();
                String transferrerClassName = transferrerClassPackageName + Constants.PACKAGE_PATH_SEPARATOR + type.name() + Transferrer.class.getSimpleName();
                Class<? extends Transferrer> transferrerClass = (Class<? extends Transferrer>) Class.forName(transferrerClassName);
                instance = transferrerClass.newInstance();
                instances.put(type, instance);
            }
        }
        return instance;
    }

    protected abstract byte[] doImport(Properties settings) throws Exception;

    protected abstract void doExport(byte[] data, Properties settings) throws Exception;

}
