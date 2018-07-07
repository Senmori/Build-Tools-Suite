package net.senmori.btsuite.command;

import net.senmori.btsuite.util.SystemChecker;

public class CommandHandler {

    private static final WindowsCommandIssuer WINDOWS = new WindowsCommandIssuer();

    public static ICommandIssuer getCommandIssuer() {
        if ( SystemChecker.isWindows() ) {
            return WINDOWS;
        }
        return null;
    }
}
