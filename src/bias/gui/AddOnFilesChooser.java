/**
 * Created on Nov 2, 2007
 */
package bias.gui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import bias.Constants;

/**
 * @author kion
 */
public class AddOnFilesChooser extends JFileChooser {
    private static final long serialVersionUID = 1L;

    public AddOnFilesChooser() {
        super();
        setMultiSelectionEnabled(true);
        setFileSelectionMode(JFileChooser.FILES_ONLY);
        setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || (file.isFile() && file.getName().matches(Constants.JAR_FILE_PATTERN));
            }
            @Override
            public String getDescription() {
                return Constants.JAR_FILE_PATTERN_DESCRIPTION;
            }
        });
    }

}
