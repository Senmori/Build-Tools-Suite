package net.senmori.btsuite.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.senmori.btsuite.Builder;
import net.senmori.btsuite.storage.adapters.BuildToolsDeserializer;
import net.senmori.btsuite.storage.adapters.BuildToolsSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
            file.mkdirs();
            if ( file.createNewFile() ) {
                return BuildToolsSettings.create( Maps.newHashMap(), Maps.newHashMap(), Lists.newLinkedList() );
            }
            BuildToolsSettings settings = GSON.fromJson( new FileReader( file ), BuildToolsSettings.class );
            if ( settings == null ) {
                return BuildToolsSettings.create( Maps.newHashMap(), Maps.newHashMap(), Lists.newLinkedList() );
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
        File settingsFile = Builder.SETTINGS_FILE.getFile();
        try ( Writer writer = new FileWriter( settingsFile ) ) {
            GSON.toJson( settings, writer );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return settingsFile;
    }
}
