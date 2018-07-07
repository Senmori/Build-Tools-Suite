package net.senmori.btsuite.util.builders;


import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import net.senmori.btsuite.command.CommandHandler;
import net.senmori.btsuite.util.LogHandler;

import java.io.File;
import java.util.List;

public class SpecialSourceCLIBuilder {

    private SpecialSourceCLIBuilder() {
    }

    public static SpecialSourceCLIBuilder builder() {
        return new SpecialSourceCLIBuilder();
    }

    List<String> commands = Lists.newLinkedList(); // just in case

    /**
     * First jar with original names, for generating mapping
     * Required.
     */
    public SpecialSourceCLIBuilder firstJar(String firstJar) {
        add( "a", firstJar );
        return this;
    }

    /**
     * Second jar with renamed names, for generating mapping
     * Required.
     */
    public SpecialSourceCLIBuilder secondJar(String secondJar) {
        add( "b", secondJar );
        return this;
    }


    /**
     * Access transformer file.
     * Required.
     */
    public SpecialSourceCLIBuilder accessTransformer(String file) {
        add( "access-transformer", file );
        return this;
    }

    /**
     * Mapping file output.
     * Required.
     */
    public SpecialSourceCLIBuilder srgOut(String file) {
        add( "s", file );
        return this;
    }


    /**
     * Output mapping file in compact form.
     */
    public SpecialSourceCLIBuilder compact(boolean compact) {
        add( "c", null );
        return this;
    }

    /**
     * Include unrenamed symbols in mapping file output
     */
    public SpecialSourceCLIBuilder generateDupes(boolean gen) {
        add( "f", gen );
        return this;
    }

    /**
     * Mapping file input
     * Required.
     */
    public SpecialSourceCLIBuilder srgIn(String in) {
        add( "m", in );
        return this;
    }

    /**
     * User numeric .srg mappings with srg-in dir (num -> map vs obf -> mcp)
     */
    public SpecialSourceCLIBuilder numericSrg(boolean numeric) {
        add( "n", numeric );
        return this;
    }

    /**
     * Simulate maven-shade-plugin relocation patterns on srg-in input names
     * Required.
     */
    public SpecialSourceCLIBuilder inShadeRelocation(boolean inShade) {
        add( "R", inShade );
        return this;
    }

    /**
     * Simulate maven-shade-plugin relocation patterns on srg-in output names
     * Required.
     */
    public SpecialSourceCLIBuilder outShadeRelocation(boolean outShade) {
        add( "out-shade-relocation", outShade );
        return this;
    }

    /**
     * Reverse input/output names on srg-in
     */
    public SpecialSourceCLIBuilder reverse(boolean rev) {
        add( "r", rev );
        return this;
    }

    /**
     * Input jar(s) to remap
     * Required.
     */
    public SpecialSourceCLIBuilder inJar(String jar) {
        add( "i", jar );
        return this;
    }

    /**
     * Output jar to write
     * Required.
     */
    public SpecialSourceCLIBuilder outJar(String out) {
        add( "o", out );
        return this;
    }

    /**
     * Force redownloading remote resources (invalid cache)
     */
    public SpecialSourceCLIBuilder forceRedownload(boolean invalidate) {
        add( "force-redownload", invalidate );
        return this;
    }

    /**
     * Enable runtime inheritance lookup
     */
    public SpecialSourceCLIBuilder live(boolean live) {
        add( "live", live ); // don't use 'l' because it's ambiguous
        return this;
    }

    /**
     * Enable runtime inheritance lookup through a mapping
     */
    public SpecialSourceCLIBuilder liveRemapped(boolean remapped) {
        add( "L", remapped );
        return this;
    }

    /**
     * Write inheritance map to file
     * Required.
     */
    public SpecialSourceCLIBuilder writeInheritance(String writeInheritance) {
        add( "H", writeInheritance );
        return this;
    }

    /**
     * Read inheritance map from file
     * Required.
     */
    public SpecialSourceCLIBuilder readInheritance(String file) {
        add( "h", file );
        return this;
    }

    /**
     * Quiet mode
     */
    public SpecialSourceCLIBuilder quiet(boolean quiet) {
        add( "q", quiet );
        return this;
    }

    /**
     * Displays the version information
     */
    public SpecialSourceCLIBuilder version(boolean displayVersion) {
        add( "v", displayVersion );
        return this;
    }

    /**
     * Removes the 'SourceFile' attribute
     */
    public SpecialSourceCLIBuilder killSource(boolean killSource) {
        add( "kill-source", killSource );
        return this;
    }

    /**
     * Removes the 'LocalVariableTable' attribute
     */
    public SpecialSourceCLIBuilder killLVT(boolean killLVT) {
        add( "kill-lvt", killLVT );
        return this;
    }

    /**
     * Removes the 'LocalVariableTypeTable' and 'Signature' attributes
     */
    public SpecialSourceCLIBuilder killGenerics(boolean killGenerics) {
        add( "kill-generics", killGenerics );
        return this;
    }

    /**
     * Identifier to place on each class that is transformed, by default, none.
     * Required.
     */
    public SpecialSourceCLIBuilder identifier(String id) {
        add( "d", id );
        return this;
    }

    /**
     * A comma seperated list of packages that should not be transformed, even if the srg specifies they should
     * Required.
     */
    public SpecialSourceCLIBuilder excludedPackages(String... excluded) {
        add( "e", Joiner.on( "," ).join( excluded ) );
        return this;
    }

    private void add(String key, Object value) {
        if ( value == null ) {
            // -q
            commands.add( "-" + key );
        } else {
            // -i <value>
            commands.add( "-" + key + " " + value );
        }
    }

    /**
     * Perform the built command in the given directory against the given jar file.
     */
    public void execute(File workDir, File jarFile) {
        execute( workDir, jarFile, null );
    }

    public void execute(File workDir, File jarFile, String... extras) {
        String[] command = commands.toArray( new String[commands.size()] );
        String[] javaCMD = { "java", "-jar", jarFile.getAbsolutePath() };
        if ( extras != null ) {
            javaCMD = ObjectArrays.concat( javaCMD, extras, String.class );
        }
        command = ObjectArrays.concat( javaCMD, command, String.class );
        LogHandler.debug( "SpecialSource Command: " + command );
        CommandHandler.getCommandIssuer().executeCommand( workDir, command );
    }
}
