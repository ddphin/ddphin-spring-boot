package com.ddphin.ddphin.elasticsearch.searcher.bean;

import lombok.Data;
import com.ddphin.ddphin.business.common.bean.CBaseBean;

import javax.validation.constraints.NotBlank;

/**
 * ClassName: ESearchkeysBean
 * Function:  ESearchkeysBean
 * Date:      2019/6/21 下午3:06
 * Author     DaintyDolphin
 * Version    V1.0
 */
@Data
public class ESearchkeysBean extends CBaseBean {
    @NotBlank
    private String input;

    public final static ORDERS orders = ORDERS.build(
            ORDERS.create("",""));
}
