package com.panda.board.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of {@link OverheardCommentSearchRepository} to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class OverheardCommentSearchRepositoryMockConfiguration {

    @MockBean
    private OverheardCommentSearchRepository mockOverheardCommentSearchRepository;

}
