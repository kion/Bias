/**
 * Created on Apr 13, 2008
 */
package bias.extension.DashBoard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JToolBar;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import bias.Constants;
import bias.core.Attachment;
import bias.core.BackEnd;
import bias.extension.EntryExtension;
import bias.extension.DashBoard.xmlb.Frame;
import bias.extension.DashBoard.xmlb.Frames;
import bias.extension.DashBoard.xmlb.ObjectFactory;
import bias.gui.FrontEnd;
import bias.utils.PropertiesUtils;

/**
 * @author kion
 */
public class DashBoard extends EntryExtension {
    private static final long serialVersionUID = 1L;
    
    private static final ImageIcon ICON_ADD = new ImageIcon(BackEnd.getInstance().getResourceURL(DashBoard.class, "add.png"));

    private static final String SCHEMA_LOCATION = "http://bias.sourceforge.net/addons/DashBoardSchema.xsd";

    private static JAXBContext context;

    private static Unmarshaller unmarshaller;

    private static Marshaller marshaller;

    private static ObjectFactory objFactory = new ObjectFactory();
    
    private Collection<InternalFrame> frames;
    
    private Properties settings;

    private JDesktopPane dashBoardPanel;

    private JToolBar toolBar;
    
    private JButton addButton;
    
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
                    addFrame(frame);
                }
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
            frame.setContent(f.getEditorPanel().getCode());
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
            toolBar.add(getAddButton());
        }
        return toolBar;
    }
    
    private JButton getAddButton() {
        if (addButton == null) {
            addButton = new JButton(addAction);
            addButton.setText(Constants.EMPTY_STR);
        }
        return addButton;
    }
    
    private JDesktopPane getDashBoardPanel() {
        if (dashBoardPanel == null) {
            dashBoardPanel = new JDesktopPane();
            dashBoardPanel.setBackground(Color.LIGHT_GRAY);
        }
        return dashBoardPanel;
    }
    
    private AddAction addAction = new AddAction();
    private class AddAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public AddAction() {
            putValue(Action.NAME, "addNote");
            putValue(Action.SHORT_DESCRIPTION, "add note");
            putValue(Action.SMALL_ICON, ICON_ADD);
        }
        
        public void actionPerformed(ActionEvent evt) {
            addFrame(null);
        }
    };
    
    private void addFrame(Frame frame) {
        getDashBoardPanel().add(createInternalFrame(frame));
    }
    
    private InternalFrame createInternalFrame(Frame frame) {
        final InternalFrame f;
        if (frame != null) {
            f = new InternalFrame(getId(), frame.getContent());
            f.setLocation(frame.getX(), frame.getY());
            f.setSize(frame.getW(), frame.getH());
        } else {
            f = new InternalFrame(getId(), "<i>content here...</i>");
            f.setLocation(0, 0);
            f.setSize(200, 100);
        }
        f.setVisible(true);
        try {
            f.setSelected(true);
        } catch (PropertyVetoException e1) {
            // ignore
        }
        f.addInternalFrameListener(new InternalFrameAdapter(){
            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                getFrames().remove(f);
                cleanUpUnUsedAttachments(f);
            }
        });
        getFrames().add(f);
        return f;
    }

    private void cleanUpUnUsedAttachments(InternalFrame f) {
        try {
            Collection<String> usedAttachmentNames = f.getEditorPanel().getProcessedAttachmentNames();
            Collection<Attachment> atts = BackEnd.getInstance().getAttachments(getId());
            for (Attachment att : atts) {
                if (!usedAttachmentNames.contains(att.getName())) {
                    BackEnd.getInstance().removeAttachment(getId(), att.getName());
                }
            }
        } catch (Exception ex) {
            // if some error occurred while cleaning up unused attachments,
            // ignore it, these attachments will be removed next time Bias persists data
        }
    }

}
