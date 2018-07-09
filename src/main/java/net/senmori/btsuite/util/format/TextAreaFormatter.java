package net.senmori.btsuite.util.format;

import java.util.logging.Level;
import java.util.regex.Pattern;

@FunctionalInterface
public interface TextAreaFormatter {
    Pattern LOG_PATTERN = Pattern.compile( "\\[(.*?)\\].*" );
    TextAreaFormatter DEFAULT_FORMATTER = new TextAreaFormatter() {
        @Override
        public String format(Level level, String message) {
            if ( LOG_PATTERN.matcher( message ).find() ) {
                return message + "\n";
            }
            if ( level == Level.CONFIG ) {
                return "[DEBUG] " + message + "\n";
            }
            return "[" + level + "] " + message + "\n";
        }
    };

    String format(Level level, String message);
}
