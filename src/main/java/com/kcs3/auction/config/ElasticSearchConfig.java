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
import org.springframework.context.annotation.Profile;

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
