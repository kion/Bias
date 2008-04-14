/**
 * Created on Apr 13, 2008
 */
package bias.extension.DashBoard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;

import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import bias.extension.EntryExtension;
import bias.extension.DashBoard.snippet.HTMLSnippet;
import bias.extension.DashBoard.snippet.InfoSnippet;
import bias.extension.DashBoard.xmlb.Frame;
import bias.extension.DashBoard.xmlb.FrameType;
import bias.extension.DashBoard.xmlb.Frames;
import bias.extension.DashBoard.xmlb.ObjectFactory;
import bias.gui.FrontEnd;
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
    
    private Collection<InfoSnippet> frames;
    
    private Map<InfoSnippet, Integer> framesZOrders;
    
    private Properties settings;

    private JDesktopPane dashBoardPanel;

    private JToolBar toolBar;
    
    private JComboBox addCB;
    
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
        try {
            if (getData() != null && getData().length != 0) {
                Frames frames = (Frames) getUnmarshaller().unmarshal(new ByteArrayInputStream(getData()));
                for (Frame frame : frames.getFrame()) {
                    addFrame(frame, false);
                }
                restoreZOrders();
            }
        } catch (JAXBException e) {
            FrontEnd.displayErrorMessage("Failed to initialize!", e);
        }
        settings = PropertiesUtils.deserializeProperties(getSettings());
    }
    
    private Collection<InfoSnippet> getFrames() {
        if (frames == null) {
            frames = new LinkedList<InfoSnippet>();
        }
        return frames;
    }
    
    private Map<InfoSnippet, Integer> getFramesZOrders() {
        if (framesZOrders == null) {
            framesZOrders = new HashMap<InfoSnippet, Integer>();
        }
        return framesZOrders;
    }
    
    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#serializeData()
     */
    @Override
    public byte[] serializeData() throws Throwable {
        Frames frames = objFactory.createFrames();
        for (InfoSnippet f : getFrames()) {
            Frame frame = new Frame();
            frame.setX(f.getX());
            frame.setY(f.getY());
            frame.setW(f.getWidth());
            frame.setH(f.getHeight());
            frame.setContent(f.serializeContent());
            frame.setTitle(f.getTitle());
            frame.setType(FrameType.fromValue(f.getName()));
            frame.setZ(getDashBoardPanel().getComponentZOrder(f));
            frames.getFrame().add(frame);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        getMarshaller().marshal(frames, baos);
        return baos.toByteArray();
    }
    
    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#serializeSettings()
     */
    @Override
    public byte[] serializeSettings() throws Throwable {
        return PropertiesUtils.serializeProperties(settings);
    }
    
    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#getSearchData()
     */
    @Override
    public Collection<String> getSearchData() throws Throwable {
        // TODO [P1] implement
        return super.getSearchData();
    }

    private JToolBar getToolBar() {
        if (toolBar == null) {
            toolBar = new JToolBar();
            toolBar.setFloatable(false);
            toolBar.add(getAddComboBox());
        }
        return toolBar;
    }
    
    private JComboBox getAddComboBox() {
        if (addCB == null) {
            addCB = new JComboBox();
            addCB.addItem("Add...");
            for (FrameType ft : FrameType.values()) {
                addCB.addItem(ft.value());
            }
            addCB.addItemListener(new ItemListener(){
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
    
    private JDesktopPane getDashBoardPanel() {
        if (dashBoardPanel == null) {
            dashBoardPanel = new JDesktopPane();
            dashBoardPanel.setBackground(Color.LIGHT_GRAY);
        }
        return dashBoardPanel;
    }
    
    private void addFrame(Frame frame, boolean isNew) {
        InfoSnippet f = createInternalFrame(frame);
        getDashBoardPanel().add(f);
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
        getFramesZOrders().put(f, zOrder);
    }
    
    private void updateZOrders() {
        for (InfoSnippet f : getFramesZOrders().keySet()) {
            Integer zo = getFramesZOrders().get(f);
            zo++;
            getFramesZOrders().put(f, zo);
        }
    }
    
    private void restoreZOrders() {
        for (Entry<InfoSnippet, Integer> entry : getFramesZOrders().entrySet()) {
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
        if (content == null) content = new byte[]{};
        switch (frame.getType()) {
        case HTML_SNIPPET:
            f = new HTMLSnippet(getId(), content);
            f.setName(frame.getType().value());
            break;
        default: f = null;
        }
        if (f != null) {
            f.setTitle(frame.getTitle());
            Dimension minSize = f.getMinimumSize();
            if (frame.getContent() != null) {
                f.setLocation(frame.getX(), frame.getY());
                f.setSize(frame.getW(), frame.getH());
            } else {
                f.setLocation(0, 0);
                f.setSize(minSize);
            }
            f.setVisible(true);
            f.addInternalFrameListener(new InternalFrameAdapter(){
                @Override
                public void internalFrameClosed(InternalFrameEvent e) {
                    getFrames().remove(f);
                    f.cleanUpUnUsedAttachments();
                }
            });
            ((BasicInternalFrameUI) f.getUI()).getNorthPane().addMouseListener(new MouseAdapter(){
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
            getFrames().add(f);
        }
        return f;
    }
    
}
