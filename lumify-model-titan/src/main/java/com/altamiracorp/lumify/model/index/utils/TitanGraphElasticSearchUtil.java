package com.altamiracorp.lumify.model.index.utils;

import com.altamiracorp.lumify.core.config.Configuration;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class TitanGraphElasticSearchUtil implements TitanGraphSearchIndexProviderUtil {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(TitanGraphElasticSearchUtil.class);
    private static final String STORAGE_INDEX_SEARCH_HOSTNAME = "storage.index.search.hostname";
    private static final String STORAGE_INDEX_SEARCH_PORT = "storage.index.search.port";
    private static final String STORAGE_INDEX_SEARCH_INDEX_NAME = "storage.index.search.indexName";

    private Configuration titanConfig;

    public TitanGraphElasticSearchUtil(Configuration titanConfig) {
        this.titanConfig = titanConfig;
    }

    @Override
    public void deleteIndex() {
        String indexName = titanConfig.get(STORAGE_INDEX_SEARCH_INDEX_NAME);
        LOGGER.info("delete search index: %s", indexName);
        TransportClient client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(titanConfig.get(STORAGE_INDEX_SEARCH_HOSTNAME), titanConfig.getInt(STORAGE_INDEX_SEARCH_PORT)));
        client.admin().indices().delete(new DeleteIndexRequest(indexName)).actionGet();
    }
}
