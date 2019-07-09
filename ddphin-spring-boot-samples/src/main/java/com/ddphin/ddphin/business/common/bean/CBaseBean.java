package com.ddphin.ddphin.business.common.bean;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ClassName: MProperty
 * Function:  Commodity Property Bean
 * Date:      2019/6/17 下午2:48
 * Author     DaintyDolphin
 * Version    V1.0
 */
public class CBaseBean {

    public static class ORDERS {
        public static class ORDER {
            private String name;
            private String value;
            private ORDER(String name, String value) {
                this.name = name;
                this.value = value;
            }

            @Override
            public String toString() {
                return this.value;
            }
        }

        private final ORDER[] orders;

        private ORDERS(ORDER[] orders) {
            this.orders = orders;
        }

        public static ORDER create(String name, String value) {
            return new ORDER(name, value);
        }

        public static ORDERS build(ORDER... orders) {
            return new ORDERS(orders);
        }

        private ORDER fromName(String name) {
            Optional<ORDER> order = Arrays.stream(this.orders).filter(o -> o.name.equals(name)).findFirst();
            return order.isPresent() ? order.get() : null;
        }

        public String format(String... orders) {
            if (null != orders) {
                String formated = Arrays.stream(orders).filter(o -> null != this.fromName(o)).map(o -> this.fromName(o).value).collect(Collectors.joining(", "));
                if (null != formated && 0 < formated.length()) {
                    return formated;
                }
            }
            return null;
        }
    }
}
