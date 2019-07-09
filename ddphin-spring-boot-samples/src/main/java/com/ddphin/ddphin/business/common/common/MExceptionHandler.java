package com.ddphin.ddphin.business.common.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ClassName: ExceptionHandler
 * Function:  ExceptionHandler
 * Date:      2019/6/18 下午2:33
 * Author     DaintyDolphin
 * Version    V1.0
 */
@RestControllerAdvice
public class MExceptionHandler {

    @ExceptionHandler(MException.class)
    public ResponseEntity<MResponse> handlerMException(MException me){
        MResponse response = new MResponse<>(null, new MMessage(me.getCode(), me.getMessage()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MResponse> handlerBindException(MethodArgumentNotValidException me){
        MResponse response = new MResponse(null, MMessage.VALID_BIND_EXCEPTION, me.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MResponse> handlerException(Exception me){
        me.printStackTrace();
        MResponse response = new MResponse(null, MMessage.UNKNOWN_EXCEPTION, me.getMessage());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
