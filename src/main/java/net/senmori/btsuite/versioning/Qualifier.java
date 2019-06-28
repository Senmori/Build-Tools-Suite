package net.senmori.btsuite.versioning;

public class Qualifier implements Item {

    private final String value;

    private Qualifier() {
        this.value = "";
    }

    public Qualifier(String str) {
        this.value = str;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if ( this == other ) {
            return true;
        }
        if ( getClass() != other.getClass() ) {
            return false;
        }
        Qualifier qualifier = ( Qualifier ) other;
        return this.value.equalsIgnoreCase( qualifier.getValue() );
    }

    @Override
    public int compareTo(Item other) {
        return 0;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public int getType() {
        return 0;
    }
}
