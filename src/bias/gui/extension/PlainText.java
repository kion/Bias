/**
 * Created on Oct 24, 2006
 */
package bias.gui.extension;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.UUID;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * @author kion
 */

@Extension.Annotation(
        name = "Plain Text", 
        version="1.0",
        description = "Simple plain text editor",
        author="kion")
public class PlainText extends Extension {

    private static final long serialVersionUID = 1L;

    private static final ImageIcon ICON_SWITCH_MODE = 
        new ImageIcon(PlainText.class.getResource("/bias/res/PlainText/switch_mode.png"));

    private static final Font FONT = new Font("SansSerif", Font.PLAIN, 12);
    
    private static class UndoRedoManager extends UndoManager implements DocumentListener {

        private static final long serialVersionUID = 1L;

        public CompoundEdit compoundEdit;

        private JTextComponent editor;

        private int lastOffset;

        private int lastLength;

        public UndoRedoManager(JTextComponent editor) {
            this.editor = editor;
            this.editor.addKeyListener(new UndoRedoKeyListener());
        }

        /* (non-Javadoc)
         * @see javax.swing.undo.UndoManager#undo()
         */
        public void undo() {
            super.undo();
        }

        /* (non-Javadoc)
         * @see javax.swing.undo.UndoManager#redo()
         */
        public void redo() {
            super.redo();
        }

        /* (non-Javadoc)
         * @see javax.swing.undo.UndoManager#undoableEditHappened(javax.swing.event.UndoableEditEvent)
         */
        public void undoableEditHappened(UndoableEditEvent e) {
            if (compoundEdit == null) {
                compoundEdit = startCompoundEdit(e.getEdit());
                lastLength = editor.getDocument().getLength();
                return;
            }

            AbstractDocument.DefaultDocumentEvent event = (AbstractDocument.DefaultDocumentEvent) e.getEdit();

            if (event.getType().equals(DocumentEvent.EventType.CHANGE)) {
                compoundEdit.addEdit(e.getEdit());
                return;
            }

            int offsetChange = editor.getCaretPosition() - lastOffset;
            int lengthChange = editor.getDocument().getLength() - lastLength;

            if (Math.abs(offsetChange) == 1 && Math.abs(lengthChange) == 1) {
                compoundEdit.addEdit(e.getEdit());
                lastOffset = editor.getCaretPosition();
                lastLength = editor.getDocument().getLength();
                return;
            }

            compoundEdit.end();
            compoundEdit = startCompoundEdit(e.getEdit());
        }

        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
         */
        public void insertUpdate(final DocumentEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    int offset = e.getOffset() + e.getLength();
                    offset = Math.min(offset, editor.getDocument().getLength());
                    editor.setCaretPosition(offset);
                }
            });
        }

        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
         */
        public void removeUpdate(DocumentEvent e) {
            editor.setCaretPosition(e.getOffset());
        }

        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
         */
        public void changedUpdate(DocumentEvent e) {}

        private CompoundEdit startCompoundEdit(UndoableEdit anEdit) {
            lastOffset = editor.getCaretPosition();
            lastLength = editor.getDocument().getLength();

            compoundEdit = new CustomCompoundEdit();
            compoundEdit.addEdit(anEdit);

            addEdit(compoundEdit);
            return compoundEdit;
        }

        private class CustomCompoundEdit extends CompoundEdit {

            private static final long serialVersionUID = 1L;
            
            /* (non-Javadoc)
             * @see javax.swing.undo.CompoundEdit#isInProgress()
             */
            public boolean isInProgress() {
                return false;
            }
            
            /* (non-Javadoc)
             * @see javax.swing.undo.CompoundEdit#undo()
             */
            public void undo() throws CannotUndoException {
                if (compoundEdit != null) {
                    compoundEdit.end();
                }
                super.undo();
                compoundEdit = null;
            }
            
        }

        private class UndoRedoKeyListener extends KeyAdapter {
            
            /* (non-Javadoc)
             * @see java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
             */
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (editor.isEditable()) {
                    if (e.getModifiers() == KeyEvent.CTRL_MASK && e.getKeyCode() == KeyEvent.VK_Z) {
                        if (canUndo()) {
                            undo();
                        }
                    } else if (e.getModifiers() == KeyEvent.CTRL_MASK && e.getKeyCode() == KeyEvent.VK_Y) {
                        if (canRedo()) {
                            redo();
                        }
                    }
                }
            };
            
        }

    }

    private JScrollPane jScrollPane = null;
    private JTextArea jTextArea = null;
    private JToolBar jToolBar = null;

    private JToggleButton jToggleButton = null;

    /**
     * Default constructor
     */
    public PlainText(UUID id, byte[] data) {
        super(id, data);
        initialize();
    }

    /* (non-Javadoc)
     * @see bias.gui.Extension#serialize()
     */
    @Override
    public byte[] serialize() {
        return getJTextArea().getText().getBytes();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(733, 515);
        this.setLayout(new BorderLayout());
        this.add(getJScrollPane(), BorderLayout.CENTER);  // Generated
        this.add(getJToolBar(), BorderLayout.SOUTH);  // Generated
        getJTextArea().setText(new String(getData()));
        getJTextArea().getDocument().addUndoableEditListener(new UndoRedoManager(jTextArea));
    }

    /**
     * This method initializes jScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTextArea());  // Generated
        }
        return jScrollPane;
    }

    /**
     * This method initializes jTextArea	
     * 	
     * @return javax.swing.JTextArea	
     */
    private JTextArea getJTextArea() {
        if (jTextArea == null) {
            jTextArea = new JTextArea();
            jTextArea.setFont(FONT);
            jTextArea.setEditable(false);
        }
        return jTextArea;
    }

    /**
     * This method initializes jToolBar	
     * 	
     * @return javax.swing.JToolBar	
     */
    private JToolBar getJToolBar() {
        if (jToolBar == null) {
            jToolBar = new JToolBar();
            jToolBar.setFloatable(false);  // Generated
            jToolBar.add(getJToggleButton());  // Generated
        }
        return jToolBar;
    }

    /**
     * This method initializes jToggleButton	
     * 	
     * @return javax.swing.JToggleButton	
     */
    private JToggleButton getJToggleButton() {
        if (jToggleButton == null) {
            jToggleButton = new JToggleButton();
            jToggleButton.setToolTipText("switch mode");  // Generated
            jToggleButton.setIcon(PlainText.ICON_SWITCH_MODE);
            jToggleButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    getJTextArea().setEditable(!getJTextArea().isEditable());
                    if (getJTextArea().isEditable()) {
                        getJTextArea().requestFocusInWindow();
                    }
                }
            });
        }
        return jToggleButton;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
