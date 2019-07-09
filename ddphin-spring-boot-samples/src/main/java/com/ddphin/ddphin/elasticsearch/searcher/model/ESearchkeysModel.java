package com.ddphin.ddphin.elasticsearch.searcher.model;

import lombok.Data;

import java.util.List;

/**
 * ClassName: ESearchkeys
 * Function:  ESearchkeys
 * Date:      2019/6/21 上午10:00
 * Author     DaintyDolphin
 * Version    V1.0
 */
@Data
public class ESearchkeysModel {
    private List<ESearchkeys> keyList;
    private Integer total;
}
