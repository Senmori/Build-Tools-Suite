package net.senmori.btsuite.task;

import net.senmori.btsuite.Builder;
import net.senmori.btsuite.Settings;
import net.senmori.btsuite.util.ProcessRunner;

public class GitConfigurationTask implements Runnable {


    public static void runTask() {
        new GitConfigurationTask().run();
    }

    @Override
    public void run() {
        Settings.Directories dirs = Builder.getSettings().getDirectories();
        try {
            ProcessRunner.runProcess( dirs.getWorkingDir(), "git", "--version" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        try {
            ProcessRunner.runProcess( dirs.getWorkingDir(), "git", "config", "--global", "--includes", "user.name" );
        } catch ( Exception ex ) {
            System.out.println( "Git name not set, setting it to default value." );
            try {
                ProcessRunner.runProcess( dirs.getWorkingDir(), "git", "config", "--global", "user.name", "BuildTools" );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
        try {
            ProcessRunner.runProcess( dirs.getWorkingDir(), "git", "config", "--global", "--includes", "user.email" );
        } catch ( Exception ex ) {
            System.out.println( "Git email not set, setting it to default value." );
            try {
                ProcessRunner.runProcess( dirs.getWorkingDir(), "git", "config", "--global", "user.email", "unconfigured@null.spigotmc.org" );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }
}
