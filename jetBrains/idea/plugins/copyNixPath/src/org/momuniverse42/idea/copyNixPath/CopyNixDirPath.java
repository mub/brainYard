package org.momuniverse42.idea.copyNixPath;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Action handler: copy directory path.
 * @author mubergens Michael Bergens
 */
public class CopyNixDirPath extends CopyNixFilePath {

    /**
     * Snips off the filename, returns the file's directory only.
     * If it is a directory, passes it straight through.
     */
    @Override @NotNull public String adjust(final @NotNull String path) {
        final File f = new File(path);

        return f.isDirectory() ? path : f.getParent();
    }
}
