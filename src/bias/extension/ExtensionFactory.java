/**
 * Created on Oct 29, 2006
 */
package bias.extension;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import bias.Constants;
import bias.annotation.AddOnAnnotation;
import bias.core.BackEnd;
import bias.core.DataEntry;

/**
 * @author kion
 *
 */
public class ExtensionFactory {
    
	private static ExtensionFactory instance;
	
	private ExtensionFactory() {
		// default constructor is not allowed
	}
	
	public static ExtensionFactory getInstance() {
		if (instance == null) {
			instance = new ExtensionFactory();
		}
		return instance;
	}
	
	public Extension newExtension(Class<Extension> clazz) throws Throwable {
        byte[] defSettings = BackEnd.getInstance().getExtensionSettings(clazz.getName());
        Extension extension = newExtension(clazz, null, new byte[]{}, defSettings);
        return extension;
    }
    
    @SuppressWarnings("unchecked")
    public Extension newExtension(DataEntry dataEntry) throws Throwable {
        String type = Constants.EXTENSION_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR 
                        + dataEntry.getType() + Constants.PACKAGE_PATH_SEPARATOR + dataEntry.getType();
        Class<Extension> entryClass = (Class<Extension>) Class.forName(type);
        Extension extension = newExtension(entryClass, dataEntry.getId(), dataEntry.getData(), dataEntry.getSettings());
        return extension;
    }

	private Extension newExtension(Class<Extension> clazz, UUID id, byte[] data, byte[] settings) throws Throwable {
        Extension extension = clazz.getConstructor(
                new Class[]{UUID.class, byte[].class, byte[].class}).newInstance(new Object[]{id, data, settings});
        return extension;
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Class<Extension>> getAnnotatedExtensions() throws Throwable {
        Map<String, Class<Extension>> types = new LinkedHashMap<String, Class<Extension>>();
        for (String extension : BackEnd.getInstance().getExtensions()) {
            String annotationStr;
            Class<Extension> extClass = (Class<Extension>) Class.forName(extension);
            AddOnAnnotation extAnn = 
                (AddOnAnnotation) extClass.getAnnotation(AddOnAnnotation.class);
            if (extAnn != null) {
                annotationStr = extClass.getSimpleName() 
                                + " [ " + extAnn.description() + " ]";
            } else {
                annotationStr = extension.substring(
                        extension.lastIndexOf(Constants.PACKAGE_PATH_SEPARATOR) + 1, extension.length()) 
                                + " [ Extension Info Is Missing ]";
            }
            // extension instantiation test
            ExtensionFactory.getInstance().newExtension(extClass);
            // extension is ok, add it to the list
            types.put(annotationStr, extClass);
        }
        return types;
    }
    
}
