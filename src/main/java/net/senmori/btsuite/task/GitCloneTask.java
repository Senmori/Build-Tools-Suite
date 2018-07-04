package net.senmori.btsuite.task;

import net.senmori.btsuite.util.GitUtil;
import net.senmori.btsuite.util.LogHandler;
import net.senmori.btsuite.util.SystemChecker;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

public class GitCloneTask implements Callable<File> {

    public static GitCloneTask clone(String url, File target) {
        return new GitCloneTask(url, target);
    }

    private final String url;
    private final File target;

    private GitCloneTask(String url, File target) {
        this.url = url;
        this.target = target;
    }

    @Override
    public File call() throws Exception {
        return call0(url, target);
    }

    private File call0(String url, File target) throws GitAPIException {
        LogHandler.info("Starting clone of " + url + " to " + target.getName());
        Git result = Git.cloneRepository().setURI(url).setDirectory(target).call();

        try {
            StoredConfig config = result.getRepository().getConfig();
            config.setBoolean("core", null, "autocrlf", SystemChecker.isAutocrlf());
            config.save();

            LogHandler.info("Cloned git repository " + url + " to " + target.getName() + ". Current HEAD: " + GitUtil.commitHash(result));
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( GitAPIException e ) {
            e.printStackTrace();
        } finally {
            result.close();
        }
        return result.getRepository().getDirectory();
    }
}
