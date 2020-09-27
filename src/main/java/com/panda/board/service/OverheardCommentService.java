package com.panda.board.service;

import com.panda.board.domain.OverheardComment;
import com.panda.board.repository.OverheardCommentRepository;
import com.panda.board.repository.search.OverheardCommentSearchRepository;
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
 * Service Implementation for managing {@link OverheardComment}.
 */
@Service
@Transactional
public class OverheardCommentService {

    private final Logger log = LoggerFactory.getLogger(OverheardCommentService.class);

    private final OverheardCommentRepository overheardCommentRepository;

    private final OverheardCommentSearchRepository overheardCommentSearchRepository;

    public OverheardCommentService(OverheardCommentRepository overheardCommentRepository, OverheardCommentSearchRepository overheardCommentSearchRepository) {
        this.overheardCommentRepository = overheardCommentRepository;
        this.overheardCommentSearchRepository = overheardCommentSearchRepository;
    }

    /**
     * Save a overheardComment.
     *
     * @param overheardComment the entity to save.
     * @return the persisted entity.
     */
    public OverheardComment save(OverheardComment overheardComment) {
        log.debug("Request to save OverheardComment : {}", overheardComment);
        OverheardComment result = overheardCommentRepository.save(overheardComment);
        overheardCommentSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the overheardComments.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<OverheardComment> findAll() {
        log.debug("Request to get all OverheardComments");
        return overheardCommentRepository.findAll();
    }


    /**
     * Get one overheardComment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<OverheardComment> findOne(Long id) {
        log.debug("Request to get OverheardComment : {}", id);
        return overheardCommentRepository.findById(id);
    }

    /**
     * Delete the overheardComment by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete OverheardComment : {}", id);
        overheardCommentRepository.deleteById(id);
        overheardCommentSearchRepository.deleteById(id);
    }

    /**
     * Search for the overheardComment corresponding to the query.
     *
     * @param query the query of the search.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<OverheardComment> search(String query) {
        log.debug("Request to search OverheardComments for query {}", query);
        return StreamSupport
            .stream(overheardCommentSearchRepository.search(queryStringQuery(query)).spliterator(), false)
        .collect(Collectors.toList());
    }
}
