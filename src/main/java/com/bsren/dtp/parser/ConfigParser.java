package com.bsren.dtp.parser;


import com.bsren.dtp.em.ConfigFileTypeEnum;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ConfigParser {

    boolean supports(ConfigFileTypeEnum type);

    List<ConfigFileTypeEnum> types();


    Map<Object, Object> doParse(String content) throws IOException;

    Map<Object, Object> doParse(String content, String prefix) throws IOException;
}
