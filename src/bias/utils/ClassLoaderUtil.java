/**
 * Created on May 27, 2018
 */
package bias.utils;

import bias.Constants;
import bias.core.pack.PackType;

/**
 * @author kion
 */
public class ClassLoaderUtil {
    
    private ClassLoaderUtil() {
        // hidden default constructor
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadAddOnClass(String addOnFullClassName) throws Throwable {
        return (Class<T>) Class.forName(addOnFullClassName);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadAddOnClass(String addOnName, PackType addOnType) throws Throwable {
        String packageName = null;
        switch (addOnType) {
            case EXTENSION:
                packageName = Constants.EXTENSION_PACKAGE_NAME;
                break;
            case SKIN:
                packageName = Constants.SKIN_PACKAGE_NAME;
                break;
            default:
                throw new Exception("Invalid add-on type");
        }
        String addOnFullClassName = packageName + Constants.PACKAGE_PATH_SEPARATOR + addOnName + Constants.PACKAGE_PATH_SEPARATOR + addOnName;
        return (Class<T>) Class.forName(addOnFullClassName);
    }
    
}
