package org.momuniverse42.idea.copyNixPath;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author mubergens Michael Bergens
 */
public class CopyNixDir extends CopyNixAction {

    /**
     * Snips off the filename, returns the file's directory only.
     */
    @Override @NotNull public String adjust(final @NotNull String path) {
        final File f = new File(path);

        return f.isDirectory() ? path : f.getParent();
    }
}
