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

package org.b333vv.metric.exec;

import org.b333vv.metric.ui.log.MetricsConsole;

import java.util.concurrent.*;

public class Memorizer<S, R> implements Computable<S, R> {
    private final ConcurrentMap<String, Future<R>> cache = new ConcurrentHashMap<>();
    private final Computable<S, R> c;
    private final MetricsConsole console;

    public Memorizer(Computable<S, R> c, MetricsConsole console) {
        this.c = c;
        this.console = console;
    }

    public R compute(final String key, final S subject) throws InterruptedException {
        while (true) {
            Future<R> f = cache.get(key);
            if (f == null) {
                cache.keySet().removeIf(s -> s.startsWith(key.split(":")[0]));
                Callable<R> eval = () -> c.compute(key, subject);
                FutureTask<R> ft = new FutureTask<>(eval);
                f = cache.putIfAbsent(key, ft);
                if (f == null) {
                    f = ft;
                    ft.run();
                }
            }
            try {
                return f.get();
            } catch (CancellationException e) {
                console.error(e.getMessage());
                cache.remove(key, f);
            } catch (ExecutionException e) {
                console.error(e.getMessage());
                throw LaunderThrowable.launderThrowable(e.getCause());
            }
        }
    }
}
