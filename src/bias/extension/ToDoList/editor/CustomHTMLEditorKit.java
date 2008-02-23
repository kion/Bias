/**
 * Created on Feb 23, 2008
 */
package bias.extension.ToDoList.editor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import bias.core.BackEnd;
import bias.extension.ToDoList.ToDoList;

/**
 * @author kion
 */
public class CustomHTMLEditorKit extends HTMLEditorKit {
    private static final long serialVersionUID = 1L;
    
    private static final StyleSheet CSS = getStyleSheet(BackEnd.getInstance().getResourceAsStream(ToDoList.class, "editor/styles.css"));

    private static final StyleSheet getStyleSheet(InputStream is) {
        StyleSheet styles = new StyleSheet();
        try {
            Reader r = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
            styles.loadRules(r, null);
            r.close();
        } catch (Throwable t) {
            // ignore, styles just won't be initialized
            t.printStackTrace(System.err);
        }
        return styles;
    }

    public CustomHTMLEditorKit() {
        super();
    }
    
    @Override
    public Document createDefaultDocument() {
        HTMLDocument doc = new CustomHTMLDocument(CSS);
        doc.setParser(getParser());
        doc.setAsynchronousLoadPriority(4);
        doc.setTokenThreshold(100);
        return doc;
    }

}
