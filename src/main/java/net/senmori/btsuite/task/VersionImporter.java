package net.senmori.btsuite.task;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import javafx.concurrent.Task;
import net.senmori.btsuite.Main;
import net.senmori.btsuite.buildtools.BuildInfo;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.version.Version;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

public class VersionImporter extends Task<Map<Version, BuildInfo>> {
    private static final Gson GSON = new Gson();
    private static final long MAX_WAIT_TIME = 5L;
    private static final Pattern JSON_PATTERN = Pattern.compile(".json");

    private final String url;
    private final ExecutorService threadPool;

    public VersionImporter(String url, ExecutorService pool) {
        this.url = url;
        this.threadPool = pool;
    }

    @Override
    protected Map<Version, BuildInfo> call() throws Exception {
        File work = new File(Main.WORK_DIR, "work");
        createDir(work);
        File versionDir = new File(work, "versions");
        createDir(versionDir);
        File versionFile = new File(versionDir, "versions.html");
        if ( !versionFile.exists() ) {
            versionFile.createNewFile();
            Future<File> future = threadPool.submit(new FileDownloader(url, versionFile), versionFile);
            waitForFuture(future);
            versionFile = future.get();
        }
        Elements links = Jsoup.parse(versionFile, StandardCharsets.UTF_8.name()).getElementsByTag("a");
        Map<Version, BuildInfo> map = Maps.newHashMap();
        for ( Element element : links ) {
            if ( element.wholeText().startsWith("..") ) // ignore non-version links
                continue;
            String text = element.wholeText(); // 1.12.2.json
            String versionText = JSON_PATTERN.matcher(text).replaceAll(""); // 1.12.2
            if ( !Version.isVersionNumber(versionText) ) {
                continue;
            }
            Version version = Version.of(versionText);
            String versionUrl = url + text; // ../work/versions/1.12.2.json
            File verFile = new File(versionDir, text);
            if ( !verFile.exists() ) {
                verFile.createNewFile();
                Future<File> future = threadPool.submit(new FileDownloader(versionUrl, verFile), verFile);
                waitForFuture(future);
                verFile = future.get();
            }
            JsonReader reader = new JsonReader(new FileReader(verFile));
            BuildInfo buildInfo = GSON.fromJson(reader, BuildInfo.class);
            map.put(version, buildInfo);
        }
        System.out.println("Loaded " + map.keySet().size() + " Spigot versions.");
        return map;
    }

    private void createDir(File file) {
        if ( ! FileUtil.isDirectory(file) ) {
            file.mkdir();
        }
    }

    private void waitForFuture(Future<?> future) {
        do {
            ;
        } while ( ! future.isDone() );
    }
}
