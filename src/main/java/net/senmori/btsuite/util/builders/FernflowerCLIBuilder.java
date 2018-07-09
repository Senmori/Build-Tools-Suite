/*
 * Copyright (c) $year, $user. BuildToolsSuite. All rights reserved.
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

package net.senmori.btsuite.util.builders;

import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import net.senmori.btsuite.command.CommandHandler;
import net.senmori.btsuite.util.FileUtil;
import net.senmori.btsuite.util.LogHandler;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FernflowerCLIBuilder {

    private FernflowerCLIBuilder() {
    }

    public static FernflowerCLIBuilder builder() {
        return new FernflowerCLIBuilder();
    }

    List<String> commands = Lists.newLinkedList();
    List<String> libraries = Lists.newLinkedList();
    List<String> sources = Lists.newLinkedList();

    public FernflowerCLIBuilder hideBrideMethods(boolean hide) {
        add( "rbr", hide );
        return this;
    }

    public FernflowerCLIBuilder hideSyntheticClassMembers(boolean hide) {
        add( "rsy", hide );
        return this;
    }

    public FernflowerCLIBuilder decompileInnerClasses(boolean decompile) {
        add( "din", decompile );
        return this;
    }

    public FernflowerCLIBuilder collapse14ClassRef(boolean collapse) {
        add( "dc4", collapse );
        return this;
    }

    public FernflowerCLIBuilder decompileAssertions(boolean decompAssertions) {
        add( "das", decompAssertions );
        return this;
    }

    public FernflowerCLIBuilder hideEmptySuperInvocation(boolean hide) {
        add( "hes", hide );
        return this;
    }

    public FernflowerCLIBuilder hideEmptyDefaultConstructor(boolean hide) {
        add( "hdc", hide );
        return this;
    }

    public FernflowerCLIBuilder decompileGenericSigs(boolean decomp) {
        add( "dgs", decomp );
        return this;
    }

    public FernflowerCLIBuilder assumeReturnNotThrowExceptions(boolean assume) {
        add( "ner", assume );
        return this;
    }

    public FernflowerCLIBuilder decompileEnums(boolean decomp) {
        add( "den", decomp );
        return this;
    }

    public FernflowerCLIBuilder removeGetClassInvocation(boolean remove) {
        add( "rgn", remove );
        return this;
    }

    public FernflowerCLIBuilder outputNumericLiterals(boolean asIs) {
        add( "lit", asIs );
        return this;
    }

    public FernflowerCLIBuilder encodeNoneAsciiAsUnicode(boolean encode) {
        add( "asc", encode );
        return this;
    }

    public FernflowerCLIBuilder interpretOneAsBoolean(boolean workaround) {
        add( "bto", workaround );
        return this;
    }

    public FernflowerCLIBuilder allowNonSetSyntheticAttrib(boolean allow) {
        add( "nns", allow );
        return this;
    }

    public FernflowerCLIBuilder namelessTypesAreObjects(boolean assume) {
        add( "uto", assume );
        return this;
    }

    public FernflowerCLIBuilder reconstructVarNamesFromDebug(boolean doReconstruct) {
        add( "udv", doReconstruct );
        return this;
    }

    public FernflowerCLIBuilder removeEmptyExceptionRanges(boolean empty) {
        add( "rer", empty );
        return this;
    }

    public FernflowerCLIBuilder deInlineFinallyStructs(boolean finaly) {
        add( "fdi", finaly );
        return this;
    }

    public FernflowerCLIBuilder maxAllowedProcessTimePerMethod(int seconds) {
        add( "mpm", String.valueOf( seconds ) );
        return this;
    }

    public FernflowerCLIBuilder renameAmbiguousClassesAndElements(boolean rename) {
        add( "ren", rename );
        return this;
    }

    public FernflowerCLIBuilder identifierClass(String clazz) {
        add( "urc", clazz );
        return this;
    }

    public FernflowerCLIBuilder intellijCheck(boolean check) {
        add( "inn", check );
        return this;
    }

    public FernflowerCLIBuilder decompileLambdaToAnonClasses(boolean decompile) {
        add( "lac", decompile );
        return this;
    }

    public FernflowerCLIBuilder defineNewLinkChar(boolean windowsOrUnix) {
        add( "nls", windowsOrUnix );
        return this;
    }

    public FernflowerCLIBuilder defaultIndent(int spaces) {
        add( "ind", String.valueOf( spaces ) );
        return this;
    }

    public FernflowerCLIBuilder log(String level) {
        add( "log", level );
        return this;
    }

    public FernflowerCLIBuilder library(String... libs) {
        libraries.addAll( Arrays.asList( libs ) );
        return this;
    }

    public FernflowerCLIBuilder sources(String... sources) {
        this.sources.addAll( Arrays.asList( sources ) );
        return this;
    }

    private void add(String key, boolean value) {
        commands.add( "-" + key + "=" + ( value ? "1" : "0" ) );
    }

    private void add(String key, String value) {
        commands.add( "-" + key + "=" + value );
    }

    /**
     * Perform the built command in the given directory against the given jar file.
     */
    public void execute(File workDir, File jarFile, File outputDir) {
        if ( ! FileUtil.isDirectory( outputDir ) ) {
            throw new IllegalArgumentException( "Output directory must be a directory!" );
        }
        String[] sources = this.sources.toArray( new String[0] );

        String[] command = commands.toArray( new String[0] );
        String[] libs = libraries.toArray( new String[0] );
        String[] javaCMD = { "java", "-jar", jarFile.getAbsolutePath() };
        command = ObjectArrays.concat( command, libs, String.class ); // -dgs -hdc -e=<source>
        command = ObjectArrays.concat( command, sources, String.class ); // -dgs -hdc -e=<source> <sources>
        command = ObjectArrays.concat( javaCMD, command, String.class ); // java -jar <jar> -dgs -hdc -e=<source> <sources>
        command = ObjectArrays.concat( command, outputDir.getAbsolutePath() ); // java -jar <jar> -dgs -hdc -e=<source> <sources> <dest>
        LogHandler.debug( "Fernflower Command: " + Arrays.toString( command ) );
        CommandHandler.getCommandIssuer().executeCommand( workDir, command );
    }
}
