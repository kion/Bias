/*
 * Created on Nov 1, 2006
 */

package bias.gui;

import java.io.IOException;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML.Tag;

/**
 * @author kion
 */

public class HTMLPageEditor {

    public static void insertHTML(JTextPane editor, String htmlText, Tag tag) throws BadLocationException, IOException {
        if (editor.getEditorKit() instanceof HTMLEditorKit && editor.getDocument() instanceof HTMLDocument) {
            HTMLEditorKit editorKit = (HTMLEditorKit) editor.getEditorKit();
            HTMLDocument document = (HTMLDocument) editor.getDocument();

            int caret = editor.getCaretPosition();
            Element pEl = document.getParagraphElement(caret);

            boolean isBreakingTag = tag.breaksFlow() || tag.isBlock();
            boolean isParagraphBegining = caret == pEl.getStartOffset();

            if (isBreakingTag && !isParagraphBegining) {
                editorKit.insertHTML(document, caret, htmlText, 1, 0, tag);
            } else if (!isBreakingTag && !isParagraphBegining) {
                editorKit.insertHTML(document, caret, htmlText, 0, 0, tag);
            } else if (isBreakingTag && isParagraphBegining) {
                // insert blank char to avoid incorrect caret positioning
                // after html text is inserted in the begining of the paragraph 
                document.insertBeforeStart(pEl, "&nbsp;");
                // insert html text actually
                editorKit.insertHTML(document, caret + 1, htmlText, 1, 0, tag);
                // remove blank char added before
                document.remove(caret, 1);
            } else if (!isBreakingTag && isParagraphBegining) {
                // insert blank char to avoid incorrect caret positioning
                // after html text is inserted in the begining of the paragraph 
                document.insertAfterStart(pEl, "&nbsp;");
                // insert html text actually
                editorKit.insertHTML(document, caret + 1, htmlText, 0, 0, tag);
                // remove blank char added before
                document.remove(caret, 1);
            }
        }
    }
    
}