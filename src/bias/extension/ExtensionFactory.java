/**
 * Created on Oct 29, 2006
 */
package bias.extension;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import bias.Constants;
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
	
	public Extension newExtension(Class entryClass) throws Throwable {
        Extension extension = newExtension(entryClass, null, new byte[]{});
        return extension;
    }
    
	public Extension newExtension(Class entryClass, UUID id, byte[] data) throws Throwable {
        Extension extension = (Extension) entryClass.getConstructor(
                new Class[]{UUID.class, byte[].class}).newInstance(new Object[]{id, data});
        return extension;
    }
    
    public Extension newExtension(DataEntry dataEntry) throws Throwable {
        String type = dataEntry.getType();
        type = type.substring(type.lastIndexOf(Constants.PACKAGE_PATH_SEPARATOR)+1, type.length());
        type = dataEntry.getType() + Constants.PACKAGE_PATH_SEPARATOR + type; 
        Class entryClass = Class.forName(type);
        Extension extension = newExtension(entryClass, dataEntry.getId(), dataEntry.getData());
        return extension;
    }

    public Map<String, Class> getExtensions() throws Throwable {
        Map<String, Class> types = new LinkedHashMap<String, Class>();
        for (String extension : BackEnd.getInstance().getExtensions()) {
            String annotationStr;
            Class<?> vcClass = Class.forName(extension);
            Extension.Annotation vcAnn = 
                (Extension.Annotation) vcClass.getAnnotation(Extension.Annotation.class);
            if (vcAnn != null) {
                annotationStr = vcAnn.name() 
                                + " [ " + vcAnn.description() + " ]";
            } else {
                annotationStr = extension.substring(extension.lastIndexOf(".") + 1, extension.length()) 
                                + " [ Extension Info Is Missing ]";
            }
            types.put(annotationStr, vcClass);
        }
        return types;
    }
    
}
