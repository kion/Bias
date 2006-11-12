/*
 * Created on Nov 1, 2006
 */

package bias.utils;

import java.io.IOException;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML.Tag;

import bias.global.Constants;

/**
 * @author kion
 */

public class HTMLPageEditor {

    public static void insertHTML(JTextPane editor, String htmlText, Tag tag) throws BadLocationException, IOException {
        if (editor.getEditorKit() instanceof HTMLEditorKit && editor.getDocument() instanceof HTMLDocument) {
            
            // remove editor's selected text if any 
            if (editor.getSelectedText() != null) {
                editor.replaceSelection(Constants.EMPTY_STR);
            }

            HTMLEditorKit editorKit = (HTMLEditorKit) editor.getEditorKit();
            HTMLDocument document = (HTMLDocument) editor.getDocument();

            // remember attribute set on current editor's caret position
            int getStylePos = editor.getCaretPosition() == 0 ? editor.getCaretPosition() : editor.getCaretPosition() - 1;
            AttributeSet as = document.getCharacterElement(getStylePos).getAttributes();
            
            int caret = editor.getCaretPosition();
            Element pEl = document.getParagraphElement(caret);
            
            // insert space after inserted html if needed
            boolean insertSpace = true;
            if (caret < document.getLength()) {
                if (document.getText(caret, 1).matches("\\s+")) {
                    insertSpace = false;
                }
            }
            if (insertSpace) {
                htmlText += "&nbsp;";
            }
            
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
            
            // set remembered attribute set for inserted html
            int caretAfter = editor.getCaretPosition();
            editor.setSelectionStart(caret);
            editor.setSelectionEnd(caretAfter);
            editor.setCharacterAttributes(as, false);
            editor.setCaretPosition(caretAfter);
            
        }
    }
    
    /**
     * inserts HTML-line-break as response to user's Enter-key pressing;
     * should be called immediately after Enter-key has been released
     * 
     * @param editor editor to insert line break to
     * @throws BadLocationException
     * @throws IOException
     */
    public static void insertLineBreakOnEnter(JTextPane editor) throws BadLocationException, IOException {
        if (editor.getEditorKit() instanceof HTMLEditorKit && editor.getDocument() instanceof HTMLDocument) {
            HTMLEditorKit editorKit = (HTMLEditorKit) editor.getEditorKit();
            HTMLDocument document = (HTMLDocument) editor.getDocument();

            int caret = editor.getCaretPosition();

            // insert HTML-line-break
            editorKit.insertHTML(document, caret-1, "<br>", 0, 0, HTML.Tag.BR);
            // remove plain line break
            document.remove(caret-1, 1);
            
        }
    }

}
