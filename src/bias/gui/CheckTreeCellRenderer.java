/**
 * Created on Nov 2, 2007
 */
package bias.gui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 * @author kion
 */
public class CheckTreeCellRenderer extends JPanel implements TreeCellRenderer {
    private static final long serialVersionUID = 1L;

    private CheckTreeSelectionModel selectionModel;

    private TreeCellRenderer delegate;

    private TristateCheckBox checkBox = new TristateCheckBox();

    public CheckTreeCellRenderer(TreeCellRenderer delegate, CheckTreeSelectionModel selectionModel) {
        this.delegate = delegate;
        this.selectionModel = selectionModel;
        setLayout(new BorderLayout());
        setOpaque(false);
        checkBox.setOpaque(false);
    }

    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Component renderer = delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        TreePath path = tree.getPathForRow(row);
        if (path != null) {
            if (selectionModel.isPathSelected(path, true)) {
                checkBox.setState(Boolean.TRUE);
            } else {
                checkBox.setState(selectionModel.isPartiallySelected(path) ? null : Boolean.FALSE);
            }
        }
        removeAll();
        add(checkBox, BorderLayout.WEST);
        add(renderer, BorderLayout.CENTER);
        return this;
    }
}