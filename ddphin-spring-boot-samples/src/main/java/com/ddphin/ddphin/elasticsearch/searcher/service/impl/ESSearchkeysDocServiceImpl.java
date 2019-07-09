package com.ddphin.ddphin.elasticsearch.searcher.service.impl;

import com.ddphin.ddphin.elasticsearch.searcher.model.ESearchkeys;
import com.ddphin.ddphin.elasticsearch.searcher.model.ESearchkeysModel;
import com.ddphin.ddphin.elasticsearch.searcher.service.ESSearchkeysDocService;
import org.apache.commons.collections4.MapUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.completion.FuzzyOptions;
import org.elasticsearch.search.suggest.completion.context.CategoryQueryContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * ClassName: ESSearchkeysDocServiceImpl
 * Function:  ESSearchkeysDocServiceImpl
 * Date:      2019/6/21 下午3:19
 * Author     DaintyDolphin
 * Version    V1.0
 */
@Service
public class ESSearchkeysDocServiceImpl implements ESSearchkeysDocService {
    private final static String SEARCHKEYS = "searchkeys";
    @Autowired
    private RestHighLevelClient esclient;

    private FetchSourceContext generateSuggestFetchSourceContext() {
        String[] resIncludeFields = {"input","extra","results"};
        String[] resExcludeFields = {};
        FetchSourceContext resFetchSourceContext = new FetchSourceContext(true, resIncludeFields, resExcludeFields);
        return resFetchSourceContext;
    }

    @Override
    public ESearchkeysModel query(String input) throws IOException {
        CompletionSuggestionBuilder titleSuggester = SuggestBuilders
                .completionSuggestion("input")
                .skipDuplicates(true)
                .size(15)
                .prefix(input, FuzzyOptions.builder().setFuzzyMinLength(10).setFuzzyPrefixLength(0).build())
                .contexts(Collections.singletonMap("status",
                        Collections.singletonList(CategoryQueryContext.builder().setCategory("1").build())));

        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("keyinfoSuggester", titleSuggester);


        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.suggest(suggestBuilder);
        sourceBuilder.fetchSource(this.generateSuggestFetchSourceContext());

        SearchRequest searchRequest = new SearchRequest(SEARCHKEYS);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = esclient.search(searchRequest, RequestOptions.DEFAULT);

        return this.convertor(searchResponse);
    }

    private ESearchkeysModel convertor(SearchResponse searchResponse) {
        ESearchkeysModel model = new ESearchkeysModel();
        model.setKeyList(new ArrayList<>());
        Suggest suggest = searchResponse.getSuggest();
        CompletionSuggestion suggestion = suggest.getSuggestion("keyinfoSuggester");
        for (CompletionSuggestion.Entry entry : suggestion.getEntries()) {
            for (CompletionSuggestion.Entry.Option option : entry) {
                Map<String, Object> map = option.getHit().getSourceAsMap();
                ESearchkeys keyinfo = new ESearchkeys();
                keyinfo.setInput(MapUtils.getString(((Map)(map.get("input"))), "input", null));
                keyinfo.setExtra(MapUtils.getString(map, "extra", null));
                keyinfo.setResults(MapUtils.getInteger(map, "results", 0));
                model.getKeyList().add(keyinfo);
            }
        }
        model.setTotal(model.getKeyList().size());
        return model;
    }
}
