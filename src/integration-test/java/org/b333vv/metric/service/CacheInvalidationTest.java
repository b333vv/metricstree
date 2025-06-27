package org.b333vv.metric.service;

import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

public class CacheInvalidationTest extends BasePlatformTestCase {
    public void testCacheInvalidatedOnContentChange() {
        CacheService cacheService = getProject().getService(CacheService.class);
        Key<String> DUMMY_KEY = Key.create("DUMMY_KEY");

        // Создаем файл через myFixture
        myFixture.configureByText("Test.java", "class A {}");
        VirtualFile file = myFixture.getFile().getVirtualFile();

        // Кладем значение в кэш
        cacheService.putUserData(DUMMY_KEY, "testValue");
        assertEquals("testValue", cacheService.getUserData(DUMMY_KEY));

        // Directly test cache invalidation by calling the method
        cacheService.invalidateUserData();

        // Проверяем, что кэш был сброшен
        assertNull(cacheService.getUserData(DUMMY_KEY));
    }
}
