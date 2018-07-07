package net.senmori.btsuite;

import net.senmori.btsuite.util.LogHandler;

public class Bootstrap {

    public static void main(String[] args) {
        float javaVersion = Float.parseFloat( System.getProperty( "java.class.version" ) );

        if ( javaVersion < 51.0F ) {
            LogHandler.error( "Outdated Java detected (" + javaVersion + "). BuildTools requires at least Java 7. Please update Java and try again." );
            LogHandler.error( "You may use java -version to double check your Java version." );
            return;
        }

        if ( javaVersion < 52.0F ) {
            LogHandler.error( "*** WARNING *** Outdated Java detected (" + javaVersion + "). Minecraft >= 1.12 requires at least Java 8." );
            LogHandler.error( "*** WARNING *** You may use java -version to double check your Java version." );
        }

        if ( javaVersion > 54.0F ) {
            LogHandler.error( "*** WARNING *** Unsupported Java detected (" + javaVersion + "). BuildTools has only been tested up to Java 10. Use of development Java version is not supported." );
            LogHandler.error( "*** WARNING *** You may use java -version to double check your Java version." );
        }
        Builder.main(args);
    }
}
