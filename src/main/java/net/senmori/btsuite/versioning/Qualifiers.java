package net.senmori.btsuite.versioning;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public final class Qualifiers {

    /**
     * A qualifier that has a value of null. This is not registered and is only used for errors.
     **/
    public static final Qualifier EMPTY_QUALIFIER = new Qualifier( null );

    public static final Qualifier GENERAL_RELEASE_QUALIFIER = new Qualifier( "" );
    public static final Qualifier ALPHA_QUALIFIER = new Qualifier( "alpha" );
    public static final Qualifier BETA_QUALIFIER = new Qualifier( "beta" );
    public static final Qualifier MILESTONE_QUALIFIER = new Qualifier( "milestone" );
    public static final Qualifier RELEASE_CANDIDATE_QUALIFIER = new Qualifier( "rc" );
    public static final Qualifier SNAPSHOT_QUALIFIER = new Qualifier( "snapshot" );
    public static final Qualifier SPECIAL_QUALIFIER = new Qualifier( "sp" );

    private static final LinkedList<Qualifier> QUALIFIERS = new LinkedList<>();
    private static final Map<String, Qualifier> ALIASES = new TreeMap<>( String.CASE_INSENSITIVE_ORDER );

    static {
        registerQualifier( GENERAL_RELEASE_QUALIFIER );
        registerQualifier( ALPHA_QUALIFIER );
        registerQualifier( BETA_QUALIFIER );
        registerQualifier( MILESTONE_QUALIFIER );
        registerQualifier( RELEASE_CANDIDATE_QUALIFIER );
        registerQualifier( SNAPSHOT_QUALIFIER );
        registerQualifier( SPECIAL_QUALIFIER );

        associate( "ga", GENERAL_RELEASE_QUALIFIER );
        associate( "final", GENERAL_RELEASE_QUALIFIER );
        associate( "cr", RELEASE_CANDIDATE_QUALIFIER );
        associate( "pre", BETA_QUALIFIER );
        associate( "a", ALPHA_QUALIFIER );
        associate( "b", BETA_QUALIFIER );
        associate( "m", MILESTONE_QUALIFIER );
        associate( "Pre-Release", BETA_QUALIFIER );
    }

    private Qualifiers() {
    }

    /**
     * A comparable value for the empty-string qualifier. This one is used to determine if a given qualifier makes
     * the version older than one without a qualifier, or more recent.
     */
    public static final String RELEASE_VERSION_INDEX = String.valueOf( QUALIFIERS.indexOf( GENERAL_RELEASE_QUALIFIER ) );

    /**
     * Register a new {@link Qualifier}.
     * A qualifier will only be registered if it's value has not already been
     * registered.
     *
     * @param qualifier the qualifier to register.
     * @return true if the qualifier was successfully registered.
     */
    public static void registerQualifier(Qualifier qualifier) {
        if ( QUALIFIERS.contains( qualifier ) ) {
            throw new IllegalStateException( "Invalid qualifier \'" + qualifier.getValue() + "\'. Already registered." );
        }
        QUALIFIERS.add( qualifier );
    }

    public static void associate(String str, Qualifier qualifier) {
        if ( ALIASES.containsKey( str ) ) {
            throw new IllegalStateException( "Invalid association. \'" + str + "\' is already associated to \'" + ALIASES.get( str ).getClass() + "\'" );
        }
        if ( !QUALIFIERS.contains( qualifier ) ) {
            throw new IllegalStateException( "Invalid qualifier. \'" + qualifier.getClass() + "\' must be registered first." );
        }
        ALIASES.put( str, qualifier );
    }

    /**
     * Get a {@link Qualifier} by it's registered qualifier.
     * i.e. Get the {@link #GENERAL_RELEASE_QUALIFIER} by an empty string,
     * or "ga", or "final".
     *
     * @param str the qualifier string to find
     * @return the appropriate {@link Qualifier}, or an {@link #EMPTY_QUALIFIER}
     */
    public static Qualifier getQualifier(String str) {
        return QUALIFIERS.stream()
                .filter( (qualifier) -> qualifier.getValue().equalsIgnoreCase( str ) )
                .findFirst()
                .orElse( GENERAL_RELEASE_QUALIFIER );
    }

    /**
     * Checks if a given string is associated with a {@link Qualifier}.
     *
     * @param str the qualifier string to check
     * @return true if the string is associated with a Qualifier
     */
    public static boolean isRegistered(String str) {
        return ALIASES.containsKey( str ) || QUALIFIERS.stream().anyMatch( qualifier -> qualifier.getValue().equalsIgnoreCase( str ) );
    }

    public static String comparableQualifier(String qualifier) {
        if ( !isRegistered( qualifier ) ) {
            throw new IllegalStateException( "Cannot get comparable qualifier to one that does not exist!" );
        }
        int i = QUALIFIERS.indexOf( getQualifier( qualifier ) );
        return i == -1 ? ( QUALIFIERS.size() + "-" + qualifier ) : String.valueOf( i );
    }
}
