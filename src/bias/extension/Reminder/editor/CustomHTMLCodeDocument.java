/**
 * Created on Apr 14, 2008
 */
package bias.extension.Reminder.editor;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;

import bias.Constants;

/**
 * @author kion
 */
public class CustomHTMLCodeDocument extends DefaultStyledDocument {
    private static final long serialVersionUID = 1L;

    private static final Font DEFAULT_CODE_EDIT_FONT = new Font("Monospaced", Font.PLAIN, 12);

    private static final String[] AUTOCOMPLETE_PATTERNS = new String[] { "CHANNEL\\[(\\w+[\\w-\\s]*)\\]",
            "(SHOW|HIDE)ON(PAGE|CHANNEL)[^\\$>]+(\\[\\w+[\\w-\\s]*\\])*", "[^\\$>]+_(GROUP|COLUMN)" };

    private static String autocompletePattern = "";

    static {
        for (int i = 0; i < AUTOCOMPLETE_PATTERNS.length; i++) {
            if (i != 0) {
                autocompletePattern += "|";
            }
            autocompletePattern += "(" + AUTOCOMPLETE_PATTERNS[i] + ")";
        }
    }

    private JTextPane editor;

    private UndoRedoManager undoManager;
    
    private MutableAttributeSet allText;

    private MutableAttributeSet otherText;

    private MutableAttributeSet WPTag;

    private MutableAttributeSet HTMLTag;

    private MutableAttributeSet specChar;

    private MutableAttributeSet attribute;

    private MutableAttributeSet quote;

    private MutableAttributeSet comment;

    private Font font;

    CustomHTMLCodeDocument(JTextPane editor) {
        this.editor = editor;
        undoManager = new UndoRedoManager(editor);
        addUndoableEditListener(undoManager);
        font = DEFAULT_CODE_EDIT_FONT;
        allText = new SimpleAttributeSet();
        StyleConstants.setFontFamily(allText, font.getFamily());
        StyleConstants.setFontSize(allText, font.getSize());
        setParagraphAttributes(0, getLength(), allText, true);
        otherText = new SimpleAttributeSet();
        StyleConstants.setForeground(otherText, Color.BLACK);
        StyleConstants.setBold(otherText, false);
        StyleConstants.setUnderline(otherText, false);
        WPTag = new SimpleAttributeSet();
        StyleConstants.setBold(WPTag, true);
        StyleConstants.setUnderline(WPTag, true);
        HTMLTag = new SimpleAttributeSet();
        StyleConstants.setForeground(HTMLTag, new Color(99, 0, 99));
        StyleConstants.setBold(HTMLTag, true);
        specChar = new SimpleAttributeSet();
        StyleConstants.setForeground(specChar, new Color(0, 51, 255));
        StyleConstants.setBold(specChar, true);
        attribute = new SimpleAttributeSet();
        StyleConstants.setForeground(attribute, new Color(0, 204, 0));
        StyleConstants.setBold(attribute, false);
        quote = new SimpleAttributeSet();
        StyleConstants.setForeground(quote, new Color(255, 0, 0));
        StyleConstants.setBold(quote, false);
        comment = new SimpleAttributeSet();
        StyleConstants.setForeground(comment, new Color(153, 153, 153));
        StyleConstants.setBold(comment, false);
        StyleConstants.setItalic(comment, true);
    }

    public UndoRedoManager getUndoManager() {
        return undoManager;
    }

    /**
     * builds whitespace depending on current document offset
     * and changes editor's caret position depending on built offset;
     * 
     * @param offset current document offset
     * @return whitespace string to add to document right before closing sequence
     * @throws BadLocationException
     */
    protected String buildWhiteSpace(int offset) throws BadLocationException {
        StringBuffer whiteSpace = new StringBuffer();
        int lineNum = getDefaultRootElement().getElementIndex(offset);
        int lineOffset = getDefaultRootElement().getElement(lineNum).getStartOffset();
        while(true) {
            String spaceChar = getText(lineOffset++, 1);
            if (" ".equals(spaceChar) || "\t".equals(spaceChar)) {
                whiteSpace.append(spaceChar);
            } else {
                break;
            }
        }
        final int fOffset = offset;
        final String whiteSpaceStr = whiteSpace.toString();
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                editor.setCaretPosition(fOffset + whiteSpaceStr.length() + 3);
            }
        });
        return "\n" + whiteSpace.toString() + "\t\n" + whiteSpaceStr;
    }

    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        String tagName = Constants.EMPTY_STR;
        if (">".equals(str)) {
            for (int i = offs - 1; i >= 0; i--) {
                String ch = getContent().getString(i, 1);
                if ("<".equals(ch)) {
                    break;
                }
                tagName = ch + tagName;
            }
            Tag tag = HTML.getTag(tagName);
            if (tag != null && tag.isBlock()) {
                str += buildWhiteSpace(offs) + "</" + tagName + ">";
            } else {
                if (("<" + tagName + ">").matches("(?i)<\\$(" + autocompletePattern + ")\\$>")) {
                    str += buildWhiteSpace(offs) + "</" + tagName + ">";
                }
            }
        }
        super.insertString(offs, str, a);
        // get changed lines start/end offsets
        int startPos = getParagraphElement(offs).getStartOffset();
        int endPos = getParagraphElement(offs + str.length()).getEndOffset();
        // highlight changed lines
        highlight(startPos, endPos);
    }

    public void remove(int offs, int len) throws BadLocationException {
        super.remove(offs, len);
        // get changed lines start/end offsets
        int startPos = getParagraphElement(offs).getStartOffset();
        int endPos = getParagraphElement(offs).getEndOffset();
        // highlight changed lines
        highlight(startPos, endPos);
    }

    private void highlight(int startPos, int endPos) throws BadLocationException {

        // TODO [P2] implement comments highlighting

        setCharacterAttributes(startPos, endPos - startPos, otherText, true);

        boolean insideSpecSeq = false;
        int specSeqStartIdx = 0;
        for (int i = startPos; i < endPos; i++) {
            String ch = getContent().getString(i, 1);
            if ("&".equals(ch)) {
                ch = getContent().getString(i + 1, 1);
                if (!ch.equals(" ") && !ch.equals("\t")) {
                    insideSpecSeq = true;
                    specSeqStartIdx = i;
                }
            } else if (";".equals(ch)) {
                if (insideSpecSeq) {
                    setCharacterAttributes(specSeqStartIdx, i - specSeqStartIdx + 1, specChar, false);
                }
                insideSpecSeq = false;
            }
        }
        boolean insideTag = false;
        int tagStartIdx = 0;
        for (int i = startPos; i < endPos; i++) {
            String ch = getContent().getString(i, 1);
            if ("<".equals(ch)) {
                ch = getContent().getString(i + 1, 1);
                if ("/".equals(ch)) {
                    ch = getContent().getString(i + 2, 1);
                    if (!"$".equals(ch)) {
                        insideTag = true;
                        tagStartIdx = i;
                        i += 2;
                    }
                } else {
                    if (!"$".equals(ch)) {
                        insideTag = true;
                        tagStartIdx = i;
                        i += 1;
                    }
                }
            } else if (i > 0 && ">".equals(ch)) {
                ch = getContent().getString(i - 1, 1);
                if (!"$".equals(ch)) {
                    if (insideTag && i != tagStartIdx + 1) {
                        setCharacterAttributes(tagStartIdx, i - tagStartIdx + 1, HTMLTag, false);
                    }
                    insideTag = false;
                }
            }
        }
        int attributeStartIdx = 0;
        boolean insideQuote1 = false;
        int quote1StartIdx = 0;
        boolean insideQuote2 = false;
        int quote2StartIdx = 0;
        insideTag = false;
        int bCnt = 0;
        for (int i = startPos; i < endPos; i++) {
            String ch = getContent().getString(i, 1);
            if ("<".equals(ch)) {
                bCnt++;
                insideTag = true;
            } else if (">".equals(ch)) {
                bCnt--;
                if (bCnt == 0) {
                    insideTag = false;
                }
            } else if (insideTag) {
                if ("=".equals(ch)) {
                    for (int j = i - 1; j > 0; j--) {
                        ch = getContent().getString(j, 1);
                        if ("\t".equals(ch) || " ".equals(ch) || ">".equals(ch)) {
                            attributeStartIdx = j + 1;
                            break;
                        }
                    }
                    if (i != attributeStartIdx) {
                        setCharacterAttributes(attributeStartIdx, i - attributeStartIdx, attribute, false);
                    }
                } else if ("\"".equals(ch)) {
                    if (insideQuote1) {
                        setCharacterAttributes(quote1StartIdx, i - quote1StartIdx + 1, quote, false);
                        insideQuote1 = false;
                    } else {
                        insideQuote1 = true;
                        quote1StartIdx = i;
                    }
                } else if ("'".equals(ch)) {
                    if (insideQuote2) {
                        setCharacterAttributes(quote2StartIdx, i - quote2StartIdx + 1, quote, false);
                        insideQuote2 = false;
                    } else {
                        insideQuote2 = true;
                        quote2StartIdx = i;
                    }
                }
            }
        }
        boolean insideWPTag = false;
        int wpTagStartIdx = 0;
        for (int i = startPos; i < endPos; i++) {
            String ch = getContent().getString(i, 1);
            if ("<".equals(ch)) {
                ch = getContent().getString(i + 1, 1);
                if ("/".equals(ch)) {
                    ch = getContent().getString(i + 2, 1);
                    if ("$".equals(ch)) {
                        insideWPTag = true;
                        wpTagStartIdx = i;
                    }
                } else {
                    if ("$".equals(ch)) {
                        insideWPTag = true;
                        wpTagStartIdx = i;
                    }
                }
            } else if (i > 0 && ">".equals(ch)) {
                ch = getContent().getString(i - 1, 1);
                if ("$".equals(ch)) {
                    if (insideWPTag) {
                        setCharacterAttributes(wpTagStartIdx, i - wpTagStartIdx + 1, WPTag, false);
                    }
                    insideWPTag = false;
                }
            }
        }
        boolean openedComment = false;
        boolean closedComment = false;
        int commentStartIdx = 0;
        int commentEndIdx = getLength();
        for (int i = startPos; i < endPos; i++) {
            String ch = getContent().getString(i, 1);
            if ("<".equals(ch)) {
                ch = getContent().getString(i + 1, 1);
                if ("!".equals(ch)) {
                    ch = getContent().getString(i + 2, 2);
                    if ("--".equals(ch) && !openedComment) {
                        openedComment = true;
                        commentStartIdx = i;
                        i += 3;
                    }
                }
            } else if (i > 1 && ">".equals(ch)) {
                ch = getContent().getString(i - 2, 2);
                if ("--".equals(ch) && i-commentStartIdx >= 6) {
                    if (openedComment) {
                        setCharacterAttributes(commentStartIdx, i - commentStartIdx + 1, comment, false);
                        openedComment = false;
                    }
                    closedComment = true;
                    commentEndIdx = i;
                }
            }
        }
        if (openedComment && !closedComment) {
            for (int i = commentStartIdx; i < getLength(); i++) {
                String ch = getContent().getString(i, 1);
                if (i > 1 && ">".equals(ch)) {
                    ch = getContent().getString(i - 2, 2);
                    if ("--".equals(ch) && i-commentStartIdx >= 6) {
                        commentEndIdx = i;
                        closedComment = true;
                        break;
                    }
                }
            }
            if (closedComment) {
                setCharacterAttributes(commentStartIdx, commentEndIdx - commentStartIdx + 1, comment, false);
            }
        } else if (closedComment && !openedComment) {
            for (int i = commentEndIdx - 3; i > 0; i--) {
                String ch = getContent().getString(i, 1);
                if (i > 1 && ">".equals(ch)) {
                    ch = getContent().getString(i - 2, 2);
                    if ("--".equals(ch)) {
                        break;
                    }
                } else if ("<".equals(ch)) {
                    ch = getContent().getString(i + 1, 1);
                    if ("!".equals(ch)) {
                        ch = getContent().getString(i + 2, 2);
                        if ("--".equals(ch) && !openedComment) {
                            openedComment = true;
                            commentStartIdx = i;
                        }
                    }
                }
            }
            if (openedComment) {
                setCharacterAttributes(commentStartIdx, commentEndIdx - commentStartIdx + 1, comment, false);
            }
        }
    }

}
