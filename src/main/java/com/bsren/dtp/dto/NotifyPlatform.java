package com.bsren.dtp.dto;

import lombok.Data;

@Data
public class NotifyPlatform {

    private String platform;

    private String urlKey;

    private String secret;

    private String receivers = "all";
}
