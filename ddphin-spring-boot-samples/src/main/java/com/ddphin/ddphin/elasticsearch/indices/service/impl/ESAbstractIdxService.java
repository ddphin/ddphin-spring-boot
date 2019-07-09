package com.ddphin.ddphin.elasticsearch.indices.service.impl;

import com.ddphin.ddphin.elasticsearch.indices.service.ESIdxService;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * ClassName: ESAbstractIdxService
 * Function:  ESAbstractIdxService
 * Date:      2019/6/21 下午3:19
 * Author     DaintyDolphin
 * Version    V1.0
 */
public abstract class ESAbstractIdxService implements ESIdxService {
    @Autowired
    private RestHighLevelClient esclient;

    protected XContentBuilder generateIdxSettingsXContentBuilder(XContentBuilder builder) throws IOException {
        builder.startObject("settings")
            .startObject("index")
                .startObject("analysis")
                    .startObject("filter")
                        .startObject("pinyin_filter")
                            .field("type", "pinyin")
                            .field("keep_full_pinyin", false)
                            .field("keep_joined_full_pinyin", true)
                            .field("keep_none_chinese_in_first_letter", false)
                            .field("ignore_pinyin_offset", false)
                        .endObject()
                    .endObject()
                    .startObject("char_filter")
                        .startObject("tsconvert")
                            .field("type", "stconvert")
                            .field("convert_type", "t2s")
                        .endObject()
                    .endObject()
                    .startObject("analyzer")
                        .startObject("ikpyIndexAnalyzer")
                            .field("type", "custom")
                            .array("char_filter", "tsconvert")
                            .field("tokenizer", "ik_max_word")
                            .array("filter", "pinyin_filter")
                        .endObject()
                        .startObject("ikpySearchAnalyzer")
                            .field("type", "custom")
                            .array("char_filter", "tsconvert")
                            .field("tokenizer", "ik_smart")
                            .array("filter", "pinyin_filter")
                        .endObject()
                    .endObject()
                .endObject()
            .endObject()
        .endObject();
        return builder;
    }


    protected abstract XContentBuilder generateIdxMappingsXContentBuilder(XContentBuilder builder) throws IOException;

    protected abstract String getIndex();


    protected XContentBuilder generateIdxXContentBuilder() throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        this.generateIdxSettingsXContentBuilder(builder);
        this.generateIdxMappingsXContentBuilder(builder);
        builder.endObject();
        return builder;
    }

    @Override
    public CreateIndexResponse create() throws Exception {
        CreateIndexRequest request = new CreateIndexRequest(this.getIndex());
        request.source(this.generateIdxXContentBuilder());

        return esclient.indices().create(request, RequestOptions.DEFAULT);
    }
}
