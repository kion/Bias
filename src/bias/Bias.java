/**
 * Created on Oct 15, 2006
 */
package bias;

import bias.gui.FrontEnd;

/**
 * @author kion
 */
public class Bias {
    
    public static void main(String[] args) {
        FrontEnd frontEnd = FrontEnd.getInstance();
        frontEnd.setVisible(true);
    }

}
