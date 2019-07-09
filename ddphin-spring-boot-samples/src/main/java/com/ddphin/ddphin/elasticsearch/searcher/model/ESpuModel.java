package com.ddphin.ddphin.elasticsearch.searcher.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * ClassName: ESpu
 * Function:  ESpu
 * Date:      2019/6/21 上午10:00
 * Author     DaintyDolphin
 * Version    V1.0
 */
@Data
public class ESpuModel {
    private List<ESpu> spuList;
    private List<Long> catalogList;
    private List<Long> brandList;
    private Map<Long, List<Long>> paraList;
    private Integer total;
}
