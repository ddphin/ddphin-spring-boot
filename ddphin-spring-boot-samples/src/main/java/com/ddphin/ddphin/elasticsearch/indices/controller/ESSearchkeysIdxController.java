package com.ddphin.ddphin.elasticsearch.indices.controller;

import com.ddphin.ddphin.elasticsearch.indices.service.impl.ESSearchkeysIdxService;
import com.ddphin.ddphin.business.common.common.MMessage;
import com.ddphin.ddphin.business.common.common.MResponse;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: ESSearchkeysIdxController
 * Function:  ESSearchkeysIdxController
 * Date:      2019/6/21 下午3:19
 * Author     DaintyDolphin
 * Version    V1.0
 */
@RestController
@RequestMapping("/es/idx/searchkeys")
public class ESSearchkeysIdxController {
    @Autowired
    private ESSearchkeysIdxService service;

    @PostMapping
    public ResponseEntity<MResponse>  put() throws Exception {
        CreateIndexResponse createIndexResponse = service.create();

        MResponse response = new MResponse(createIndexResponse, MMessage.SUCCESS);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
