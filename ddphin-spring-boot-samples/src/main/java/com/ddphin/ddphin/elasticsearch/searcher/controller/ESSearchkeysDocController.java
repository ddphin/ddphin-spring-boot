package com.ddphin.ddphin.elasticsearch.searcher.controller;

import com.ddphin.ddphin.business.common.common.MMessage;
import com.ddphin.ddphin.business.common.common.MResponse;
import com.ddphin.ddphin.elasticsearch.searcher.model.ESearchkeysModel;
import com.ddphin.ddphin.elasticsearch.searcher.service.ESSearchkeysDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * ClassName: ESSearchkeysDocController
 * Function:  ESSearchkeysDocController
 * Date:      2019/6/21 下午3:19
 * Author     DaintyDolphin
 * Version    V1.0
 */
@RestController
@RequestMapping("/es/doc/searchkeys")
public class ESSearchkeysDocController {
    @Autowired
    private ESSearchkeysDocService service;


    @GetMapping
    public ResponseEntity<MResponse>  query(String input) throws IOException {

        ESearchkeysModel data = service.query(input);

        MResponse response = new MResponse(data, MMessage.SUCCESS);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
