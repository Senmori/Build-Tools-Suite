package net.senmori.btsuite.util;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.function.Predicate;

public final class FileUtil {

    public static void copyJar(File sourceDir, File outDir, String finalJarName, Predicate<String> predicate) throws IOException {
        File[] files = sourceDir.listFiles( (file, name) -> predicate.test( name ) && ! name.contains( "-shaded" ) && ! name.contains( "-remapped" ) );
        if ( ( files == null ) || ( files.length == 0 ) ) {
            LogHandler.error( "No files found in " + sourceDir + " that matched the requirements." );
            return;
        }
        LogHandler.debug( "FileUtil#copyJar found " + files.length + " in " + sourceDir.getPath() );

        if ( ! outDir.isDirectory() ) {
            outDir.mkdirs();
        }

        for ( File source : files ) {
            FileInputStream inputStream = new FileInputStream( source );
            FileOutputStream outputStream = new FileOutputStream( new File( outDir, finalJarName ) );

            FileChannel sourceChannel = inputStream.getChannel();
            FileChannel outChannel = outputStream.getChannel();

            long size = sourceChannel.size();
            sourceChannel.transferTo( 0L, size, outChannel );
            LogHandler.info( "- Copied " + source.getName() + " into " + outDir.getPath() );
        }
    }

    public static boolean isDirectory(File file) {
        return file != null && file.isDirectory();
    }

    public static boolean isNonEmptyDirectory(File dir) {
        if ( dir != null && dir.exists() && dir.isDirectory() ) {
            File[] files = dir.listFiles();
            return files != null && files.length > 0;
        } else {
            return false;
        }
    }

    public static void deleteDirectory(File dir) {
        if ( ! dir.exists() || ! dir.isDirectory() ) return;
        for ( File file : dir.listFiles() ) {
            if ( file.isDirectory() ) {
                deleteDirectory(file);
            } else {
                file.delete();
            }
        }
        dir.delete();
    }

    public static void deleteFilesInDirectory(File dir, Predicate<String> fileNamePredicate) {
        List<File> toDelete = Lists.newArrayList();
        for ( File file : dir.listFiles() ) {
            if ( fileNamePredicate.test( file.getName() ) ) {
                toDelete.add( file );
            }
        }
        toDelete.forEach( (file) -> {
            LogHandler.debug( "Deleting " + file.getName() + " from " + file.getPath() );
            file.delete();
        } );
    }
}
