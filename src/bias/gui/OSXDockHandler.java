/**
 * Created on Jun 14, 2017
 */
package bias.gui;

import javax.swing.JFrame;

/**
 * This class is not supposed to be used on any other platforms except Mac OS X. 
 * More so, b/c it depends on platform-specific features provided by OS X JRE only, 
 * it doesn't compile on any other platform.
 * 
 * To avoid compilation errors during development, OS X JRE specific calls 
 * are commented out by default. 
 * 
 * To make it work on OS X though, this class has been compiled on it 
 * (i.e. with commented out lines below uncommented for that purpose) 
 * and corresponding .class files were placed into "osx" folder 
 * - these are simply copied to corresponding path under "bin" folder 
 * during build process.
 * 
 * This might not be very convenient from development standpoint 
 * (i.e. uncomment OS X JRE specific calls, compile this on OS X, 
 * copy .class files to "osx" dir and then comment it all out again), 
 * but as long as there's no better way of doing this (to my knowledge) 
 * - it is what it is.
 * 
 * @author kion
 */
public class OSXDockHandler {
    
    private OSXDockHandler() {
        // hidden default constructor
    }

    public static void handle(JFrame appWindow) {
//        Application.getApplication().addAppEventListener(new AppReOpenedListener() {
//            @Override
//            public void appReOpened(AppReOpenedEvent evt) {
//                appWindow.setVisible(true);
//            }
//        });
    }
    
}
