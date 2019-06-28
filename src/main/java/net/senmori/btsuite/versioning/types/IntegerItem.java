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
