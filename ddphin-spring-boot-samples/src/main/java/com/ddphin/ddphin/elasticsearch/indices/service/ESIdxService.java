package com.ddphin.ddphin.elasticsearch.indices.service;

import org.elasticsearch.client.indices.CreateIndexResponse;

/**
 * ClassName: ESIdxService
 * Function:  ESIdxService
 * Date:      2019/6/21 下午3:19
 * Author     DaintyDolphin
 * Version    V1.0
 */
public interface ESIdxService {
    CreateIndexResponse create() throws Exception;
}
