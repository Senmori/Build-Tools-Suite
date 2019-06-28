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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

public class ListItem extends ArrayList<Item> implements Item {


    private static final long serialVersionUID = 1L; // Added to quiet warnings

    @Override
    public boolean isNull() {
        return size() == 0;
    }

    @Override
    public int getType() {
        return Item.LIST_ITEM;
    }

    @Override
    public String getValue() {
        return null;
    }

    public void normalize() {
        for ( ListIterator<Item> iterator = listIterator( size() ); iterator.hasPrevious(); ) {
            Item item = iterator.previous();

            if ( item.isNull() ) {
                iterator.remove();
            } else {
                break;
            }
        }
    }

    @Override
    public int compareTo(Item other) {
        if ( other == null ) {
            if ( size() == 0 ) {
                return 0;
            }
            Item first = get( 0 );
            return first.compareTo( null );
        }

        switch ( other.getType() ) {
            case Item.INTEGER_ITEM:
                return -1;

            case Item.STRING_ITEM:
                return 1;

            case Item.LIST_ITEM:
                Iterator<Item> left = iterator();
                Iterator<Item> right = ( ( ListItem ) other ).iterator();

                while ( left.hasNext() || right.hasNext() ) {
                    Item leftItem = left.hasNext() ? left.next() : null;
                    Item rightItem = right.hasNext() ? right.next() : null;

                    int result = ( leftItem == null ) ? -1 * rightItem.compareTo( leftItem ) : leftItem.compareTo( rightItem );

                    if ( result != 0 ) {
                        return result;
                    }
                }
                return 0;
            default:
                throw new IllegalStateException( "Invalid item: " + other.getClass() );
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder( "(" );
        for ( Iterator<Item> iter = iterator(); iter.hasNext(); ) {
            builder.append( iter.next() );
            if ( iter.hasNext() ) {
                builder.append( ',' );
            }
        }
        builder.append( ')' );
        return builder.toString();
    }
}
