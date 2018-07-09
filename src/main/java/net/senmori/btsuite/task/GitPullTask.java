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
