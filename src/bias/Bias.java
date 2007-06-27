/**
 * Created on Oct 15, 2006
 */
package bias;

import java.io.File;
import java.net.URL;

import bias.gui.FrontEnd;
import bias.utils.FSUtils;

/**
 * @author kion
 */
public class Bias {
    
    private static File jarFile;
    
    public static File getJarFile() {
        return jarFile;
    }

    public static void launchApp() throws Throwable {
        try {
            // find out what JAR file application is run from
            URL url = Bias.class.getResource(Bias.class.getSimpleName() + Constants.CLASS_FILE_SUFFIX);
            String jarFilePath = url.getFile().substring(0, url.getFile().indexOf(Bias.class.getName().replaceAll("\\.", "/")) - 2);
            jarFilePath = jarFilePath.substring(Constants.FILE_PROTOCOL_PREFIX.length(), jarFilePath.length());
            jarFile = new File(jarFilePath);
            // display front-end
            FrontEnd.display();
        } finally {
            // delete temporary directory that may contain decrypted user files
            FSUtils.delete(Constants.TMP_DIR);
        }
    }
    
}
