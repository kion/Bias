package bias.utils;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;

public class AppManager {
    
    private static AppManager instance;
    
    private static Desktop desktop;
    
    private AppManager() throws Exception {
        if (!Desktop.isDesktopSupported()) {
            throw new Exception("Desktop API is not available on this platform!");
        }    
        desktop = Desktop.getDesktop();
    }
    
    public static AppManager getInstance() throws Exception {
        if (instance == null) {
            instance = new AppManager();
        }
        return instance;
    }

    public void handleAddress(String address) throws Exception {
        
        if (address.contains("@")) {
            if (!desktop.isSupported(Desktop.Action.MAIL)) {
                throw new Exception("Desktop Mail API is not available on this platform!");
            }
            URI uri = new URI("mailto", address, null);
            desktop.mail(uri);
        } else {
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                throw new Exception("Desktop Browse API is not available on this platform!");
            }
            if (!address.startsWith("http://")) {
                address = "http://" + address;
            }
            URI uri = new URI(address);
            desktop.browse(uri);
        }
        
    }

    public void handleFile(File file) throws Exception {
        if (file != null && file.exists()) {
            if (!desktop.isSupported(Desktop.Action.OPEN)) {
                throw new Exception("Desktop Default Application API is not available on this platform!");
            }
            desktop.open(file.getAbsoluteFile());
        }
    }
    
}
