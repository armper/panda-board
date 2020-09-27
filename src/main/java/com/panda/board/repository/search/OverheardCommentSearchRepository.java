package com.panda.board.repository.search;

import com.panda.board.domain.OverheardComment;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


/**
 * Spring Data Elasticsearch repository for the {@link OverheardComment} entity.
 */
public interface OverheardCommentSearchRepository extends ElasticsearchRepository<OverheardComment, Long> {
}
