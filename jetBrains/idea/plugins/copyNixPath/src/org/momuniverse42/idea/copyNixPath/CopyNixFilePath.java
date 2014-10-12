package org.momuniverse42.idea.copyNixPath;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.util.regex.Pattern;

import static com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT;
import static com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE;


//import com.intellij.openapi.diagnostic.Logger;

/**
 * Ancestor for specific path element copiers, does 99% of the work.
 * @author mubergens Michael Bergens
 */
public class CopyNixFilePath extends AnAction {

    //private static final Logger L = Logger.getInstance("#" + CopyNixPath.class.getName());

    /**
     * Patterns are thread-safe.
     */
    public static final Pattern BACKSLASH = Pattern.compile("\\\\");

    /**
     * Idea API hook.
     */
    public void actionPerformed(@NotNull final AnActionEvent event) {
        final VirtualFile file = event.getData(VIRTUAL_FILE);
        if (file == null) {
            showPopup(event, "<p><b>No active file</b></p>");
            return;
        }

        final String path = file.getCanonicalPath();
        if (path == null) {
            showPopup(event, "<p><b>No path for the current file</b></p>");
            return;
        }

        CopyPasteManager.getInstance().setContents(new StringSelection(
            BACKSLASH.matcher(adjust(file.getCanonicalPath())).replaceAll("/")
        ));
    }

    /**
     * Pass-through, no changes made, subclasses can change this.
     */
    @NotNull protected String adjust(final @NotNull String path) {
        return path;
    }

    private void showPopup(@NotNull final AnActionEvent event, @NotNull final String html) {

        final StatusBar statusBar = WindowManager.getInstance().getStatusBar(PROJECT.getData(event.getDataContext()));
        if (statusBar == null) return;

        JBPopupFactory.getInstance()
            .createHtmlTextBalloonBuilder(html, MessageType.INFO, null)
            .setFadeoutTime(2500)
            .createBalloon()
            .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
    }

}
