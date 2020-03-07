package org.b333vv.metric;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.extensions.ExtensionsArea;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.net.ssl.CertificateManager;
import org.junit.After;
import org.junit.Before;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Modifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public abstract class MetricsTest {
  protected Project project = createProject();
  protected Module module = createModule();
  protected VirtualFile root;
  protected Application app = mock(Application.class);

  @Before
  public final void setUp() {
    ApplicationManager.setApplication(app, mock(Disposable.class));
    when(app.isUnitTestMode()).thenReturn(true);
    when(app.isHeadlessEnvironment()).thenReturn(true);
    when(app.acquireReadActionLock()).thenReturn(mock(AccessToken.class));
    Answer<Void> runArg = invocation -> {
      ((ThrowableComputable) invocation.getArgument(0)).compute();
      return null;
    };

    doAnswer(runArg).when(app).runReadAction(any(ThrowableComputable.class));
    register(app, CertificateManager.class, new CertificateManager());
    createModuleRoot();
  }

  @After
  public final void tearDown() {
    project = null;
    module = null;
  }

  private Project createProject() {
    Project project = mock(Project.class);
    when(project.isDisposed()).thenReturn(false);

    return project;
  }

  private void createModuleRoot() {
    ModuleRootManager moduleRootManager = mock(ModuleRootManager.class);
    root = mock(VirtualFile.class);
    when(root.getCanonicalPath()).thenReturn("/src");
    when(root.getPath()).thenReturn("/src");
    VirtualFile[] roots = {root};
    when(moduleRootManager.getContentRoots()).thenReturn(roots);
    register(module, ModuleRootManager.class, moduleRootManager);
  }

  protected Module createModule() {
    Module m = mock(Module.class);
    when(m.getName()).thenReturn("testModule");
    when(m.getProject()).thenReturn(project);
    return m;
  }

  protected Project getProject() {
    return project;
  }

  protected <T> T register(Class<T> clazz) {
    T t = mock(clazz);
    register(clazz, t);
    return t;
  }

  protected <T> T register(ComponentManager comp, Class<T> clazz) {
    T t = mock(clazz);
    register(comp, clazz, t);
    return t;
  }

  protected void register(Class<?> clazz, Object instance) {
    register(project, clazz, instance);
  }

  protected void register(ComponentManager comp, Class<?> clazz, Object instance) {
    doReturn(instance).when(comp).getComponent(clazz);
  }

  protected <T> void registerEP(final ExtensionPointName<T> extensionPointName, final Class<T> clazz) {
    ExtensionsArea area = Extensions.getRootArea();
    final String name = extensionPointName.getName();
    if (!area.hasExtensionPoint(name)) {
      ExtensionPoint.Kind kind = clazz.isInterface() || (clazz.getModifiers() & Modifier.ABSTRACT) != 0 ? ExtensionPoint.Kind.INTERFACE : ExtensionPoint.Kind.BEAN_CLASS;
      area.registerExtensionPoint(name, clazz.getName(), kind);
    }
  }
}
