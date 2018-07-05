package net.senmori.btsuite.log;

import net.senmori.btsuite.util.LogHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LoggerStream extends OutputStream {
    private final Level logLevel;
    private final OutputStream outputStream;

    public LoggerStream(Logger logger, Level logLevel, OutputStream outputStream) {
        this.logLevel = logLevel;
        this.outputStream = outputStream;
    }

    @Override
    public void write(byte[] b) throws IOException {
        String string = new String(b);
        if (!string.trim().isEmpty()) {
            LogHandler.log.log(logLevel, string);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        String string = new String(b, off, len);
        if (!string.trim().isEmpty()) {
            LogHandler.log.log(logLevel, string);
        }

    }

    @Override
    public void write(int b) throws IOException {
        String string = String.valueOf((char) b);
        if (!string.trim().isEmpty()) {
            LogHandler.log.log(logLevel, string);
        }
    }

    public static void setOutAndErrToLog() {
        setOutToLog();
        setErrToLog();
    }

    public static void setOutToLog() {
        System.setOut(new PrintStream(new LoggerStream(LogHandler.log, Level.INFO, System.out)));
    }

    public static void setErrToLog() {
        System.setErr(new PrintStream(new LoggerStream(LogHandler.log, Level.SEVERE, System.err)));
    }
}
