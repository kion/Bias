/**
 * Created on Apr 6, 2007
 */
package bias;

import java.io.StringWriter;
import java.lang.reflect.Field;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import bias.annotation.PreferenceAnnotation;
import bias.core.BackEnd;
import bias.gui.FrontEnd;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * @author kion
 */
public class Preferences {
    
    private static Preferences instance;
    
    private Document prefs;
    
    private Preferences() {
        prefs = BackEnd.getInstance().getPrefs();
        if (prefs != null) {
            Node rootNode = prefs.getFirstChild();
            NodeList prefNodes = rootNode.getChildNodes();
            for (int i = 0; i < prefNodes.getLength(); i++) {
                Node prefNode = prefNodes.item(i);
                if (prefNode.getNodeName().equals(Constants.XML_ELEMENT_PREFERENCE)) {
                    String fieldName = prefNode.getAttributes().getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ID).getNodeValue();
                    try {
                        Field field = Preferences.class.getDeclaredField(fieldName);
                        String type = prefNode.getAttributes().getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_TYPE).getNodeValue();
                        String value = prefNode.getAttributes().getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_VALUE).getNodeValue();
                        if ("string".equals(type)) {
                            field.set(this, value);
                        } else if ("boolean".equals(type)) {
                            field.setBoolean(this, Boolean.parseBoolean(value));
                        }
                    } catch (NoSuchFieldException nsfe) {
                        // field is not used anymore, ignore
                    } catch (Exception ex) {
                        FrontEnd.displayErrorMessage(ex);
                    }
                }
            }
        }
    }
    
    public static Preferences getInstance() {
        if (instance == null) {
            instance = new Preferences();
        }
        return instance;
    }
    
    public byte[] serialize() throws Exception {
        prefs = new DocumentBuilderFactoryImpl().newDocumentBuilder().newDocument();
        Element rootNode = prefs.createElement(Constants.XML_ELEMENT_ROOT_CONTAINER);
        prefs.appendChild(rootNode);
        Field[] fields = Preferences.class.getDeclaredFields();
        for (final Field field : fields) {
            PreferenceAnnotation prefAnn = field.getAnnotation(PreferenceAnnotation.class);
            if (prefAnn != null) {
                Element prefElement = prefs.createElement(Constants.XML_ELEMENT_PREFERENCE);
                prefElement.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_ID, field.getName());
                String type = field.getType().getSimpleName().toLowerCase();
                prefElement.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_TYPE, type);
                try {
                    if ("string".equals(type)) {
                        prefElement.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_VALUE, "" + (String) field.get(this));
                    } else if ("boolean".equals(type)) {
                        prefElement.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_VALUE, "" + field.getBoolean(this));
                    }
                } catch (Exception ex) {
                    prefElement = null;
                }
                if (prefElement != null) {
                    rootNode.appendChild(prefElement);
                }
            }
        }
        OutputFormat of = new OutputFormat();
        of.setIndenting(true);
        of.setIndent(4);
        StringWriter sw = new StringWriter();
        new XMLSerializer(sw, of).serialize(prefs);
        return sw.getBuffer().toString().getBytes();
    }
    
    /* PREFERENCES DECLARATION SECTION */
    
    @PreferenceAnnotation(
            title = "Show system tray icon",
            description = "Defines if application can allocate space in system tray, allows to hide/restore application to/from system tray icon")
    public boolean useSysTrayIcon;

    @PreferenceAnnotation(
            title = "Remain in system tray on window close",
            description = "Defines if application should remain in system tray when application's window is closed")
    public boolean remainInSysTrayOnWindowClose;
    
    @PreferenceAnnotation(
            title = "Exit without confirmation",
            description = "Defines if exit-confirmation should be displayed on exit")
    public boolean exitWithoutConfirmation;
    
    @PreferenceAnnotation(
            title = "Auto save on exit",
            description = "Defines if user data have to be automatically saved on exit")
    public boolean autoSaveOnExit;
    
}
