/*
 * Copyright (c) $year, $user. BuildToolsSuite. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * The name of the author may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package net.senmori.btsuite.pool;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class handles the creation of new {@link TaskPool}s.
 * <br>
 * This class also houses the common thread pool for this program.
 * Anyone can submit tasks to be scheduled for execution via this class.
 * <br>
 * To create a {@link TaskPool} using a specified {@link ExecutorService} use {@link #createTaskPool(ExecutorService)}. <br>
 * To create a cached task pool use {@link #createCachedTaskPool()}.<br>
 * To create a fixed thread pool use {@link #createFixedThreadPool(int)}.<br>
 * To create a single threaded scheduled task pool use {@link #createSingleTaskPool()}
 */
public class TaskPools {
    private static final AtomicInteger threadID = new AtomicInteger();

    private static final TaskPool commonPool = createCachedTaskPool();

    /**
     * Create a new {@link TaskPool} with a specified {@link ExecutorService}.
     *
     * @param service the {@link ExecutorService}
     * @return a new {@link TaskPool}
     */
    public static TaskPool createTaskPool(ExecutorService service) {
        return new ExecutorTaskPool(service);
    }

    /**
     * Create a new {@link TaskPool} using {@link Executors#newCachedThreadPool()}
     *
     * @return a new {@link TaskPool}
     */
    public static TaskPool createCachedTaskPool() {
        return createTaskPool(Executors.newCachedThreadPool(r -> {
            final Thread thread = new Thread(r);
            thread.setName("BuildToolsSuiteQueue Thread " + threadID.getAndIncrement());
            return thread;
        }));
    }

    /**
     * Create a new {@link TaskPool} using {@link Executors#newFixedThreadPool(int)}
     *
     * @param maxThreads maximum number of thread in the pool
     * @return a new {@link TaskPool}
     */
    public static TaskPool createFixedThreadPool(int maxThreads) {
        return createTaskPool(Executors.newFixedThreadPool(maxThreads));
    }

    /**
     * Create a new {@link TaskPool} using {@link Executors#newSingleThreadScheduledExecutor()}
     *
     * @return a new {@link TaskPool}
     */
    public static TaskPool createSingleTaskPool() {
        return createTaskPool(Executors.newSingleThreadScheduledExecutor());
    }

    public static <T> Future<T> submit(Callable<T> callable) {
        return commonPool.submit(callable);
    }

    public static void submit(Runnable runnable) {
        commonPool.submit(runnable);
    }

    public static TaskPool async(Runnable runnable) {
        return commonPool.async(runnable);
    }


    public static ExecutorService getService() {
        return commonPool.getService();
    }

    public static void shutdown() {
        commonPool.getService().shutdown();
    }

    public static void shutdownNow() {
        commonPool.getService().shutdownNow();
    }
}
