/**
 * Created on Nov 2, 2007
 */
package bias.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import bias.Constants;

/**
 * @author kion
 */
public class IconsFileChooser extends ImageFileChooser {
    private static final long serialVersionUID = 1L;

    public IconsFileChooser() {
        super(true);
        final FileFilter imgFF = getFileFilter();
        setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return imgFF.accept(f) || f.getName().matches(Constants.JAR_FILE_PATTERN);
            }
            @Override
            public String getDescription() {
                return imgFF.getDescription() + ", " + Constants.JAR_FILE_PATTERN_DESCRIPTION;
            }
        });
    }

}
