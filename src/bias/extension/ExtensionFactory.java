/**
 * Created on Oct 29, 2006
 */
package bias.extension;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import bias.Constants;
import bias.Constants.ADDON_TYPE;
import bias.core.AddOnInfo;
import bias.core.BackEnd;
import bias.core.DataEntry;

/**
 * @author kion
 *
 */
public class ExtensionFactory {
    
    private static Map<String, Class<? extends EntryExtension>> entryTypes = null;
    private static Map<ToolExtension, String> toolTypes = null;
    
    private ExtensionFactory() {
        // hidden default constructor
    }

    @SuppressWarnings("unchecked")
    private static Extension newExtension(Class<? extends Extension> clazz, UUID id, byte[] data, byte[] settings) throws Throwable {
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
    
	public static Extension newExtension(Class<? extends Extension> clazz) throws Throwable {
        byte[] defSettings = BackEnd.getInstance().getAddOnSettings(clazz.getName(), ADDON_TYPE.Extension);
        Extension extension = newExtension(clazz, null, new byte[]{}, defSettings);
        return extension;
    }
    
    public static ToolExtension newToolExtension(Class<? extends ToolExtension> clazz, byte[] data) throws Throwable {
        byte[] settings = BackEnd.getInstance().getAddOnSettings(clazz.getName(), ADDON_TYPE.Extension);
        return (ToolExtension) newExtension(clazz, null, data, settings);
    }
    
    @SuppressWarnings("unchecked")
    public static EntryExtension newEntryExtension(DataEntry dataEntry) throws Throwable {
        String type = Constants.EXTENSION_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR 
                        + dataEntry.getType() + Constants.PACKAGE_PATH_SEPARATOR + dataEntry.getType();
        Class<EntryExtension> entryClass = (Class<EntryExtension>) Class.forName(type);
        EntryExtension extension = (EntryExtension) newExtension(entryClass, dataEntry.getId(), dataEntry.getData(), dataEntry.getSettings());
        return extension;
    }
    
    public static EntryExtension newEntryExtension(Class<? extends EntryExtension> clazz) throws Throwable {
        return (EntryExtension) newExtension(clazz);
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String, Class<? extends EntryExtension>> getAnnotatedEntryExtensionClasses() throws Throwable {
        if (entryTypes == null) {
            entryTypes = new LinkedHashMap<String, Class<? extends EntryExtension>>();
            for (AddOnInfo extension : BackEnd.getInstance().getAddOns(ADDON_TYPE.Extension)) {
                try {
                    String fullExtName = Constants.EXTENSION_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR + extension.getName() 
                                            + Constants.PACKAGE_PATH_SEPARATOR + extension.getName();
                    Class<Extension> extClass = (Class<Extension>) Class.forName(fullExtName);
                    // extension instantiation test
                    if (EntryExtension.class.isAssignableFrom(extClass)) {
                        // extension is ok, add it to the list
                        String annotationStr = extension.getName() + (extension.getDescription() != null ? " [" + extension.getDescription() + "]" : Constants.EMPTY_STR);
                        entryTypes.put(annotationStr, (Class<? extends EntryExtension>) extClass);
                    }
                } catch (Throwable t) {
                    // ignore broken extensions
                    t.printStackTrace(System.err);
                }
            }
        }
        return entryTypes;
    }
    
    @SuppressWarnings("unchecked")
    public static Map<ToolExtension, String> getAnnotatedToolExtensions() throws Throwable {
        if (toolTypes == null) {
            toolTypes = new LinkedHashMap<ToolExtension, String>();
            for (AddOnInfo extension : BackEnd.getInstance().getAddOns(ADDON_TYPE.Extension)) {
                try {
                    String fullExtName = Constants.EXTENSION_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR + extension.getName() 
                                            + Constants.PACKAGE_PATH_SEPARATOR + extension.getName();
                    Class<Extension> extClass = (Class<Extension>) Class.forName(fullExtName);
                    if (ToolExtension.class.isAssignableFrom(extClass)) {
                        // extension instantiation test
                        ToolExtension ext = newToolExtension((Class<ToolExtension>) Class.forName(fullExtName), BackEnd.getInstance().getToolData(extension.getName()));
                        // extension is ok, add it to the list
                        String annotationStr = extension.getName() + (extension.getDescription() != null ? " [" + extension.getDescription() + "]" : Constants.EMPTY_STR);
                        toolTypes.put(ext, annotationStr);
                    }
                } catch (Throwable t) {
                    // ignore, broken extensions just won't be returned
                }
            }
        }
        return toolTypes;
    }
    
}
