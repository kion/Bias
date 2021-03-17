/**
 * Created on Apr 13, 2008
 */
package bias.extension.DashBoard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;

import javax.swing.DefaultDesktopManager;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import bias.Constants;
import bias.core.Attachment;
import bias.core.BackEnd;
import bias.extension.EntryExtension;
import bias.extension.DashBoard.snippet.BrokenSnippet;
import bias.extension.DashBoard.snippet.HTMLSnippet;
import bias.extension.DashBoard.snippet.InfoSnippet;
import bias.extension.DashBoard.snippet.TextSnippet;
import bias.extension.DashBoard.xmlb.Frame;
import bias.extension.DashBoard.xmlb.FrameType;
import bias.extension.DashBoard.xmlb.Frames;
import bias.extension.DashBoard.xmlb.ObjectFactory;
import bias.gui.FrontEnd;
import bias.gui.IconChooserComboBox;
import bias.utils.CommonUtils;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * @author kion
 */
public class DashBoard extends EntryExtension {
    private static final long serialVersionUID = 1L;

    // TODO [P2] add more different snippet-types

    private static final String SCHEMA_LOCATION = "http://bias.sourceforge.net/addons/DashBoardSchema.xsd";
    
    private static final ImageIcon DEFAULT_FRAME_ICON = new ImageIcon(CommonUtils.getResourceURL(DashBoard.class, "default_frame_icon.png"));

    private static JAXBContext context;

    private static Unmarshaller unmarshaller;

    private static Marshaller marshaller;

    private static ObjectFactory objFactory = new ObjectFactory();

    private Collection<InfoSnippet> snippets;

    private Map<InfoSnippet, Integer> snippetsZOrders;

    private Map<UUID, JToggleButton> iconButtons;

    private Properties settings;

    private JDesktopPane dashBoardPanel;

    private JPanel toolPanel;

    private JToolBar toolBar;

    private JToolBar iconBar;

    private JComboBox addCB;

    private JButton configButt;

    private static JAXBContext getContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(), ObjectFactory.class.getClassLoader());
        }
        return context;
    }

    private static Unmarshaller getUnmarshaller() throws JAXBException {
        if (unmarshaller == null) {
            unmarshaller = getContext().createUnmarshaller();
        }
        return unmarshaller;
    }

    private static Marshaller getMarshaller() throws JAXBException {
        if (marshaller == null) {
            marshaller = getContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SCHEMA_LOCATION);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        }
        return marshaller;
    }

    public DashBoard(UUID id, byte[] data, byte[] settings) throws Throwable {
        super(id, data, settings);
        setLayout(new BorderLayout());
        getToolPanel().add(getToolBar(), BorderLayout.WEST);
        getToolPanel().add(getIconBar(), BorderLayout.CENTER);
        add(getToolPanel(), BorderLayout.SOUTH);
        add(getDashBoardPanel(), BorderLayout.CENTER);
        initialize();
    }

    private void initialize() {
        settings = PropertiesUtils.deserializeProperties(getSettings());
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    if (getData() != null && getData().length != 0) {
                        Frames frames = (Frames) getUnmarshaller().unmarshal(new ByteArrayInputStream(getData()));
                        Collections.sort(frames.getFrame(), new Comparator<Frame>() {
							@Override
							public int compare(Frame f1, Frame f2) {
								if (f1.getPosition() > f2.getPosition()) {
									return 1;
								} else {
									return -1;
								}
							}
						});
                        for (Frame frame : frames.getFrame()) {
                            addFrame(frame, false);
                        }
                        restoreZOrders();
                    }
                } catch (JAXBException ex) {
                    FrontEnd.displayErrorMessage("Failed to initialize!", ex);
                }
            }
        });
        FrontEnd.addMainWindowComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e) {
                for (InfoSnippet f : getSnippets()) {
                    relocate(f);
                }
                super.componentResized(e);
            }
        });
    }

    private Collection<InfoSnippet> getSnippets() {
        if (snippets == null) {
            snippets = new LinkedList<InfoSnippet>();
        }
        return snippets;
    }

    private Map<InfoSnippet, Integer> getSnippetsZOrders() {
        if (snippetsZOrders == null) {
            snippetsZOrders = new HashMap<InfoSnippet, Integer>();
        }
        return snippetsZOrders;
    }
    
	public Map<UUID, JToggleButton> getIconButtons() {
        if (iconButtons == null) {
        	iconButtons = new HashMap<UUID, JToggleButton>();
        }
		return iconButtons;
	}

    private void cleanUpUnUsedAttachments(Collection<String> referencedAttachmentNames) {
        try {
        	if (referencedAttachmentNames == null || referencedAttachmentNames.isEmpty()) {
        		BackEnd.getInstance().removeAttachments(getId());
        	} else {
                Collection<Attachment> atts = BackEnd.getInstance().getAttachments(getId());
                for (Attachment att : atts) {
                    if (!referencedAttachmentNames.contains(att.getName())) {
                        BackEnd.getInstance().removeAttachment(getId(), att.getName());
                    }
                }
        	}
        } catch (Exception ex) {
            // if some error occurred while cleaning up unused attachments,
            // ignore it, these attachments will be removed next time Bias persists data
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see bias.extension.EntryExtension#serializeData()
     */
    @Override
    public byte[] serializeData() throws Throwable {
    	Collection<String> refAttNames = null;
        Dimension size = FrontEnd.getMainWindowSize();
        Frames frames = objFactory.createFrames();
        for (InfoSnippet f : getSnippets()) {
            Frame frame = new Frame();
            Rectangle r = f.getNormalBounds();
            frame.setId(f.getId().toString());
            frame.setX(r.getX() / size.getWidth());
            frame.setY(r.getY() / size.getHeight());
            frame.setW(r.getWidth() / size.getWidth());
            frame.setH(r.getHeight() / size.getHeight());
            frame.setIconified(f.isIcon());
            frame.setSelected(f.isSelected());
            frame.setContent(f.serializeContent());
            frame.setSettings(f.serializeSettings());
            frame.setTitle(f.getTitle());
            if (f.getFrameIcon() != null && f.getFrameIcon() instanceof ImageIcon) {
            	frame.setIcon(((ImageIcon) f.getFrameIcon()).getDescription());
            }
            frame.setType(FrameType.fromValue(f.getName()));
            frame.setZ(getDashBoardPanel().getComponentZOrder(f));
            frame.setPosition(getIconBar().getComponentIndex(getIconButtons().get(f.getId())));
            frames.getFrame().add(frame);
            Collection<String> attNames = f.getReferencedAttachmentNames();
            if (attNames != null && !attNames.isEmpty()) {
            	if (refAttNames == null) {
            		refAttNames = new ArrayList<>(attNames);
            	} else {
            		refAttNames.addAll(attNames);
            	}
            }
        }
        cleanUpUnUsedAttachments(refAttNames);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getMarshaller().marshal(frames, baos);
        return baos.toByteArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see bias.extension.EntryExtension#serializeSettings()
     */
    @Override
    public byte[] serializeSettings() throws Throwable {
        return PropertiesUtils.serializeProperties(settings);
    }

    /*
     * (non-Javadoc)
     * 
     * @see bias.extension.EntryExtension#getSearchData()
     */
    @Override
    public Collection<String> getSearchData() throws Throwable {
        Collection<String> searchData = new ArrayList<String>();
        for (InfoSnippet snippet : getSnippets()) {
            searchData.addAll(snippet.getSearchData());
        }
        return searchData;
    }
    
    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#highlightSearchResults(java.lang.String, boolean, boolean)
     */
    @Override
    public void highlightSearchResults(String searchExpression, boolean isCaseSensitive, boolean isRegularExpression) throws Throwable {
        for (InfoSnippet snippet : getSnippets()) {
            snippet.highlightSearchResults(searchExpression, isCaseSensitive, isRegularExpression);
        }
    }
    
    @Override
    public void clearSearchResultsHighlight() throws Throwable {
        for (InfoSnippet snippet : getSnippets()) {
            snippet.clearSearchResultsHighlight();
        }
    }

    private JPanel getToolPanel() {
        if (toolPanel == null) {
        	toolPanel = new JPanel(new BorderLayout());
        }
        return toolPanel;
    }

    private JToolBar getToolBar() {
        if (toolBar == null) {
            toolBar = new JToolBar();
            toolBar.setFloatable(false);
            toolBar.add(getAddComboBox());
            toolBar.add(getConfigButton());
        }
        return toolBar;
    }

    private JToolBar getIconBar() {
        if (iconBar == null) {
        	iconBar = new JToolBar(JToolBar.HORIZONTAL);
        	iconBar.setFloatable(false);
        }
        return iconBar;
    }

    private JButton getConfigButton() {
        if (configButt == null) {
            configButt = new JButton(FrontEnd.getGUIIcons().getIconConfigureEntry());
            configButt.setPreferredSize(new Dimension(32, 32));
            configButt.setMinimumSize(new Dimension(32, 32));
            configButt.setMaximumSize(new Dimension(32, 32));
            configButt.setText(Constants.EMPTY_STR);
            configButt.setToolTipText("Configure snippet");
            configButt.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        InfoSnippet snippet = (InfoSnippet) getDashBoardPanel().getSelectedFrame();
                        if (snippet != null) {
                            snippet.configure();
                        }
                    } catch (Throwable t) {
                        FrontEnd.displayErrorMessage("Failed to configure/apply/store snippet settings! " + CommonUtils.getFailureDetails(t), t);
                    }
                }
            });
        }
        return configButt;
    }

    private JComboBox getAddComboBox() {
        if (addCB == null) {
            addCB = new JComboBox();
            addCB.setPreferredSize(new Dimension(100, 32));
            addCB.setMinimumSize(new Dimension(100, 32));
            addCB.setMaximumSize(new Dimension(100, 32));
            addCB.addItem(" + ");
            for (FrameType ft : FrameType.values()) {
                addCB.addItem(ft.value());
            }
            addCB.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED && addCB.getSelectedIndex() != 0) {
                        try {
                            FrameType ft = FrameType.fromValue((String) addCB.getSelectedItem());
                            Frame frame = new Frame();
                            frame.setType(ft);
                            addFrame(frame, true);
                        } finally {
                            addCB.setSelectedIndex(0);
                        }
                    }
                }
            });
        }
        return addCB;
    }

    private class CustomDesktopManager extends DefaultDesktopManager {
        private static final long serialVersionUID = 1L;

        @Override
        public void dragFrame(JComponent component, int xCordinate, int yCordinate) {
            if (component instanceof JInternalFrame) {
                JInternalFrame frame = (JInternalFrame) component;
                JDesktopPane desktop = frame.getDesktopPane();
                int width = desktop.getSize().width;
                int height = desktop.getSize().height;
                int w = component.getSize().width;
                int h = component.getSize().height;
                if (xCordinate < 0) {
                    xCordinate = 0;
                }
                if (xCordinate + w > width) {
                    xCordinate = width - w;
                }
                if (yCordinate < 0) {
                    yCordinate = 0;
                }
                if (yCordinate + h > height) {
                    yCordinate = height - h;
                }
                super.dragFrame(component, xCordinate, yCordinate);
            }
        }

        @Override
        public void resizeFrame(JComponent component, int xCordinate, int yCordinate, int w, int h) {
            if (component instanceof JInternalFrame) {
                JInternalFrame frame = (JInternalFrame) component;
                JDesktopPane desktop = frame.getDesktopPane();
                int width = desktop.getSize().width;
                int height = desktop.getSize().height;
                if (xCordinate < 0) {
                    w += xCordinate;
                }
                if (xCordinate + w > width) {
                    w = width - xCordinate;
                }
                if (yCordinate < 0) {
                    h += yCordinate;
                }
                if (yCordinate + h > height) {
                    h = height - yCordinate;
                }
                if (xCordinate < 0) {
                    xCordinate = 0;
                }
                if (xCordinate + w > width) {
                    w = width - xCordinate;
                }
                if (yCordinate < 0) {
                    yCordinate = 0;
                }
                if (yCordinate + h > height) {
                    yCordinate = height - h;
                }
            }
            super.resizeFrame(component, xCordinate, yCordinate, w, h);
        }
        
        @Override
        public void iconifyFrame(JInternalFrame f) {
        	super.iconifyFrame(f);
        	f.getDesktopIcon().setVisible(false);
        	JToggleButton b = getIconButtons().get(((InfoSnippet) f).getId());
        	b.setSelected(false);
        }
        
        @Override
        public void deiconifyFrame(JInternalFrame f) {
        	super.deiconifyFrame(f);
        	JToggleButton b = getIconButtons().get(((InfoSnippet) f).getId());
        	b.setSelected(true);
        }
        
    }

    private JDesktopPane getDashBoardPanel() {
        if (dashBoardPanel == null) {
            dashBoardPanel = new JDesktopPane();
            dashBoardPanel.setDesktopManager(new CustomDesktopManager());
            dashBoardPanel.setBackground(Color.LIGHT_GRAY);
        }
        return dashBoardPanel;
    }
    
    private void relocate(InfoSnippet f) {
        boolean relocate = false;
        double x = f.getX();
        if (f.getLocation().getX() + f.getSize().getWidth() > getDashBoardPanel().getSize().getWidth()) {
            x = getDashBoardPanel().getSize().getWidth() - f.getSize().getWidth();
            relocate = true;
        }
        if (f.getSize().getWidth() >= getDashBoardPanel().getSize().getWidth() - 15) {
        	x = 0;
        	relocate = true;
        }
        double y = f.getLocation().getY();
        if (f.getLocation().getY() + f.getSize().getHeight() > getDashBoardPanel().getSize().getHeight()) {
            y = getDashBoardPanel().getSize().getHeight() - f.getSize().getHeight();
            relocate = true;
        }
        if (f.getSize().getHeight() >= getDashBoardPanel().getSize().getHeight() - 15) {
        	y = 0;
        	relocate = true;
        }
        if (relocate) {
        	f.setLocation((int) x, (int) y);
        }
    }

    private void addFrame(final Frame frame, boolean isNew) {
    	if (frame.getId() == null) {
    		frame.setId(UUID.randomUUID().toString());
    	}
        final InfoSnippet f = createInternalFrame(frame);
        getDashBoardPanel().add(f);
        int zOrder = isNew ? 0 : frame.getZ();
        if (isNew) {
            try {
                getDashBoardPanel().setComponentZOrder(f, zOrder);
                updateZOrders();
                f.setSelected(true);
                JToggleButton b = getIconButtons().get(f.getId());
            	b.setSelected(true);
            } catch (IllegalArgumentException iae) {
                // ignore
            } catch (PropertyVetoException e) {
                // ignore
            }
        } else {
            // restore snippet state
            try {
                f.setIcon(frame.isIconified());
                JToggleButton b = getIconButtons().get(f.getId());
            	b.setSelected(!frame.isIconified());
                f.setSelected(frame.isSelected());
    		} catch (PropertyVetoException e1) {
    			// ignore, snippet state won't be restored (not critical)
    		}
        }
        getSnippetsZOrders().put(f, zOrder);
    	f.addInternalFrameListener(new InternalFrameAdapter() {
    		@Override
    		public void internalFrameClosing(InternalFrameEvent e) {
    			JToggleButton b = getIconButtons().get(f.getId());
    			getIconBar().remove(b);
    			getIconBar().repaint();
    			getIconButtons().remove(f.getId());
    		}
		});
    }

    private void updateZOrders() {
        for (InfoSnippet f : getSnippetsZOrders().keySet()) {
            Integer zo = getSnippetsZOrders().get(f);
            zo++;
            getSnippetsZOrders().put(f, zo);
        }
    }

    private void restoreZOrders() {
        for (Entry<InfoSnippet, Integer> entry : getSnippetsZOrders().entrySet()) {
            try {
                getDashBoardPanel().setComponentZOrder(entry.getKey(), entry.getValue());
            } catch (IllegalArgumentException iae) {
                // ignore
            }
        }
    }

    private InfoSnippet createInternalFrame(Frame frame) {
        InfoSnippet is;
        byte[] content = frame.getContent();
        if (content == null)
            content = new byte[] {};
        byte[] settings = frame.getSettings();
        if (settings == null)
            settings = new byte[] {};
        try {
            switch (frame.getType()) {
            case HTML_SNIPPET:
                is = new HTMLSnippet(getId(), UUID.fromString(frame.getId()), content, settings);
                break;
            case TEXT_SNIPPET:
                is = new TextSnippet(getId(), UUID.fromString(frame.getId()), content, settings);
                break;
            default:
                is = new BrokenSnippet(getId(), UUID.fromString(frame.getId()), content, settings);
            }
        } catch (Throwable t) {
            is = new BrokenSnippet(getId(), UUID.fromString(frame.getId()), content, settings);
        }
        if (frame != null) {
            final InfoSnippet f = is;
            f.setName(frame.getType().value());
            f.setTitle(frame.getTitle());
        	final JToggleButton b = new JToggleButton();
        	b.setPreferredSize(new Dimension(32, 32));
        	b.setMinimumSize(new Dimension(32, 32));
        	b.setMaximumSize(new Dimension(32, 32));
        	b.setName(frame.getId());
        	if (!Validator.isNullOrBlank(frame.getTitle())) {
        		b.setToolTipText(frame.getTitle());
        	}
            if (!Validator.isNullOrBlank(frame.getIcon())) {
            	ImageIcon icon = BackEnd.getInstance().getIcon(UUID.fromString(frame.getIcon()));
            	f.setFrameIcon(icon);
            	b.setIcon(icon);
            } else {
            	f.setFrameIcon(null);
            	b.setIcon(DEFAULT_FRAME_ICON);
            }
            b.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						f.setIcon(!b.isSelected());
					} catch (PropertyVetoException e1) {
						// ignore, shoudn't happen ever
					}
				}
			});
        	getIconBar().add(b);
        	getIconButtons().put(f.getId(), b);
            Dimension minSize = f.getMinimumSize();
            if (frame.getContent() != null) {
                Dimension size = FrontEnd.getMainWindowSize();
                int wpxValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (size.getWidth() * frame.getX())));
                int wpyValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (size.getHeight() * frame.getY())));
                int wwValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (size.getWidth() * frame.getW())));
                if (wwValue < minSize.width)
                    wwValue = minSize.width;
                int whValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (size.getHeight() * frame.getH())));
                if (whValue < minSize.height)
                    whValue = minSize.height;
                f.setLocation(wpxValue, wpyValue);
                f.setSize(wwValue, whValue);
            } else {
                f.setLocation(0, 0);
                f.setSize(minSize);
            }
            f.setVisible(true);
            f.addInternalFrameListener(new InternalFrameAdapter() {
                @Override
                public void internalFrameClosed(InternalFrameEvent e) {
                    getSnippets().remove(f);
                }
            });
            ((BasicInternalFrameUI) f.getUI()).getNorthPane().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    	JLabel captionLabel = new JLabel("Caption");
                    	JLabel iconLabel = new JLabel("Icon");
                    	String title = f.getTitle();
                    	ImageIcon icon = f.getFrameIcon() != null && f.getFrameIcon() instanceof ImageIcon ? ((ImageIcon) f.getFrameIcon()) : null;
                    	String selectedID = icon != null ? icon.getDescription() : null;
                        IconChooserComboBox iconChooser = new IconChooserComboBox(selectedID);
                        
                        String newTitle = JOptionPane.showInputDialog(
                        		DashBoard.this,
                        		new Component[]{ iconLabel, iconChooser, captionLabel },
                        		title);
                        
                        JToggleButton btn = getIconButtons().get(f.getId());
                        if (newTitle != null) {
                            f.setTitle(newTitle);
                            btn.setToolTipText(newTitle);
                        	ImageIcon newIcon = iconChooser.getSelectedIcon();
                        	f.setFrameIcon(newIcon);
                        	btn.setIcon(newIcon != null ? newIcon : DEFAULT_FRAME_ICON);
                        }
                    }
                }
            });
            ((BasicInternalFrameUI) f.getUI()).getNorthPane().addMouseWheelListener(new MouseWheelListener() {
				@Override
				public void mouseWheelMoved(MouseWheelEvent e) {
					if (e.getWheelRotation() != 0) {
						if (e.getWheelRotation() > 0) {
	                    	JToggleButton b = getIconButtons().get(f.getId());
	                    	int idx = getIconBar().getComponentIndex(b);
	                    	getIconBar().remove(b);
	                    	getIconBar().add(b, idx - 1);
						} else {
	                    	JToggleButton b = getIconButtons().get(f.getId());
	                    	int idx = getIconBar().getComponentIndex(b);
	                    	getIconBar().remove(b);
	                    	if (idx == getIconBar().getComponentCount()) idx = -1;
	                    	getIconBar().add(b, idx + 1);
						}
					}
				}
			});
            getSnippets().add(f);
        }
        return is;
    }

}
