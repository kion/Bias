/**
 * Created on Oct 29, 2006
 */
package bias.gui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import bias.core.BackEnd;
import bias.core.DataEntry;
import bias.gui.extension.Extension;

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
	
	public Extension newExtension(Class entryClass) throws Exception {
        Extension extension = newExtension(entryClass, null, new byte[]{});
        return extension;
    }
    
	public Extension newExtension(Class entryClass, UUID id, byte[] data) throws Exception {
        Extension extension = (Extension) entryClass.getConstructor(
                new Class[]{UUID.class, byte[].class}).newInstance(new Object[]{id, data});
        return extension;
    }
    
    public Extension newExtension(DataEntry dataEntry) throws Exception {
        Class entryClass = Class.forName(Extension.class.getPackage().getName() + "." + dataEntry.getType());
        Extension extension = newExtension(entryClass, dataEntry.getId(), dataEntry.getData());
        return extension;
    }

    public final Map<String, Class> getExtensions() throws Exception {
        Map<String, Class> types = new LinkedHashMap<String, Class>();
        for (String component : BackEnd.getInstance().getExtensions()) {
            Class<?> vcClass = Class.forName(component);
            Extension.Annotation vcAnn = 
                (Extension.Annotation) vcClass.getAnnotation(Extension.Annotation.class);
            String annotationStr = vcAnn.name() + " [ " + vcAnn.description() + " ]";
            types.put(annotationStr, vcClass);
        }
        return types;
    }
    
}
