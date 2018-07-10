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
import net.senmori.btsuite.SpigotVersion;
import net.senmori.btsuite.buildtools.BuildInfo;
import net.senmori.btsuite.pool.TaskPools;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.storage.SettingsFactory;
import net.senmori.btsuite.util.LogHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

public class SpigotVersionImportTask implements Callable<Map<SpigotVersion, BuildInfo>> {
    private static final Pattern JSON_PATTERN = Pattern.compile(".json");
    private static final BuildToolsSettings SETTINGS = BuildToolsSettings.getInstance();
    private static final BuildToolsSettings.Directories DIRS = SETTINGS.getDirectories();

    private final String url;

    public SpigotVersionImportTask(String url) {
        this.url = url;
    }

    @Override
    public Map<SpigotVersion, BuildInfo> call() throws Exception {
        File spigotVersionsDir = new File( DIRS.getVersionsDir().getFile(), "spigot" );
        spigotVersionsDir.mkdirs();
        File versionFile = new File( spigotVersionsDir, "versions.html" );
        if ( !versionFile.exists() ) {
            versionFile.createNewFile();
            versionFile = TaskPools.submit( new FileDownloadTask( url, versionFile ) ).get(); // block
            LogHandler.debug(" Downloaded " + versionFile);
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
                verFile = TaskPools.submit( new FileDownloadTask( versionUrl, verFile ) ).get(); // block
            }
            JsonReader reader = new JsonReader(new FileReader(verFile));
            BuildInfo buildInfo = SettingsFactory.getGson().fromJson( reader, BuildInfo.class );
            map.put(version, buildInfo);
        }
        LogHandler.info("Loaded " + map.keySet().size() + " Spigot versions.");
        return map;
    }
}