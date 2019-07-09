# ddphin-spring-boot
## Collect the complex data and sync to elasticsearch intrusivly and automaticly.

## Elasticsearch Configuration

add the configuration to your application.yml
## explain
- `elasticsearch.repo`: is the elasticsearch server configuration, just ignore it if you do not transmit the data to elasticsearch directly.
- `elasticsearch.sync`: is the elasticsearch sync configuration, `ddphin` need it to collect the data and then convert to elasticsearch request body, `ddphin` has a default `transmitor`, with which, the request body will transmit to the elasticsearch server directly (if you have config the `elasticsearch.repo`). 
  - `api`: a list, tell `ddphin` which apis will be listened to collect the data.
  - `point`: `INSERT/UPDATE/DELETE` is the supported mybatis command, `AFTER` tell `ddphin` collect data `AFTER` the mybatis command, BEFORE tell `ddphin` collect data before teh mybatis command.
  - `input`: is a K-V map, the key tells `ddphin` which mybatis will be listened, and the value is the entity corresponds to the mapper.(usually, the mapper is the handler of a single table, and the entity is the data of the table).
  - `output`: is a K-V map, is the relation definition for the whole entity, finally, all the data will be convert to the elasticsearch index mapping base on the `output`configuration.
    - `key`: the entity key, will use to be cache(every mapper has a cache), usually, it is the id.
    - `map`: map entity column to elasticsearch simple property.
    - `has`: is a K-V map, value indicate elasticsearch array property or nested property.
      - `with-type`: `primitive` add the field as the simple property directly.`nested` is the elasticsearch nested property array, `array` is the array of simple property array.
      - `as`: just for `nested` and `array`, is the array name
      - `unique-by`: just for `nested`, is the unique property in the array.
      - `association`: is the association relation to parent `output`.
        - `belongs.to`: the parent `output`
        - `belongs.with`: the association key will used to associate the parent `output`
        - `belongs.as`: the name in parent `output`
        - `on-missing.query`: the method used to query the parent `output` when `belongs.with` is missing. it can be the method of the mapper, or a sql command.
        - `on-missing.with`: the param of the current `output` used to query the parent `output`.
      - `reload`: will be ignored if`on-missing`is triggered.
        - `query`: the method used to query the current `output` and then reload the data
        - `with`: the param of the current `output` used to reload data.
## example
```
elasticsearch:
  repo:
    host: 127.0.0.1
    port: 9200
    scheme: http
  sync:
    api:
      - '/**'
    point:
      INSERT: AFTER
      UPDATE: AFTER
      DELETE: BEFORE
    input:
      com.ddphin.ddphin.business.commodity.mapper.MSpuMapper: spu
      com.ddphin.ddphin.business.commodity.mapper.MSpuStockMapper: spu_stock
      com.ddphin.ddphin.business.commodity.mapper.MSpuMarkMapper: spu_mark
      com.ddphin.ddphin.business.commodity.mapper.MSpuLabelMapper: spu_label
      com.ddphin.ddphin.business.commodity.mapper.MSpuPlatformMapper: spu_platform
      com.ddphin.ddphin.business.commodity.mapper.MSpuServiceMapper: spu_service
      com.ddphin.ddphin.business.commodity.mapper.MSkuMapper: sku
      com.ddphin.ddphin.business.commodity.mapper.MSkuParaMapper: sku_para
      com.ddphin.ddphin.business.commodity.mapper.MSkuStockMapper: sku_stock
    output:
      spu:
        key: id
        map:
          id: id
          cid: cid
          bid: bid
          status: status
          summary: summary
          pic: pic
          shows: shows
          marketDate: marketDate
        has:
          sku:
            with-type: nested
            as: sku
            unique-by: id
          spu_label:
            with-type: array
            as: label
          spu_platform:
            with-type: array
            as: platform
          spu_service:
            with-type: array
            as: service
          spu_mark:
            with-type: primitive
          spu_stock:
            with-type: primitive
      spu_mark:
        association:
          belongs:
            to: spu
            with: spuid
            as: id
          on-missing:
            query: query
            with: id
        key: spuid
        map:
          mark: mark
      spu_stock:
        association:
          belongs:
            to: spu
            with: spuid
            as: id
          on-missing:
            query: query
            with: id
        reload:
          query: query
          with: spuid
        key: spuid
        map:
          sales: sales
          praise: praise
          comment: comment
          poor: poor
      spu_label:
        association:
          belongs:
            to: spu
            with: spuid
            as: id
          on-missing:
            query: query
            with: id
        key: id
        map:
          lid: lid
      spu_platform:
        association:
          belongs:
            to: spu
            with: spuid
            as: id
          on-missing:
            query: query
            with: id
        key: id
        map:
          type: type
      spu_service:
        key: id
        map:
          sid: sid
        association:
          belongs:
            to: spu
            with: spuid
            as: id
          on-missing:
            query: query
            with: id
      sku:
        key: id
        map:
          id: id
          title: title
          mprice: mprice
          pprice: pprice
          vprice: vprice
        has:
          sku_stock:
            with-type: primitive
          sku_para:
            with-type: nested
            as: para
            unique-by: pid
        association:
          belongs:
            to: spu
            with: spuid
            as: id
          on-missing:
            query: query
            with: id
      sku_stock:
        key: skuid
        map:
          inventory: inventory
        association:
          belongs:
            to: sku
            with: skuid
            as: id
          on-missing:
            query: query
            with: id
        reload:
          query: query
          with: skuid
      sku_para:
        key: id
        map:
          pid: pid
          vid: vid
        association:
          belongs:
            to: sku
            with: sid
            as: id
          on-missing:
            query: query
            with: id
```

## elasticsearch index mapping
```
{
    "spu": {
        "mappings": {
            "properties": {
                "bid": {
                    "type": "keyword"
                },
                "cid": {
                    "type": "keyword"
                },
                "comment": {
                    "type": "long"
                },
                "evals": {
                    "type": "integer",
                    "index": false,
                    "null_value": 0
                },
                "id": {
                    "type": "keyword"
                },
                "label": {
                    "type": "keyword"
                },
                "mark": {
                    "type": "float",
                    "index": false,
                    "null_value": 1
                },
                "marketDate": {
                    "type": "date"
                },
                "pic": {
                    "type": "text",
                    "index": false
                },
                "platform": {
                    "type": "keyword"
                },
                "poor": {
                    "type": "long"
                },
                "praise": {
                    "type": "integer",
                    "index": false,
                    "null_value": 0
                },
                "price": {
                    "type": "integer"
                },
                "sales": {
                    "type": "integer",
                    "index": false,
                    "null_value": 0
                },
                "service": {
                    "type": "keyword"
                },
                "shows": {
                    "type": "text",
                    "index": false
                },
                "sku": {
                    "type": "nested",
                    "properties": {
                        "id": {
                            "type": "keyword"
                        },
                        "inventory": {
                            "type": "integer",
                            "null_value": 0
                        },
                        "mprice": {
                            "type": "integer",
                            "index": false
                        },
                        "para": {
                            "type": "nested",
                            "properties": {
                                "pid": {
                                    "type": "keyword"
                                },
                                "vid": {
                                    "type": "keyword"
                                }
                            }
                        },
                        "pprice": {
                            "type": "integer",
                            "index": false
                        },
                        "title": {
                            "type": "text",
                            "index": false
                        }
                    }
                },
                "status": {
                    "type": "keyword"
                },
                "summary": {
                    "type": "text",
                    "analyzer": "ikpyIndexAnalyzer",
                    "search_analyzer": "ikpySearchAnalyzer"
                }
            }
        }
    }
}
```

# elasticsearch sync version and log

you can implement the interface `ESVersionService` to handle the elasticsearch sync version and log.

- `version`: just for success case
```
public class ESVersionBean {
    private String index;// elasearch index
    private String id;// document id
    private Long seqNo;// elasearch seqNo
    private Long primaryTerm;// elasearch primaryTerm
    }
```
- `version log`: for every case
```
public class ESVersionLogBean {
    private String index;// elasearch index
    private String id;// document id
    private Long seqNo;// elasearch seqNo
    private Long primaryTerm;// elasearch primaryTerm
    private Long lid;// execute id
    private String context;//request data
    private String message;// failed message
    private Integer status;// 0:success; 1:failed; -1:not executed
```
- `esVersion Service`:
```
public interface ESVersionService {
    Integer replaceList(List<ESVersionBean> var1);// save/update version
    Integer insertLogList(List<ESVersionLogBean> var1);// save log
}
```

# handler the request body
`ddphin` will collect data and convert the whole data to bulk request body, you can implement `BulkRequestBodyTransmitor` to handler the request body, such as transmit it to message queue.
by default, ddphin transmit the request body to elasticsearch with bulk processor directly if you have config the `elasticsearch.repo`.
- `BulkRequestBodyTransmitor`:
```
public interface BulkRequestBodyTransmitor {
    void transmit(String var1) throws IOException;
}
```
