package org.alfresco.maven.mmt.archiver;

import java.io.File;
import org.codehaus.plexus.archiver.jar.JarArchiver;

public class AmpArchiver extends JarArchiver {

    public AmpArchiver() {
        super.archiveType = "amp";
    }

    /**
     * @see org.codehaus.plexus.archiver.AbstractArchiver#addDirectory(java.io.File, String, String[], String[])
     */
    public void addDirectory(final File directory, final String prefix, final String[] includes,
                              final String[] excludes ) {
        getLogger().info("Adding directory [ '"+directory+"' '"+prefix+"']");
        super.addDirectory(directory, prefix, includes, excludes);
    }
}
