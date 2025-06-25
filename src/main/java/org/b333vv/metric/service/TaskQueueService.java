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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;

import java.util.concurrent.ConcurrentLinkedQueue;

@Service(Service.Level.PROJECT)
public final class TaskQueueService {
    private final ConcurrentLinkedQueue<Task.Backgroundable> taskQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean isProcessing = false;

    public void queue(Task.Backgroundable task) {
        taskQueue.offer(task);
        ApplicationManager.getApplication().invokeLater(this::processNextTask, ModalityState.NON_MODAL);
    }

    private void processNextTask() {
        if (isProcessing) return;
        Task.Backgroundable nextTask = taskQueue.poll();
        if (nextTask != null) {
            isProcessing = true;
