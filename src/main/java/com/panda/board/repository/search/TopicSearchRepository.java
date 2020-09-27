package com.panda.board.repository.search;

import com.panda.board.domain.Topic;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


/**
 * Spring Data Elasticsearch repository for the {@link Topic} entity.
 */
public interface TopicSearchRepository extends ElasticsearchRepository<Topic, Long> {
}
