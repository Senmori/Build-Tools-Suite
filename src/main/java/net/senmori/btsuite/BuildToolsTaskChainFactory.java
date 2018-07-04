package net.senmori.btsuite;

import co.aikar.taskchain.AsyncQueue;
import co.aikar.taskchain.GameInterface;
import co.aikar.taskchain.TaskChainAsyncQueue;
import co.aikar.taskchain.TaskChainFactory;
import javafx.application.Platform;

import java.util.concurrent.TimeUnit;

public class BuildToolsTaskChainFactory extends TaskChainFactory {
    private BuildToolsTaskChainFactory(GameInterface impl) {
        super(impl);
    }

    public static TaskChainFactory create() {
        return new BuildToolsTaskChainFactory(new BuildToolsGameInterface());
    }

    private static class BuildToolsGameInterface implements GameInterface {

        private final AsyncQueue asyncQueue;

        BuildToolsGameInterface() {
            this.asyncQueue = new TaskChainAsyncQueue();
        }

        public boolean isMainThread() {
            return Platform.isFxApplicationThread();
        }

        public AsyncQueue getAsyncQueue() {
            return asyncQueue;
        }

        public void postToMain(Runnable run) {
            Platform.runLater(run);
        }

        public void scheduleTask(int gameUnits, Runnable run) {
            run.run();
        }

        public void registerShutdownHandler(TaskChainFactory factory) {
            factory.shutdown(10, TimeUnit.SECONDS);
        }
    }
}
