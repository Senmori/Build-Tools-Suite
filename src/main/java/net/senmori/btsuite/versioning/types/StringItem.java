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

package net.senmori.btsuite.versioning.types;

import net.senmori.btsuite.versioning.Item;
import net.senmori.btsuite.versioning.Qualifiers;

public class StringItem implements Item {


    private final String value;

    public StringItem(String value, boolean followedByDigit) {
        if ( followedByDigit && value.length() == 1 ) {
            value = Qualifiers.getQualifier( String.valueOf( value.charAt( 0 ) ) ).getValue();
        }
        this.value = Qualifiers.getQualifier( value ).getValue();
    }

    @Override
    public int compareTo(Item item) {
        if ( item == null ) {
            // 1-rc < 1, 1-ga > 1
            return Qualifiers.comparableQualifier( value ).compareTo( Qualifiers.RELEASE_VERSION_INDEX );
        }
        switch ( item.getType() ) {
            case Item.INTEGER_ITEM:
                return -1; // 1.any < 1.1 ?

            case Item.STRING_ITEM:
                return Qualifiers.comparableQualifier( value ).compareTo( Qualifiers.comparableQualifier( ( ( StringItem ) item ).value ) );

            case Item.LIST_ITEM:
                return -1; // 1.any < 1-1

            default:
                throw new RuntimeException( "invalid item: " + item.getClass() );
        }
    }

    @Override
    public boolean isNull() {
        return ( Qualifiers.comparableQualifier( value ).compareTo( Qualifiers.RELEASE_VERSION_INDEX ) == 0 );
    }

    @Override
    public int getType() {
        return Item.STRING_ITEM;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
