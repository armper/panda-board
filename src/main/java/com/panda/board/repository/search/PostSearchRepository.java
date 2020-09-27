package com.panda.board.repository.search;

import com.panda.board.domain.Post;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


/**
 * Spring Data Elasticsearch repository for the {@link Post} entity.
 */
public interface PostSearchRepository extends ElasticsearchRepository<Post, Long> {
}
