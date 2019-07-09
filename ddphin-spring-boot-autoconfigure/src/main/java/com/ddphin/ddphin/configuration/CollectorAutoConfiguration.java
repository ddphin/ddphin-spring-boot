package com.ddphin.ddphin.configuration;
/**
 * ClassName: CollectorAutoConfiguration
 * Function:  CollectorAutoConfiguration
 * Date:      2019/7/2 下午8:39
 * Author     DaintyDolphin
 * Version    V1.0
 */


import com.ddphin.ddphin.collector.collector.impl.DefaultCollector;
import com.ddphin.ddphin.collector.entity.ESSyncProperties;
import com.ddphin.ddphin.collector.interceptor.CollectorInterceptor;
import com.ddphin.ddphin.collector.interceptor.SynchronizerInterceptor;
import com.ddphin.ddphin.collector.requestbody.RequestBodyBuilder;
import com.ddphin.ddphin.collector.requestbody.impl.DefaultRequestBodyBuilder;
import com.ddphin.ddphin.synchronizer.listener.EBulkProcessorListener;
import com.ddphin.ddphin.synchronizer.requester.ESRequester;
import com.ddphin.ddphin.synchronizer.requester.impl.DefaultESRequester;
import com.ddphin.ddphin.synchronizer.service.ESVersionService;
import com.ddphin.ddphin.transmitor.BulkRequestBodyTransmitor;
import com.ddphin.ddphin.transmitor.impl.DefaultBulkRequestBodyTransmitor;
import org.apache.http.HttpHost;
import org.apache.ibatis.session.SqlSessionFactory;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
@ConditionalOnBean({SqlSessionFactory.class})
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class CollectorAutoConfiguration implements WebMvcConfigurer {
    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;

    @Autowired(required = false)
    private ESVersionService esVersionService;

    @Bean
    @ConfigurationProperties(prefix=ESClientProperties.prefix)
    public ESClientProperties esClientProperties() {
        return new ESClientProperties();
    }


    @Bean
    @ConfigurationProperties(prefix= ESSyncProperties.COLLECTOR_PREFIX)
    public ESSyncProperties esSyncProperties() {
        return new ESSyncProperties();
    }

    @PostConstruct
    public void addCollector() {
        ESSyncProperties properties = this.esSyncProperties();
        if (properties.validate()) {
            DefaultCollector collector = new DefaultCollector(properties);
            CollectorInterceptor interceptor = new CollectorInterceptor(properties, collector);
            for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
                sqlSessionFactory.getConfiguration().addInterceptor(interceptor);
            }
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (null != this.esSyncProperties().getApi() && !this.esSyncProperties().getApi().isEmpty()) {
            ESClientProperties esproperties = this.esClientProperties();
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(
                                    esproperties.getHost(), esproperties.getPort(), esproperties.getScheme())));

            BulkProcessor bulkProcessor = BulkProcessor.builder(
                    (request, bulkListener) -> client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                    new EBulkProcessorListener(esVersionService))
                    .setBulkActions(1000)
                    .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
                    .setFlushInterval(TimeValue.timeValueSeconds(5))
                    .setConcurrentRequests(1)
                    .build();

            ESRequester esRequester = new DefaultESRequester(bulkProcessor);
            BulkRequestBodyTransmitor bulkRequestBodyTransmitor = new DefaultBulkRequestBodyTransmitor(esRequester);
            RequestBodyBuilder requestBodyBuilder = new DefaultRequestBodyBuilder(this.esSyncProperties());
            SynchronizerInterceptor synchronizerInterceptor = new SynchronizerInterceptor(requestBodyBuilder, bulkRequestBodyTransmitor);

            registry.addInterceptor(synchronizerInterceptor).addPathPatterns(this.esSyncProperties().getApi());
        }
    }
}