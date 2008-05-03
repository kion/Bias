/**
 * Created on Apr 13, 2008
 */
package bias.extension.DashBoard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;

import javax.swing.DefaultDesktopManager;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import bias.Constants;
import bias.extension.EntryExtension;
import bias.extension.DashBoard.snippet.HTMLSnippet;
import bias.extension.DashBoard.snippet.InfoSnippet;
import bias.extension.DashBoard.snippet.TextSnippet;
import bias.extension.DashBoard.xmlb.Frame;
import bias.extension.DashBoard.xmlb.FrameType;
import bias.extension.DashBoard.xmlb.Frames;
import bias.extension.DashBoard.xmlb.ObjectFactory;
import bias.gui.FrontEnd;
import bias.utils.CommonUtils;
import bias.utils.PropertiesUtils;

/**
 * @author kion
 */
public class DashBoard extends EntryExtension {
    private static final long serialVersionUID = 1L;

    // TODO [P2] add more different snippet-types

    private static final String SCHEMA_LOCATION = "http://bias.sourceforge.net/addons/DashBoardSchema.xsd";

    private static JAXBContext context;

    private static Unmarshaller unmarshaller;

    private static Marshaller marshaller;

    private static ObjectFactory objFactory = new ObjectFactory();

    private Collection<InfoSnippet> snippets;

    private Map<InfoSnippet, Integer> snippetsZOrders;

    private Properties settings;

    private JDesktopPane dashBoardPanel;

    private JToolBar toolBar;

    private JComboBox addCB;

    private JButton configButt;

    private static JAXBContext getContext() throws JAXBException {
        if (context == null) {
            context = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
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

    public DashBoard(UUID id, byte[] data, byte[] settings) {
        super(id, data, settings);
        setLayout(new BorderLayout());
        add(getToolBar(), BorderLayout.SOUTH);
        add(getDashBoardPanel(), BorderLayout.CENTER);
        initialize();
    }

    private void initialize() {
        settings = PropertiesUtils.deserializeProperties(getSettings());
        // TODO [P1] snippet's location is not fully correctly restored (due to differences between stored and loaded root pane size values)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    if (getData() != null && getData().length != 0) {
                        Frames frames = (Frames) getUnmarshaller().unmarshal(new ByteArrayInputStream(getData()));
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

    /*
     * (non-Javadoc)
     * 
     * @see bias.extension.EntryExtension#serializeData()
     */
    @Override
    public byte[] serializeData() throws Throwable {
        Frames frames = objFactory.createFrames();
        for (InfoSnippet f : getSnippets()) {
            Frame frame = new Frame();
            frame.setX(f.getLocation().getX() / getDashBoardPanel().getSize().getWidth());
            frame.setY(f.getLocation().getY() / getDashBoardPanel().getSize().getHeight());
            frame.setW(f.getSize().getWidth() / getDashBoardPanel().getSize().getWidth());
            frame.setH(f.getSize().getHeight() / getDashBoardPanel().getSize().getHeight());
            frame.setContent(f.serializeContent());
            frame.setSettings(f.serializeSettings());
            frame.setTitle(f.getTitle());
            frame.setType(FrameType.fromValue(f.getName()));
            frame.setZ(getDashBoardPanel().getComponentZOrder(f));
            frames.getFrame().add(frame);
        }
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

    private JToolBar getToolBar() {
        if (toolBar == null) {
            toolBar = new JToolBar();
            toolBar.setFloatable(false);
            toolBar.add(getAddComboBox());
            toolBar.add(getConfigButton());
        }
        return toolBar;
    }

    private JButton getConfigButton() {
        if (configButt == null) {
            configButt = new JButton(FrontEnd.getGUIIcons().getIconConfigure());
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
            addCB.addItem("Add...");
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

    private static class CustomDesktopManager extends DefaultDesktopManager {
        private static final long serialVersionUID = 1L;

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
    }

    private JDesktopPane getDashBoardPanel() {
        if (dashBoardPanel == null) {
            dashBoardPanel = new JDesktopPane();
            dashBoardPanel.setDesktopManager(new CustomDesktopManager());
            dashBoardPanel.setBackground(Color.LIGHT_GRAY);
            addComponentListener(new ComponentAdapter(){
                /* (non-Javadoc)
                 * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
                 */
                @Override
                public void componentResized(ComponentEvent e) {
                    for (InfoSnippet f : getSnippets()) {
                        relocate(f);
                    }
                    super.componentResized(e);
                }
            });
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
        double y = f.getLocation().getY();
        if (f.getLocation().getY() + f.getSize().getHeight() > getDashBoardPanel().getSize().getHeight()) {
            y = getDashBoardPanel().getSize().getHeight() - f.getSize().getHeight();
            relocate = true;
        }
        if (relocate) f.setLocation((int) x, (int) y);
    }

    private void addFrame(Frame frame, boolean isNew) {
        InfoSnippet f = createInternalFrame(frame);
        getDashBoardPanel().add(f);
        relocate(f);
        int zOrder = isNew ? 0 : frame.getZ();
        if (isNew) {
            try {
                getDashBoardPanel().setComponentZOrder(f, zOrder);
                updateZOrders();
                f.setSelected(true);
            } catch (IllegalArgumentException iae) {
                // ignore
            } catch (PropertyVetoException e) {
                // ignore
            }
        }
        getSnippetsZOrders().put(f, zOrder);
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
        final InfoSnippet f;
        byte[] content = frame.getContent();
        if (content == null)
            content = new byte[] {};
        byte[] settings = frame.getSettings();
        if (settings == null)
            settings = new byte[] {};
        switch (frame.getType()) {
        case HTML_SNIPPET:
            f = new HTMLSnippet(getId(), content, settings);
            break;
        case TEXT_SNIPPET:
            f = new TextSnippet(getId(), content, settings);
            break;
        default:
            f = null;
        }
        if (f != null) {
            f.setName(frame.getType().value());
            f.setTitle(frame.getTitle());
            Dimension minSize = f.getMinimumSize();
            if (frame.getContent() != null) {
                int wpxValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getDashBoardPanel().getSize().getWidth() * frame.getX())));
                int wpyValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getDashBoardPanel().getSize().getHeight() * frame.getY())));
                int wwValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getDashBoardPanel().getSize().getWidth() * frame.getW())));
                if (wwValue < minSize.width)
                    wwValue = minSize.width;
                int whValue = Math.round(Float.valueOf(Constants.EMPTY_STR + (getDashBoardPanel().getSize().getHeight() * frame.getH())));
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
                    f.cleanUpUnUsedAttachments();
                }
            });
            ((BasicInternalFrameUI) f.getUI()).getNorthPane().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!e.isPopupTrigger() && e.getClickCount() == 2) {
                        String title = JOptionPane.showInputDialog(DashBoard.this, "Snippet title", f.getTitle());
                        if (title != null) {
                            f.setTitle(title);
                        }
                    }
                }
            });
            getSnippets().add(f);
        }
        return f;
    }

}
