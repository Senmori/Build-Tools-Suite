package net.senmori.btsuite.task;

import javafx.concurrent.Task;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.util.FS;

import java.io.File;

public class GitPullTask extends Task<GitPullTask.Response> {

    private final File gitDir;
    private final String ref;

    public GitPullTask(File gitDir, String ref) {
        this.gitDir = gitDir;
        this.ref = ref;
    }

    @Override
    protected Response call() throws Exception {
        if(!gitDir.exists()) {
            return Response.FAILURE;
        }

        Git repo = Git.open(gitDir);

        System.out.println("Pulling updates for \'" + repo.getRepository().getDirectory() + "\'");

        repo.reset().setRef("origin/master").setMode(ResetCommand.ResetType.HARD).call();
        repo.fetch().call();

        repo.reset().setRef(ref).setMode(ResetCommand.ResetType.HARD).call();
        if(ref.equals("master")) {
            repo.reset().setRef("origin/master").setMode(ResetCommand.ResetType.HARD).call();
        }
        System.out.println("Checked out: " + ref);

        return Response.SUCCESS;
    }

    public enum Response {
        SUCCESS,
        FAILURE;
    }
}
