package com.ddphin.ddphin.elasticsearch.searcher.service;


import com.ddphin.ddphin.elasticsearch.searcher.bean.ESpuConditionBean;
import com.ddphin.ddphin.elasticsearch.searcher.model.ESpuModel;

import java.io.IOException;

/**
 * ClassName: ESSpuDocService
 * Function:  ESSpuDocService
 * Date:      2019/6/21 下午3:19
 * Author     DaintyDolphin
 * Version    V1.0
 */

public interface ESSpuDocService {
    ESpuModel query(ESpuConditionBean t,
                    Integer pageNo,
                    Integer pageSize,
                    String order) throws IOException;
}
