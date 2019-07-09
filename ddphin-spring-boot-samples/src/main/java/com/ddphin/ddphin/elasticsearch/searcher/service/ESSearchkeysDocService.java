package com.ddphin.ddphin.elasticsearch.searcher.service;


import com.ddphin.ddphin.elasticsearch.searcher.model.ESearchkeysModel;

import java.io.IOException;

/**
 * ClassName: ESSearchkeysDocService
 * Function:  ESSearchkeysDocService
 * Date:      2019/6/21 下午3:19
 * Author     DaintyDolphin
 * Version    V1.0
 */
public interface ESSearchkeysDocService {
    ESearchkeysModel query(String input) throws IOException;
}
