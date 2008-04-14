/*
 * Created on Aug 27, 2005
 */

package bias.extension.DashBoard.editor;

import javax.swing.JTextPane;
import javax.swing.text.Document;
import javax.swing.text.StyledEditorKit;

/**
 * @author kion
 */

public class CustomHTMLCodeEditorKit extends StyledEditorKit {
    private static final long serialVersionUID = 1L;

    private JTextPane editor;

    public CustomHTMLCodeEditorKit(JTextPane editor) {
        this.editor = editor;
    }

    public Document createDefaultDocument() {
        return new CustomHTMLCodeDocument(editor);
    }

}
