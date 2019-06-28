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

import java.math.BigInteger;

/**
 * Represents a numeric item in the version item list that can be represented with an int.
 */
public class IntegerItem implements Item {

    public static final IntegerItem ZERO = new IntegerItem();

    private final BigInteger value;

    private IntegerItem() {
        this.value = BigInteger.ZERO;
    }

    public IntegerItem(String str) {
        this.value = new BigInteger( str );
    }

    @Override
    public int compareTo(Item other) {
        if ( other == null ) {
            return BigInteger.ZERO.equals( value ) ? 0 : 1;
        }

        switch ( other.getType() ) {
            case INTEGER_ITEM:
                return value.compareTo( ( ( IntegerItem ) other ).value );

            case STRING_ITEM: // 1.1 > 1-sp
            case LIST_ITEM: // 1.1 > 1-1
                return 1;
            default:
                throw new IllegalStateException( "Invalid item: " + other.getClass() );
        }
    }

    @Override
    public boolean isNull() {
        return BigInteger.ZERO.equals( value );
    }

    @Override
    public int getType() {
        return Item.INTEGER_ITEM;
    }

    @Override
    public String getValue() {
        return value.toString();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
