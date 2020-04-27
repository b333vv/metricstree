package org.b333vv.metric.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.b333vv.metric.MetricsTest;
import org.b333vv.metric.ui.log.MetricsConsole;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class CleanConsoleActionTest extends MetricsTest {

    @Test
    public void testAction() {
        AnActionEvent event = mock(AnActionEvent.class);
        MetricsConsole console = mock(MetricsConsole.class);

        when(event.getProject()).thenReturn(project);
        super.register(project, MetricsConsole.class, console);

        CleanConsoleAction clean = new CleanConsoleAction(null, null, null);

        clean.actionPerformed(event);
//        verify(console).clear();
    }

    @Test
    public void testNoOpIfNoProject() {
        AnActionEvent event = mock(AnActionEvent.class);
        CleanConsoleAction clean = new CleanConsoleAction(null, null, null);
        clean.actionPerformed(event);
    }
}
