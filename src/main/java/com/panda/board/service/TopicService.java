package com.panda.board.service;

import com.panda.board.domain.Topic;
import com.panda.board.repository.TopicRepository;
import com.panda.board.repository.search.TopicSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing {@link Topic}.
 */
@Service
@Transactional
public class TopicService {

    private final Logger log = LoggerFactory.getLogger(TopicService.class);

    private final TopicRepository topicRepository;

    private final TopicSearchRepository topicSearchRepository;

    public TopicService(TopicRepository topicRepository, TopicSearchRepository topicSearchRepository) {
        this.topicRepository = topicRepository;
        this.topicSearchRepository = topicSearchRepository;
    }

    /**
     * Save a topic.
     *
     * @param topic the entity to save.
     * @return the persisted entity.
     */
    public Topic save(Topic topic) {
        log.debug("Request to save Topic : {}", topic);
        Topic result = topicRepository.save(topic);
        topicSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the topics.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<Topic> findAll() {
        log.debug("Request to get all Topics");
        return topicRepository.findAll();
    }


    /**
     * Get one topic by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Topic> findOne(Long id) {
        log.debug("Request to get Topic : {}", id);
        return topicRepository.findById(id);
    }

    /**
     * Delete the topic by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Topic : {}", id);
        topicRepository.deleteById(id);
        topicSearchRepository.deleteById(id);
    }

    /**
     * Search for the topic corresponding to the query.
     *
     * @param query the query of the search.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<Topic> search(String query) {
        log.debug("Request to search Topics for query {}", query);
        return StreamSupport
            .stream(topicSearchRepository.search(queryStringQuery(query)).spliterator(), false)
        .collect(Collectors.toList());
    }
}
