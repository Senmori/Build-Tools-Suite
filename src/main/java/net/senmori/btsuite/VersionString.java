/*
 * Copyright (c) 2018, Senmori. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * The name of the author may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package net.senmori.btsuite;

public final class VersionString implements Comparable<VersionString> {

    private final String versionString;
    private String displayString;
    private int major;
    private int minor;
    private int revision;
    private String[] extra;


    private VersionString(String versionString) {
        this.versionString = versionString;
        parse(versionString);
        this.displayString = versionString;
    }

    private VersionString(String versionString, String displayString) {
        this(versionString);
        this.displayString = displayString;
    }

    public static VersionString valueOf(String versionString) {
        return VersionString.valueOf(versionString, versionString);
    }

    public static VersionString valueOf(String versionString, String displayString) {
        return new VersionString(versionString, displayString);
    }

    public static boolean isVersionNumber(String versionString) {
        return ( versionString.split( "\\." ).length > 1 );
    }

    public String getAlias() {
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
    public int compareTo(VersionString other) {
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
        return obj instanceof VersionString && this.compareTo(( VersionString ) obj) == 0;
    }

    public String toString() {
        return String.valueOf(major) + "." + String.valueOf(minor) + "." + String.valueOf(revision);
    }
}
