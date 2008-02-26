/**
 * Created on Nov 2, 2007
 */
package bias.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
                TabMoveUtil.moveTab(tabbedPane, srcIndex, dstIndex);
            }
        }
        deHighLight(tabbedPane);
        tabbedPane.setCursor(Cursor.getDefaultCursor());
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
                tabbedPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                tabbedPane.setCursor(Cursor.getDefaultCursor());
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
