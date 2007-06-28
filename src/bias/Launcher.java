package bias;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.Timer;

public class Launcher extends Window {

    private static final long serialVersionUID = 1L;
    
    private static final String APP_MAIN_CLASS = "bias.Bias";
    
    private static final URL SPLASH_IMAGE_RESOURCE_URL = Launcher.class.getResource("/bias/res/splash.gif");

    private static Launcher instance;
    
    private Image image;
    
    private boolean painted = false;
    
    private Launcher() {
        super(new Frame());
        this.image = Toolkit.getDefaultToolkit().createImage(SPLASH_IMAGE_RESOURCE_URL);
        
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
        
        MouseAdapter disposeOnClick = new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                synchronized(Launcher.this) {
                    Launcher.this.painted = true;
                    Launcher.this.notifyAll();
                }
                dispose();
            }
        };
        addMouseListener(disposeOnClick);
    }
    
    public static void showSplash() {
        if (instance == null) {
            instance = new Launcher();
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

    private static void invokeApp(String password) throws Throwable {
        Class.forName(APP_MAIN_CLASS).getMethod(
                "launchApp", new Class[]{String.class}).invoke(null, new Object[]{password});
    }

    public static void main(String[] args) throws Throwable {
        JLabel label = new JLabel("password:");
        final JPasswordField passField = new JPasswordField();
        ActionListener al = new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                passField.requestFocusInWindow();
            }
        };
        Timer timer = new Timer(500,al);
        timer.setRepeats(false);
        timer.start();
        if (JOptionPane.showConfirmDialog(
                null, 
                new Component[]{label, passField}, 
                "Load authentification", 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String password = new String(passField.getPassword());            
            if (password != null) {
                showSplash();
                invokeApp(password);
                hideSplash();
            } else {
                System.exit(0);
            }
        }
    }

}
