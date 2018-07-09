package net.senmori.btsuite;

import net.senmori.btsuite.util.LogHandler;

public class Bootstrap {


    public static void main(String[] args) {
        float javaVersion = Float.parseFloat( System.getProperty( "java.class.version" ) );

        if ( javaVersion < 52.0F ) {
            LogHandler.error( "*** WARNING *** Outdated Java detected (" + javaVersion + "). Minecraft >= 1.12 requires at least Java 8." );
            LogHandler.error( "*** WARNING *** You may use java -version to double check your Java version." );
        }

        Builder.main( args );
    }
}
