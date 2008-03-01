/**
 * Created on Jan 19, 2008
 */
package bias;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Window;
import java.net.URL;

/**
 * @author kion
 */
public class Splash extends Window {

    private static final long serialVersionUID = 1L;

    private URL imageURL;

    private Image image;
    
    private static Splash instance;
    
    private boolean painted = false;
    
    private Splash(URL imageURL, Window parent) {
        super(parent != null ? parent : new Frame());
        this.image = Toolkit.getDefaultToolkit().createImage(imageURL);
        
        MediaTracker mt = new MediaTracker(this);
        mt.addImage(image,0);
        try {
            mt.waitForID(0);
        } catch(InterruptedException ie){}

        if (mt.isErrorID(0)) {
            setSize(0,0);
            synchronized(this) {
                painted = true;
                notifyAll();
            }
            return;
        }
        
        int imgWidth = image.getWidth(this);
        int imgHeight = image.getHeight(this);
        setSize(imgWidth, imgHeight);
        Dimension screenDim = parent != null ? parent.getSize() : Toolkit.getDefaultToolkit().getScreenSize();
        int x = parent != null ? parent.getLocation().x : 0;
        int y = parent != null ? parent.getLocation().y : 0;
        setLocation(x + (screenDim.width - imgWidth) / 2, y + (screenDim.height - imgHeight) / 2);
        
    }

    public static void showSplash(URL imageURL, Window parent) {
        if (instance == null || !imageURL.equals(instance.imageURL)) {
            instance = new Splash(imageURL, parent);
            instance.setVisible(true);
            if (!EventQueue.isDispatchThread()
                    && Runtime.getRuntime().availableProcessors() == 1) {
                synchronized(instance) {
                    while (!instance.painted) {
                        try { 
                            instance.wait(); 
                        } catch (InterruptedException e) {}
                    }
                }
            }
        }
    }
    
    public static void hideSplash() {
        if (instance != null) {
            if (!(instance.getOwner() instanceof Dialog)) {
                instance.getOwner().dispose();
            }
            instance.dispose();
            instance = null;
        }
    }
    
    public void update(Graphics g) {
        paint(g);
    }

    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);
        if (!painted) {
            painted = true;
            synchronized(this) { 
                notifyAll(); 
            }
        }
    }

}
