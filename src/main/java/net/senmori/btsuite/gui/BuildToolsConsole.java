package net.senmori.btsuite.gui;

import com.google.common.collect.Maps;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import net.senmori.btsuite.util.format.TextAreaFormatter;

import java.util.Map;
import java.util.logging.Level;

public class BuildToolsConsole {
    private static final Map<Level, TextAreaFormatter> formatterMap = Maps.newConcurrentMap();
    private static final TextAreaFormatter DEFAULT = ( (level, message) -> {
        return new Text( message );
    } );
    private static final Font DEFAULT_FONT = Font.getDefault();

    private final TextArea console;

    public BuildToolsConsole(TextArea area) {
        this.console = area;


        // register text formatters here
        formatterMap.put( Level.INFO, DEFAULT ); // do nothing // info
        formatterMap.put( Level.WARNING, (level, message) -> { // warning
            Text text = new Text( message );
            text.setFill( Color.color( 26.0D, 175.0D, 239.0D ) ); // #1aafef
            text.setFont( DEFAULT_FONT );
            return text;
        } );
        formatterMap.put( Level.SEVERE, (level, message) -> { // error
            Text text = new Text( message );
            text.setFill( Color.color( 239.0D, 26.0D, 33.0D ) ); // #ef1a21
            text.setFont( DEFAULT_FONT );
            return text;
        } );
        formatterMap.put( Level.FINE, (level, message) -> { // Debug
            Text text = new Text( level + message );
            text.setFill( Color.color( 146.0D, 38.0D, 224.0D ) ); // #9226e0
            text.setFont( DEFAULT_FONT );
            return text;
        } );
    }


    public TextAreaFormatter getFormatter(Level level) {
        return formatterMap.getOrDefault( level, DEFAULT );
    }

    public TextArea getConsole() {
        return console;
    }
}
