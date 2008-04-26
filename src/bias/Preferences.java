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
            title = "preferred.language.preference.title")
    @PreferenceChoice(
            providerClass = PreferredLanguageChoiceProvider.class)
    public String preferredLanguage = Constants.DEFAULT_LANGUAGE;
    
    @Preference(
            title = "preferred.datetime.format.preference.title")
    @PreferenceValidation(
            validationClass = PreferredDateFormatValidator.class)        
    public String preferredDateTimeFormat = "dd.MM.yyyy @ HH:mm:ss";
    
    @Preference(
            title = "preferred.timeout.preference.title")
    public int preferredTimeOut = 60;
    
    @Preference(
            title = "use.systrayicon.preference.title",
            description = "use.systrayicon.preference.description")
    public boolean useSysTrayIcon = false;

    @Preference(
            title = "remain.in.systray.on.window.close.preference.title")
    public boolean remainInSysTrayOnWindowClose = false;
    
    @Preference(
            title = "start.hidden.preference.title")
    @PreferenceEnable(
            enabledByField = "remainInSysTrayOnWindowClose", 
            enabledByValue = "true")        
    public boolean startHidden = false;
    
    @Preference(
            title = "minimize.to.systray.preference.title")
    public boolean minimizeToSysTray = false;
    
    @Preference(
            title = "auto.save.on.exit.preference.title")
    public boolean autoSaveOnExit = false;
    
    @Preference(
            title = "display.confirmation.dialogs.preference.title",
            description = "display.confirmation.dialogs.preference.description")
    public boolean displayConfirmationDialogs = true;
    
    @Preference(
            title = "auto.mode.preference.title",
            description = "auto.mode.preference.description")
    public boolean autoMode = false;
    
    @Preference(
            title = "enable.auto.update.preference.title")
    public boolean enableAutoUpdate = true;

    @Preference(
            title = "auto.update.interval.preference.title")
    @PreferenceEnable(
            enabledByField = "enableAutoUpdate", 
            enabledByValue = "true")        
    public int autoUpdateInterval = 7;
    
    @Preference(
            title = "show.memory.usage.preference.title")
    public boolean showMemoryUsage = false;
    
}
