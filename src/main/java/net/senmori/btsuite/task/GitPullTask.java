/*
 * Copyright (c) 2018, Senmori. All rights reserved.
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

import javafx.concurrent.Task;
import net.senmori.btsuite.Console;
import net.senmori.btsuite.util.LogHandler;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.lib.BatchingProgressMonitor;

import java.io.File;

public class GitPullTask extends Task<File> {
    private final Git repo;
    private final String ref;
    private final Console console;

    public GitPullTask(Git repo, String ref, Console console) {
        this.repo = repo;
        this.ref = ref;
        this.console = console;
    }

    @Override
    public File call() throws Exception {
        LogHandler.info("Pulling updates for " + repo.getRepository().getDirectory());

        repo.reset()
            .setRef( "origin/master" )
            .setMode( ResetCommand.ResetType.HARD )
            .call();

        repo.fetch()
            .setProgressMonitor( getMonitor() )
            .call();

        LogHandler.info("Successfully fetched updates!");

        repo.reset()
            .setRef( ref )
            .setMode( ResetCommand.ResetType.HARD )
            .call();

        if ( ref.equals("master") ) {
            repo.reset()
                .setRef( "origin/master" )
                .setMode( ResetCommand.ResetType.HARD )
                .call();
        }
        LogHandler.info("Checked out: " + ref);
        return repo.getRepository().getDirectory();
    }

    private BatchingProgressMonitor getMonitor() {
        return new BatchingProgressMonitor() {
            @Override
            protected void onUpdate(String taskName, int workCurr) {
                StringBuilder s = new StringBuilder();
                format( s, taskName, workCurr );
                send( s );
            }

            @Override
            protected void onEndTask(String taskName, int workCurr) {
                StringBuilder s = new StringBuilder();
                format( s, taskName, workCurr );
                s.append( "\n" ); //$NON-NLS-1$
                send( s );
            }

            private void format(StringBuilder s, String taskName, int workCurr) {
                s.append( "\r" ); //$NON-NLS-1$
                s.append( taskName );
                s.append( ": " ); //$NON-NLS-1$
                while ( s.length() < 25 )
                    s.append( ' ' );
                s.append( workCurr );
            }

            @Override
            protected void onUpdate(String taskName, int cmp, int totalWork, int pcnt) {
                StringBuilder s = new StringBuilder();
                format( s, taskName, cmp, totalWork, pcnt );
                send( s );
            }

            @Override
            protected void onEndTask(String taskName, int cmp, int totalWork, int pcnt) {
                StringBuilder s = new StringBuilder();
                format( s, taskName, cmp, totalWork, pcnt );
                s.append( "\n" ); //$NON-NLS-1$
                send( s );
            }

            private void format(StringBuilder s, String taskName, int cmp,
                                int totalWork, int pcnt) {
                s.append( "\r" ); //$NON-NLS-1$
                s.append( taskName );
                s.append( ": " ); //$NON-NLS-1$
                while ( s.length() < 25 )
                    s.append( ' ' );

                String endStr = String.valueOf( totalWork );
                String curStr = String.valueOf( cmp );
                while ( curStr.length() < endStr.length() )
                    curStr = " " + curStr; //$NON-NLS-1$
                if ( pcnt < 100 )
                    s.append( ' ' );
                if ( pcnt < 10 )
                    s.append( ' ' );
                s.append( pcnt );
                s.append( "% (" ); //$NON-NLS-1$
                s.append( curStr );
                s.append( "/" ); //$NON-NLS-1$
                s.append( endStr );
                s.append( ")" ); //$NON-NLS-1$
            }

            private void send(StringBuilder s) {
                console.setOptionalText( s.toString() );
            }
        };
    }
}
