/**
 * Created on Jan 7, 2007
 */
package bias.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import bias.Constants;

/**
 * @author kion
 */
public class PropertiesUtils {
    
    private static final String PROPERTIES_COMMNETS_PATTERN = "(?s)#[^\\n]*\\n+\\s*";
    
    private PropertiesUtils(){
        // hidden default constructor
    };

    /**
     * Removes all secondary attributes from given Properties instance 
     * and serializes it to array of bytes.
     * 
     * @param settings settings to normalize
     * @return normalized settings serialized to byte array
     */
    public static byte[] serializeProperties(Properties properties) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            properties.store(baos, null);
        } catch (IOException e) {}
        String nonNormalizedSettings = new String(baos.toByteArray());
        String normalizedSettings = nonNormalizedSettings.replaceAll(
                PROPERTIES_COMMNETS_PATTERN, Constants.EMPTY_STR);
        return normalizedSettings.getBytes();
    }
    
    /**
     * Tries to deserialize given bytes array to Properties instance
     * 
     * @param bytes bytes array that is supposed to contain serialized properties data
     * @return Properties instance populated with data deserialized from given bytes array, 
     *         or empty if deserialization failed
     */
    public static Properties deserializeProperties(byte[] bytes) {
        Properties properties = new Properties();
        if (bytes != null) {
            try {
                properties.load(new ByteArrayInputStream(bytes));
            } catch (IOException e) {}
        }
        return properties;
    }

}
