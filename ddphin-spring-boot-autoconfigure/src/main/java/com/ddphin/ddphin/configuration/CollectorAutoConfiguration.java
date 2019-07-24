package com.ddphin.ddphin.configuration;
/**
 * ClassName: CollectorAutoConfiguration
 * Function:  CollectorAutoConfiguration
 * Date:      2019/7/2 下午8:39
 * Author     DaintyDolphin
 * Version    V1.0
 */


import com.ddphin.ddphin.collector.collector.Collector;
import com.ddphin.ddphin.collector.collector.impl.DefaultCollector;
import com.ddphin.ddphin.collector.context.ContextHolder;
import com.ddphin.ddphin.collector.entity.ESSyncProperties;
import com.ddphin.ddphin.collector.requestbody.RequestBodyBuilder;
import com.ddphin.ddphin.collector.requestbody.impl.DefaultBulkRequestBodyBuilder;
import com.ddphin.ddphin.interceptor.CollectorInterceptor;
import com.ddphin.ddphin.synchronizer.listener.EBulkProcessorListener;
import com.ddphin.ddphin.synchronizer.requester.ESRequester;
import com.ddphin.ddphin.synchronizer.requester.impl.DefaultESRequester;
import com.ddphin.ddphin.synchronizer.service.ESVersionService;
import com.ddphin.ddphin.transmitor.RequestBodyTransmitor;
import com.ddphin.ddphin.transmitor.impl.DefaultBulkRequestBodyTransmitor;
import org.apache.http.HttpHost;
import org.apache.ibatis.session.SqlSessionFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
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

import javax.annotation.PostConstruct;
import java.util.List;

@Aspect
@Configuration
@ConditionalOnBean({SqlSessionFactory.class})
@AutoConfigureAfter(MybatisAutoConfiguration.class)
public class CollectorAutoConfiguration {
    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;

    @Autowired(required = false)
    private ESVersionService esVersionService;
    @Autowired(required = false)
    private RequestBodyTransmitor customizedRequestBodyTransmitor;
    @Autowired(required = false)
    private RequestBodyBuilder customizedRequestBodyBuilder;
    @Autowired(required = false)
    private Collector customizedCollector;

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

    @Bean
    public RestHighLevelClient esclient() {
        ESClientProperties esproperties = this.esClientProperties();
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(
                                esproperties.getHost(), esproperties.getPort(), esproperties.getScheme())));
        return client;
    }

    @PostConstruct
    public void addCollector() {
        ESSyncProperties properties = this.esSyncProperties();
        if (properties.validate()) {
            Collector collector = this.customizedCollector;
            if (null == collector) {
                collector = new DefaultCollector(properties);
            }
            CollectorInterceptor interceptor = new CollectorInterceptor(properties, collector);
            for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
                sqlSessionFactory.getConfiguration().addInterceptor(interceptor);
            }
        }
    }

    @PostConstruct
    public void initRequestBodyBuilder() {
        if (null == this.customizedRequestBodyTransmitor) {
            BulkProcessor bulkProcessor = BulkProcessor.builder(
                    (request, bulkListener) -> this.esclient().bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                    new EBulkProcessorListener(esVersionService))
                    .setBulkActions(1000)
                    .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
                    .setFlushInterval(TimeValue.timeValueSeconds(5))
                    .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                    .setConcurrentRequests(1)
                    .build();

            ESRequester esRequester = new DefaultESRequester(bulkProcessor);
            this.customizedRequestBodyTransmitor = new DefaultBulkRequestBodyTransmitor(esRequester);
        }
    }
    @PostConstruct
    public void initRequestBodyTransmitor() {
        if (null == this.customizedRequestBodyBuilder) {
            this.customizedRequestBodyBuilder = new DefaultBulkRequestBodyBuilder(this.esSyncProperties());
        }
        else {
            this.customizedRequestBodyBuilder.setOutputMap(this.esSyncProperties().getOutput());
        }
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void PostMappingPointcut() {}
    @Pointcut("@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void DeleteMappingPointcut() {}
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void PutMappingPointcut() {}
    @Around("PostMappingPointcut() || DeleteMappingPointcut() || PutMappingPointcut()")
    public Object doSynchronizerInterceptor(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            ContextHolder.remove();
            Object obj = joinPoint.proceed();
            String body = this.customizedRequestBodyBuilder.build();
            this.customizedRequestBodyTransmitor.transmit(body);
            return obj;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw throwable;
        }
        finally {
            ContextHolder.remove();
        }
    }
}