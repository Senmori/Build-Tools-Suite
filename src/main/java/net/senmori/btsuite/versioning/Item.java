package net.senmori.btsuite.versioning;

public interface Item extends Component {
    int INTEGER_ITEM = 0;
    int STRING_ITEM = 1;
    int LIST_ITEM = 2;

    int compareTo(Item other);

    boolean isNull();

    int getType();
}
