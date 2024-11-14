package org.b333vv.metric.ui.hints;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "MetricsTreeCodeVisionSettings",
        storages = @Storage("metricsTreeCodeVisionSettings.xml")
)
public final class MetricsTreeCodeVisionSettings implements PersistentStateComponent<MetricsTreeCodeVisionSettings> {

    private boolean enabled = true;

    public static MetricsTreeCodeVisionSettings getInstance() {
        return ApplicationManager.getApplication().getService(MetricsTreeCodeVisionSettings.class);
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Nullable
    @Override
    public MetricsTreeCodeVisionSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull MetricsTreeCodeVisionSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
