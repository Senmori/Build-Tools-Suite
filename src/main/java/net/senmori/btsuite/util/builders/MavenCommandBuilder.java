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

package net.senmori.btsuite.util.builders;

import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.util.LogHandler;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.util.List;

public class MavenCommandBuilder {

    private Invoker invoker = new DefaultInvoker();
    private InvocationRequest request = new DefaultInvocationRequest();
    private final BuildToolsSettings settings = BuildToolsSettings.getInstance();
    private final BuildToolsSettings.Directories dirs = settings.getDirectories();

    private MavenCommandBuilder() {
        invoker.setOutputHandler( (str) -> LogHandler.info( str ) );
        invoker.setErrorHandler( (err) -> LogHandler.error( err ) );
        request.setOutputHandler( (out) -> LogHandler.info( out ) );
        request.setErrorHandler( (err) -> LogHandler.error( err ) );

        invoker.setMavenHome( dirs.getMvnDir().getFile() ); // No need for setMvnExecutable since we have this
    }

    public static MavenCommandBuilder builder() {
        return new MavenCommandBuilder();
    }

    public static MavenCommandBuilder copy(MavenCommandBuilder other) {
        MavenCommandBuilder builder = new MavenCommandBuilder();
        builder.invoker = other.invoker;
        builder.request = other.request;

        builder.invoker.setOutputHandler( (str) -> LogHandler.info( str ) );
        builder.invoker.setErrorHandler( (err) -> LogHandler.error( err ) );
        builder.request.setOutputHandler( (out) -> LogHandler.info( out ) );
        builder.request.setErrorHandler( (err) -> LogHandler.error( err ) );

        builder.invoker.setMavenHome( other.invoker.getMavenHome() ); // No need for setMvnExecutable since we have this
        return builder;
    }

    public MavenCommandBuilder setMavenOpts(String opts) {
        request.setMavenOpts(opts);
        return this;
    }

    public MavenCommandBuilder setInteractiveMode(boolean interactive) {
        request.setBatchMode( ! interactive );
        return this;
    }

    public MavenCommandBuilder setBaseDirectory(File baseDirectory) {
        request.setBaseDirectory(baseDirectory);
        return this;
    }

    public MavenCommandBuilder setGoals(List<String> list) {
        request.setGoals(list);
        return this;
    }

    public InvocationResult execute() {
        try {
            return invoker.execute(request);
        } catch ( MavenInvocationException e ) {
            e.printStackTrace();
        }
        return null;
    }
}
