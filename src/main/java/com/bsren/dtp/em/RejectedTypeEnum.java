package com.bsren.dtp.em;

import lombok.Getter;

@Getter
public enum RejectedTypeEnum {

    ABORT_POLICY("AbortPolicy"),

    /**
     * A handler for rejected tasks that runs the rejected task directly in the calling thread of the execute method,
     * unless the executor has been shut down, in which case the task is discarded.
     */
    CALLER_RUNS_POLICY("CallerRunsPolicy"),

    DISCARD_OLDEST_POLICY("DiscardOldestPolicy"),

    DISCARD_POLICY("DiscardPolicy");

    private final String name;

    RejectedTypeEnum(String name) {
        this.name = name;
    }

}
