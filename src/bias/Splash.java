/**
 * Created on Jan 19, 2008
 */
package bias;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

/**
 * @author kion
 */
public class Splash extends Window {

    private static final long serialVersionUID = 1L;

    public static final URL SPLASH_IMAGE_LOAD = Launcher.class.getResource("/bias/res/load.gif");
    
    public static final URL SPLASH_IMAGE_INSTALL = Launcher.class.getResource("/bias/res/install.gif");
    
    private URL imageURL;

    private Image image;
    
    private static Splash instance;
    
    private boolean painted = false;
    
    private Splash(URL imageURL, boolean hiddable) {
        super(new Frame());
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
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenDim.width - imgWidth) / 2, (screenDim.height - imgHeight) / 2);
        
        if (!hiddable) {
            MouseAdapter disposeOnClick = new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    synchronized(Splash.this) {
                        Splash.this.painted = true;
                        Splash.this.notifyAll();
                    }
                    dispose();
                }
            };
            addMouseListener(disposeOnClick);
        }
    }

    public static void showSplash(URL imageURL, boolean hiddable) {
        if (instance == null || !imageURL.equals(instance.imageURL)) {
            instance = new Splash(imageURL, hiddable);
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
            instance.getOwner().dispose();
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
