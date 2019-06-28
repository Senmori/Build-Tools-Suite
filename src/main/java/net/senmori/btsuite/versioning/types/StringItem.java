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
