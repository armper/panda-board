package com.panda.board.web.rest;

import com.panda.board.domain.OverheardComment;
import com.panda.board.service.OverheardCommentService;
import com.panda.board.web.rest.errors.BadRequestAlertException;
import com.panda.board.service.dto.OverheardCommentCriteria;
import com.panda.board.service.OverheardCommentQueryService;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing {@link com.panda.board.domain.OverheardComment}.
 */
@RestController
@RequestMapping("/api")
public class OverheardCommentResource {

    private final Logger log = LoggerFactory.getLogger(OverheardCommentResource.class);

    private static final String ENTITY_NAME = "overheardComment";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final OverheardCommentService overheardCommentService;

    private final OverheardCommentQueryService overheardCommentQueryService;

    public OverheardCommentResource(OverheardCommentService overheardCommentService, OverheardCommentQueryService overheardCommentQueryService) {
        this.overheardCommentService = overheardCommentService;
        this.overheardCommentQueryService = overheardCommentQueryService;
    }

    /**
     * {@code POST  /overheard-comments} : Create a new overheardComment.
     *
     * @param overheardComment the overheardComment to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new overheardComment, or with status {@code 400 (Bad Request)} if the overheardComment has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/overheard-comments")
    public ResponseEntity<OverheardComment> createOverheardComment(@Valid @RequestBody OverheardComment overheardComment) throws URISyntaxException {
        log.debug("REST request to save OverheardComment : {}", overheardComment);
        if (overheardComment.getId() != null) {
            throw new BadRequestAlertException("A new overheardComment cannot already have an ID", ENTITY_NAME, "idexists");
        }
        OverheardComment result = overheardCommentService.save(overheardComment);
        return ResponseEntity.created(new URI("/api/overheard-comments/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /overheard-comments} : Updates an existing overheardComment.
     *
     * @param overheardComment the overheardComment to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated overheardComment,
     * or with status {@code 400 (Bad Request)} if the overheardComment is not valid,
     * or with status {@code 500 (Internal Server Error)} if the overheardComment couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/overheard-comments")
    public ResponseEntity<OverheardComment> updateOverheardComment(@Valid @RequestBody OverheardComment overheardComment) throws URISyntaxException {
        log.debug("REST request to update OverheardComment : {}", overheardComment);
        if (overheardComment.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        OverheardComment result = overheardCommentService.save(overheardComment);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, overheardComment.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /overheard-comments} : get all the overheardComments.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of overheardComments in body.
     */
    @GetMapping("/overheard-comments")
    public ResponseEntity<List<OverheardComment>> getAllOverheardComments(OverheardCommentCriteria criteria) {
        log.debug("REST request to get OverheardComments by criteria: {}", criteria);
        List<OverheardComment> entityList = overheardCommentQueryService.findByCriteria(criteria);
        return ResponseEntity.ok().body(entityList);
    }

    /**
     * {@code GET  /overheard-comments/count} : count all the overheardComments.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/overheard-comments/count")
    public ResponseEntity<Long> countOverheardComments(OverheardCommentCriteria criteria) {
        log.debug("REST request to count OverheardComments by criteria: {}", criteria);
        return ResponseEntity.ok().body(overheardCommentQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /overheard-comments/:id} : get the "id" overheardComment.
     *
     * @param id the id of the overheardComment to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the overheardComment, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/overheard-comments/{id}")
    public ResponseEntity<OverheardComment> getOverheardComment(@PathVariable Long id) {
        log.debug("REST request to get OverheardComment : {}", id);
        Optional<OverheardComment> overheardComment = overheardCommentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(overheardComment);
    }

    /**
     * {@code DELETE  /overheard-comments/:id} : delete the "id" overheardComment.
     *
     * @param id the id of the overheardComment to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/overheard-comments/{id}")
    public ResponseEntity<Void> deleteOverheardComment(@PathVariable Long id) {
        log.debug("REST request to delete OverheardComment : {}", id);
        overheardCommentService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }

    /**
     * {@code SEARCH  /_search/overheard-comments?query=:query} : search for the overheardComment corresponding
     * to the query.
     *
     * @param query the query of the overheardComment search.
     * @return the result of the search.
     */
    @GetMapping("/_search/overheard-comments")
    public List<OverheardComment> searchOverheardComments(@RequestParam String query) {
        log.debug("REST request to search OverheardComments for query {}", query);
        return overheardCommentService.search(query);
    }
}
