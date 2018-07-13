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

import com.google.common.collect.Maps;
import com.google.gson.stream.JsonReader;
import javafx.concurrent.Task;
import net.senmori.btsuite.Console;
import net.senmori.btsuite.buildtools.BuildInfo;
import net.senmori.btsuite.buildtools.BuildTools;
import net.senmori.btsuite.buildtools.SpigotVersion;
import net.senmori.btsuite.pool.TaskPool;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.storage.SettingsFactory;
import net.senmori.btsuite.util.LogHandler;
import net.senmori.btsuite.util.TaskUtil;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

public class SpigotVersionImportTask extends Task<Map<SpigotVersion, BuildInfo>> {
    private final Pattern JSON_PATTERN = Pattern.compile( ".json" );
    private final BuildToolsSettings settings;
    private final BuildToolsSettings.Directories dirs;

    private final String url;

    private final TaskPool pool = TaskPools.createFixedThreadPool( 3 );
    private final BuildTools buildTools;
    private final Console console;

    public SpigotVersionImportTask(BuildTools buildTools) {
        this.buildTools = buildTools;
        this.console = buildTools.getConsole();

        this.settings = buildTools.getSettings();
        this.dirs = buildTools.getSettings().getDirectories();
        this.url = this.settings.getVersionLink();
    }

    @Override
    public Map<SpigotVersion, BuildInfo> call() throws Exception {
        File spigotVersionsDir = new File( dirs.getVersionsDir().getFile(), "spigot" );
        spigotVersionsDir.mkdirs();
        File versionFile = new File( spigotVersionsDir, "versions.html" );
        if ( !versionFile.exists() ) {
            versionFile.createNewFile();
            versionFile = TaskUtil.asyncDownloadFile( url, versionFile );
            LogHandler.info( " Downloaded " + FilenameUtils.getBaseName( versionFile.getName() ) );
        } else {
            LogHandler.info( versionFile.getName() + " already exists!" );
        }

        Elements links = Jsoup.parse(versionFile, StandardCharsets.UTF_8.name()).getElementsByTag("a");
        Map<SpigotVersion, BuildInfo> map = Maps.newHashMap();

        for ( Element element : links ) {
            if ( element.wholeText().startsWith("..") ) // ignore non-version links
                continue;
            String text = element.wholeText(); // 1.12.2.json
            String versionText = JSON_PATTERN.matcher(text).replaceAll(""); // 1.12.2
            if ( ! SpigotVersion.isVersionNumber( versionText ) ) {
                continue;
            }
            SpigotVersion version = SpigotVersion.valueOf( versionText );
            String versionUrl = url + text; // ../work/versions/1.12.2.json
            File verFile = new File( spigotVersionsDir, text );
            if ( !verFile.exists() ) {
                verFile.createNewFile();
                verFile = TaskUtil.asyncDownloadFile( versionUrl, verFile );
            }
            console.setOptionalText( FilenameUtils.getBaseName( verFile.getName() ) );
            JsonReader reader = new JsonReader( new FileReader( verFile ) );
            BuildInfo buildInfo = SettingsFactory.getGson().fromJson( reader, BuildInfo.class );
            map.put( version, buildInfo );
        }
        return map;
    }
}
