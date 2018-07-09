/*
 * Copyright (c) $year, $user. BuildToolsSuite. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * The name of the author may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

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

    public static String commitHash(Git repo) throws GitAPIException {
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
}
