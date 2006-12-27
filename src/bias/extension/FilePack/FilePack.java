/**
 * Dec 26, 2006
 */
package bias.extension.FilePack;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.UUID;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.filechooser.FileFilter;

import bias.core.Attachment;
import bias.core.BackEnd;
import bias.extension.Extension;
import bias.extension.PlainText.PlainText;
import bias.gui.FrontEnd;
import bias.utils.FSUtils;

/**
 * @author kion
 *
 */

@Extension.Annotation(
        name = "File Pack", 
        version="0.1",
        description = "Simple file pack",
        author="kion")
public class FilePack extends Extension {

	private static final long serialVersionUID = 1L;
	
    private static final ImageIcon ICON_ADD = 
        new ImageIcon(PlainText.class.getResource("/bias/res/FilePack/add.png"));
    
    private static final ImageIcon ICON_DELETE = 
        new ImageIcon(PlainText.class.getResource("/bias/res/FilePack/delete.png"));
    
    private static final ImageIcon ICON_SAVE = 
        new ImageIcon(PlainText.class.getResource("/bias/res/FilePack/save.png"));
    
	private Collection<Attachment> filePack;
	
	private JToolBar jToolBar1;
	private JButton jButton2;
	private JPanel jPanel1;
	private JList jList1;
	private JButton jButton3;
	private JButton jButton1;

	public FilePack(UUID id, byte[] data) {
		super(id, data);
		filePack = BackEnd.getInstance().getAttachments(id);
		initGUI();
	}

	/* (non-Javadoc)
	 * @see bias.extension.Extension#serialize()
	 */
	@Override
	public byte[] serialize() throws Exception {
		return new byte[]{};
	}
	
	private void initGUI() {
		try {
			{
				BorderLayout thisLayout = new BorderLayout();
				this.setLayout(thisLayout);
				this.setPreferredSize(new java.awt.Dimension(743, 453));
				{
					jToolBar1 = new JToolBar();
					jToolBar1.setFloatable(false);
					this.add(jToolBar1, BorderLayout.SOUTH);
					jToolBar1.setPreferredSize(new java.awt.Dimension(743, 26));
					{
						jButton1 = new JButton();
						jToolBar1.add(jButton1);
						jButton1.setIcon(ICON_ADD);
						jButton1.setToolTipText("add file to pack");
						jButton1.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent evt) {
								jButton1ActionPerformed(evt);
							}
						});
					}
					{
						jButton2 = new JButton();
						jToolBar1.add(jButton2);
						jButton2.setIcon(ICON_DELETE);
						jButton2.setToolTipText("delete file from pack");
						jButton2.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent evt) {
								jButton2ActionPerformed(evt);
							}
						});
					}
					{
						jButton3 = new JButton();
						jToolBar1.add(jButton3);
						jButton3.setIcon(ICON_SAVE);
						jButton3.setToolTipText("save file to external target");
						jButton3.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent evt) {
								jButton3ActionPerformed(evt);
							}
						});
					}
				}
				{
					jPanel1 = new JPanel();
					BorderLayout jPanel1Layout = new BorderLayout();
					jPanel1.setLayout(jPanel1Layout);
					this.add(jPanel1, BorderLayout.CENTER);
					{
						ListModel jList1Model = new DefaultComboBoxModel();
						jList1 = new JList();
						jPanel1.add(new JScrollPane(jList1), BorderLayout.CENTER);
						jList1.setModel(jList1Model);
					}
				}
				refreshView();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void jButton1ActionPerformed(ActionEvent evt) {
		JFileChooser jfc = new JFileChooser();
		jfc.setMultiSelectionEnabled(true);
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jfc.setFileFilter(new FileFilter(){

			@Override
			public boolean accept(File f) {
				return true;
			}

			@Override
			public String getDescription() {
				return "Any file";
			}
			
		});
		if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File[] files = jfc.getSelectedFiles();
			try {
				for (File file : files) {
					Attachment attachment = new Attachment(file);
					BackEnd.getInstance().addAttachment(getId(), attachment);
					filePack.add(attachment);
				}
				refreshView();
			} catch (Exception e) {
				FrontEnd.getInstance().displayErrorMessage(e);
				refreshView();
			}
		}
	}
	
	private void jButton2ActionPerformed(ActionEvent evt) {
		if (jList1.getSelectedIndices().length > 0) {
			for (Object fileName : jList1.getSelectedValues()) {
				BackEnd.getInstance().removeAttachment(getId(), (String) fileName);
				removeFile((String) fileName);
			}
			refreshView();
		}
	}
	
	private void jButton3ActionPerformed(ActionEvent evt) {
		if (jList1.getSelectedIndices().length > 0) {
			for (Object fileName : jList1.getSelectedValues()) {
				JFileChooser jfc = new JFileChooser();
				jfc.setMultiSelectionEnabled(false);
				File file = new File((String) fileName); 
				jfc.setSelectedFile(file);
				if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
					file = jfc.getSelectedFile();
					byte[] data = getFileData((String) fileName);
					try {
						FSUtils.getInstance().writeFile(file, data);
					} catch (Exception e) {
						FrontEnd.getInstance().displayErrorMessage(e);
						refreshView();
					}
				}
			}
		}
	}
	
	private void refreshView() {
		DefaultComboBoxModel model = (DefaultComboBoxModel) jList1.getModel();
		model.removeAllElements();
		for (Attachment att : filePack) {
			model.addElement(att.getName());
		}
	}
	
	private byte[] getFileData(String fileName) {
		byte[] data = null;
		for (Attachment att : filePack) {
			if (att.getName().equals(fileName)) {
				data = att.getData();
				break;
			}
		}
		return data;
	}

	private void removeFile(String fileName) {
		Attachment toRemove = null;
		for (Attachment att : filePack) {
			if (att.getName().equals(fileName)) {
				toRemove = att;
				break;
			}
		}
		if (toRemove != null) {
			filePack.remove(toRemove);
		}
	}

}
