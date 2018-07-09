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

package net.senmori.btsuite.buildtools;

import com.google.common.collect.Lists;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Data;
import net.senmori.btsuite.Builder;
import net.senmori.btsuite.WindowTab;
import net.senmori.btsuite.controllers.BuildTabController;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.util.LogHandler;

import java.util.List;

@Data
public final class BuildTools implements Runnable {
    private final BuildTabController controller;

    private BooleanProperty runningProperty = new SimpleBooleanProperty( false );
    BuildToolsSettings buildToolsSettings = BuildToolsSettings.getInstance();
    private boolean disableCertificateCheck = false;
    private boolean dontUpdate = false;
    private boolean skipCompile = false;
    private boolean genSrc = false;
    private boolean genDoc = false;
    private boolean invalidateCache = false;
    private String version = BuildToolsSettings.getInstance().getDefaultVersion();
    private List<String> outputDirectories = Lists.newArrayList();


    public BuildTools(BuildTabController controller) {
        this.controller = controller;
    }

    public void setOutputDirectories(List<String> directories) {
        this.outputDirectories.clear();
        this.outputDirectories.addAll(directories);
    }

    public boolean isRunning() {
        return runningProperty.get();
    }

    public void setRunning(boolean value) {
        runningProperty.set( value );
    }

    @Override
    public void run() {
        setRunning( true );
        Builder.setActiveTab(WindowTab.CONSOLE);
        LogHandler.debug( "Starting BuildToolsSuite" );
        BuildToolsProject task = new BuildToolsProject( this, buildToolsSettings );
        try {
            task.call();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public void setFinished() {
        setRunning( false );
    }
}
