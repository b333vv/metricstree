/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b333vv.metric.service;

import com.intellij.openapi.Disposable;
 import com.intellij.openapi.components.Service;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.Key;
 import com.intellij.openapi.util.UserDataHolder;
 import com.intellij.openapi.util.UserDataHolderBase;
 import com.intellij.openapi.vfs.AsyncFileListener;
 import com.intellij.openapi.vfs.AsyncFileListener.ChangeApplier;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.openapi.vfs.VirtualFileManager;
 import com.intellij.openapi.vfs.newvfs.events.VFileCopyEvent;
 import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
 import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
 import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
 import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
 import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;
 import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
 import org.b333vv.metric.builder.DependenciesBuilder;
 import org.b333vv.metric.model.code.JavaFile;
 import org.b333vv.metric.model.code.JavaProject;
 import org.b333vv.metric.model.metric.MetricLevel;
 import org.b333vv.metric.model.metric.MetricType;
 import org.b333vv.metric.task.InvalidateCachesTask;
 import org.b333vv.metric.ui.fitnessfunction.FitnessFunction;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;

 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.stream.Stream;

/**
 * Service for managing in-memory caches and VFS-based cache invalidation.
 * This service replaces cache management previously handled by MetricTaskCache.
 */
@Service(Service.Level.PROJECT)
public final class CacheService implements UserDataHolder, Disposable {
    // Cache keys
    public static final Key<DependenciesBuilder> DEPENDENCIES = Key.create("DEPENDENCIES");
    public static final Key<JavaProject> CLASS_AND_METHODS_METRICS = Key.create("CLASS_AND_METHODS_METRICS");
    public static final Key<JavaProject> PACKAGE_METRICS = Key.create("PACKAGE_METRICS");
    public static final Key<JavaProject> PACKAGE_ONLY_METRICS = Key.create("PACKAGE_ONLY_METRICS");
    public static final Key<JavaProject> PROJECT_METRICS = Key.create("PROJECT_METRICS");
    public static final Key<Map<FitnessFunction, Set<org.b333vv.metric.model.code.JavaClass>>> CLASS_LEVEL_FITNESS_FUNCTION = 
            Key.create("CLASS_LEVEL_FITNESS_FUNCTION");
    public static final Key<Map<FitnessFunction, Map<MetricType, Map<String, Integer>>>> PROFILE_CHART_DATA = 
            Key.create("PROFILE_CHART_DATA");
    public static final Key<Map<MetricLevel, Map<MetricType, Map<String, Integer>>>> DISTRIBUTION_CHART_DATA = 
            Key.create("DISTRIBUTION_CHART_DATA");

    private final UserDataHolderBase userData = new UserDataHolderBase();
    private final ConcurrentHashMap<String, JavaFile> javaFiles = new ConcurrentHashMap<>();
    private final Project project;
    private final AsyncFileListener vfsListener;

    public CacheService(Project project) {
        this.project = project;
        this.vfsListener = new MyAsyncVfsListener();
        VirtualFileManager.getInstance().addAsyncFileListener(vfsListener, this);
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        userData.putUserData(key, value);
    }

    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return userData.getUserData(key);
    }

    /**
     * Invalidates all cached user data.
     */
    public void invalidateUserData() {
        // Clear all user data by setting each key to null
        for (Key<?> key : new Key<?>[] {
                DEPENDENCIES, CLASS_AND_METHODS_METRICS, PACKAGE_METRICS,
                PACKAGE_ONLY_METRICS, PROJECT_METRICS, CLASS_LEVEL_FITNESS_FUNCTION,
                PROFILE_CHART_DATA, DISTRIBUTION_CHART_DATA
        }) {
            putUserData(key, null);
        }
    }

    /**
     * Adds a JavaFile to the cache.
     *
     * @param javaFile the JavaFile to add
     */
    public void addJavaFile(VirtualFile virtualFile, JavaFile javaFile) {
        javaFiles.put(virtualFile.getPath(), javaFile);
    }

    /**
     * Removes a JavaFile from the cache based on its VirtualFile.
     *
     * @param virtualFile the VirtualFile of the JavaFile to remove
     */
    public void removeJavaFile(VirtualFile virtualFile) {
        javaFiles.remove(virtualFile.getPath());
    }

    /**
     * Returns a stream of all JavaFiles in the cache.
     *
     * @return a stream of JavaFiles
     */
    public Stream<JavaFile> getJavaFiles() {
        return javaFiles.values().stream();
    }

    @Override
    public void dispose() {
        invalidateUserData();
        javaFiles.clear();
    }

    /**
     * AsyncFileListener implementation that invalidates caches when Java files are modified.
     */
    private class MyAsyncVfsListener implements AsyncFileListener {
        @Override
        public @Nullable ChangeApplier prepareChange(@NotNull List<? extends VFileEvent> events) {
            for (VFileEvent event : events) {
                if (event instanceof VFileContentChangeEvent || 
                        event instanceof VFileDeleteEvent || 
                        event instanceof VFileMoveEvent || 
                        event instanceof VFilePropertyChangeEvent) {
                    VirtualFile file = event.getFile();
                    if (file != null && "java".equals(file.getExtension())) {
                        return new ChangeApplier() {
                            @Override
                            public void afterVfsChange() {
                                project.getService(TaskQueueService.class)
                                        .queue(new InvalidateCachesTask(project, file));
                            }
                        };
                    }
                } else if (event instanceof VFileCopyEvent || event instanceof VFileCreateEvent) {
                    VirtualFile file = event.getFile();
                    if (file != null && "java".equals(file.getExtension())) {
                        return new ChangeApplier() {
                            @Override
                            public void afterVfsChange() {
                                project.getService(TaskQueueService.class)
                                        .queue(new InvalidateCachesTask(project, file));
                            }
                        };
                    }
                }
            }
            return null;
        }
    }
}