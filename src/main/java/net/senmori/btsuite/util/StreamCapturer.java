package net.senmori.btsuite.util;

import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

@RequiredArgsConstructor
public class StreamCapturer implements Runnable {

    private final InputStream in;
    private final PrintStream out;

    @Override
    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        try {
            String line;
            while ( ( line = br.readLine() ) != null ) {
                out.println(line);
            }
        } catch ( IOException ex ) {
            throw new RuntimeException( ex.getMessage() );
        }
    }
}
