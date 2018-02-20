package net.senmori.btsuite.buildtools.util;

public final class Version implements Comparable<Version> {

    private final String versionString;
    private int major, minor, revision;
    public Version(String versionString) {
        this.versionString = versionString;
        parse(versionString);
    }

    public void parse(String versionString) {
        String[] arr = versionString.split(".");

        try {
            if(arr.length == 1) {
                major = Integer.valueOf(arr[0]);
            } else if(arr.length == 2) {
                major = Integer.valueOf(arr[0]);
                minor = Integer.valueOf(arr[1]);
            } else if(arr.length >= 3) {
                major = Integer.valueOf(arr[0]);
                minor = Integer.valueOf(arr[1]);
                revision = Integer.valueOf(arr[2]);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid version string (" + versionString + ")");
        }
    }

    public String toString() {
        return String.valueOf(major) + "." + String.valueOf(minor) + "." + String.valueOf(revision);
    }


    @Override
    public int compareTo(Version other) {
        if(this.major != other.major) {
            return Integer.compare(this.major, other.major);
        }
        if(this.minor != other.minor) {
            return Integer.compare(this.minor, other.minor);
        }
        if(this.revision != other.revision) {
            return Integer.compare(this.revision, other.revision);
        }
        return 0;
    }
}
