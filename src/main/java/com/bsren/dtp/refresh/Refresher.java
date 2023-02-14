package com.bsren.dtp.refresh;

import com.bsren.dtp.em.ConfigFileTypeEnum;

public interface Refresher {

    void refresh(String content, ConfigFileTypeEnum fileType);

}
