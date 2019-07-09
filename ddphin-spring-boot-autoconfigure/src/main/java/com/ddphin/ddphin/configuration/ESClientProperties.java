package com.ddphin.ddphin.configuration;

import lombok.Data;

/**
 * ClassName: ESClientProperties
 * Function:  ESClientProperties
 * Date:      2019/6/21 下午2:37
 * Author     DaintyDolphin
 * Version    V1.0
 */
@Data
public class ESClientProperties {
    public static final String prefix = "elasticsearch.repo";
    private String scheme;
    private String host;
    private Integer port;
}
