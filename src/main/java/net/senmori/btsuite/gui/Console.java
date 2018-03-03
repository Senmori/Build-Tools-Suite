package net.senmori.btsuite.gui;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class Console extends OutputStream {

    private final TextArea output;

    public Console(TextArea textArea) {
        this.output = textArea;

        this.output.setWrapText(true);
        this.output.setEditable(false);

        System.setOut(new PrintStream(this, true));
        System.setErr(new PrintStream(this, true));
    }

    public void appendText(String text) {
        this.output.appendText(text);
        this.output.selectEnd();
    }

    @Override
    public void write(int b) throws IOException {
        Platform.runLater(() -> appendText(String.valueOf((char)b)));
        this.output.selectEnd();
    }
}
