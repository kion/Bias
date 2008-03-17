/**
 * Created on Oct 29, 2006
 */
package bias.extension;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import bias.Constants;
import bias.core.AddOnInfo;
import bias.core.BackEnd;
import bias.core.DataEntry;
import bias.core.pack.PackType;

/**
 * @author kion
 *
 */
public class ExtensionFactory {
    
    private static Map<String, Class<? extends EntryExtension>> annotatedEntryTypes = null;
    private static Map<ToolExtension, String> annotatedToolTypes = null;
    private static Map<String, TransferExtension> annotatedTransferTypes = null;
    private static Map<String, TransferExtension> transferTypes = null;

    private static Map<String, String> extensionStatuses = new HashMap<String, String>();
    
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
                } else if (pts.length == 1 && pts[0].equals(byte[].class)) {
                    extension = clazz.getConstructor(new Class[]{byte[].class}).newInstance(new Object[]{settings});
                    break;
                } else {
                    throw new Exception("Failed to instantiate extension (class does not declare expected constructor)!");
                }
            }
        }
        return extension;
    }
    
	public static Extension newExtension(Class<? extends Extension> clazz) throws Throwable {
        byte[] defSettings = BackEnd.getInstance().getAddOnSettings(clazz.getName(), PackType.EXTENSION);
        Extension extension = newExtension(clazz, null, new byte[]{}, defSettings);
        return extension;
    }
    
    private static ToolExtension newToolExtension(Class<? extends Extension> clazz, byte[] data) throws Throwable {
        byte[] settings = BackEnd.getInstance().getAddOnSettings(clazz.getName(), PackType.EXTENSION);
        return (ToolExtension) newExtension(clazz, null, data, settings);
    }
    
    public static TransferExtension newTransferExtension(Class<? extends Extension> clazz) throws Throwable {
        byte[] settings = BackEnd.getInstance().getAddOnSettings(clazz.getName(), PackType.EXTENSION);
        return (TransferExtension) newExtension(clazz, null, null, settings);
    }
    
    @SuppressWarnings("unchecked")
    public static EntryExtension newEntryExtension(DataEntry dataEntry) throws Throwable {
        String type = Constants.EXTENSION_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR 
                        + dataEntry.getType() + Constants.PACKAGE_PATH_SEPARATOR + dataEntry.getType();
        Class<EntryExtension> entryClass = (Class<EntryExtension>) Class.forName(type);
        EntryExtension extension = (EntryExtension) newExtension(entryClass, dataEntry.getId(), dataEntry.getData(), dataEntry.getSettings());
        return extension;
    }
    
    public static EntryExtension newEntryExtension(Class<? extends Extension> clazz) throws Throwable {
        return (EntryExtension) newExtension(clazz);
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String, Class<? extends EntryExtension>> getAnnotatedEntryExtensionClasses() throws Throwable {
        if (annotatedEntryTypes == null) {
            annotatedEntryTypes = new LinkedHashMap<String, Class<? extends EntryExtension>>();
            for (AddOnInfo extension : BackEnd.getInstance().getAddOns(PackType.EXTENSION)) {
                try {
                    String fullExtName = Constants.EXTENSION_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR + extension.getName() 
                                            + Constants.PACKAGE_PATH_SEPARATOR + extension.getName();
                    Class<Extension> extClass = (Class<Extension>) Class.forName(fullExtName);
                    if (EntryExtension.class.isAssignableFrom(extClass)) {
                        // extension instantiation test
                        newEntryExtension(extClass);
                        // extension is ok, add it to the list
                        String annotationStr = extension.getName() + (extension.getDescription() != null ? " [" + extension.getDescription() + "]" : Constants.EMPTY_STR);
                        annotatedEntryTypes.put(annotationStr, (Class<? extends EntryExtension>) extClass);
                        extensionStatuses.put(extension.getName(), Constants.ADDON_STATUS_LOADED);
                    }
                } catch (Throwable t) {
                    System.err.println("Extension [ " + extension.getName() + " ] failed to initialize!");
                    t.printStackTrace(System.err);
                    extensionStatuses.put(extension.getName(), Constants.ADDON_STATUS_BROKEN);
                }
            }
        }
        return annotatedEntryTypes;
    }
    
    @SuppressWarnings("unchecked")
    public static Map<ToolExtension, String> getAnnotatedToolExtensions() throws Throwable {
        if (annotatedToolTypes == null) {
            annotatedToolTypes = new LinkedHashMap<ToolExtension, String>();
            for (AddOnInfo extension : BackEnd.getInstance().getAddOns(PackType.EXTENSION)) {
                try {
                    String fullExtName = Constants.EXTENSION_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR + extension.getName() 
                                            + Constants.PACKAGE_PATH_SEPARATOR + extension.getName();
                    Class<Extension> extClass = (Class<Extension>) Class.forName(fullExtName);
                    if (ToolExtension.class.isAssignableFrom(extClass)) {
                        // extension instantiation test
                        ToolExtension ext = newToolExtension(extClass, BackEnd.getInstance().getToolData(fullExtName));
                        // extension is ok, add it to the list
                        String annotationStr = extension.getName() + (extension.getDescription() != null ? " [" + extension.getDescription() + "]" : Constants.EMPTY_STR);
                        annotatedToolTypes.put(ext, annotationStr);
                        extensionStatuses.put(extension.getName(), Constants.ADDON_STATUS_LOADED);
                    }
                } catch (Throwable t) {
                    System.err.println("Extension [ " + extension.getName() + " ] failed to initialize!");
                    t.printStackTrace(System.err);
                    extensionStatuses.put(extension.getName(), Constants.ADDON_STATUS_BROKEN);
                }
            }
        }
        return annotatedToolTypes;
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String, TransferExtension> getAnnotatedTransferExtensions() throws Throwable {
        if (annotatedTransferTypes == null) {
            annotatedTransferTypes = new LinkedHashMap<String, TransferExtension>();
            transferTypes = new LinkedHashMap<String, TransferExtension>();
            TransferExtension ext = newTransferExtension(LocalTransfer.class);
            annotatedTransferTypes.put("LocalTransfer [Transfer from/to local file system]", ext);
            transferTypes.put(LocalTransfer.class.getSimpleName(), ext);
            for (AddOnInfo extension : BackEnd.getInstance().getAddOns(PackType.EXTENSION)) {
                try {
                    String fullExtName = Constants.EXTENSION_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR + extension.getName() 
                                            + Constants.PACKAGE_PATH_SEPARATOR + extension.getName();
                    Class<Extension> extClass = (Class<Extension>) Class.forName(fullExtName);
                    if (TransferExtension.class.isAssignableFrom(extClass)) {
                        // extension instantiation test
                        ext = newTransferExtension(extClass);
                        // extension is ok, add it to the list
                        String annotationStr = extension.getName() + " [" + (extension.getDescription() != null ? extension.getDescription() : "No description") + "]";
                        annotatedTransferTypes.put(annotationStr, ext);
                        transferTypes.put(extClass.getSimpleName(), ext);
                        extensionStatuses.put(extension.getName(), Constants.ADDON_STATUS_LOADED);
                    }
                } catch (Throwable t) {
                    System.err.println("Extension [ " + extension.getName() + " ] failed to initialize!");
                    t.printStackTrace(System.err);
                    extensionStatuses.put(extension.getName(), Constants.ADDON_STATUS_BROKEN);
                }
            }
        }
        return annotatedTransferTypes;
    }
    
    public static String getExtensionStatus(String extName) {
        return extensionStatuses.get(extName);
    }

    public static TransferExtension getTransferExtension(String name) throws Throwable {
        if (transferTypes == null) {
            getAnnotatedTransferExtensions();
        }
        return transferTypes.get(name);
    }
    
}
