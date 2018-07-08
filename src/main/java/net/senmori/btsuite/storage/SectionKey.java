package net.senmori.btsuite.storage;

/**
 * Defines a new section for settings files
 */
public enum SectionKey {

    URL( "urls" ),
    DIRECTORIES( "directories" ),
    VERSIONS( "versions" ),
    OUTPUT_DIRS( "output_directories" ),
    NONE( "" );


    private final String section;

    private SectionKey(String section) {
        this.section = section;
    }

    public String getSection() {
        return section;
    }
}
