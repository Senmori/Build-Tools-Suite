package net.senmori.btsuite.buildtools.util;

public class VersionInfo {

    private String minecraftVersion;
    private String accessTransforms;
    private String classMappings;
    private String memberMappings;
    private String packageMappings;
    private String minecraftHash;
    private String decompileCommand;
    private String serverUrl;

    public VersionInfo(String minecraftVersion, String accessTransforms, String classMappings, String memberMappings, String packageMappings, String minecraftHash)
    {
        this.minecraftVersion = minecraftVersion;
        this.accessTransforms = accessTransforms;
        this.classMappings = classMappings;
        this.memberMappings = memberMappings;
        this.packageMappings = packageMappings;
        this.minecraftHash = minecraftHash;
    }

    public VersionInfo(String minecraftVersion, String accessTransforms, String classMappings, String memberMappings, String packageMappings, String minecraftHash, String decompileCommand)
    {
        this.minecraftVersion = minecraftVersion;
        this.accessTransforms = accessTransforms;
        this.classMappings = classMappings;
        this.memberMappings = memberMappings;
        this.packageMappings = packageMappings;
        this.minecraftHash = minecraftHash;
        this.decompileCommand = decompileCommand;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public String getAccessTransforms() {
        return accessTransforms;
    }

    public String getClassMappings() {
        return classMappings;
    }

    public String getMemberMappings() {
        return memberMappings;
    }

    public String getPackageMappings() {
        return packageMappings;
    }

    public String getMinecraftHash() {
        return minecraftHash;
    }

    public String getDecompileCommand() {
        return decompileCommand;
    }

    public String getServerUrl() {
        return serverUrl;
    }
}
