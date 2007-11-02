/**
 * Created on Nov 2, 2007
 */
package bias.gui;

import javax.swing.JTabbedPane;

/**
 * @author kion
 */
public class Placement {

    private String string;

    private Integer integer;

    public Placement(int placementType) {
        this.integer = placementType;
        switch (placementType) {
        case JTabbedPane.TOP:
            this.string = "Top";
            break;
        case JTabbedPane.LEFT:
            this.string = "Left";
            break;
        case JTabbedPane.RIGHT:
            this.string = "Right";
            break;
        case JTabbedPane.BOTTOM:
            this.string = "Bottom";
            break;
        default:
            this.string = "Top";
        }
    }

    public Integer getInteger() {
        return integer;
    }

    public void setInteger(Integer integer) {
        this.integer = integer;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }

}
