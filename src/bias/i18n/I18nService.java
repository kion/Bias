/**
 * Created on Sep 28, 2007
 */
package bias.i18n;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import bias.Constants;
import bias.Preferences;
import bias.extension.AddOn;

/**
 * @author kion
 */
public class I18nService {
    
    private static I18nService instance;
    
    private Map<Class<? extends AddOn>, Map<String, String>> addOnMessages;
    
    private Map<String, String> messages;
    
    private Map<String, String> langs;
    
    private I18nService() {}
    
    public static I18nService getInstance() {
        if (instance == null) {
            instance = new I18nService();
        }
        return instance;
    }
    
    public Map<String, String> getMessages(Class<? extends AddOn> addOnClass) {
        Map<String, String> messages = null;
        try {
            if (addOnMessages == null) {
                addOnMessages = new HashMap<Class<? extends AddOn>, Map<String, String>>();
            }
            messages = addOnMessages.get(addOnClass);
            if (messages == null) {
                String locale = getLanguageLocale(Preferences.getInstance().preferredLanguage);
                File addOnInfoDir = new File(Constants.ADDON_INFO_DIR, addOnClass.getSimpleName());
                if (addOnInfoDir.exists()) {
                    File messagesFile = new File(addOnInfoDir, locale + Constants.MESSAGE_FILE_ENDING);
                    if (!messagesFile.exists()) {
                        locale = Constants.DEFAULT_LOCALE;
                    }
                    messagesFile = new File(addOnInfoDir, locale + Constants.MESSAGE_FILE_ENDING);
                    FileInputStream is = new FileInputStream(messagesFile);
                    messages = initLocale(is);
                    addOnMessages.put(addOnClass, messages);
                    is.close();
                }
            }
        } catch (Throwable t) {
            System.err.println("Failed to initialize internationalization support for class '" + addOnClass.getName() + "'!");
            t.printStackTrace(System.err);
        }
        return messages;
    }
    
    public Map<String, String> getMessages() {
        if (messages == null) {
            try {
                String locale = getLanguageLocale(Preferences.getInstance().preferredLanguage);
                InputStream is = I18nService.class.getResourceAsStream(Constants.MESSAGE_FILE_PATH + locale + Constants.MESSAGE_FILE_ENDING);
                messages = initLocale(is);
                is.close();
            } catch (Throwable t) {
                System.err.println("Failed to initialize general internationalization support!");
                t.printStackTrace(System.err);
            }
        }
        return messages;
    }
    
    public Map<String, String> initLocale(InputStream is) throws Exception {
        Map<String, String> messages = new HashMap<String, String>();
        InputStreamReader isr = new InputStreamReader(is, Constants.MESSAGES_CHARSET);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter osr = new OutputStreamWriter(baos, Constants.MESSAGES_CHARSET);
        int b;
        while ((b = isr.read()) != -1) {
            osr.write(b);
        }
        osr.close();
        baos.close();
        isr.close();
        String text = new String(baos.toByteArray());
        String[] msgs = text.split(Constants.MESSAGE_SEPARATOR);
        for (String message : msgs) {
            String[] keyValuePair = message.split(Constants.MESSAGE_KEY_VALUE_SEPARATOR);
            for (int i = 0; i < keyValuePair.length; i += 2) {
                messages.put(keyValuePair[i].trim(), keyValuePair[i+1].trim());
            }
        }
        return messages;
    }
    
    public Collection<String> getAvailableLanguages() {
        return getAvailableLangsLocales().keySet();
    }
    
    public String getLanguageLocale(String lang) {
        return getAvailableLangsLocales().get(lang);
    }
    
    private Map<String, String> getAvailableLangsLocales() {
        if (langs == null) {
            try {
                langs = new LinkedHashMap<String, String>();
                InputStream is = I18nService.class.getResourceAsStream(Constants.MESSAGE_FILE_PATH + Constants.LANGUAGES_LIST_FILENAME);
                InputStreamReader isr = new InputStreamReader(is, Constants.MESSAGES_CHARSET);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                OutputStreamWriter osr = new OutputStreamWriter(baos, Constants.MESSAGES_CHARSET);
                int b;
                while ((b = isr.read()) != -1) {
                    osr.write(b);
                }
                osr.close();
                baos.close();
                isr.close();
                String text = new String(baos.toByteArray());
                String[] msgs = text.split(Constants.MESSAGE_SEPARATOR);
                for (String message : msgs) {
                    String[] keyValuePair = message.split(Constants.MESSAGE_KEY_VALUE_SEPARATOR);
                    for (int i = 0; i < keyValuePair.length; i += 2) {
                        langs.put(keyValuePair[i].trim(), keyValuePair[i+1].trim());
                    }
                }
            } catch (Throwable t) {
                System.err.println("Failed to initialize list of available languages!");
                t.printStackTrace(System.err);
            }
        }
        return langs;
    }

}
