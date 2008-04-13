/**
 * Created on Apr 13, 2008
 */
package bias.extension.DashBoard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.JToolBar;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import bias.Constants;
import bias.extension.EntryExtension;
import bias.extension.DashBoard.frame.InternalFrame;
import bias.extension.DashBoard.frame.HTMLPageFrame.HTMLPageFrame;
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
    
    // TODO [P2] add more different note-types (image, link etc. - some can be took right from clipboard)
    
    private static final String SCHEMA_LOCATION = "http://bias.sourceforge.net/addons/DashBoardSchema.xsd";

    private static JAXBContext context;

    private static Unmarshaller unmarshaller;

    private static Marshaller marshaller;

    private static ObjectFactory objFactory = new ObjectFactory();
    
    private Collection<InternalFrame> frames;
    
    private Map<InternalFrame, Integer> framesZOrders;
    
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
    
    private Collection<InternalFrame> getFrames() {
        if (frames == null) {
            frames = new LinkedList<InternalFrame>();
        }
        return frames;
    }
    
    private Map<InternalFrame, Integer> getFramesZOrders() {
        if (framesZOrders == null) {
            framesZOrders = new HashMap<InternalFrame, Integer>();
        }
        return framesZOrders;
    }
    
    /* (non-Javadoc)
     * @see bias.extension.EntryExtension#serializeData()
     */
    @Override
    public byte[] serializeData() throws Throwable {
        Frames frames = objFactory.createFrames();
        for (InternalFrame f : getFrames()) {
            Frame frame = new Frame();
            frame.setX(f.getX());
            frame.setY(f.getY());
            frame.setW(f.getWidth());
            frame.setH(f.getHeight());
            frame.setContent(f.serializeContent());
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
        InternalFrame f = createInternalFrame(frame);
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
        for (InternalFrame f : getFramesZOrders().keySet()) {
            Integer zo = getFramesZOrders().get(f);
            zo++;
            getFramesZOrders().put(f, zo);
        }
    }
    
    private void restoreZOrders() {
        for (Entry<InternalFrame, Integer> entry : getFramesZOrders().entrySet()) {
            try {
                getDashBoardPanel().setComponentZOrder(entry.getKey(), entry.getValue());
            } catch (IllegalArgumentException iae) {
                // ignore
            }
        }
    }
    
    private InternalFrame createInternalFrame(Frame frame) {
        final InternalFrame f;
        String content = frame.getContent();
        if (content == null) content = Constants.EMPTY_STR;
        switch (frame.getType()) {
        case HTML_SNIPPET:
            f = new HTMLPageFrame(getId(), content);
            f.setName(frame.getType().value());
            break;
        default: f = null;
        }
        if (f != null) {
            Dimension minSize = new Dimension(260, 120);
            f.setMinimumSize(minSize);
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
            getFrames().add(f);
        }
        return f;
    }
    
}
