/**
 * 
 */
package bias.gui;

import java.util.LinkedHashMap;
import java.util.Map;

import bias.core.DataEntry;

/**
 * @author kion
 *
 */
public class VisualEntryFactory {
	
	private static VisualEntryFactory instance;
	
	private VisualEntryFactory() {
		// default constructor is not allowed
	}
	
	public static VisualEntryFactory getInstance() {
		if (instance == null) {
			instance = new VisualEntryFactory();
		}
		return instance;
	}
	
	public VisualEntry newVisualEntry(Class entryClass, byte[] data) throws Exception {
        VisualEntry visualEntry = (VisualEntry) entryClass.getConstructor(
                new Class[]{byte[].class}).newInstance(new Object[]{data});
        return visualEntry;
    }
    
    public VisualEntry newVisualEntry(DataEntry dataEntry) throws Exception {
        Class entryClass = Class.forName(getClass().getPackage().getName() + "." + dataEntry.getType());
        VisualEntry visualEntry = newVisualEntry(entryClass, dataEntry.getData());
        return visualEntry;
    }

    // registered visual entry types
    public final Map<String, Class> getEntryTypes() {
        Map<String, Class> types = new LinkedHashMap<String, Class>();
        types.put("Plain text", PlainText.class);
        types.put("Free formatted text (HTML Page)", HTMLPage.class);
        types.put("Graffiti", Graffiti.class);
        return types;
    }
    
}
