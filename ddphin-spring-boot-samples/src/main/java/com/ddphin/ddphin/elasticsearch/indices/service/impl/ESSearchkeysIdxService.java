package com.ddphin.ddphin.elasticsearch.indices.service.impl;

import com.ddphin.ddphin.elasticsearch.indices.service.ESIdxService;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * ClassName: ESSearchkeysIdxServiceImpl
 * Function:  ESSearchkeysIdxServiceImpl
 * Date:      2019/6/21 下午3:19
 * Author     DaintyDolphin
 * Version    V1.0
 */
@Service
public class ESSearchkeysIdxService extends ESAbstractIdxService implements ESIdxService {
    @Override
    protected String getIndex() {
        return "searchkeys";
    }
    @Override
    protected XContentBuilder generateIdxMappingsXContentBuilder(XContentBuilder builder) throws IOException {
        builder.startObject("mappings")
            .startObject("properties")
                .startObject("input")
                    .field("type", "completion")
                    .field("analyzer", "ikpyIndexAnalyzer")
                    .field("search_analyzer", "ikpySearchAnalyzer")
                    .startArray("contexts")
                        .startObject()
                            .field("name", "status")
                            .field("type", "category")
                            .field("path", "status")
                        .endObject()
                    .endArray()
                .endObject()
                .startObject("extra")
                    .field("type", "text")
                    .field("index", "false")
                .endObject()
                .startObject("results")
                    .field("type", "integer")
                    .field("index", "false")
                    .field("null_value", 0)
                .endObject()
                .startObject("status")
                    .field("type", "keyword")
                    .field("null_value", 1)
                .endObject()
            .endObject()
        .endObject();
        return builder;
    }
}
