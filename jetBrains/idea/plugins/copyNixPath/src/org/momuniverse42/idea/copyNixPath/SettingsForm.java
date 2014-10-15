package org.momuniverse42.idea.copyNixPath;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author mubergens Michael Bergens
 */
public class SettingsForm {
    @NotNull private Project project;
    private JLabel lblFrom;
    private JTextField txtFrom;
    private JLabel lblReplace;
    private JTextField txtRepl;
    private JTextField txtTry;
    private JButton btnTry;
    private JLabel lblTryPath;
    private JPanel rootComponent;
    private JLabel lblFullPath;
    private JLabel lblDirPath;
    private JLabel lblTryDir;
    private JLabel lblWarn;
    private Notifier notifier;
    private CopyNixDirPath copyNixDirPath;
    private CopyNixFilePath copyNixFilePath;

    public SettingsForm(@NotNull Project project) {
        this.project = project;
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
                    notifier.showDialogError("You got an error here:\n" + x.getMessage());
                }
            }
        });
        refreshWith(SettingsComponent.getInstance(project).getState());
    }

    public void refreshWith(final Settings s) {
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

    public void exportTo(final Settings settings) {
        settings.setSearch(txtFrom.getText());
        settings.setReplaceWith(txtRepl.getText());
        settings.setTryString(txtTry.getText());
    }

    public boolean isModified(final Settings settings) {
        return txtFrom.getText() == null || !txtFrom.getText().equals(settings.getSearch()) ||
            txtRepl.getText() == null || !txtRepl.getText().equals(settings.getReplaceWith())
            || txtTry.getText() == null || !txtTry.getText().equals(settings.getTryString());
    }
}
