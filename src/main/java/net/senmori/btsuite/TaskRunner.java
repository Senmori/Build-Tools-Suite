package net.senmori.btsuite;

import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskRunner {

    private final int poolSize;
    private final ExecutorService pool;

    public TaskRunner(int poolSize) {
        this.poolSize = poolSize;
        this.pool = newCachedThreadPool(this.poolSize);
    }

    /**
     * Submit a task for completion.
     * Use this method if you want the result(s).
     *
     * @param task the task to run
     *
     * @return the {@link Future} associated with the task
     */
    public <T> Future<T> submit(Task<T> task) {
        return ( Future<T> ) pool.submit(task);
    }

    /**
     * Execute a task; ignoring any possible results.
     *
     * @param task the task to run
     */
    public <T> void execute(Task<T> task) {
        pool.execute(task);
    }

    /**
     * @return the {@link ExecutorService}
     */
    public ExecutorService getPool() {
        return pool;
    }

    public ExecutorService newCachedThreadPool(int poolSize) {
        return new ThreadPoolExecutor(poolSize, poolSize * poolSize,
                15L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(true), // FIFO
                new ThreadPoolExecutor.DiscardPolicy());
    }
}
