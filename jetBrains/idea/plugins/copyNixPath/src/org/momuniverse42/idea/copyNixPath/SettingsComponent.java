package org.momuniverse42.idea.copyNixPath;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author mubergens Michael Bergens
 */
@State(name = SettingsComponent.COMPONENT_NAME, storages = {@Storage(id = "other", file = "$PROJECT_FILE$")})
public class SettingsComponent  implements ProjectComponent, Configurable, PersistentStateComponent<Settings> {
    public static final String COMPONENT_NAME = "Michael.U.Bergens.CopyNixDirPlugin";
    public static final String GRP_DISPLAY_ID_ERR = "Copy *nix Paths plugin ERROR";
    public static final String CONFIGURATION_LOCATION;
    static {
        CONFIGURATION_LOCATION = System.getProperty("user.home");
        //+"/.IntelliJIdea70/config/inspection";
      }
    @NotNull private Project project;
    @Nullable SettingsForm form;
    @NotNull Settings settings;
    @NotNull Notifier notifier;

    public SettingsComponent(@NotNull Project project) {
        this.project = project;
        notifier = new Notifier(project);
        settings = new Settings();
        settings.setSearch("");
        settings.setReplaceWith("");
        settings.setTryString("C:\\dir\\blah\\meh\\file.ext");
    }

    @Override public void initComponent() {

    }

    @Override public void disposeComponent() {

    }

    @Nls @Override public String getDisplayName() {
        return "Copy *nix paths to the clipboard";
    }

    @Nullable @Override public String getHelpTopic() {
        return null;
    }

    @Nullable @Override public Settings getState() {
        return settings.clone();
    }

    @Override public void loadState(Settings state) {
        XmlSerializerUtil.copyBean(state, settings);
    }

    @Override public void projectOpened() {

    }

    @Override public void projectClosed() {

    }

    @NotNull @Override public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Nullable @Override public JComponent createComponent() {
        if(form == null) {
           form = new SettingsForm(project);
        }
        return form.getRootComponent();
    }

    @Override public boolean isModified() {
        return form != null && form.isModified(settings);
    }

    @Override public void apply() throws ConfigurationException {
        if (form != null) {
            try {
                form.exportTo(settings);
            }
            catch (Exception x) {
                notifier.showDialogError("You better come back and adjust this, you got an error here:\n\n" + x.getMessage());
            }
        }

    }

    @Override public void reset() {
        if (form != null) {
            try {
                form.importFrom(settings);
            }
            catch(Exception x) {
                notifier.showDialogError("Error importing settings:\n\n" + x.getMessage());
            }
        }
    }

    @Override public void disposeUIResources() {
        form = null;
    }

    public static SettingsComponent getInstance(Project project) {
        return project.getComponent(SettingsComponent.class);
    }
}
