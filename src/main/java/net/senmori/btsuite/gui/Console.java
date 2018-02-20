package net.senmori.btsuite.gui;

import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class Console extends OutputStream {

    private TextArea textArea = new TextArea();
    private PrintStream out;
    public Console() {

        out = new PrintStream(this, true);

        System.setOut(out);
        System.setErr(out);
    }

    public void appent(String line) {
        textArea.appendText(line);
    }

    @Override
    public void write(int b) throws IOException {

    }
}
