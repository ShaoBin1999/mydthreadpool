package com.bsren.dtp.em;

public enum NotifyItemEnum {

    /**
     * config change notify
     */
    CONFIG_CHANGE("config_change"),

    /**
     * threadPool notify
     * live = active / maximumPoolSize
     */
    STATUS("status"),

    /**
     * capacity threshold notify
     */
    CAPACITY("capacity"),

    REJECT("reject"),

    RUN_TIMEOUT("run_timeout"),

    QUEUE_TIMEOUT("queue_timeout");

    private final String value;


    NotifyItemEnum(String value) {
        this.value = value;
    }

    public static NotifyItemEnum of(String value) {
        for (NotifyItemEnum notifyItem : NotifyItemEnum.values()) {
            if (notifyItem.value.equals(value)) {
                return notifyItem;
            }
        }
        return null;
    }


}
