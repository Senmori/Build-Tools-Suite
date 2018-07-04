package net.senmori.btsuite.pool;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public interface TaskPool {

    /**
     * Submit a {@link Callable} for execution.
     *
     * @param callable the {@link Callable} to run
     * @param <T> the type
     * @return a {@link Future} of the same type as {@link Callable}
     */
    <T> Future<T> submit(Callable<T> callable);

    /**
     * Submit a {@link Runnable} for execution.
     *
     * @param runnable the {@link Runnable}
     */
    void submit(Runnable runnable);


    /**
     * Used to chain together several {@link #submit(Runnable)} calls.
     *
     * @param run the {@link Runnable} to run
     * @return the {@link TaskPool} for chaining calls.
     */
    default TaskPool async(Runnable run) {
        submit(run);
        return this;
    }


    ExecutorService getService();
}
