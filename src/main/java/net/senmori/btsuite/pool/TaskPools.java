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
