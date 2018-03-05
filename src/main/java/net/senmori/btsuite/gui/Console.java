package net.senmori.btsuite.gui;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class Console extends OutputStream {

    private final TextArea output;

    public Console(TextArea textArea) {
        this.output = textArea;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.setOut(new PrintStream( new FileOutputStream(FileDescriptor.out)));
            System.setErr(new PrintStream( new FileOutputStream(FileDescriptor.err)));
        }));

        this.output.setWrapText(true);
        this.output.setEditable(false);

        System.setOut(new PrintStream(this, true));
        System.setErr(new PrintStream(this, true));
    }

    public TextArea getTextArea() {
        return output;
    }

    public void appendText(String text) {
        this.output.appendText(text);
        this.output.selectEnd();
    }

    @Override
    public void write(int b) throws IOException {
        Platform.runLater(() -> appendText(String.valueOf((char)b)));
    }
}
