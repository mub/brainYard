package org.momuniverse42.idea.copyNixPath;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author mubergens Michael Bergens
 */
public class SettingsForm {
    private JTextField txtFrom;
    private JTextField txtRepl;
    private JTextField txtTry;
    private JButton btnTry;
    private JLabel lblTryPath;
    private JPanel rootComponent;
    private JLabel lblTryDir;
    private Notifier notifier;
    private CopyNixDirPath copyNixDirPath;
    private CopyNixFilePath copyNixFilePath;

    public SettingsForm(@NotNull final Project project) {
        notifier = new Notifier(project);
        copyNixDirPath = new CopyNixDirPath();
        copyNixFilePath = new CopyNixFilePath();
        btnTry.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                try {
                    final Settings s = new Settings();
                    exportTo(s);
                    refreshWith(s);
                }
                catch (Exception x) {
                    notifier.showDialogError("You got an error here:\n\n" + x.getMessage());
                }
            }
        });
        // populate the fields initially at the first showing with whatever we have in the state:
        final SettingsComponent settingsComponent = SettingsComponent.getInstance(project);
        if(settingsComponent != null) refreshWith(settingsComponent.getState());
    }

    /**
     * Set controls from the settings and calculate the sample labels accordingly.
     */
    public void refreshWith(@Nullable final Settings s) {
        if(s == null) return;
        final String v = txtTry.getText();
        lblTryPath.setText(copyNixFilePath.transform(s, v));
        lblTryDir.setText(copyNixDirPath.transform(s, v));
    }

    public JPanel getRootComponent() { return rootComponent; }

    public void importFrom(@NotNull final Settings data) {
        txtFrom.setText(data.getSearch());
        txtRepl.setText(data.getReplaceWith());
        txtTry.setText(data.getTryString());
    }

    public void exportTo(@NotNull final Settings settings) {
        settings.setSearch(txtFrom.getText());
        settings.setReplaceWith(txtRepl.getText());
        settings.setTryString(txtTry.getText());
    }

    public boolean isModified(@NotNull final Settings settings) {
        return txtFrom.getText() == null || !txtFrom.getText().equals(settings.getSearch()) ||
            txtRepl.getText() == null || !txtRepl.getText().equals(settings.getReplaceWith())
            || txtTry.getText() == null || !txtTry.getText().equals(settings.getTryString());
    }
}
