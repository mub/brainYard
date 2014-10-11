package org.momuniverse42.idea.copyNixPath;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
//import com.intellij.openapi.diagnostic.Logger;
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

/**
 * @author mubergens Michael Bergens
 */
public class CopyNixPath extends AnAction {
    //private static final Logger L = Logger.getInstance("#" + CopyNixPath.class.getName());

    private static final Pattern BACKSLASH = Pattern.compile("\\\\");

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
        final String nixFile = BACKSLASH.matcher(file.getCanonicalPath()).replaceAll("/");
        CopyPasteManager.getInstance().setContents(new StringSelection(nixFile));

    }

    private void showPopup(@NotNull final AnActionEvent event, @NotNull final String html) {

        StatusBar statusBar = WindowManager.getInstance().getStatusBar(PROJECT.getData(event.getDataContext()));
        if (statusBar == null) return;

        JBPopupFactory.getInstance()
            .createHtmlTextBalloonBuilder(html, MessageType.INFO, null)
            .setFadeoutTime(2500)
            .createBalloon()
            .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
    }

}
