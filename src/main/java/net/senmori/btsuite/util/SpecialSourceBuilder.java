package net.senmori.btsuite.util;

import java.io.File;

public class SpecialSourceBuilder {

    private SpecialSourceBuilder() { }

    public static SpecialSourceBuilder builder() {
        return new SpecialSourceBuilder();
    }

    private File firstJar, secondJar;
    private File atFile;
    private File srgOut;
    private File compact;
}
