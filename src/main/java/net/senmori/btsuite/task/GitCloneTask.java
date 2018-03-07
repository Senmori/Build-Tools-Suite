package net.senmori.btsuite.task;

import javafx.concurrent.Task;
import net.senmori.btsuite.util.SystemChecker;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.StoredConfig;

import java.io.File;

public class GitCloneTask extends Task<GitCloneTask.Response> {

    private final String url;
    private final File target;

    public GitCloneTask(String url, File target) {
        this.url = url;
        this.target = target;
    }

    @Override
    protected GitCloneTask.Response call() throws Exception {
        if(!target.exists()) {
            target.mkdirs();
        }

        System.out.println("Starting clone of \'" + url + "\' to " + target);

        Git result = Git.cloneRepository().setURI(url).setDirectory(target).call();
        try {
            StoredConfig config = result.getRepository().getConfig();
            config.setBoolean("core", null, "autocrlf", SystemChecker.isAutocrlf());
            config.save();
            System.out.println("Successfully cloned " + result.getRepository().getDirectory());
        } finally {
            result.close();
        }

        return GitCloneTask.Response.SUCCESS;
    }

    public enum Response {
        SUCCESS,
        FAILURE;
    }
}
