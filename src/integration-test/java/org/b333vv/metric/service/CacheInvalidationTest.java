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
        Document document = myFixture.getEditor().getDocument();

        // Кладем значение в кэш
        cacheService.putUserData(DUMMY_KEY, "testValue");
        assertEquals("testValue", cacheService.getUserData(DUMMY_KEY));

        // Модифицируем содержимое файла
        ApplicationManager.getApplication().runWriteAction(() -> document.setText("class B {}"));
        FileDocumentManager.getInstance().saveAllDocuments();

        // Ждем срабатывания асинхронного слушателя VFS
        UIUtil.dispatchAllInvocationEvents();
        try {
            Thread.sleep(200); // небольшой запас для асинхронности
        } catch (InterruptedException ignored) {}

        // Проверяем, что кэш был сброшен
        assertNull(cacheService.getUserData(DUMMY_KEY));
    }
}

