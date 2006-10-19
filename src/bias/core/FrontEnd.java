/**
 * Created on Oct 15, 2006
 */
package bias.core;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLEditorKit;

/**
 * @author kion
 */
public class FrontEnd extends JFrame {

    private static final long serialVersionUID = 1L;
    
    private static final String PROPERTY_WINDOW_COORDINATE_X = "WINDOW_COORDINATE_X";

    private static final String PROPERTY_WINDOW_COORDINATE_Y = "WINDOW_COORDINATE_Y";

    private static final String PROPERTY_WINDOW_WIDTH = "WINDOW_WIDTH";

    private static final String PROPERTY_WINDOW_HEIGHT = "WINDOW_HEIGHT";
    
    private static final String PROPERTY_LAST_SELECTED_TAB_INDEX = "LAST_SELECTED_TAB_INDEX";

    private Map<String, String> notes;  //  @jve:decl-index=0:
    
    private JPanel jContentPane = null;

    private JTabbedPane jTabbedPane = null;

    private JToolBar jToolBar = null;

    private JButton jButton = null;

    private JButton jButton1 = null;
    
    private JButton jButton2 = null;

    private JButton jButton3 = null;

    private JButton jButton4 = null;

    private JToolBar jToolBar1 = null;

    private JButton jButton5 = null;

    private JButton jButton6 = null;

    private JButton jButton7 = null;

    static {
        // setup application's Look&Feel
        try {
            UIManager.setLookAndFeel("net.sourceforge.napkinlaf.NapkinLookAndFeel");
        } catch (Exception e) {
            // do nothing - default Look&Feel will be set automatically
        }
    }

    /**
     * This is the default constructor
     */
    public FrontEnd() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(new Dimension(796, 558));  // Generated
        try {
            BackEnd.load();
            Properties properties = BackEnd.getProperties();

            int wpxValue;
            int wpyValue;
            int wwValue;
            int whValue;
            String wpx = properties.getProperty(PROPERTY_WINDOW_COORDINATE_X);
            if (wpx == null) {
                wpxValue = getToolkit().getScreenSize().width/4;
            } else {
                getToolkit().getScreenSize().getWidth();
                Double.valueOf(wpx);
                wpxValue = Math.round(Float.valueOf(GenericConstants.EMPTY_STR + (getToolkit().getScreenSize().getWidth() * Double.valueOf(wpx))));
            }
            String wpy = properties.getProperty(PROPERTY_WINDOW_COORDINATE_Y);
            if (wpy == null) {
                wpyValue = getToolkit().getScreenSize().height/4;
            } else {
                wpyValue = Math.round(Float.valueOf(GenericConstants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(wpy))));
            }
            String ww = properties.getProperty(PROPERTY_WINDOW_WIDTH);
            if (ww == null) {
                wwValue = (getToolkit().getScreenSize().width/4)*2;
            } else {
                wwValue = Math.round(Float.valueOf(GenericConstants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(ww))));
            }
            String wh = properties.getProperty(PROPERTY_WINDOW_HEIGHT);
            if (wh == null) {
                whValue = (getToolkit().getScreenSize().height/4)*2;
            } else {
                whValue = Math.round(Float.valueOf(GenericConstants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(wh))));
            }

            this.setLocation(wpxValue, wpyValue);
            this.setSize(wwValue, whValue);
            
            this.setContentPane(getJContentPane());
            this.setTitle("Bias");
            
            getJTabbedPane().removeAll();
            notes = BackEnd.getNotes();
            for (Entry<String, String> note : notes.entrySet()) {
                String key = note.getKey();
                String caption = properties.getProperty(key);
                String value = note.getValue();
                JTextPane textPane = new JTextPane();
                textPane.setEditorKit(new HTMLEditorKit());
                textPane.setText(value);
                textPane.setEditable(false);
                getJTabbedPane().addTab(caption, new JScrollPane(textPane));
            }
            
            int lstiValue;
            String lstiStr = properties.getProperty(PROPERTY_LAST_SELECTED_TAB_INDEX);
            if (lstiStr == null) {
                lstiValue = 0;
            } else {
                lstiValue = Integer.valueOf(lstiStr);
            }
            if (getJTabbedPane().getTabCount() > 0) {
                getJTabbedPane().setSelectedIndex(lstiValue);
            }
            
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
            this.addWindowListener(new java.awt.event.WindowAdapter() {   
                public void windowClosing(java.awt.event.WindowEvent e) {
                    try {
                        Properties properties = new Properties();
                        properties.put(PROPERTY_WINDOW_COORDINATE_X, GenericConstants.EMPTY_STR+getLocation().getX()/getToolkit().getScreenSize().getWidth());
                        properties.put(PROPERTY_WINDOW_COORDINATE_Y, GenericConstants.EMPTY_STR+getLocation().getY()/getToolkit().getScreenSize().getHeight());
                        properties.put(PROPERTY_WINDOW_WIDTH, GenericConstants.EMPTY_STR+getSize().getWidth()/getToolkit().getScreenSize().getHeight());
                        properties.put(PROPERTY_WINDOW_HEIGHT, GenericConstants.EMPTY_STR+getSize().getHeight()/getToolkit().getScreenSize().getHeight());
                        properties.put(PROPERTY_LAST_SELECTED_TAB_INDEX, GenericConstants.EMPTY_STR+getJTabbedPane().getSelectedIndex());
                        for (int i = 0; i < getJTabbedPane().getTabCount(); i++) {
                            JTextPane textPane = (JTextPane) 
                                                    ((JViewport) 
                                                         ((JScrollPane) 
                                                                 getJTabbedPane().getComponent(i)).getComponent(0)).getComponent(0);
                            String caption = getJTabbedPane().getTitleAt(i);
                            String value = textPane.getText();
                            properties.put(GenericConstants.EMPTY_STR+(i+1), caption);
                            notes.put(GenericConstants.EMPTY_STR+(i+1), value);
                        }
                        BackEnd.setProperties(properties);
                        BackEnd.setNotes(notes);
                        BackEnd.store();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJTabbedPane(), BorderLayout.CENTER);
            jContentPane.add(getJToolBar(), BorderLayout.SOUTH);  // Generated
            jContentPane.add(getJToolBar1(), BorderLayout.NORTH);  // Generated
        }
        return jContentPane;
    }

    /**
     * This method initializes jTabbedPane	
     * 	
     * @return javax.swing.JTabbedPane	
     */
    private JTabbedPane getJTabbedPane() {
        if (jTabbedPane == null) {
            jTabbedPane = new JTabbedPane();
            jTabbedPane.setBackground(null);  // Generated
        }
        return jTabbedPane;
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
            jToolBar.add(getJButton());  // Generated
            jToolBar.add(getJButton3());  // Generated
            jToolBar.add(getJButton1());  // Generated
            jToolBar.add(getJButton2());  // Generated
            jToolBar.add(getJButton4());  // Generated
        }
        return jToolBar;
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setText("add note");  // Generated
            jButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JTextPane textPane = new JTextPane();
                    textPane.setEditorKit(new HTMLEditorKit());
                    textPane.setEditable(false);
                    String noteCaption = JOptionPane.showInputDialog("Note caption:");
                    getJTabbedPane().addTab(noteCaption, new JScrollPane(textPane));
                    getJTabbedPane().setSelectedIndex(getJTabbedPane().getTabCount()-1);
                    getJTabbedPane().setTitleAt(getJTabbedPane().getSelectedIndex(), noteCaption);
                    if (getJTabbedPane().getTabCount() == 1) {
                        getJButton1().setEnabled(true);
                        getJButton2().setEnabled(true);
                    }
                }
            });
        }
        return jButton;
    }

    /**
     * This method initializes jButton1	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton1() {
        if (jButton1 == null) {
            jButton1 = new JButton();
            jButton1.setText("delete note");  // Generated
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    int index = getJTabbedPane().getSelectedIndex();
                    notes.remove(getJTabbedPane().getTitleAt(index));
                    getJTabbedPane().remove(index);
                    if (getJTabbedPane().getTabCount() == 0) {
                        getJButton1().setEnabled(false);
                        getJButton2().setEnabled(false);
                    }
                }
            });
        }
        return jButton1;
    }

    /**
     * This method initializes jButton3	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton3() {
        if (jButton3 == null) {
            jButton3 = new JButton();
            jButton3.setText("rename note");  // Generated
            jButton3.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String noteCaption = JOptionPane.showInputDialog("Note caption:");
                    getJTabbedPane().setTitleAt(getJTabbedPane().getSelectedIndex(), noteCaption);
                }
            });
        }
        return jButton3;
    }

    /**
     * This method initializes jButton2 
     *  
     * @return javax.swing.JButton  
     */
    private JButton getJButton2() {
        if (jButton2 == null) {
            jButton2 = new JButton();
            jButton2.setText("switch view mode");  // Generated
            jButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JTextPane textPane = (JTextPane) 
                                            ((JViewport) 
                                                 ((JScrollPane) 
                                                         getJTabbedPane().getComponent(getJTabbedPane().getSelectedIndex()))
                                                         .getComponent(0)).getComponent(0);
                    String text = textPane.getText();
                    if (textPane.getEditorKit() instanceof HTMLEditorKit) {
                        textPane.setEditorKit(new StyledEditorKit());
                    } else {
                        textPane.setEditorKit(new HTMLEditorKit());
                    }
                    textPane.setText(text);
                }
            });
        }
        return jButton2;
    }

    /**
     * This method initializes jButton4	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton4() {
        if (jButton4 == null) {
            jButton4 = new JButton();
            jButton4.setText("switch edit mode");  // Generated
            jButton4.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JTextPane textPane = (JTextPane) 
                    ((JViewport) 
                         ((JScrollPane) 
                                 getJTabbedPane().getComponent(getJTabbedPane().getSelectedIndex()))
                                 .getComponent(0)).getComponent(0);
                    textPane.setEditable(!textPane.isEditable());
                    switchToolbalEnabledState();
                }
            });
        }
        return jButton4;
    }

    /**
     * This method initializes jToolBar1	
     * 	
     * @return javax.swing.JToolBar	
     */
    private JToolBar getJToolBar1() {
        if (jToolBar1 == null) {
            jToolBar1 = new JToolBar();
            jToolBar1.setFloatable(false);  // Generated
            jToolBar1.add(getJButton5());  // Generated
            jToolBar1.add(getJButton6());  // Generated
            jToolBar1.add(getJButton7());  // Generated
        }
        return jToolBar1;
    }

    /**
     * This method initializes jButton5	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton5() {
        if (jButton5 == null) {
            jButton5 = new JButton();
            jButton5.setText("B");  // Generated
            jButton5.setEnabled(false);  // Generated
            jButton5.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    new StyledEditorKit.BoldAction().actionPerformed(e);
                }
            });
        }
        return jButton5;
    }

    /**
     * This method initializes jButton6	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton6() {
        if (jButton6 == null) {
            jButton6 = new JButton();
            jButton6.setText("I");  // Generated
            jButton6.setEnabled(false);  // Generated
            jButton6.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    new StyledEditorKit.ItalicAction().actionPerformed(e);
                }
            });
        }
        return jButton6;
    }

    /**
     * This method initializes jButton7	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton7() {
        if (jButton7 == null) {
            jButton7 = new JButton();
            jButton7.setText("U");  // Generated
            jButton7.setEnabled(false);  // Generated
            jButton7.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    new StyledEditorKit.UnderlineAction().actionPerformed(e);
                }
            });
        }
        return jButton7;
    }
    
    private void switchToolbalEnabledState() {
        for (Component button : getJToolBar1().getComponents()) {
            button.setEnabled(!button.isEnabled());
        }
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
