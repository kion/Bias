/**
 * Created on Oct 24, 2006
 */
package bias.gui;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JToggleButton;

/**
 * @author kion
 */
public class PlainText extends VisualEntry {

    private static final long serialVersionUID = 1L;

    private static final ImageIcon ICON_SWITCH_MODE = 
        new ImageIcon(PlainText.class.getResource("/bias/res/switch_mode.png"));

    private JScrollPane jScrollPane = null;
    private JTextArea jTextArea = null;
    private JToolBar jToolBar = null;

    private JToggleButton jToggleButton = null;
    /**
     * This is the default constructor
     */
    public PlainText(String data) {
        super(data);
        initialize();
    }

    /* (non-Javadoc)
     * @see bias.gui.VisualEntry#serialize()
     */
    @Override
    public String serialize() {
        return getJTextArea().getText();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(733, 515);
        this.setLayout(new BorderLayout());
        this.add(getJScrollPane(), BorderLayout.CENTER);  // Generated
        this.add(getJToolBar(), BorderLayout.SOUTH);  // Generated
        getJTextArea().setText(getData());
    }

    /**
     * This method initializes jScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTextArea());  // Generated
        }
        return jScrollPane;
    }

    /**
     * This method initializes jTextArea	
     * 	
     * @return javax.swing.JTextArea	
     */
    private JTextArea getJTextArea() {
        if (jTextArea == null) {
            jTextArea = new JTextArea();
            jTextArea.setEditable(false);
        }
        return jTextArea;
    }

    /**
     * This method initializes jToolBar	
     * 	
     * @return javax.swing.JToolBar	
     */
    private JToolBar getJToolBar() {
        if (jToolBar == null) {
            jToolBar = new JToolBar();
            jToolBar.setFloatable(false);  // Generated
            jToolBar.add(getJToggleButton());  // Generated
        }
        return jToolBar;
    }

    /**
     * This method initializes jToggleButton	
     * 	
     * @return javax.swing.JToggleButton	
     */
    private JToggleButton getJToggleButton() {
        if (jToggleButton == null) {
            jToggleButton = new JToggleButton();
            jToggleButton.setToolTipText("switch mode");  // Generated
            jToggleButton.setIcon(PlainText.ICON_SWITCH_MODE);
            jToggleButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    getJTextArea().setEditable(!getJTextArea().isEditable());
                    if (getJTextArea().isEditable()) {
                        getJTextArea().requestFocusInWindow();
                    }
                }
            });
        }
        return jToggleButton;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
