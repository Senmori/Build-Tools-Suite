package net.senmori.btsuite.util;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import net.senmori.btsuite.buildtools.VersionInfo;

import java.io.File;
import java.io.IOException;

public final class HashChecker {


    public static boolean checkHash(File jar, VersionInfo info) throws IOException {
        String hash = Files.hash(jar, Hashing.md5()).toString();
        if (( info.getMinecraftHash() != null ) && ! hash.equals(info.getMinecraftHash())) {
            System.out.println("**** Warning, Minecraft jar has of \'" + hash + "\' does not match stored hash of " + info.getMinecraftHash());
            return false;
        } else {
            System.out.println("Found good Minecraft hash (" + hash + ")");
            return true;
        }
    }
}
