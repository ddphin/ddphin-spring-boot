package com.ddphin.ddphin.elasticsearch.indices.service.impl;

import com.ddphin.ddphin.elasticsearch.indices.service.ESIdxService;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * ClassName: ESSpuIdxServiceImpl
 * Function:  ESSpuIdxServiceImpl
 * Date:      2019/6/21 下午3:19
 * Author     DaintyDolphin
 * Version    V1.0
 */
@Service
public class ESSpuIdxService extends ESAbstractIdxService implements ESIdxService {
    @Override
    protected String getIndex() {
        return "spu";
    }

    @Override
    protected XContentBuilder generateIdxMappingsXContentBuilder(XContentBuilder builder) throws IOException {
        builder.startObject("mappings")
            .startObject("properties")
                .startObject("id").field("type", "keyword").endObject()
                .startObject("bid").field("type", "keyword").endObject()
                .startObject("cid").field("type", "keyword").endObject()
                .startObject("status").field("type", "keyword").endObject()
                .startObject("summary")
                    .field("type", "text")
                    .field("analyzer", "ikpyIndexAnalyzer")
                    .field("search_analyzer", "ikpySearchAnalyzer")
                .endObject()
                .startObject("label").field("type", "keyword").endObject()
                .startObject("platform").field("type", "keyword").endObject()
                .startObject("service").field("type", "keyword").endObject()
                .startObject("marketDate").field("type", "date").endObject()
                .startObject("pic")
                    .field("type", "text")
                    .field("index", "false")
                .endObject()
                .startObject("mark")
                    .field("type", "float")
                    .field("index", "false")
                    .field("null_value", 1)
                .endObject()
                .startObject("shows")
                    .field("type", "text")
                    .field("index", "false")
                .endObject()
                .startObject("sales")
                    .field("type", "integer")
                    .field("index", "false")
                    .field("null_value", 0)
                .endObject()
                .startObject("evals")
                    .field("type", "integer")
                    .field("index", "false")
                    .field("null_value", 0)
                .endObject()
                .startObject("praise")
                    .field("type", "integer")
                    .field("index", "false")
                    .field("null_value", 0)
                .endObject()
                .startObject("price")
                    .field("type", "integer")
                .endObject()
                .startObject("sku")
                    .field("type", "nested")
                    .startObject("properties")
                        .startObject("id").field("type", "keyword").endObject()
                        .startObject("title")
                            .field("type", "text")
                            .field("index", "false")
                        .endObject()
                        .startObject("inventory")
                            .field("type", "integer")
                            .field("null_value", 0)
                        .endObject()
                        .startObject("pprice")
                            .field("type", "integer")
                            .field("index", "false")
                        .endObject()
                        .startObject("mprice")
                            .field("type", "integer")
                            .field("index", "false")
                        .endObject()
                        .startObject("para")
                            .field("type", "nested")
                            .startObject("properties")
                                .startObject("pid").field("type", "keyword").endObject()
                                .startObject("vid").field("type", "keyword").endObject()
                            .endObject()
                        .endObject()
                    .endObject()
                .endObject()
            .endObject()
        .endObject();
        return builder;
    }
}
