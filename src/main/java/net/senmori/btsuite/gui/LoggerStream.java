package net.senmori.btsuite.gui;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;


@Log4j2
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
            log.log(logLevel, string);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        String string = new String(b, off, len);
        if (!string.trim().isEmpty()) {
            log.log(logLevel, string);
        }

    }

    @Override
    public void write(int b) throws IOException {
        String string = String.valueOf((char) b);
        if (!string.trim().isEmpty()) {
            log.log(logLevel, string);
        }
    }

    public static void setOutAndErrToLog() {
        setOutToLog();
        setErrToLog();
    }

    public static void setOutToLog() {
        System.setOut(new PrintStream(new LoggerStream(log, Level.INFO, System.out)));
    }

    public static void setErrToLog() {
        System.setErr(new PrintStream(new LoggerStream(log, Level.ERROR, System.err)));
    }
}
