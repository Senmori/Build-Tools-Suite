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

package net.senmori.btsuite;

import net.senmori.btsuite.util.JFxUtils;

import java.io.File;

public class BootStrap {
    public static boolean shouldStart() {

        File workingDir = Main.WORKING_DIR.getFile();

        String errorTitle = "Error running Build Tools Suite";

        if ( workingDir.getAbsolutePath().contains( "\'" ) || workingDir.getAbsolutePath().contains( "#" ) ) {
            JFxUtils.createAlert( errorTitle, "Build Tools Suite cannot be run from files containing special charactesr.", "" );
            return false;
        }

        if ( workingDir.getAbsolutePath().contains( " " ) ) {

        }

        float javaVersion = Float.parseFloat( System.getProperty( "java.class.version" ) );

        // java 8
        if ( javaVersion < 52.0F ) {
            String header = "Build Tools Suite requires at least Java 8 in order to run.";
            String content = "Outdated Java Version detected (" + javaVersion + "). Minecraft >=1.2 requires at least Java 8.";
            JFxUtils.createAlert( errorTitle, header, content );
            return false;
        }

        if ( javaVersion > 55.0F ) {
            String header = "Build Tools Suite has only been tested up to Java 11.";
            String content = "Unsupported Java Version (" + javaVersion + ").";
            JFxUtils.createAlert( errorTitle, header, content );
            return false;
        }
        return true;
    }
}
