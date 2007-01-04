/**
 * Created on Oct 15, 2006
 */
package bias;

import java.io.File;
import java.net.URL;

import javax.swing.UIManager;

import bias.gui.FrontEnd;

import com.jgoodies.looks.FontPolicies;
import com.jgoodies.looks.FontPolicy;
import com.jgoodies.looks.FontSet;
import com.jgoodies.looks.FontSets;
import com.jgoodies.looks.Fonts;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.Silver;

/**
 * @author kion
 */
public class Bias {
    
    private static File jarFile;
    
    public static File getJarFile() {
        return jarFile;
    }

    public static void main(String[] args) throws Throwable {
        // find out what JAR file application is run from
        URL url = Bias.class.getResource(Bias.class.getSimpleName()+Constants.CLASS_FILE_ENDING);
        String jarFilePath = url.getFile().substring(0, url.getFile().indexOf(Bias.class.getName().replaceAll("\\.", "/")) - 2);
        jarFilePath = jarFilePath.substring(Constants.FILE_PROTOCOL_PREFIX.length(), jarFilePath.length());
        jarFile = new File(jarFilePath);
        
        // set and configure look-&-feel
        FontSet defaultFontSet = FontSets.createDefaultFontSet(Fonts.SEGOE_UI_12PT);
        FontPolicy defaultFontPolicy = FontPolicies.createFixedPolicy(defaultFontSet);
        PlasticXPLookAndFeel.setFontPolicy(defaultFontPolicy);
        PlasticXPLookAndFeel.setPlasticTheme(new Silver());
        UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
        
        // display front-end
        FrontEnd frontEnd = FrontEnd.getInstance();
        frontEnd.setVisible(true);
    }
    
}
