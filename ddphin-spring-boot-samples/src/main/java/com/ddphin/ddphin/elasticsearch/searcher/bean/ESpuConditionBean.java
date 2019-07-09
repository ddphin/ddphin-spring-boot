package com.ddphin.ddphin.elasticsearch.searcher.bean;

import lombok.Data;
import com.ddphin.ddphin.business.common.bean.CBaseBean;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * ClassName: ESpuConditionBean
 * Function:  ESpuConditionBean
 * Date:      2019/6/21 下午3:06
 * Author     DaintyDolphin
 * Version    V1.0
 */
@Data
public class ESpuConditionBean extends CBaseBean {
    @NotEmpty
    private String match;

    private List<Long>  bid;
    private Long cid;
    private List<String> label;
    private Integer platform;

    private Map<Long, List<Long>> para;
    private Boolean available;

    private Boolean refreshBrand = false;
    private Boolean refreshCatalog = false;
    private Boolean refreshPara = false;

    private Integer minprice;
    private Integer maxprice;

    public final static ORDERS orders = ORDERS.build(
            ORDERS.create("LAST","LAST"),
            ORDERS.create("PRICE_ASC","PRICE_ASC"),
            ORDERS.create("PRICE_DESC","PRICE_DESC"),
            ORDERS.create("SALES","SALES"),
            ORDERS.create("EVALS","EVALS"),
            ORDERS.create("COMPOSITE","COMPOSITE"));
}
