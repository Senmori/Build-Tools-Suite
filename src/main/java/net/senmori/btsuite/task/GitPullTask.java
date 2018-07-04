package net.senmori.btsuite.task;

import net.senmori.btsuite.util.LogHandler;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.util.concurrent.Callable;

public class GitPullTask implements Callable<File> {


    public static GitPullTask pull(Git repo, String ref) {
        return new GitPullTask(repo, ref);
    }

    private final Git repo;
    private final String ref;

    private GitPullTask(Git repo, String ref) {
        this.repo = repo;
        this.ref = ref;
    }

    @Override
    public File call() throws Exception {
        return call0(repo, ref);
    }

    private File call0(Git repo, String ref) throws GitAPIException {
        LogHandler.info("Pulling updates for " + repo.getRepository().getDirectory());

        repo.reset().setRef("origin/master").setMode(ResetCommand.ResetType.HARD).call();
        repo.fetch().call();

        LogHandler.info("Successfully fetched updates!");

        repo.reset().setRef(ref).setMode(ResetCommand.ResetType.HARD).call();
        if ( ref.equals("master") ) {
            repo.reset().setRef("origin/master").setMode(ResetCommand.ResetType.HARD).call();
        }
        LogHandler.info("Checked out: " + ref);
        return repo.getRepository().getDirectory();
    }
}
