package com.boyinet.demo.pipelineleakage.constant;


/**
 * @author lengchunyun
 */

public enum StatusEnum {
    /**
     * 无泄漏 code 0
     */
    NORMAL(0),
    /**
     * 有泄漏 code 2
     */
    ABNORMAL(2),
    /**
     * 疑问 code 1
     */
    DOUBT(1);
    private final Integer code;


    StatusEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
