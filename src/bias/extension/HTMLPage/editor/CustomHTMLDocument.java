/**
 * Created on Feb 23, 2008
 */
package bias.extension.HTMLPage.editor;

import java.awt.Font;

import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

/**
 * @author kion
 */
public class CustomHTMLDocument extends HTMLDocument {
    private static final long serialVersionUID = 1L;

    private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 12);

    public CustomHTMLDocument(StyleSheet ss) {
        super(ss);
        putProperty("IgnoreCharsetDirective", Boolean.TRUE);
        setPreservesUnknownTags(false);
    }

    @Override
    public Font getFont(AttributeSet attr) {
        Object family = attr.getAttribute(StyleConstants.FontFamily);
        Object size = attr.getAttribute(StyleConstants.FontSize);
        if (family == null && size == null) {
            return DEFAULT_FONT;
        }
        return super.getFont(attr);
    }
};
