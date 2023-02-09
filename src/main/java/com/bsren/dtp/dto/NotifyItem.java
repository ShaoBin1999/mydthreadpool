package com.bsren.dtp.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotifyItem {

    private List<String> platforms;

    private boolean enabled = true;

    private String type;

    private int threshold;

    private int interval = 120;

    private int clusterLimit = 1;


}
