package net.senmori.btsuite.task;

import net.senmori.btsuite.storage.BuildToolsSettings;
import net.senmori.btsuite.util.ProcessRunner;

public class GitConfigurationTask implements Runnable {


    public static void runTask() {
        new GitConfigurationTask().run();
    }

    @Override
    public void run() {
        BuildToolsSettings.Directories dirs = BuildToolsSettings.getInstance().getDirectories();
        try {
            ProcessRunner.runProcess( dirs.getWorkingDir().getFile(), "git", "--version" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        try {
            ProcessRunner.runProcess( dirs.getWorkingDir().getFile(), "git", "config", "--global", "--includes", "user.name" );
        } catch ( Exception ex ) {
            System.out.println( "Git name not set, setting it to default value." );
            try {
                ProcessRunner.runProcess( dirs.getWorkingDir().getFile(), "git", "config", "--global", "user.name", "BuildTools" );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
        try {
            ProcessRunner.runProcess( dirs.getWorkingDir().getFile(), "git", "config", "--global", "--includes", "user.email" );
        } catch ( Exception ex ) {
            System.out.println( "Git email not set, setting it to default value." );
            try {
                ProcessRunner.runProcess( dirs.getWorkingDir().getFile(), "git", "config", "--global", "user.email", "unconfigured@null.spigotmc.org" );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }
}
