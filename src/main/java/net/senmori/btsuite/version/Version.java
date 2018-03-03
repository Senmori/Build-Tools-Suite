package net.senmori.btsuite.version;

public final class Version implements Comparable<Version> {

    private final String versionString;
    private String displayString;
    private int major;
    private int minor;
    private int revision;
    public Version(String versionString) {
        this.versionString = versionString;
        parse(versionString);
        this.displayString = versionString;
    }

    public Version(String versionString, String displayString) {
        this(versionString);
        this.displayString = displayString;
    }

    public String getDisplayName() {
        return displayString;
    }

    public String getVersionString() {
        return versionString;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRevision() {
        return revision;
    }

    public void parse(String versionString) {
        String[] arr = versionString.split("\\.");

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
            } else {
                throw new IllegalArgumentException("Invalid version string (" + versionString + ")");
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

    @Override
    public boolean equals(Object o) {
        if(o == null || !(o instanceof Version)) {
            return false;
        }
        Version ver = (Version)o;
        return this.compareTo(ver) == 0;
    }
}
