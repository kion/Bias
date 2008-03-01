/**
 * Created on Feb 29, 2008
 */
package bias.gui;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import bias.Constants;
import bias.utils.AppManager;

/**
 * @author kion
 */
public class LinkLabel extends JLabel {
    private static final long serialVersionUID = 1L;
    
    public LinkLabel(final String text) {
        this(text, text);
    }
    
    public LinkLabel(final String text, final String address) {
        super();
        setText(Constants.HTML_PREFIX + "<u>" + Constants.HTML_COLOR_HIGHLIGHT_LINK + text + Constants.HTML_COLOR_SUFFIX + "</u>" + Constants.HTML_SUFFIX);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    AppManager.getInstance().handleAddress(address);
                } catch (Exception ex) {
                    // ignore, do nothing
                    ex.printStackTrace(System.err);
                }
            }
        });
    }

}
