package net.senmori.btsuite.util;

import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.task.FileDownloader;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class TaskUtil {

    /**
     * Clone a Git repo async.
     *
     * @param url the url to the repo
     * @param target where to clone the repository
     */
    public static void asyncCloneRepo(String url, File target) {
        TaskPools.submit(() -> GitUtil.cloneWrapped(url, target));
    }

    /**
     * Pull a {@link Git} repository with a certain branch/tree
     *
     * @param repo the repository to pull from
     * @param ref what branch/tree to pull
     */
    public static void asyncPullRepo(Git repo, String ref) {
        TaskPools.submit(() -> GitUtil.pullWrapped(repo, ref));
    }

    /**
     * Download a File from a given url and block while retreiving it.
     *
     * @param url the url to download from
     * @param target the target file
     * @return the target file
     */
    public static File asyncDownloadFile(String url, File target) {
        try {
            return TaskPools.submit(new FileDownloader(url, target)).get();
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        } catch ( ExecutionException e ) {
            e.printStackTrace();
        }
        return null;
    }
}
