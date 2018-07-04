package net.senmori.btsuite.log;

import javafx.scene.control.TextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.builder.api.Component;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.awt.*;

public class BuildToolsConfiguration extends BuiltConfiguration {
    public BuildToolsConfiguration(LoggerContext loggerContext, ConfigurationSource source, Component rootComponent) {
        super(loggerContext, source, rootComponent);
    }


    @Override
    public void setup() {
        super.setup();
        final LoggerContext context = (LoggerContext) LogManager.getContext(false);
        final Configuration config = context.getConfiguration();
        PatternLayout layout = PatternLayout.newBuilder().withPattern("p - %m%n").build();
        Appender custom = TextAreaAppender.createAppender("JavaFXLogger", layout, null);
        TextArea tempArea = new TextArea();
        TextAreaAppender.setTextArea(tempArea);
        custom.start();
        config.addAppender(custom);
        context.getRootLogger().addAppender(custom);
        context.updateLoggers();
    }
}
