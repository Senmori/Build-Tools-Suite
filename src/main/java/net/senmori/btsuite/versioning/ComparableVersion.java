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

package net.senmori.btsuite.versioning;

import net.senmori.btsuite.versioning.types.IntegerItem;
import net.senmori.btsuite.versioning.types.ListItem;
import net.senmori.btsuite.versioning.types.StringItem;

import java.util.Locale;
import java.util.Stack;

public class ComparableVersion implements Comparable<ComparableVersion> {

    private String value;
    private String canonical;

    private ListItem items;

    private ComparableVersion() {
        throw new RuntimeException( "Cannot instantiate ComparableVersion without a version string" );
    }

    public ComparableVersion(String version) {
        parseVersion( version );
    }

    public String getCanonical() {
        return canonical;
    }

    @Override
    public int compareTo(ComparableVersion o) {
        return items.compareTo( o.items );
    }

    @Override
    public boolean equals(Object other) {
        return ( other instanceof ComparableVersion ) && canonical.equalsIgnoreCase( ( ( ComparableVersion ) other ).canonical );
    }

    @Override
    public int hashCode() {
        return canonical.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }

    public final void parseVersion(String version) {
        this.value = version;

        items = new ListItem();

        version = version.toLowerCase( Locale.ENGLISH );

        ListItem list = items;

        Stack<Item> stack = new Stack<Item>();
        stack.push( list );

        boolean isDigit = false;

        int startIndex = 0;

        for ( int i = 0; i < version.length(); i++ ) {
            char c = version.charAt( i );

            if ( c == '.' ) {
                if ( i == startIndex ) {
                    list.add( IntegerItem.ZERO );
                } else {
                    list.add( parseItem( isDigit, version.substring( startIndex, i ) ) );
                }
                startIndex = i + 1;
            } else if ( c == '-' ) {
                if ( i == startIndex ) {
                    list.add( IntegerItem.ZERO );
                } else {
                    list.add( parseItem( isDigit, version.substring( startIndex, i ) ) );
                }
                startIndex = i + 1;

                if ( isDigit ) {
                    list.normalize(); // 1.0-* = 1-*

                    if ( ( i + 1 < version.length() ) && Character.isDigit( version.charAt( i + 1 ) ) ) {
                        // new ListItem only if previous were digits and new char is a digit,
                        // ie need to differentiate only 1.1 from 1-1
                        list.add( list = new ListItem() );

                        stack.push( list );
                    }
                }
            } else if ( Character.isDigit( c ) ) {
                if ( !isDigit && i > startIndex ) {
                    list.add( new StringItem( version.substring( startIndex, i ), true ) );
                    startIndex = i;
                }

                isDigit = true;
            } else {
                if ( isDigit && i > startIndex ) {
                    list.add( parseItem( true, version.substring( startIndex, i ) ) );
                    startIndex = i;
                }

                isDigit = false;
            }
        }

        if ( version.length() > startIndex ) {
            list.add( parseItem( isDigit, version.substring( startIndex ) ) );
        }

        while ( !stack.isEmpty() ) {
            list = ( ListItem ) stack.pop();
            list.normalize();
        }

        canonical = items.toString();
    }

    private static Item parseItem(boolean isDigit, String buf) {
        return isDigit ? new IntegerItem( buf ) : new StringItem( buf, false );
    }
}
