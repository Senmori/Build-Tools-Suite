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

import net.senmori.btsuite.versioning.ComparableVersion;

public final class SpigotVersion implements Comparable<SpigotVersion> {

    private final ComparableVersion version;
    private String displayString;


    public SpigotVersion(ComparableVersion version) {
        this.version = version;
        this.displayString = version.toString();
    }

    private SpigotVersion(ComparableVersion version, String displayString) {
        this( version );
        this.displayString = displayString;
    }

    public String getAlias() {
        return displayString;
    }

    public String getVersionString() {
        return version.toString();
    }

    @Override
    public int compareTo(SpigotVersion other) {
        return this.version.compareTo( other.version );
    }

    @Override
    public boolean equals(Object obj) {
        return ( obj instanceof SpigotVersion ) && ( this.compareTo( ( SpigotVersion ) obj ) == 0 );
    }

    public String toString() {
        return version.toString();
    }
}
