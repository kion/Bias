/**
 * Created on Oct 15, 2006
 */
package bias.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import bias.core.BackEnd;
import bias.core.DataEntry;
import bias.global.Constants;

/**
 * @author kion
 */
public class FrontEnd extends JFrame {

    private static final long serialVersionUID = 1L;

    public static final ImageIcon ICON_ABOUT = 
        new ImageIcon(Constants.class.getResource("/bias/res/about.png"));

    public static final ImageIcon ICON_IMPORT_DATA = 
        new ImageIcon(Constants.class.getResource("/bias/res/import_data.png"));

    public static final ImageIcon ICON_DELETE = 
        new ImageIcon(Constants.class.getResource("/bias/res/delete.png"));

    public static final ImageIcon ICON_ADD_ENTRY = 
        new ImageIcon(Constants.class.getResource("/bias/res/add_entry.png"));

    public static final ImageIcon ICON_ADD_CATEGORY = 
        new ImageIcon(Constants.class.getResource("/bias/res/add_category.png"));

    public static final ImageIcon ICON_APP = 
        new ImageIcon(Constants.class.getResource("/bias/res/app_icon.png"));

    private JPanel jContentPane = null;

    private JTabbedPane jTabbedPane = null;

    private JToolBar jToolBar = null;

    private JButton jButton = null;

    private JButton jButton1 = null;
    
    private JButton jButton2 = null;

    private JPanel jPanel = null;

    private JToolBar jToolBar2 = null;

    private JButton jButton6 = null;

    private JButton jButton3 = null;

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
        this.setSize(new Dimension(772, 535));  // Generated
        try {
            this.setTitle("Bias");
            this.setIconImage(ICON_APP.getImage());
            this.setContentPane(getJContentPane());

            BackEnd.load();
            Properties properties = BackEnd.getProperties();
            
            int wpxValue;
            int wpyValue;
            int wwValue;
            int whValue;
            String wpx = properties.getProperty(Constants.PROPERTY_WINDOW_COORDINATE_X);
            if (wpx == null) {
                wpxValue = getToolkit().getScreenSize().width/4;
            } else {
                getToolkit().getScreenSize().getWidth();
                Double.valueOf(wpx);
                wpxValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getWidth() * Double.valueOf(wpx))));
            }
            String wpy = properties.getProperty(Constants.PROPERTY_WINDOW_COORDINATE_Y);
            if (wpy == null) {
                wpyValue = getToolkit().getScreenSize().height/4;
            } else {
                wpyValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(wpy))));
            }
            String ww = properties.getProperty(Constants.PROPERTY_WINDOW_WIDTH);
            if (ww == null) {
                wwValue = (getToolkit().getScreenSize().width/4)*2;
            } else {
                wwValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(ww))));
            }
            String wh = properties.getProperty(Constants.PROPERTY_WINDOW_HEIGHT);
            if (wh == null) {
                whValue = (getToolkit().getScreenSize().height/4)*2;
            } else {
                whValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getToolkit().getScreenSize().getHeight() * Double.valueOf(wh))));
            }

            this.setLocation(wpxValue, wpyValue);
            this.setSize(wwValue, whValue);
            
            representData(BackEnd.getData());

            int lstcValue;
            String lstcStr = properties.getProperty(Constants.PROPERTY_LAST_SELECTED_CATEGORY_INDEX);
            if (lstcStr == null) {
                lstcValue = 0;
            } else {
                lstcValue = Integer.valueOf(lstcStr);
            }
            if (getJTabbedPane().getTabCount() > 0) {
                getJTabbedPane().setSelectedIndex(lstcValue);
                setNotesManagementToolbalEnabledState(true);
                
                int lsteValue;
                String lsteStr = properties.getProperty(Constants.PROPERTY_LAST_SELECTED_ENTRY_INDEX);
                if (lsteStr == null) {
                    lsteValue = 0;
                } else {
                    lsteValue = Integer.valueOf(lsteStr);
                }
                JTabbedPane tabbedPane = (JTabbedPane) getJTabbedPane().getComponent(lstcValue);
                if (tabbedPane.getTabCount() > 0) {
                    tabbedPane.setSelectedIndex(lsteValue);
                }
            } else {
                setNotesManagementToolbalEnabledState(false);
            }

            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
            this.addWindowListener(new java.awt.event.WindowAdapter() {   
                public void windowClosing(java.awt.event.WindowEvent e) {
                    try {
                        Properties properties = new Properties();
                        properties.put(Constants.PROPERTY_WINDOW_COORDINATE_X, Constants.EMPTY_STR+getLocation().getX()/getToolkit().getScreenSize().getWidth());
                        properties.put(Constants.PROPERTY_WINDOW_COORDINATE_Y, Constants.EMPTY_STR+getLocation().getY()/getToolkit().getScreenSize().getHeight());
                        properties.put(Constants.PROPERTY_WINDOW_WIDTH, Constants.EMPTY_STR+getSize().getWidth()/getToolkit().getScreenSize().getHeight());
                        properties.put(Constants.PROPERTY_WINDOW_HEIGHT, Constants.EMPTY_STR+getSize().getHeight()/getToolkit().getScreenSize().getHeight());
                        int categoryIdx = getJTabbedPane().getSelectedIndex();
                        if (categoryIdx != -1) {
                            properties.put(Constants.PROPERTY_LAST_SELECTED_CATEGORY_INDEX, Constants.EMPTY_STR+categoryIdx);
                            JTabbedPane tabbedPane = (JTabbedPane) getJTabbedPane().getComponent(categoryIdx);
                            int entryIdx = tabbedPane.getSelectedIndex();
                            if (entryIdx != -1) {
                                properties.put(Constants.PROPERTY_LAST_SELECTED_ENTRY_INDEX, Constants.EMPTY_STR+entryIdx);
                            }
                        }

                        BackEnd.setProperties(properties);
                        
                        Collection<DataEntry> data = new LinkedList<DataEntry>();
                        for (int i = 0; i < getJTabbedPane().getTabCount(); i++) {
                            String category = getJTabbedPane().getTitleAt(i);
                            JTabbedPane categoryTabPane = (JTabbedPane) getJTabbedPane().getComponent(i);
                            for (int j = 0; j < categoryTabPane.getTabCount(); j++) {
                                VisualEntry visualEntry = (VisualEntry) categoryTabPane.getComponent(j);
                                byte[] serializedData = visualEntry.serialize();
                                String caption = categoryTabPane.getTitleAt(j);
                                data.add(new DataEntry(caption, category, visualEntry.getClass().getSimpleName(), serializedData));
                            }
                        }
                        
                        BackEnd.setData(data);
                        
                        BackEnd.store();
                    } catch (Exception ex) {
                        displayErrorMessage(ex);
                    }
                }
            });
        } catch (Exception ex) {
            displayErrorMessage(ex);
        }
    }
    
    private void representData(Collection<DataEntry> data) {
        try {
            Map<String, Integer> categoryIdxMapping = new HashMap<String, Integer>();
            for (int i = 0; i < getJTabbedPane().getTabCount(); i++) {
                categoryIdxMapping.put(getJTabbedPane().getTitleAt(i), i);
            }
            for (DataEntry dataEntry : data) {
                String caption = dataEntry.getCaption();
                VisualEntry visualEntry = VisualEntryFactory.getInstance().newVisualEntry(dataEntry);
                String category = dataEntry.getCategory();
                JTabbedPane categoryTabPane;
                Integer categoryIdx = categoryIdxMapping.get(category);
                if (categoryIdx == null) {
                    categoryTabPane = new JTabbedPane();
                    getJTabbedPane().add(category, categoryTabPane);
                    categoryTabPane.addTab(caption, visualEntry);
                    categoryTabPane.addMouseListener(tabDoubleClickListener);
                    categoryIdxMapping.put(category, getJTabbedPane().getTabCount()-1);
                } else {
                    categoryTabPane = (JTabbedPane) getJTabbedPane().getComponent(categoryIdx);
                    categoryTabPane.addTab(caption, visualEntry);
                }
            }
            if (getJTabbedPane().getTabCount() > 0) {
                setNotesManagementToolbalEnabledState(true);
            }
        } catch (Exception e) {
            displayErrorMessage(e);
        }
    }
    
    private void setNotesManagementToolbalEnabledState(boolean enabled) {
        for (Component button : getJToolBar().getComponents()) {
            if (!button.equals(getJButton2()) 
                    && !button.equals(getJButton3())) {
                button.setEnabled(enabled);
            }
        }
    }

    private void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void displayErrorMessage(Exception ex) {
        JOptionPane.showMessageDialog(this, "Details: " + ex, "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
    
    MouseListener tabDoubleClickListener = new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent e) {
            if (e.getClickCount() == 2) {
                JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
                int index = tabbedPane.getSelectedIndex();
                String caption = tabbedPane.getTitleAt(index);
                caption = JOptionPane.showInputDialog("Entry caption:", caption);
                if (caption != null) { 
                    tabbedPane.setTitleAt(index, caption);
                }
            }
        }
    };

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
            jContentPane.add(getJPanel(), BorderLayout.NORTH);  // Generated
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
            jTabbedPane.setTabPlacement(JTabbedPane.LEFT);  // Generated
            jTabbedPane.addMouseListener(tabDoubleClickListener);
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
            jToolBar.add(getJButton2());  // Generated
            jToolBar.add(getJButton3());  // Generated
            jToolBar.add(getJButton());  // Generated
            jToolBar.add(getJButton1());  // Generated
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
            jButton.setToolTipText("add entry");  // Generated
            jButton.setIcon(ICON_ADD_ENTRY);
            jButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        int idx = getJTabbedPane().getSelectedIndex();
                        if (idx != -1) {
                            JLabel entryTypeLabel = new JLabel("Type:");
                            JComboBox entryTypeComboBox = new JComboBox();
                            for (String entryType : VisualEntryFactory.getInstance().getEntryTypes().keySet()) {
                                entryTypeComboBox.addItem(entryType);
                            }
                            entryTypeComboBox.setEditable(false);
                            String caption = JOptionPane.showInputDialog(
                                    null, 
                                    new Component[]{
                                            entryTypeLabel,
                                            entryTypeComboBox}, 
                                    "New entry:", 
                                    JOptionPane.QUESTION_MESSAGE);
                            if (caption != null) {
                                String typeDescription = (String) entryTypeComboBox.getSelectedItem();
                                Class type = VisualEntryFactory.getInstance().getEntryTypes().get(typeDescription);
                                VisualEntry visualEntry = VisualEntryFactory.getInstance().newVisualEntry(type, new byte[]{});
                                if (visualEntry != null) {
                                    JTabbedPane categoryTabPane = (JTabbedPane) getJTabbedPane().getComponent(idx);
                                    categoryTabPane.addTab(caption, visualEntry);
                                    getJTabbedPane().setSelectedComponent(categoryTabPane);
                                    categoryTabPane.setSelectedComponent(visualEntry);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        displayErrorMessage(ex);
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
            jButton1.setToolTipText("delete entry");  // Generated
            jButton1.setIcon(ICON_DELETE);
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        int categoryIndex = getJTabbedPane().getSelectedIndex();
                        JTabbedPane categoryTabPane = (JTabbedPane) getJTabbedPane().getComponent(categoryIndex);
                        int entryIndex = categoryTabPane.getSelectedIndex();
                        if (entryIndex != -1) {
                            categoryTabPane.remove(entryIndex);
                        } else {
                            getJTabbedPane().remove(categoryIndex);
                        }
                        if (getJTabbedPane().getTabCount() == 0) {
                            setNotesManagementToolbalEnabledState(false);
                        }
                    } catch (Exception ex) {
                        displayErrorMessage(ex);
                    }
                }
            });
        }
        return jButton1;
    }

    /**
     * This method initializes jButton2	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton2() {
        if (jButton2 == null) {
            jButton2 = new JButton();
            jButton2.setToolTipText("import data from another Bias JAR");  // Generated
            jButton2.setIcon(ICON_IMPORT_DATA);
            jButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        JFileChooser jfc = new JFileChooser();
                        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        jfc.setFileFilter(new FileFilter(){
                            @Override
                            public boolean accept(File file) {
                                if (file.getName().endsWith(".jar")) {
                                    return true;
                                }
                                return false;
                            }
                            @Override
                            public String getDescription() {
                                return "Java Archive File (*.jar)";
                            }
                        });
                        File jarFile = null;
                        int rVal = jfc.showOpenDialog(FrontEnd.this);
                        if (rVal == JFileChooser.APPROVE_OPTION) {
                            jarFile = jfc.getSelectedFile();
                            representData(BackEnd.importData(jarFile));
                        }
                    } catch (Exception ex) {
                        displayErrorMessage(ex);
                    }
                }
            });
        }
        return jButton2;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(new BorderLayout());  // Generated
            jPanel.add(getJToolBar(), BorderLayout.CENTER);  // Generated
            jPanel.add(getJToolBar2(), BorderLayout.EAST);  // Generated
        }
        return jPanel;
    }

    /**
     * This method initializes jToolBar2	
     * 	
     * @return javax.swing.JToolBar	
     */
    private JToolBar getJToolBar2() {
        if (jToolBar2 == null) {
            jToolBar2 = new JToolBar();
            jToolBar2.setFloatable(false);  // Generated
            jToolBar2.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);  // Generated
            jToolBar2.add(getJButton6());  // Generated
        }
        return jToolBar2;
    }

    /**
     * This method initializes jButton6	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton6() {
        if (jButton6 == null) {
            jButton6 = new JButton();
            jButton6.setToolTipText("about Bias");  // Generated
            jButton6.setIcon(ICON_ABOUT);
            jButton6.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JOptionPane.showMessageDialog(FrontEnd.this, 
                            "<html>Bias Personal Information Manager, version 0.1-beta" +
                            "<br>(c) ki0n, 2006" +
                            "<br>http://bias.sourceforge.net");
                }
            });
        }
        return jButton6;
    }

    /**
     * This method initializes jButton3	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton3() {
        if (jButton3 == null) {
            jButton3 = new JButton();
            jButton3.setToolTipText("add category");  // Generated
            jButton3.setIcon(ICON_ADD_CATEGORY);
            jButton3.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String category = JOptionPane.showInputDialog("New category:");
                    if (category != null) {
                        boolean duplicate = false;
                        for (int i = 0; i < getJTabbedPane().getTabCount(); i++) {
                            if(category.equals(getJTabbedPane().getTitleAt(i))) {
                                duplicate = true;
                                break;
                            }
                        }
                        if (!duplicate) {
                            JTabbedPane tabbedPane = new JTabbedPane();
                            getJTabbedPane().addTab(category, tabbedPane);
                            getJTabbedPane().setSelectedComponent(tabbedPane);
                            if (getJTabbedPane().getTabCount() == 1) {
                                setNotesManagementToolbalEnabledState(true);
                            }
                        } else {
                            displayErrorMessage("Category name should be unique!");
                        }
                    }
                }
            });    
        }
        return jButton3;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"