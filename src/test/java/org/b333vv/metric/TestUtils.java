package org.b333vv.metric;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.components.impl.ComponentManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;

import javax.annotation.Nullable;
import java.awt.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {
    static {
        System.out.println("headless mode: " + GraphicsEnvironment.isHeadless());
    }

    private TestUtils() {
    }

    public static <T> T mockInContainer(Class<T> clazz, Project project) {
        T mocked = mock(clazz);
        ComponentManagerImpl compManager = (ComponentManagerImpl) project;
        compManager.registerComponentInstance(mocked, null);
        return mocked;
    }

    public static MessageBusConnection mockMessageBus(ComponentManager mockedComponentManager) {
        MessageBusConnection connection = mock(MessageBusConnection.class);
        MessageBus bus = mock(MessageBus.class);
        when(bus.connect(mockedComponentManager)).thenReturn(connection);
        when(bus.connect()).thenReturn(connection);
        when(mockedComponentManager.getMessageBus()).thenReturn(bus);
        return connection;
    }

    public static AnActionEvent createAnActionEvent(@Nullable Project project) {
        AnActionEvent event = mock(AnActionEvent.class);
        when(event.getProject()).thenReturn(project);
        return event;
    }
}
