package com.bsren.dtp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Instance {

    private String ip;

    private int port;

    private String serviceName;

    private String env;
}
