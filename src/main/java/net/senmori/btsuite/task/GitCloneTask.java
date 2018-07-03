package net.senmori.btsuite.task;

import javafx.concurrent.Task;
import net.senmori.btsuite.buildtools.BuildTools;
import net.senmori.btsuite.util.SystemChecker;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;

public class GitCloneTask extends Task<GitCloneTask.Response> {

    private final String url;
    private final File target;
    private final BuildTools options;

    public GitCloneTask(String url, File target, BuildTools options) {
        this.url = url;
        this.target = target;
        this.options = options;
    }

    @Override
    protected GitCloneTask.Response call() throws Exception {
        if ( isRepo(target) ) {
            System.out.println("Cannot clone to a non-empty directory.");
            return Response.FAILURE;
        }
        System.out.println("Starting clone of \'" + url + "\' to " + target);
        final Git result = Git.cloneRepository().setURI(url).setDirectory(target).call();
        try {
            StoredConfig config = result.getRepository().getConfig();
            config.setBoolean("core", null, "autocrlf", SystemChecker.isAutocrlf());
            config.save();
            System.out.println("Successfully cloned " + result.getRepository().getDirectory());
            return Response.SUCCESS;
        } catch ( IOException e ) {
            return Response.FAILURE;
        } finally {
            result.close();
        }
    }

    public boolean isRepo(File dir) {
        return RepositoryCache.FileKey.isGitRepository(dir, FS.DETECTED);
    }

    public enum Response {
        DONT_UPDATE,
        SUCCESS,
        FAILURE;
    }
}
