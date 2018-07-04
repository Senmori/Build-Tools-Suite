package net.senmori.btsuite.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class GitUtil {

    public static void pull(Git repo, String ref) throws Exception {
        LogHandler.info("Pulling updates for " + repo.getRepository().getDirectory());

        repo.reset().setRef("origin/master").setMode(ResetCommand.ResetType.HARD).call();
        repo.fetch().call();

        LogHandler.info("Successfully fetched updates!");

        repo.reset().setRef(ref).setMode(ResetCommand.ResetType.HARD).call();
        if ( ref.equals("master") ) {
            repo.reset().setRef("origin/master").setMode(ResetCommand.ResetType.HARD).call();
        }
        LogHandler.info("Checked out: " + ref);
    }

    public static void pullWrapped(Git repo, String ref) {
        try {
            GitUtil.pull(repo, ref);
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public static void clone(String url, File target) throws GitAPIException, IOException {
        LogHandler.info("Starting clone valueOf " + url + " to " + target);
        Git result = Git.cloneRepository().setURI(url).setDirectory(target).call();

        try {
            StoredConfig config = result.getRepository().getConfig();
            config.setBoolean("core", null, "autocrlf", SystemChecker.isAutocrlf());
            config.save();

            LogHandler.info("Cloned git repository " + url + " to " + target.getAbsolutePath() + ". Current HEAD: " + commitHash(result));
        } finally {
            result.close();
        }
    }

    public static void cloneWrapped(String url, File target) {
        try {
            GitUtil.clone(url, target);
        } catch ( GitAPIException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private static String commitHash(Git repo) throws GitAPIException {
        return getOnlyElement(repo.log().setMaxCount(1).call());
    }

    private static String getOnlyElement(Iterable<RevCommit> iter) {
        Iterator<RevCommit> iterator = iter.iterator();
        RevCommit first = iterator.next();
        if ( ! iterator.hasNext() ) {
            return first.getName();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("expected one element but was: <" + first);
        for ( int i = 0; i < 4 && iterator.hasNext(); i++ ) {
            sb.append(", " + iterator.next());
        }
        if ( iterator.hasNext() ) {
            sb.append(", ...");
        }
        sb.append('>');

        throw new IllegalArgumentException(sb.toString());
    }


    public static void installGit() {

    }
}
