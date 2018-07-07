package net.senmori.btsuite.util.format;

import javafx.scene.text.Text;

import java.util.logging.Level;

@FunctionalInterface
public interface TextAreaFormatter {

    Text format(Level level, String message);
}
