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
