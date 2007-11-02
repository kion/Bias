/**
 * Created on Nov 2, 2007
 */
package bias.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.plaf.TabbedPaneUI;

/**
 * @author kion
 */
public class TabMoveListener extends MouseAdapter {

    private int srcIndex = -1;

    private int currIndex = -1;

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
            srcIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
        }
        currIndex = -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
        JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
        if (!e.isPopupTrigger()) {
            int dstIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
            if (srcIndex != -1 && dstIndex != -1 && srcIndex != dstIndex) {
                moveTab(tabbedPane, srcIndex, dstIndex);
            }
        }
        deHighLight(tabbedPane);
        ((JTabbedPane) e.getSource()).setCursor(Cursor.getDefaultCursor());
        srcIndex = -1;
        currIndex = -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent e) {
        if (srcIndex != -1) {
            JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
            int index = tabbedPane.indexAtLocation(e.getX(), e.getY());
            if (index != -1) {
                ((JTabbedPane) e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                ((JTabbedPane) e.getSource()).setCursor(Cursor.getDefaultCursor());
            }
            if (index != -1 && index != currIndex) { // moved over another tab
                deHighLight(tabbedPane);
                currIndex = index;
            }
            if (currIndex != -1 && currIndex != srcIndex) {
                highLight(tabbedPane);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
        deHighLight((JTabbedPane) e.getSource());
        currIndex = -1;
    }

    /**
     * As far as internal structure of JTabbedPane data model does not correspond to its visual representation, that is, component located
     * on tab with index X is <b>not</b> located in the internal components array using the same index, we have to rearrange this array
     * each time tab has been moved and repopulate/repaint JTabbedPane instance after that.
     * 
     */
    private void moveTab(JTabbedPane tabPane, int srcIndex, int dstIndex) {

        int cnt = tabPane.getTabCount();

        // get tabpane's components/captions/icons
        Component[] components = new Component[cnt];
        for (int i = 0; i < cnt; i++) {
            components[i] = tabPane.getComponent(i);
        }
        String[] captions = new String[cnt];
        for (int i = 0; i < cnt; i++) {
            captions[i] = tabPane.getTitleAt(i);
        }
        ImageIcon[] icons = new ImageIcon[cnt];
        for (int i = 0; i < cnt; i++) {
            icons[i] = (ImageIcon) tabPane.getIconAt(i);
        }

        // remember component/caption that has to be moved
        Component srcComp = components[srcIndex];
        String srcCap = captions[srcIndex];
        ImageIcon srcIcon = icons[srcIndex];

        // rearrange components/captions using shifting
        if (srcIndex > dstIndex) {
            for (int i = srcIndex; i > dstIndex; i--) {
                components[i] = components[i - 1];
                captions[i] = captions[i - 1];
                icons[i] = icons[i - 1];
            }
        } else {
            for (int i = srcIndex; i < dstIndex; i++) {
                components[i] = components[i + 1];
                captions[i] = captions[i + 1];
                icons[i] = icons[i + 1];
            }
        }

        // set moved component/caption to its new position
        components[dstIndex] = srcComp;
        captions[dstIndex] = srcCap;
        icons[dstIndex] = srcIcon;

        // remove everything from tabpane before repopulating it
        tabPane.removeAll();

        // repopulate tabpane with resulting components/captions
        for (int i = 0; i < cnt; i++) {
            tabPane.addTab(captions[i], icons[i], components[i]);
        }

        // set moved component as selected
        tabPane.setSelectedIndex(dstIndex);

        // repaint tabpane
        tabPane.repaint();

    }

    private void deHighLight(JTabbedPane tabbedPane) {
        if (currIndex == -1) {
            return;
        }
        TabbedPaneUI ui = tabbedPane.getUI();
        Rectangle rect = ui.getTabBounds(tabbedPane, currIndex);
        tabbedPane.repaint(rect);
    }

    private void highLight(JTabbedPane tabbedPane) {
        TabbedPaneUI ui = tabbedPane.getUI();
        Rectangle rect = ui.getTabBounds(tabbedPane, currIndex);
        Graphics graphics = tabbedPane.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
    }

}
