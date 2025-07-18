package com.kcs3.auction.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ElasticSearchConfig {

    @Value("${elasticsearch.host}")
    private String elasticsearchHost;

    @Bean
    public ElasticsearchClient elasticsearchClient() throws URISyntaxException {
        URI uri = new URI(elasticsearchHost);
        RestClient restClient = RestClient.builder(
            new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme())
        ).build();

        RestClientTransport transport = new RestClientTransport(
            restClient, new JacksonJsonpMapper()
        );

        return new ElasticsearchClient(transport);
    }
}





//@Slf4j
//@Configuration
//public class ElasticSearchConfig {
//
//    @Value("${elasticsearch.host}")
//    private String elasticsearchHost;
//
//    @Value("${elasticsearch.connect-timeout:5000}")
//    private int connectTimeout;
//
//    @Value("${elasticsearch.read-timeout:60000}")
//    private int readTimeout;
//
//    @Bean
//    public ElasticsearchClient elasticsearchClient() throws URISyntaxException {
//        URI uri = new URI(elasticsearchHost);
//
//        RestClient restClient = RestClient.builder(
//                new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme())
//            )
//            .setRequestConfigCallback(requestConfigBuilder ->
//                requestConfigBuilder
//                    .setConnectTimeout(connectTimeout)        // 연결 타임아웃
//                    .setSocketTimeout(readTimeout)           // 읽기 타임아웃
//                    .setConnectionRequestTimeout(3000)       // 커넥션 풀 대기 타임아웃
//            )
//            .build();
//
//        RestClientTransport transport = new RestClientTransport(
//            restClient, new JacksonJsonpMapper()
//        );
//
//        return new ElasticsearchClient(transport);
//    }
//}