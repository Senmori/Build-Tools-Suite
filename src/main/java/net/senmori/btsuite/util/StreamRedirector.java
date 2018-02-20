package net.senmori.btsuite.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamRedirector implements Runnable {

    private final InputStream in;
    //private ConsoleContent console;
    public StreamRedirector(InputStream in) {
        this.in = in;
        //console = (ConsoleContent) Main.consoleTab.getContent();
    }

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );

        try {
            String line;
            while ( (line = reader.readLine()) != null) {
                //console.appendText(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
