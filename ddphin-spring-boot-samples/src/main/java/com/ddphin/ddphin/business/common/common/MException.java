package com.ddphin.ddphin.business.common.common;

import lombok.Data;

/**
 * ClassName: MException
 * Function:  MException
 * Date:      2019/6/18 下午2:03
 * Author     DaintyDolphin
 * Version    V1.0
 */
@Data
public class MException extends RuntimeException {
    private static final long serialVersionUID = -6370612186038915645L;

    private Integer code;
    private Object data;

    public MException(Object data, MMessage message, Object... args) {
        super(String.format(message.getMessage(), args));

        this.setData(data);
        this.setCode(message.getCode());
    }
}
