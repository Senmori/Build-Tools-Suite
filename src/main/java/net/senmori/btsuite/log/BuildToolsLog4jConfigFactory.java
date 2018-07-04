package net.senmori.btsuite.log;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;

@Plugin(name = "BuildToolsConfiguration", category = ConfigurationFactory.CATEGORY)
@Order(Integer.MAX_VALUE) // always use this config
public class BuildToolsLog4jConfigFactory extends ConfigurationFactory {

    private static final String[] ALL = { "*" };

    @Override
    public Configuration getConfiguration(LoggerContext context, ConfigurationSource source) {
        return new BuildToolsConfiguration(context, source, null);
    }

    @Override
    protected String[] getSupportedTypes() {
        return ALL;
    }
}
