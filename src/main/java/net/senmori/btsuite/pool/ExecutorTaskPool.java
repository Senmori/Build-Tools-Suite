package net.senmori.btsuite.pool;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ExecutorTaskPool implements TaskPool {

    private final ExecutorService service;

    protected ExecutorTaskPool(ExecutorService executorService) {
        this.service = executorService;
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return service.submit(callable);
    }

    @Override
    public void submit(Runnable runnable) {
        service.execute(runnable);
    }

    @Override
    public ExecutorService getService() {
        return service;
    }
}
