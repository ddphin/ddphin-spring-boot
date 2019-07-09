package com.ddphin.ddphin.elasticsearch.searcher.controller;

import com.ddphin.ddphin.business.common.common.MMessage;
import com.ddphin.ddphin.business.common.common.MResponse;
import com.ddphin.ddphin.elasticsearch.searcher.bean.ESpuConditionBean;
import com.ddphin.ddphin.elasticsearch.searcher.model.ESpuModel;
import com.ddphin.ddphin.elasticsearch.searcher.service.ESSpuDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * ClassName: ESSpuDocController
 * Function:  ESSpuDocController
 * Date:      2019/6/21 下午3:19
 * Author     DaintyDolphin
 * Version    V1.0
 */
@RestController
@RequestMapping("/es/doc/spu")
public class ESSpuDocController {
    @Autowired
    private ESSpuDocService service;



    @GetMapping
    public ResponseEntity<MResponse> query(@RequestBody ESpuConditionBean t,
                                           Integer pageNo,
                                           Integer pageSize,
                                           String order) throws IOException {

        ESpuModel data = service.query(t, pageNo, pageSize, order);

        MResponse response = new MResponse(data, MMessage.SUCCESS);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
