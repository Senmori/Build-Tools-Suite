package net.senmori.btsuite.util;

import co.aikar.taskchain.TaskChain;
import org.eclipse.jgit.api.Git;

import java.io.File;

public class TaskUtil {

    /**
     * Clone a Git repo async.
     *
     * @param chain the {@link TaskChain} to use.
     * @param url the url to the repo
     * @param target where to clone the repository
     */
    public static void asyncCloneRepo(TaskChain<?> chain, String url, File target) {
        chain.async(() -> GitUtil.cloneWrapped(url, target));
    }

    /**
     * Pull a {@link Git} repository with a certain branch/tree
     *
     * @param chain the {@link TaskChain} to use.
     * @param repo the repository to pull from
     * @param ref what branch/tree to pull
     */
    public static void asyncPullRepo(TaskChain<?> chain, Git repo, String ref) {
        chain.async(() -> GitUtil.pullWrapped(repo, ref));
    }

    /**
     * Download a File from a given url.
     *
     * @param chain the chain to use
     * @param url the url to download from
     * @param target the target file
     * @return the target file
     */
    public static File asyncDownloadFile(TaskChain<?> chain, String url, File target) {
        chain.asyncFirst(() -> {
                return FileDownloader.downloadWrapped(url, target);
            })
             .abortIfNull()
             .storeAsData("target")
                .<File>syncLast((file) -> LogHandler.debug("Finished downloading " + file));
        File result = chain.getTaskData("target");
        chain.removeTaskData("target");
        return result;
    }
}
