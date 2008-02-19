/**
 * Created on Feb 19, 2008
 */
package bias.extension;

import javax.swing.JButton;
import javax.swing.JComponent;

/**
 * @author kion
 */
public class ToolRepresentation {
    
    private JButton button;
    
    private JComponent indicator;
    
    @SuppressWarnings("unused")
    private ToolRepresentation() {
        // hidden default constructor
    }

    public ToolRepresentation(JButton button, JComponent indicator) {
        if (button == null && indicator == null) {
            throw new IllegalArgumentException("Both button and indicator can not be null!");
        }
        this.button = button;
        this.indicator = indicator;
    }

    public JButton getButton() {
        return button;
    }

    public JComponent getIndicator() {
        return indicator;
    }

}
