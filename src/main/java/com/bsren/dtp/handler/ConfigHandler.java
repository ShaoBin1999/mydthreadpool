package com.bsren.dtp.handler;
import com.bsren.dtp.em.ConfigFileTypeEnum;
import com.bsren.dtp.parser.ConfigParser;
import com.bsren.dtp.parser.JsonConfigParser;
import com.bsren.dtp.parser.PropertiesConfigParser;
import com.bsren.dtp.parser.YamlConfigParser;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * ConfigHandler related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public final class ConfigHandler {

    private static final List<ConfigParser> PARSERS = Lists.newArrayList();

    private ConfigHandler() {
        ServiceLoader<ConfigParser> loader = ServiceLoader.load(ConfigParser.class);
        for (ConfigParser configParser : loader) {
            PARSERS.add(configParser);
        }

        PARSERS.add(new PropertiesConfigParser());
        PARSERS.add(new YamlConfigParser());
        PARSERS.add(new JsonConfigParser());
    }

    public Map<Object, Object> parseConfig(String content, ConfigFileTypeEnum type) throws IOException {
        for (ConfigParser parser : PARSERS) {
            if (parser.supports(type)) {
                return parser.doParse(content);
            }
        }

        return Collections.emptyMap();
    }

    public static ConfigHandler getInstance() {
        return ConfigHandlerHolder.INSTANCE;
    }

    private static class ConfigHandlerHolder {
        private static final ConfigHandler INSTANCE = new ConfigHandler();
    }
}
