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

package net.senmori.btsuite.buildtools;

import java.util.regex.Pattern;

public final class SpigotVersion implements Comparable<SpigotVersion> {
    private static final Pattern VERSION_PATTERN = Pattern.compile( "(\\d+)\\.(\\d+)?.*" );

    private final String versionString;
    private String displayString;


    public SpigotVersion(String versionString) {
        this.versionString = versionString;
        this.displayString = versionString;
    }

    public SpigotVersion(String versionString, String displayString) {
        this( versionString );
        this.displayString = displayString;
    }

    public static boolean isVersionNumber(String versionString) {
        return VERSION_PATTERN.matcher( versionString ).find();
    }

    public String getAlias() {
        return displayString;
    }

    public String getVersionString() {
        return versionString;
    }

    @Override
    public int compareTo(SpigotVersion other) {
        String thisVersion = this.versionString.contains( "-" ) ? this.versionString.split( "-" )[0] : versionString;
        String otherVer = other.versionString.contains( "-" ) ? other.versionString.split( "-" )[0] : other.versionString;
        return versionCompare( thisVersion, otherVer );
    }

    @Override
    public boolean equals(Object obj) {
        return ( obj instanceof SpigotVersion ) && ( this.compareTo( ( SpigotVersion ) obj ) == 0 );
    }

    public String toString() {
        return this.versionString;
    }

    /**
     * Compares two version strings.
     * <p>
     * Use this instead of String.compareTo() for a non-lexicographical
     * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     *
     * @param str1 a string of ordinal numbers separated by decimal points.
     * @param str2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is _numerically_ less than str2.
     * The result is a positive integer if str1 is _numerically_ greater than str2.
     * The result is zero if the strings are _numerically_ equal.
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     */
    public static int versionCompare(String str1, String str2) {
        String[] vals1 = str1.split( "\\." );
        String[] vals2 = str2.split( "\\." );
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while ( i < vals1.length && i < vals2.length && vals1[i].equals( vals2[i] ) ) {
            i++;
        }
        // compare first non-equal ordinal number
        if ( i < vals1.length && i < vals2.length ) {
            int diff = Integer.valueOf( vals1[i] ).compareTo( Integer.valueOf( vals2[i] ) );
            return Integer.signum( diff );
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        return Integer.signum( vals1.length - vals2.length );
    }
}
