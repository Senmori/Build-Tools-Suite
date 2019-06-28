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

package net.senmori.btsuite.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.storage.adapters.BuildToolsDeserializer;
import net.senmori.btsuite.storage.adapters.BuildToolsSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;

public class SettingsFactory {
    private static final Gson GSON = new GsonBuilder().serializeNulls()
                                                      .setPrettyPrinting()
                                                      .registerTypeAdapter( BuildToolsSettings.class, new BuildToolsSerializer() )
                                                      .registerTypeAdapter( BuildToolsSettings.class, new BuildToolsDeserializer() )
                                                      .create();

    public static Gson getGson() {
        return GSON;
    }

    public static BuildToolsSettings loadSettings(File file) {
        try {
            BuildToolsSettings settings = null;
            if ( ! file.exists() ) {
                file.createNewFile();
                settings = GSON.fromJson( new InputStreamReader( Main.class.getResourceAsStream( "BTS_Settings.json" ) ), BuildToolsSettings.class );
            } else {
                settings = GSON.fromJson( new FileReader( file ), BuildToolsSettings.class );
            }
            if ( settings == null ) {
                return new BuildToolsSettings();
            }
            return settings;
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return null;
    }

    public static File saveSettings(BuildToolsSettings settings) {
        File settingsFile = Main.SETTINGS_FILE.getFile();
        try ( Writer writer = new FileWriter( settingsFile ) ) {
            GSON.toJson( settings, writer );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return settingsFile;
    }
}
