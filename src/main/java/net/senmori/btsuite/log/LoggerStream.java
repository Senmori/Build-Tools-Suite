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
