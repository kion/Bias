/**
 * Created on Nov 16, 2006
 */
package bias.extension;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;

import bias.annotation.AddOnAnnotation;
import bias.core.DataEntry;

/**
 * @author kion
 */

@AddOnAnnotation(
        name = "Missing Extension Informer", 
        version="1.0",
        description = "Special extension to inform about missing extensions",
        author="kion")
public class MissingExtensionInformer extends Extension {

    private static final long serialVersionUID = 1L;

    private DataEntry dataEntry;
    
    private JScrollPane jScrollPane = null;
    private JTextPane jTextPane = null;

    /**
     * Default constructor
     */
    public MissingExtensionInformer(DataEntry dataEntry) {
        super(dataEntry.getId(), dataEntry.getData(), null);
        this.dataEntry = dataEntry;
        initialize();
    }

    public DataEntry getDataEntry() {
        return dataEntry;
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeData()
     */
    @Override
    public byte[] serializeData() throws Throwable {
        return getData();
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeSettings()
     */
    @Override
    public byte[] serializeSettings() throws Throwable {
        return getSettings();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.add(getJScrollPane(), BorderLayout.CENTER);
    }
    
    /**
     * This method initializes jScrollPane  
     *  
     * @return javax.swing.JScrollPane  
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTextPane());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jTextPane   
     *  
     * @return javax.swing.JTextPane   
     */
    private JTextPane getJTextPane() {
        if (jTextPane == null) {
            jTextPane = new JTextPane();
            jTextPane.setEditable(false);
            jTextPane.setEditorKit(new HTMLEditorKit());
            jTextPane.setText(buildMissingExtensionMessage());
        }
        return jTextPane;
    }

    private String buildMissingExtensionMessage() {
        return
        "<html><body>" + 
        "<font size=\"3\" face=\"SansSerif\">" +
        "<b><font color=\"#FF0000\">MISSING EXTENSION!</font></b><br><br>" + 
        "<b>To display this entry you should install following extension:</b><br><br>" + 
        "<b><i><font color=\"#0000FF\">" + dataEntry.getType() + "</font></i></b>" +
        "</font>" +
        "</body></html>"; 
    }

}
