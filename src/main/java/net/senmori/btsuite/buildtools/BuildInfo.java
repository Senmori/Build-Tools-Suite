package net.senmori.btsuite.buildtools;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BuildInfo {

    private String name;
    private String description;
    private int toolsVersion = - 1;
    private Refs refs;

    @Data
    @AllArgsConstructor
    public static class Refs {
        private String BuildData;
        private String Bukkit;
        private String CraftBukkit;
        private String Spigot;
    }



    public static BuildInfo getDefaultImpl() {
        return new BuildInfo("Dev Build", "Development", 0, new BuildInfo.Refs("master", "master", "master", "master"));
    }
}
