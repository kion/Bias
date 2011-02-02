/**
 * Created on Dec 21, 2010
 */
package bias.gui;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;

import bias.Constants;
import bias.core.BackEnd;

/**
 * @author kion
 */
public class IconChooserComboBox extends JComboBox {

	private static final long serialVersionUID = 1L;
	
	public IconChooserComboBox() {
		this(null);
	}
	
	public IconChooserComboBox(String selectedID) {
		int idx = 0, i = 0;
        addItem(new ImageIcon(new byte[]{}, Constants.EMPTY_STR));
        for (ImageIcon icon : BackEnd.getInstance().getIcons()) {
            addItem(icon);
            if (idx == 0 && selectedID != null && icon.getDescription() != null && icon.getDescription().equals(selectedID)) {
            	idx = ++i;
            } else {
                ++i;
            }
        }
        setSelectedIndex(idx);
	}
	
	public ImageIcon getSelectedIcon() {
		if (getSelectedIndex() == 0) return null;
		return (ImageIcon) super.getSelectedItem();
	}

}
