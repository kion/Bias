/**
 * Created on Oct 29, 2006
 */
package bias.extension;

import java.lang.reflect.Constructor;
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
	
    @SuppressWarnings("unchecked")
    private Extension newExtension(Class<? extends Extension> clazz, UUID id, byte[] data, byte[] settings) throws Throwable {
        Extension extension = null;
        Constructor<? extends Extension>[] cs = (Constructor<? extends Extension>[]) clazz.getConstructors();
        for (Constructor<? extends Extension> c : cs) {
            Class<?>[] pts = c.getParameterTypes();
            if (pts.length != 0) {
                if (pts.length == 3 && pts[0].equals(UUID.class) && pts[1].equals(byte[].class) && pts[2].equals(byte[].class)) {
                    extension = clazz.getConstructor(new Class[]{UUID.class, byte[].class, byte[].class}).newInstance(new Object[]{id, data, settings});
                    break;
                } else if (pts.length == 2 && pts[0].equals(byte[].class) && pts[1].equals(byte[].class)) {
                    extension = clazz.getConstructor(new Class[]{byte[].class, byte[].class}).newInstance(new Object[]{data, settings});
                    break;
                } else {
                    throw new Exception("Failed to instantiate extension (class does not declare expected constructor)!");
                }
            }
        }
        return extension;
    }
    
	public Extension newExtension(Class<? extends Extension> clazz) throws Throwable {
        byte[] defSettings = BackEnd.getInstance().getExtensionSettings(clazz.getName());
        Extension extension = newExtension(clazz, null, new byte[]{}, defSettings);
        return extension;
    }
    
    public EntryExtension newEntryExtension(Class<? extends EntryExtension> clazz) throws Throwable {
        return (EntryExtension) newExtension(clazz);
    }
    
    public ToolExtension newToolExtension(Class<? extends ToolExtension> clazz) throws Throwable {
        return (ToolExtension) newExtension(clazz);
    }
    
    public ToolExtension newToolExtension(Class<? extends ToolExtension> clazz, byte[] data) throws Throwable {
        byte[] settings = BackEnd.getInstance().getExtensionSettings(clazz.getName());
        return (ToolExtension) newExtension(clazz, null, data, settings);
    }
    
    @SuppressWarnings("unchecked")
    public EntryExtension newEntryExtension(DataEntry dataEntry) throws Throwable {
        String type = Constants.EXTENSION_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR 
                        + dataEntry.getType() + Constants.PACKAGE_PATH_SEPARATOR + dataEntry.getType();
        Class<EntryExtension> entryClass = (Class<EntryExtension>) Class.forName(type);
        EntryExtension extension = (EntryExtension) newExtension(entryClass, dataEntry.getId(), dataEntry.getData(), dataEntry.getSettings());
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
            newExtension(extClass);
            // extension is ok, add it to the list
            types.put(annotationStr, extClass);
        }
        return types;
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Class<? extends EntryExtension>> getAnnotatedEntryExtensions() throws Throwable {
        Map<String, Class<? extends EntryExtension>> types = new LinkedHashMap<String, Class<? extends EntryExtension>>();
        for (String extension : BackEnd.getInstance().getExtensions()) {
            String annotationStr;
            Class<Extension> extClass = (Class<Extension>) Class.forName(extension);
            // extension instantiation test
            Extension ext = newExtension(extClass);
            if (ext instanceof EntryExtension) {
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
                // extension is ok, add it to the list
                types.put(annotationStr, (Class<? extends EntryExtension>) extClass);
            }
        }
        return types;
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Class<? extends ToolExtension>> getAnnotatedToolExtensions() throws Throwable {
        Map<String, Class<? extends ToolExtension>> types = new LinkedHashMap<String, Class<? extends ToolExtension>>();
        for (String extension : BackEnd.getInstance().getExtensions()) {
            String annotationStr;
            Class<Extension> extClass = (Class<Extension>) Class.forName(extension);
            // extension instantiation test
            Extension ext = newExtension(extClass);
            if (ext instanceof ToolExtension) {
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
                // extension is ok, add it to the list
                types.put(annotationStr, (Class<? extends ToolExtension>) extClass);
            }
        }
        return types;
    }
    
}
