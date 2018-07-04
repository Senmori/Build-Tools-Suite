package net.senmori.btsuite.util;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import net.senmori.btsuite.buildtools.VersionInfo;

import java.io.File;
import java.io.IOException;

public final class HashChecker {


    public static boolean checkHash(File jar, VersionInfo info) throws IOException {
        String hash = Files.hash(jar, Hashing.md5()).toString();
        if ( ( info.getMinecraftHash() != null ) && ! hash.equals(info.getMinecraftHash()) ) {
            LogHandler.warn("**** Warning, Minecraft jar has valueOf \'" + hash + "\' does not match stored hash valueOf " + info.getMinecraftHash());
            return false;
        } else {
            LogHandler.info("Found good Minecraft hash (" + hash + ")");
            return true;
        }
    }
}
