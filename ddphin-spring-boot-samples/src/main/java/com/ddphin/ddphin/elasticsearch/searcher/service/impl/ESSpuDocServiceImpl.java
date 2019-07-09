package com.ddphin.ddphin.elasticsearch.searcher.service.impl;

import com.ddphin.ddphin.elasticsearch.searcher.bean.ESpuConditionBean;
import com.ddphin.ddphin.elasticsearch.searcher.model.ESpu;
import com.ddphin.ddphin.elasticsearch.searcher.model.ESpuModel;
import com.ddphin.ddphin.elasticsearch.searcher.service.ESSpuDocService;
import org.apache.commons.collections4.MapUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * ClassName: ESSpuDocServiceImpl
 * Function:  ESSpuDocServiceImpl
 * Date:      2019/6/21 下午3:19
 * Author     DaintyDolphin
 * Version    V1.0
 */
@Service
public class ESSpuDocServiceImpl implements ESSpuDocService {
    private final static String INDEX_SPU = "spu";
    @Autowired
    private RestHighLevelClient esclient;


    private List<QueryBuilder> generateSkuParaQueryList(Map<Long, List<Long>> t) {
        if (null != t && !t.isEmpty()) {
            List<QueryBuilder> nestedParaQueryList = new ArrayList<>();
            for (Map.Entry<Long, List<Long>> para : t.entrySet()) {

                BoolQueryBuilder paraQuery = QueryBuilders.boolQuery();
                paraQuery.filter().add(QueryBuilders.termQuery("sku.para.pid", para.getKey()));
                paraQuery.filter().add(QueryBuilders.termsQuery("sku.para.vid", para.getValue()));

                NestedQueryBuilder nestedParaQuery = QueryBuilders.nestedQuery("sku.para", paraQuery, ScoreMode.Max);
                nestedParaQueryList.add(nestedParaQuery);
            }
            return nestedParaQueryList;
        }
        return null;
    }

    private QueryBuilder generateSkuAvailableQuery(Boolean t) {
        if (null != t && t) {
            return QueryBuilders.rangeQuery("inventory").gt(0);
        }
        return null;
    }

    private QueryBuilder generateSkuMPriceQuery(Integer from, Integer to) {
        if (null == from && null == to) {
            return null;
        }
        RangeQueryBuilder builder = QueryBuilders.rangeQuery("mprice");
        if (null != from) {
            builder.gte(from);
        }
        if (null != to) {
            builder.lte(to);
        }
        return builder;
    }

    private QueryBuilder generateSkuQuery(ESpuConditionBean t) {
        List<QueryBuilder> nestedParaQueryList = this.generateSkuParaQueryList(t.getPara());
        QueryBuilder availableQuery = this.generateSkuAvailableQuery(t.getAvailable());
        QueryBuilder mpriceQuery = this.generateSkuMPriceQuery(t.getMinprice(), t.getMaxprice());



        NestedQueryBuilder nestedSkuQuery;
        if (null == nestedParaQueryList && null == availableQuery && null == mpriceQuery) {
            nestedSkuQuery = QueryBuilders.nestedQuery("sku", QueryBuilders.matchAllQuery(), ScoreMode.Max);;
        }
        else {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            if (null != nestedParaQueryList) {
                boolQueryBuilder.filter().addAll(nestedParaQueryList);
            }
            if (null != availableQuery) {
                boolQueryBuilder.filter().add(availableQuery);
            }
            if (null != mpriceQuery) {
                boolQueryBuilder.filter().add(mpriceQuery);
            }
            nestedSkuQuery = QueryBuilders.nestedQuery("sku", boolQueryBuilder, ScoreMode.Max);
        }
        String[] includeFields = {"sku.id", "sku.pprice", "sku.mprice", "sku.title"};
        String[] excludeFields = {};
        FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includeFields, excludeFields);

        InnerHitBuilder innerHitBuilder = new InnerHitBuilder();
        innerHitBuilder.setFetchSourceContext(fetchSourceContext).setSize(1);
        nestedSkuQuery.innerHit(innerHitBuilder);

        return nestedSkuQuery;
    }

    private QueryBuilder generateSpuQuery(ESpuConditionBean t) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must().add(QueryBuilders.matchQuery("summary", t.getMatch()).minimumShouldMatch("25%"));

        QueryBuilder skuQuery = this.generateSkuQuery(t);

        boolQuery.filter().add(QueryBuilders.termQuery("status", 1));
        if (null != skuQuery) {
            boolQuery.filter().add(skuQuery);
        }
        if (null != t.getCid()) {
            boolQuery.filter().add(QueryBuilders.termQuery("cid", t.getCid()));
        }
        if (null != t.getBid()) {
            boolQuery.filter().add(QueryBuilders.termsQuery("bid", t.getBid()));
        }
        if (null != t.getLabel()) {
            boolQuery.filter().add(QueryBuilders.termsQuery("label", t.getLabel()));
        }
        if (null != t.getPlatform()) {
            boolQuery.filter().add(QueryBuilders.termQuery("platform", t.getPlatform()));
        }

        return boolQuery;
    }

    private ScoreFunctionBuilder generateScoreFunc_LAST() {
        GaussDecayFunctionBuilder func = ScoreFunctionBuilders.gaussDecayFunction(
                "marketDate",
                new Date(System.currentTimeMillis()),
                "7d",
                "0d",
                0.2);
        return func;
    }
    private ScoreFunctionBuilder generateScoreFunc_PRICE_ASC() {
        GaussDecayFunctionBuilder func = ScoreFunctionBuilders.gaussDecayFunction(
                "price",
                0,
                "10",
                "0d",
                0.2);
        return func;
    }
    private ScoreFunctionBuilder generateScoreFunc_PRICE_DESC() {
        FieldValueFactorFunctionBuilder func = ScoreFunctionBuilders.fieldValueFactorFunction("price");
        func.modifier(FieldValueFactorFunction.Modifier.LOG1P);
        func.factor(0.1f);
        func.missing(0);
        return func;
    }
    private ScoreFunctionBuilder generateScoreFunc_SALES() {
        FieldValueFactorFunctionBuilder func = ScoreFunctionBuilders.fieldValueFactorFunction("sales");
        func.modifier(FieldValueFactorFunction.Modifier.LOG1P);
        func.factor(0.01f);
        func.missing(0);
        return func;
    }
    private ScoreFunctionBuilder generateScoreFunc_EVALS() {
        FieldValueFactorFunctionBuilder func = ScoreFunctionBuilders.fieldValueFactorFunction("evals");
        func.modifier(FieldValueFactorFunction.Modifier.LOG1P);
        func.factor(0.01f);
        func.missing(0);
        return func;
    }
    private ScoreFunctionBuilder generateScoreFunc_COMPOSITE() {
        FieldValueFactorFunctionBuilder func = ScoreFunctionBuilders.fieldValueFactorFunction("mark");
        func.modifier(FieldValueFactorFunction.Modifier.LOG1P);
        func.missing(0);
        return func;
    }
    private QueryBuilder generateFuncScoreQuery(String order, QueryBuilder query) {
        ScoreFunctionBuilder func;
        switch (order) {
            case "LAST":
                func = this.generateScoreFunc_LAST();
                break;
            case "PRICE_ASC":
                func = this.generateScoreFunc_PRICE_ASC();
                break;
            case "PRICE_DESC":
                func = this.generateScoreFunc_PRICE_DESC();
                break;
            case "SALES":
                func = this.generateScoreFunc_SALES();
                break;
            case "EVALS":
                func = this.generateScoreFunc_EVALS();
                break;
            default:
                func = this.generateScoreFunc_COMPOSITE();
        }

        FunctionScoreQueryBuilder fQuery = QueryBuilders.functionScoreQuery(query, func);
        fQuery.boostMode(CombineFunction.AVG);
        return fQuery;
    }

    /*
    private SortBuilder generateSpuSort(String order) {
        if ("PRICE_ASC".equals(ESpuConditionBean.orders.format(order))) {
            return SortBuilders.fieldSort("sku.mprice")
                    .order(SortOrder.ASC)
                    .sortMode(SortMode.MIN)
                    .setNestedSort(new NestedSortBuilder("sku"));
        }
        else if ("PRICE_DESC".equals(ESpuConditionBean.orders.format(order))) {
            return SortBuilders.fieldSort("sku.mprice")
                    .order(SortOrder.DESC)
                    .sortMode(SortMode.MIN)
                    .setNestedSort(new NestedSortBuilder("sku"));
        }
        else if ("LAST".equals(ESpuConditionBean.orders.format(order))) {
            return SortBuilders.fieldSort("id").order(SortOrder.DESC);
        }
        else if ("SALES".equals(ESpuConditionBean.orders.format(order))) {
            return SortBuilders.fieldSort("sales").order(SortOrder.DESC);
        }
        else if ("EVALS".equals(ESpuConditionBean.orders.format(order))) {
            return SortBuilders.fieldSort("sales").order(SortOrder.DESC);
        }
        else {
            return SortBuilders
                    .scriptSort(
                            new Script("_score + doc['mark'].value")
                            ,ScriptSortBuilder.ScriptSortType.NUMBER)
                    .order(SortOrder.DESC);
        }
    }
    */

    private FetchSourceContext generateFetchSourceContext() {
        String[] resIncludeFields = {"id", "shows", "pic", "sales", "evals", "praise"};
        String[] resExcludeFields = {};
        FetchSourceContext resFetchSourceContext = new FetchSourceContext(true, resIncludeFields, resExcludeFields);
        return resFetchSourceContext;
    }

    private List<AggregationBuilder> generateAggregationList(ESpuConditionBean t) {
        List<AggregationBuilder> list = new ArrayList<>();
        if (t.getRefreshBrand()) {
            TermsAggregationBuilder aggBrand = AggregationBuilders.terms("agg_brand");
            aggBrand.field("bid");
            aggBrand.order(BucketOrder.count(false));
            list.add(aggBrand);
        }
        if (t.getRefreshCatalog()) {
            TermsAggregationBuilder aggCatalog = AggregationBuilders.terms("agg_catalog");
            aggCatalog.field("cid");
            aggCatalog.order(BucketOrder.count(false));
            list.add(aggCatalog);
        }
        if (t.getRefreshPara()) {
            NestedAggregationBuilder aggSku = AggregationBuilders.nested("agg_sku", "sku");
            NestedAggregationBuilder aggPara = AggregationBuilders.nested("agg_para", "sku.para");
            TermsAggregationBuilder aggPid = AggregationBuilders.terms("agg_pid");
            aggPid.field("sku.para.pid");
            aggPid.order(BucketOrder.count(false));
            TermsAggregationBuilder aggVid = AggregationBuilders.terms("agg_vid");
            aggVid.field("sku.para.vid");
            aggVid.order(BucketOrder.count(false));
            aggPid.subAggregation(aggVid);
            aggPara.subAggregation(aggPid);
            aggSku.subAggregation(aggPara);
            list.add(aggSku);
        }
        return list;
    }

    @Override
    public ESpuModel query(ESpuConditionBean t,
                           Integer pageNo,
                           Integer pageSize,
                           String order) throws IOException {
        SearchRequest searchRequest = new SearchRequest(INDEX_SPU);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        sourceBuilder.query(this.generateFuncScoreQuery(order, this.generateSpuQuery(t)));
        //sourceBuilder.sort(this.generateSpuSort(order));
        sourceBuilder.fetchSource(this.generateFetchSourceContext());

        sourceBuilder.from(pageNo-1);
        sourceBuilder.size(pageSize);
        //sourceBuilder.trackScores(true);
        this.generateAggregationList(t).forEach(o -> sourceBuilder.aggregations().addAggregator(o));
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = esclient.search(searchRequest, RequestOptions.DEFAULT);

        ESpuModel data = this.convertor(searchResponse);

        return data;
    }

    private ESpuModel convertor(SearchResponse searchResponse) {
        ESpuModel model = new ESpuModel();
        model.setSpuList(new ArrayList<>());
        model.setBrandList(new ArrayList<>());
        model.setCatalogList(new ArrayList<>());
        model.setParaList(new HashMap<>());

        for (SearchHit entry : searchResponse.getHits().getHits()) {
            Map<String, Object> map = entry.getSourceAsMap();
            Map<String, Object> innerMap = entry.getInnerHits().get("sku").getHits()[0].getSourceAsMap();

            ESpu spu = new ESpu();
            spu.setId(MapUtils.getLong(map, "id", null));

            spu.setShows(MapUtils.getString(map, "shows", null));
            spu.setPic(MapUtils.getString(map, "pic", null));
            spu.setSales(MapUtils.getInteger(map, "shows", 0));
            spu.setEvals(MapUtils.getInteger(map, "evals", 0));
            spu.setPraise(MapUtils.getInteger(map, "praise", 0));

            spu.setSkuid(MapUtils.getLong(innerMap, "id", null));
            spu.setTitle(MapUtils.getString(innerMap, "title", null));
            spu.setMprice(MapUtils.getInteger(innerMap, "mprice", null));
            spu.setPprice(MapUtils.getInteger(innerMap, "pprice", null));

            model.getSpuList().add(spu);
        }

        Aggregations aggregations = searchResponse.getAggregations();
        if (null != aggregations) {
            Terms aggBrand = aggregations.get("agg_brand");
            if (null != aggBrand && !aggBrand.getBuckets().isEmpty()) {
                aggBrand.getBuckets().forEach(o ->
                    model.getBrandList().add(Long.valueOf(o.getKeyAsString()))
                );
            }

            Terms aggCatalog = aggregations.get("agg_catalog");
            if (null != aggCatalog && !aggCatalog.getBuckets().isEmpty()) {
                aggCatalog.getBuckets().forEach(o ->
                    model.getCatalogList().add(Long.valueOf(o.getKeyAsString()))
                );
            }

            Nested skuGroup = aggregations.get("sku_group");
            if (null != skuGroup) {
                Nested parasGroup = skuGroup.getAggregations().get("paras_group");
                Terms parasNameGroup = parasGroup.getAggregations().get("paras_name_group");
                if (null != parasNameGroup && !parasNameGroup.getBuckets().isEmpty()) {
                    parasNameGroup.getBuckets().forEach(o -> {
                        Terms parasValueGroup = o.getAggregations().get("paras_value_group");
                        if (null != parasValueGroup && !parasValueGroup.getBuckets().isEmpty()) {
                            List<Long> paraList = new ArrayList<>();
                            parasValueGroup.getBuckets().forEach(p ->
                                paraList.add(Long.valueOf(p.getKeyAsString()))
                            );
                            model.getParaList().put(Long.valueOf(o.getKeyAsString()), paraList);
                        }
                    });
                }
            }
        }

        model.setTotal(new Long(searchResponse.getHits().getTotalHits().value).intValue());
        return model;
    }
}
