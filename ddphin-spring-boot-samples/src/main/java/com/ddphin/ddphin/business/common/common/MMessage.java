package com.ddphin.ddphin.business.common.common;

import lombok.Data;

/**
 * ClassName: MMessage
 * Function:  MMessage
 * Date:      2019/6/18 下午2:01
 * Author     DaintyDolphin
 * Version    V1.0
 */
@Data
public class MMessage {
    public final static MMessage SUCCESS = new MMessage(0, "Success");
    public final static MMessage UNKNOWN_EXCEPTION = new MMessage(-100000, "Unknown Error:{0}");
    public final static MMessage VALID_BIND_EXCEPTION = new MMessage(-110000, "{0}");

    private Integer code;
    private String message;

    public MMessage(Integer code, String message) {
        this.code = code;
        this.message = message.replaceAll("\\{\\d+\\}", "%s");
    }
}
