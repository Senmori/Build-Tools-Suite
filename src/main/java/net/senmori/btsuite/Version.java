package net.senmori.btsuite;

public final class Version implements Comparable<Version> {

    private final String versionString;
    private String displayString;
    private int major;
    private int minor;
    private int revision;
    private String[] extra;


    private Version(String versionString) {
        this.versionString = versionString;
        parse(versionString);
        this.displayString = versionString;
    }

    private Version(String versionString, String displayString) {
        this(versionString);
        this.displayString = displayString;
    }

    public static Version of(String versionString) {
        return Version.of(versionString, versionString);
    }

    public static Version of(String versionString, String displayString) {
        return new Version(versionString, displayString);
    }

    public static boolean isVersionNumber(String versionString) {
        return "latest".equalsIgnoreCase(versionString) || ( versionString.split("\\.").length > 1 );
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

    public String[] getExtra() {
        return extra;
    }

    public void parse(String versionString) {
        if ( versionString.equalsIgnoreCase("latest") ) {
            return;
        }
        String[] arr = versionString.split("\\.");

        try {
            if ( arr.length == 1 ) {
                major = Integer.valueOf(arr[0]);
            } else if ( arr.length == 2 ) {
                major = Integer.valueOf(arr[0]);
                minor = Integer.valueOf(arr[1]);
            } else if ( arr.length >= 3 ) {
                major = Integer.valueOf(arr[0]);
                minor = Integer.valueOf(arr[1]);
                revision = Integer.valueOf(arr[2]);

                if ( arr.length > 3 ) {
                    extra = new String[arr.length - 3];
                    System.arraycopy(arr, 3, extra, 0, arr.length);
                }
            } else {
                throw new IllegalArgumentException("Invalid version string (" + versionString + ")");
            }
        } catch ( NumberFormatException e ) {
            throw new IllegalArgumentException("Invalid version string (" + versionString + ")");
        }
    }

    @Override
    public int compareTo(Version other) {
        if ( this.versionString.equalsIgnoreCase("latest") )
            return 1;

        if ( this.major != other.major )
            return Integer.compare(this.major, other.major);

        if ( this.minor != other.minor )
            return Integer.compare(this.minor, other.minor);

        if ( this.revision != other.revision )
            return Integer.compare(this.revision, other.revision);

        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Version && this.compareTo(( Version ) obj) == 0;
    }

    public String toString() {
        return String.valueOf(major) + "." + String.valueOf(minor) + "." + String.valueOf(revision);
    }
}
