package org.momuniverse42.idea.copyNixPath;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.ui.Messages.getErrorIcon;
import static com.intellij.openapi.ui.Messages.showMessageDialog;
import static org.momuniverse42.idea.copyNixPath.SettingsComponent.GRP_DISPLAY_ID_ERR;

/**
 * @author mubergens Michael Bergens
 */
@SuppressWarnings("DialogTitleCapitalization") public class Notifier {
    @NotNull private final Project project;

    public Notifier(@NotNull Project project) {
        this.project = project;
    }
/*
    public void showError(final String msg) {
        Notification notification = new Notification(GRP_DISPLAY_ID_ERR, GRP_DISPLAY_ID_ERR, msg, ERROR);
    }

    public void showOk(final String msg) {
        Notification notification = new Notification(GRP_DISPLAY_ID_OK, GRP_DISPLAY_ID_OK, msg, ERROR);
    }
    private void show(final Notification notification) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override public void run() {
                Notifications.Bus.notify(notification, project);
            }
        });
    }

    public void showPopup(@NotNull final String html) {

        final StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar == null) return;

        JBPopupFactory.getInstance()
            .createHtmlTextBalloonBuilder(html, MessageType.INFO, null)
            .setFadeoutTime(2500)
            .createBalloon()
            .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.above);
    }

    public void showDialogOk(@NotNull final String msg) {
        // https://confluence.jetbrains.com/display/IDEADEV/Getting+Started+with+Plugin+Development
        //  String txt= Messages.showInputDialog(project, "What is your name?", "Input your name", Messages.getQuestionIcon());
        showMessageDialog(project, msg, GRP_DISPLAY_ID_OK, getInformationIcon());
    }
*/
    public void showDialogError(@NotNull final String msg) {
        // https://confluence.jetbrains.com/display/IDEADEV/Getting+Started+with+Plugin+Development
        //  String txt= Messages.showInputDialog(project, "What is your name?", "Input your name", Messages.getQuestionIcon());
        showMessageDialog(project, msg, GRP_DISPLAY_ID_ERR, getErrorIcon());
    }
}
