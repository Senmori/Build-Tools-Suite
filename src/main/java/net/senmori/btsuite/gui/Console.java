package net.senmori.btsuite.gui;

import javafx.scene.control.TextArea;
import lombok.extern.log4j.Log4j2;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

@Log4j2
public class Console extends OutputStream {

    private final TextArea output;

    public Console(TextArea textArea) {
        this.output = textArea;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
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
        appendText(String.valueOf((char)b));
    }
}
