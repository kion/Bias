package bias;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.Timer;

public class Launcher {

    private static final String APP_MAIN_CLASS = "bias.Bias";
    
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
                "Bias :: Authorization", 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String password = new String(passField.getPassword());            
            if (password != null) {
                Splash.showSplash(Splash.SPLASH_IMAGE_LOAD, null);
                invokeApp(password);
                Splash.hideSplash();
            } else {
                System.exit(0);
            }
        }
    }

}
