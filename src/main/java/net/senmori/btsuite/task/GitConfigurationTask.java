package net.senmori.btsuite.task;

import net.senmori.btsuite.command.CommandHandler;
import net.senmori.btsuite.command.ICommandIssuer;
import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.util.LogHandler;

public class GitConfigurationTask implements Runnable {


    public static void runTask() {
        new GitConfigurationTask().run();
    }

    @Override
    public void run() {
        ICommandIssuer commandHandler = CommandHandler.getCommandIssuer();
        BuildToolsSettings.Directories dirs = BuildToolsSettings.getInstance().getDirectories();
        try {
            commandHandler.executeCommand( dirs.getWorkingDir().getFile(), "git", "--version" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        try {
            commandHandler.executeCommand( dirs.getWorkingDir().getFile(), "git", "config", "--global", "--includes", "user.name" );
        } catch ( Exception ex ) {
            LogHandler.info( "Git name not set, setting it to default value." );
            try {
                commandHandler.executeCommand( dirs.getWorkingDir().getFile(), "git", "config", "--global", "user.name", "BuildToolsSuite" );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
        try {
            commandHandler.executeCommand( dirs.getWorkingDir().getFile(), "git", "config", "--global", "--includes", "user.email" );
        } catch ( Exception ex ) {
            LogHandler.info( "Git email not set, setting it to default value." );
            try {
                commandHandler.executeCommand( dirs.getWorkingDir().getFile(), "git", "config", "--global", "user.email", "buildToolsSuite@null.spigotmc.org" );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }
}
