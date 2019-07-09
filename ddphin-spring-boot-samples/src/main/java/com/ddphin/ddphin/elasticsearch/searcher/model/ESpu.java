package com.ddphin.ddphin.elasticsearch.searcher.model;

import lombok.Data;

/**
 * ClassName: ESpu
 * Function:  ESpu
 * Date:      2019/6/21 上午10:00
 * Author     DaintyDolphin
 * Version    V1.0
 */
@Data
public class ESpu {
    private Long id;
    private Long skuid;
    private String title;
    private Integer pprice;
    private Integer mprice;
    private Integer sales;
    private Integer evals;
    private Integer praise;
    private String pic;
    private String shows;
}
