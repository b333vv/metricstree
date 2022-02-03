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

package org.b333vv.metric.event;

import com.intellij.util.messages.Topic;

import javax.swing.*;

public interface ButtonsEventListener {

    Topic<ButtonsEventListener> BUTTONS_EVENT_LISTENER_TOPIC = new Topic<>("ButtonsEventListener", ButtonsEventListener.class);

    default void plusButtonPressed(JButton plusButton, JButton minusButton) {
    }

    default void minusButtonPressed(JButton plusButton, JButton minusButton) {
    }
}
