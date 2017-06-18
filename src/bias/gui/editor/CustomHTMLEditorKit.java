/**
 * Created on Feb 23, 2008
 */
package bias.gui.editor;

import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import bias.utils.CommonUtils;

/**
 * @author kion
 */
public class CustomHTMLEditorKit extends HTMLEditorKit {
    private static final long serialVersionUID = 1L;
    
    private static final StyleSheet DEFAULT_CSS = CommonUtils.loadStyleSheet("editor/styles.css");

    private StyleSheet customCSS;

    public CustomHTMLEditorKit() {
        super();
    }
    
    public CustomHTMLEditorKit(StyleSheet customCSS) {
        this();
        this.customCSS = customCSS;
    }
    
    @Override
    public Document createDefaultDocument() {
        HTMLDocument doc = new CustomHTMLDocument(customCSS != null ? customCSS : DEFAULT_CSS);
        doc.setParser(getParser());
        doc.setAsynchronousLoadPriority(4);
        doc.setTokenThreshold(100);
        return doc;
    }

}
