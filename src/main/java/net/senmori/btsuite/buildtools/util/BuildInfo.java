package net.senmori.btsuite.buildtools.util;

public class BuildInfo {

    private String name;
    private String description;
    private int toolsVersion = -1;
    private Refs refs;

    public BuildInfo(String name, String description, int toolsVersion, Refs refs) {
        this.name = name;
        this.description = description;
        this.toolsVersion = toolsVersion;
        this.refs = refs;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getToolsVersion() {
        return toolsVersion;
    }

    public Refs getRefs() {
        return refs;
    }

    public static class Refs {
        private String BuildData;
        private String Bukkit;
        private String CraftBukkit;
        private String Spigot;

        public Refs(String buildData, String bukkit, String craftBukkit, String spigot) {
            this.BuildData = buildData;
            this.Bukkit = bukkit;
            this.CraftBukkit = craftBukkit;
            this.Spigot = spigot;
        }

        public String getBuildData() {
            return BuildData;
        }

        public String getBukkit() {
            return Bukkit;
        }

        public String getCraftBukkit() {
            return CraftBukkit;
        }

        public String getSpigot() {
            return Spigot;
        }
    }
}
