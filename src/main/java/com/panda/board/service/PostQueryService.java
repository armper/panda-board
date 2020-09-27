package com.panda.board.service;

import java.util.List;

import javax.persistence.criteria.JoinType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.QueryService;

import com.panda.board.domain.Post;
import com.panda.board.domain.*; // for static metamodels
import com.panda.board.repository.PostRepository;
import com.panda.board.repository.search.PostSearchRepository;
import com.panda.board.service.dto.PostCriteria;

/**
 * Service for executing complex queries for {@link Post} entities in the database.
 * The main input is a {@link PostCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link Post} or a {@link Page} of {@link Post} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class PostQueryService extends QueryService<Post> {

    private final Logger log = LoggerFactory.getLogger(PostQueryService.class);

    private final PostRepository postRepository;

    private final PostSearchRepository postSearchRepository;

    public PostQueryService(PostRepository postRepository, PostSearchRepository postSearchRepository) {
        this.postRepository = postRepository;
        this.postSearchRepository = postSearchRepository;
    }

    /**
     * Return a {@link List} of {@link Post} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<Post> findByCriteria(PostCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Post> specification = createSpecification(criteria);
        return postRepository.findAll(specification);
    }

    /**
     * Return a {@link Page} of {@link Post} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<Post> findByCriteria(PostCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Post> specification = createSpecification(criteria);
        return postRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(PostCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Post> specification = createSpecification(criteria);
        return postRepository.count(specification);
    }

    /**
     * Function to convert {@link PostCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Post> createSpecification(PostCriteria criteria) {
        Specification<Post> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Post_.id));
            }
            if (criteria.getTitle() != null) {
                specification = specification.and(buildStringSpecification(criteria.getTitle(), Post_.title));
            }
            if (criteria.getContent() != null) {
                specification = specification.and(buildStringSpecification(criteria.getContent(), Post_.content));
            }
            if (criteria.getDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getDate(), Post_.date));
            }
            if (criteria.getRankOne() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRankOne(), Post_.rankOne));
            }
            if (criteria.getRankTwo() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRankTwo(), Post_.rankTwo));
            }
            if (criteria.getRankThree() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRankThree(), Post_.rankThree));
            }
            if (criteria.getRankFour() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRankFour(), Post_.rankFour));
            }
            if (criteria.getRankFive() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRankFive(), Post_.rankFive));
            }
            if (criteria.getRankSix() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRankSix(), Post_.rankSix));
            }
            if (criteria.getRankSeven() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getRankSeven(), Post_.rankSeven));
            }
            if (criteria.getOverheardCommentId() != null) {
                specification = specification.and(buildSpecification(criteria.getOverheardCommentId(),
                    root -> root.join(Post_.overheardComments, JoinType.LEFT).get(OverheardComment_.id)));
            }
            if (criteria.getUserId() != null) {
                specification = specification.and(buildSpecification(criteria.getUserId(),
                    root -> root.join(Post_.user, JoinType.LEFT).get(User_.id)));
            }
            if (criteria.getTopicId() != null) {
                specification = specification.and(buildSpecification(criteria.getTopicId(),
                    root -> root.join(Post_.topic, JoinType.LEFT).get(Topic_.id)));
            }
        }
        return specification;
    }
}
