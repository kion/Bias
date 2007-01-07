/**
 * Dec 26, 2006
 */
package bias.extension.FilePack;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import bias.annotation.AddOnAnnotation;
import bias.core.Attachment;
import bias.core.BackEnd;
import bias.extension.Extension;
import bias.gui.FrontEnd;
import bias.utils.FSUtils;


/**
 * @author kion
 *
 */

@AddOnAnnotation(
        version="0.1.2",
        author="kion",
        description = "Simple file pack")
public class FilePack extends Extension {

	private static final long serialVersionUID = 1L;
	
    private static final ImageIcon ICON_ADD = 
        new ImageIcon(FilePack.class.getResource("/bias/res/FilePack/add.png"));
    
    private static final ImageIcon ICON_DELETE = 
        new ImageIcon(FilePack.class.getResource("/bias/res/FilePack/delete.png"));
    
    private static final ImageIcon ICON_SAVE = 
        new ImageIcon(FilePack.class.getResource("/bias/res/FilePack/save.png"));
    
	private Map<Attachment, String> filePack;
    
    private File lastInputDir = null;
	
    private File lastOutputDir = null;
    
	private JToolBar jToolBar1;
	private JButton jButton2;
	private JPanel jPanel1;
	private JTable jTable1;
	private JScrollPane jScrollPane1;
	private JButton jButton3;
	private JButton jButton1;

	public FilePack(UUID id, byte[] data, byte[] settings) {
		super(id, data, settings);
        Properties p = new Properties();
        if (getData() != null) {
            try {
                p.load(new ByteArrayInputStream(getData()));
            } catch (IOException e) {}
        }
        filePack = new LinkedHashMap<Attachment, String>();
		for (Attachment att : BackEnd.getInstance().getAttachments(getId())) {
		    String date = p.getProperty(att.getName());
            filePack.put(att, date);
        }
		initGUI();
	}

	/* (non-Javadoc)
	 * @see bias.extension.Extension#serializeData()
	 */
	@Override
	public byte[] serializeData() throws Throwable {
        Properties p = new Properties();
        for (Entry<Attachment, String> fpEntry : filePack.entrySet()) {
            p.setProperty(fpEntry.getKey().getName(), fpEntry.getValue());
        }
        StringWriter sw = new StringWriter();
        p.list(new PrintWriter(sw));
		return sw.getBuffer().toString().getBytes();
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
								addFileAction(evt);
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
								removeFileAction(evt);
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
								saveFileAction(evt);
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
						jScrollPane1 = new JScrollPane();
						jPanel1.add(jScrollPane1, BorderLayout.CENTER);
						{
							TableModel jTable1Model = new DefaultTableModel();
							jTable1 = new JTable();
							jScrollPane1.setViewportView(jTable1);
							jTable1.setModel(jTable1Model);
						}
					}
				}
				for (Entry<Attachment, String> fpEntry : filePack.entrySet()) {
					addRow(fpEntry.getKey(), fpEntry.getValue());
				}
			}
		} catch (Exception e) {
			FrontEnd.getInstance().displayErrorMessage(e);
		}
	}
	
	private void addFileAction(ActionEvent evt) {
		try {
            JFileChooser jfc;
            if (lastInputDir != null) {
                jfc = new JFileChooser(lastInputDir);
            } else {
                jfc = new JFileChooser();
            }
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
						addFile(file);
					}
                    lastInputDir = files[0].getParentFile();
				} catch (Exception e) {
					FrontEnd.getInstance().displayErrorMessage(e);
				}
			}
		} catch (Exception e) {
			FrontEnd.getInstance().displayErrorMessage(e);
		}
	}
	
	private void removeFileAction(ActionEvent evt) {
		try {
			if (jTable1.getSelectedRows().length > 0) {
				DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
				while (jTable1.getSelectedRow() != -1) {
					int i = jTable1.getSelectedRow();
					String fileName = (String) model.getValueAt(i, 0);
					BackEnd.getInstance().removeAttachment(getId(), fileName);
					removeFile(fileName, i);
				}
			}
		} catch (Exception e) {
			FrontEnd.getInstance().displayErrorMessage(e);
		}
	}
	
	private void saveFileAction(ActionEvent evt) {
		try {
			if (jTable1.getSelectedRows().length > 0) {
				DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
				for (int i : jTable1.getSelectedRows()) {
                    JFileChooser jfc;
                    if (lastOutputDir != null) {
                        jfc = new JFileChooser(lastOutputDir);
                    } else {
                        jfc = new JFileChooser();
                    }
					jfc.setMultiSelectionEnabled(false);
					String fileName = (String) model.getValueAt(i, 0);
					File file = new File(fileName);
					jfc.setSelectedFile(file);
					if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
						file = jfc.getSelectedFile();
                        Integer option = null;
                        if (file.exists()) {
                            option = JOptionPane.showConfirmDialog(
                                    this, 
                                    "File already exists, overwrite?", 
                                    "Overwrite existing file", 
                                    JOptionPane.YES_NO_OPTION);
                        }
                        if (option == null || option == JOptionPane.YES_OPTION) {
                            byte[] data = getFileData(fileName);
                            FSUtils.writeFile(file, data);
                            lastOutputDir = file.getParentFile();
                        }
					}
				}
			}
		} catch (Exception e) {
			FrontEnd.getInstance().displayErrorMessage(e);
		}
	}
	
	private byte[] getFileData(String fileName) throws Exception {
		byte[] data = null;
		for (Attachment att : filePack.keySet()) {
			if (att.getName().equals(fileName)) {
				data = att.getData();
				break;
			}
		}
		return data;
	}

	private void addFile(File file) throws Exception {
		Attachment attachment = new Attachment(file);
		BackEnd.getInstance().addAttachment(getId(), attachment);
        String date = new SimpleDateFormat("yyyy.MM.dd @ HH:mm:ss").format(new Date());
		filePack.put(attachment, date);
		// update grid
		addRow(attachment, date);
	}
	
	private void addRow(Attachment attachment, String date) {
		DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
		if (model.getColumnCount() == 0) {
			model.addColumn("File");
			model.addColumn("Size");
            model.addColumn("Date");
		}
		String metrics = " B";
		float size = attachment.getData().length;
		if (size >= 1024) {
			size = size/1024;
			metrics = " KB";
		}
		if (size >= 1024) {
			size = size/1024;
			metrics = " MB";
		}
        String sizeStr = "" + size;
        int idx = sizeStr.indexOf(".");
        if (idx != -1) {
            if (sizeStr.charAt(idx + 1) != '0') {
                sizeStr = sizeStr.substring(0, idx + 2);
            } else {
                sizeStr = sizeStr.substring(0, idx);
            }
        }
        sizeStr += metrics;
		model.addRow(new Object[]{attachment.getName(), sizeStr, date});
	}

	private void removeFile(String fileName, int rowIdx) throws Exception {
		Attachment toRemove = null;
		for (Attachment att : filePack.keySet()) {
			if (att.getName().equals(fileName)) {
				toRemove = att;
				break;
			}
		}
		if (toRemove != null) {
			filePack.remove(toRemove);
			// update grid
			removeRow(rowIdx);
		}
	}
	
	private void removeRow(int rowIdx) {
		DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
		model.removeRow(rowIdx);
	}

}
