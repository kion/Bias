/**
 * Created on Apr 6, 2007
 */
package bias;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import bias.annotation.Preference;
import bias.annotation.PreferenceChoice;
import bias.annotation.PreferenceEnable;
import bias.annotation.PreferenceNeedsRestart;
import bias.annotation.PreferenceValidation;
import bias.core.BackEnd;
import bias.gui.FrontEnd;
import bias.i18n.I18nService;
import bias.utils.Validator;

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
        // hidden default constructor
    }
    
    public void init() {
        prefs = BackEnd.getInstance().getPreferences();
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
                        } else if ("int".equals(type)) {
                            field.setInt(this, Integer.valueOf(value));
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
            instance.init();
        }
        return instance;
    }
    
    public byte[] serialize() throws Exception {
        prefs = new DocumentBuilderFactoryImpl().newDocumentBuilder().newDocument();
        Element rootNode = prefs.createElement(Constants.XML_ELEMENT_ROOT_CONTAINER);
        prefs.appendChild(rootNode);
        Field[] fields = Preferences.class.getDeclaredFields();
        for (final Field field : fields) {
            Preference prefAnn = field.getAnnotation(Preference.class);
            if (prefAnn != null) {
                Element prefElement = prefs.createElement(Constants.XML_ELEMENT_PREFERENCE);
                prefElement.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_ID, field.getName());
                String type = field.getType().getSimpleName().toLowerCase();
                prefElement.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_TYPE, type);
                try {
                    if ("string".equals(type)) {
                        String s = (String) field.get(this);
                        if (Validator.isNullOrBlank(s)) {
                            s = Constants.EMPTY_STR;
                        }
                        prefElement.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_VALUE, s);
                    } else if ("boolean".equals(type)) {
                        prefElement.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_VALUE, "" + field.getBoolean(this));
                    } else if ("int".equals(type)) {
                        prefElement.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_VALUE, "" + field.getInt(this));
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
    
    /* VALIDATION CLASSES SECTION */
    
    // ********* interfaces *********

    public static interface PreferenceValidator<T> {
        public void validate(T value) throws Exception;
    }
    
    public static interface PreferenceChoiceProvider {
        public Collection<String> getPreferenceChoices() throws Exception;
    }
    
    // ********* implementors *********
    
    public static class PreferredDateFormatValidator implements PreferenceValidator<String> {
        public void validate(String value) throws Exception {
            if (Validator.isNullOrBlank(value)) {
                throw new Exception("Pattern can not be empty!");
            }
            try {
                new SimpleDateFormat(value);
            } catch (IllegalArgumentException iae) {
                String errMsg = "Pattern is invalid!";
                String detMsg = iae.getMessage();
                if (!Validator.isNullOrBlank(detMsg)) {
                    errMsg += Constants.BLANK_STR + detMsg;
                }
                if (iae.getCause() != null) {
                    detMsg = iae.getCause().getMessage();
                    if (!Validator.isNullOrBlank(detMsg)) {
                        errMsg += Constants.BLANK_STR + detMsg;
                    }
                }
                throw new Exception(errMsg);
            }
        }
    }
    
    public static class PreferredLanguageChoiceProvider implements PreferenceChoiceProvider {
        public Collection<String> getPreferenceChoices() throws Exception {
            return I18nService.getInstance().getAvailableLanguages();
        }
    }
    
    /* PREFERENCES DECLARATION SECTION */
    
    // TODO [P1] preferences should be internationalized as well (!)

    @Preference(
            title = "Preferred language",
            description = "Defines preferred language for GUI interface, messages etc.")
    @PreferenceChoice(
            providerClass = PreferredLanguageChoiceProvider.class)
    @PreferenceNeedsRestart        
    public String preferredLanguage = Constants.DEFAULT_LANGUAGE;
    
    @Preference(
            title = "Preferred date-time format:",
            description = "Defines preferred format for date-time rendering")
    @PreferenceValidation(
            validationClass = PreferredDateFormatValidator.class)        
    public String preferredDateTimeFormat = "dd.MM.yyyy @ HH:mm:ss";
    
    @Preference(
            title = "Preferred timeout for network operations (in seconds):",
            description = "Defines preferred timeout for network operations (which will fail on timeout specified)")
    public int preferredTimeOut = 60;
    
    @Preference(
            title = "Show system tray icon",
            description = "Defines whether application can allocate space in system tray; allows to hide/restore application to/from system tray icon")
    public boolean useSysTrayIcon = false;

    @Preference(
            title = "Remain in system tray on window close",
            description = "Defines whether application should remain in system tray when application's window is closed")
    public boolean remainInSysTrayOnWindowClose = false;
    
    @Preference(
            title = "Hide main window on start",
            description = "Defines whether application should start with hidden main window (system tray icon will be shown in this case)")
    @PreferenceEnable(
            enabledByField = "remainInSysTrayOnWindowClose", 
            enabledByValue = "true")        
    public boolean startHidden = false;
    
    @Preference(
            title = "Minimize to system tray",
            description = "Defines whether application should be minimized to system tray instead of task panel")
    public boolean minimizeToSysTray = false;
    
    @Preference(
            title = "Auto save on exit",
            description = "Defines whether user data have to be automatically saved on exit")
    public boolean autoSaveOnExit = false;
    
    @Preference(
            title = "Display confirmation dialogs",
            description = "Defines whether confirmation dialogs should appear whenever user tries to delete entry, uninstall add-on and so on.")
    public boolean displayConfirmationDialogs = true;
    
    @Preference(
            title = "Enable automatic update",
            description = "Application core and add-ons will be checked for updates on startup if this option is enabled.")
    public boolean enableAutoUpdate = true;

    @Preference(
            title = "Automatic update interval, in days (set to 0 to update on startup only): ",
            description = "Defines how often automatic update is performed")
    @PreferenceEnable(
            enabledByField = "enableAutoUpdate", 
            enabledByValue = "true")        
    public int autoUpdateInterval = 7;
    
    @Preference(
            title = "Show memory usage information in status bar",
            description = "Defines if memory usage information should be shown in the status bar")
    public boolean showMemoryUsage = false;
    
}
