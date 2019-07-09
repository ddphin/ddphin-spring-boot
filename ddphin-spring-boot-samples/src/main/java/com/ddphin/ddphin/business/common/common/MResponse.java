package com.ddphin.ddphin.business.common.common;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: MResponse
 * Function:  MResponse
 * Date:      2019/6/18 下午1:56
 * Author     DaintyDolphin
 * Version    V1.0
 */
@Data
public class MResponse<T> implements Serializable {
    private static final long serialVersionUID = -4505655308965878999L;
    //返回数据
    private T data;
    //返回码
    private Integer code;
    //返回描述
    private String message;

    public MResponse(T data, MMessage message, Object... args){
        this.code = message.getCode();
        this.message = String.format(message.getMessage(), args);
        this.data = data;
    }
}
