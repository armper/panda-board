package com.panda.board.web.rest;

import com.panda.board.PandaBoardApp;
import com.panda.board.domain.OverheardComment;
import com.panda.board.domain.User;
import com.panda.board.domain.Post;
import com.panda.board.repository.OverheardCommentRepository;
import com.panda.board.repository.search.OverheardCommentSearchRepository;
import com.panda.board.service.OverheardCommentService;
import com.panda.board.service.dto.OverheardCommentCriteria;
import com.panda.board.service.OverheardCommentQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link OverheardCommentResource} REST controller.
 */
@SpringBootTest(classes = PandaBoardApp.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
public class OverheardCommentResourceIT {

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final Instant DEFAULT_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Integer DEFAULT_RANKING = 1;
    private static final Integer UPDATED_RANKING = 2;
    private static final Integer SMALLER_RANKING = 1 - 1;

    @Autowired
    private OverheardCommentRepository overheardCommentRepository;

    @Autowired
    private OverheardCommentService overheardCommentService;

    /**
     * This repository is mocked in the com.panda.board.repository.search test package.
     *
     * @see com.panda.board.repository.search.OverheardCommentSearchRepositoryMockConfiguration
     */
    @Autowired
    private OverheardCommentSearchRepository mockOverheardCommentSearchRepository;

    @Autowired
    private OverheardCommentQueryService overheardCommentQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restOverheardCommentMockMvc;

    private OverheardComment overheardComment;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static OverheardComment createEntity(EntityManager em) {
        OverheardComment overheardComment = new OverheardComment()
            .content(DEFAULT_CONTENT)
            .date(DEFAULT_DATE)
            .ranking(DEFAULT_RANKING);
        // Add required entity
        User user = UserResourceIT.createEntity(em);
        em.persist(user);
        em.flush();
        overheardComment.setUser(user);
        // Add required entity
        Post post;
        if (TestUtil.findAll(em, Post.class).isEmpty()) {
            post = PostResourceIT.createEntity(em);
            em.persist(post);
            em.flush();
        } else {
            post = TestUtil.findAll(em, Post.class).get(0);
        }
        overheardComment.setPost(post);
        return overheardComment;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static OverheardComment createUpdatedEntity(EntityManager em) {
        OverheardComment overheardComment = new OverheardComment()
            .content(UPDATED_CONTENT)
            .date(UPDATED_DATE)
            .ranking(UPDATED_RANKING);
        // Add required entity
        User user = UserResourceIT.createEntity(em);
        em.persist(user);
        em.flush();
        overheardComment.setUser(user);
        // Add required entity
        Post post;
        if (TestUtil.findAll(em, Post.class).isEmpty()) {
            post = PostResourceIT.createUpdatedEntity(em);
            em.persist(post);
            em.flush();
        } else {
            post = TestUtil.findAll(em, Post.class).get(0);
        }
        overheardComment.setPost(post);
        return overheardComment;
    }

    @BeforeEach
    public void initTest() {
        overheardComment = createEntity(em);
    }

    @Test
    @Transactional
    public void createOverheardComment() throws Exception {
        int databaseSizeBeforeCreate = overheardCommentRepository.findAll().size();
        // Create the OverheardComment
        restOverheardCommentMockMvc.perform(post("/api/overheard-comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(overheardComment)))
            .andExpect(status().isCreated());

        // Validate the OverheardComment in the database
        List<OverheardComment> overheardCommentList = overheardCommentRepository.findAll();
        assertThat(overheardCommentList).hasSize(databaseSizeBeforeCreate + 1);
        OverheardComment testOverheardComment = overheardCommentList.get(overheardCommentList.size() - 1);
        assertThat(testOverheardComment.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testOverheardComment.getDate()).isEqualTo(DEFAULT_DATE);
        assertThat(testOverheardComment.getRanking()).isEqualTo(DEFAULT_RANKING);

        // Validate the OverheardComment in Elasticsearch
        verify(mockOverheardCommentSearchRepository, times(1)).save(testOverheardComment);
    }

    @Test
    @Transactional
    public void createOverheardCommentWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = overheardCommentRepository.findAll().size();

        // Create the OverheardComment with an existing ID
        overheardComment.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restOverheardCommentMockMvc.perform(post("/api/overheard-comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(overheardComment)))
            .andExpect(status().isBadRequest());

        // Validate the OverheardComment in the database
        List<OverheardComment> overheardCommentList = overheardCommentRepository.findAll();
        assertThat(overheardCommentList).hasSize(databaseSizeBeforeCreate);

        // Validate the OverheardComment in Elasticsearch
        verify(mockOverheardCommentSearchRepository, times(0)).save(overheardComment);
    }


    @Test
    @Transactional
    public void checkContentIsRequired() throws Exception {
        int databaseSizeBeforeTest = overheardCommentRepository.findAll().size();
        // set the field null
        overheardComment.setContent(null);

        // Create the OverheardComment, which fails.


        restOverheardCommentMockMvc.perform(post("/api/overheard-comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(overheardComment)))
            .andExpect(status().isBadRequest());

        List<OverheardComment> overheardCommentList = overheardCommentRepository.findAll();
        assertThat(overheardCommentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = overheardCommentRepository.findAll().size();
        // set the field null
        overheardComment.setDate(null);

        // Create the OverheardComment, which fails.


        restOverheardCommentMockMvc.perform(post("/api/overheard-comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(overheardComment)))
            .andExpect(status().isBadRequest());

        List<OverheardComment> overheardCommentList = overheardCommentRepository.findAll();
        assertThat(overheardCommentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllOverheardComments() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList
        restOverheardCommentMockMvc.perform(get("/api/overheard-comments?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(overheardComment.getId().intValue())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].ranking").value(hasItem(DEFAULT_RANKING)));
    }
    
    @Test
    @Transactional
    public void getOverheardComment() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get the overheardComment
        restOverheardCommentMockMvc.perform(get("/api/overheard-comments/{id}", overheardComment.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(overheardComment.getId().intValue()))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()))
            .andExpect(jsonPath("$.ranking").value(DEFAULT_RANKING));
    }


    @Test
    @Transactional
    public void getOverheardCommentsByIdFiltering() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        Long id = overheardComment.getId();

        defaultOverheardCommentShouldBeFound("id.equals=" + id);
        defaultOverheardCommentShouldNotBeFound("id.notEquals=" + id);

        defaultOverheardCommentShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultOverheardCommentShouldNotBeFound("id.greaterThan=" + id);

        defaultOverheardCommentShouldBeFound("id.lessThanOrEqual=" + id);
        defaultOverheardCommentShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllOverheardCommentsByContentIsEqualToSomething() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList where content equals to DEFAULT_CONTENT
        defaultOverheardCommentShouldBeFound("content.equals=" + DEFAULT_CONTENT);

        // Get all the overheardCommentList where content equals to UPDATED_CONTENT
        defaultOverheardCommentShouldNotBeFound("content.equals=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    public void getAllOverheardCommentsByContentIsNotEqualToSomething() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList where content not equals to DEFAULT_CONTENT
        defaultOverheardCommentShouldNotBeFound("content.notEquals=" + DEFAULT_CONTENT);

        // Get all the overheardCommentList where content not equals to UPDATED_CONTENT
        defaultOverheardCommentShouldBeFound("content.notEquals=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    public void getAllOverheardCommentsByContentIsInShouldWork() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList where content in DEFAULT_CONTENT or UPDATED_CONTENT
        defaultOverheardCommentShouldBeFound("content.in=" + DEFAULT_CONTENT + "," + UPDATED_CONTENT);

        // Get all the overheardCommentList where content equals to UPDATED_CONTENT
        defaultOverheardCommentShouldNotBeFound("content.in=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    public void getAllOverheardCommentsByContentIsNullOrNotNull() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList where content is not null
        defaultOverheardCommentShouldBeFound("content.specified=true");

        // Get all the overheardCommentList where content is null
        defaultOverheardCommentShouldNotBeFound("content.specified=false");
    }
                @Test
    @Transactional
    public void getAllOverheardCommentsByContentContainsSomething() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList where content contains DEFAULT_CONTENT
        defaultOverheardCommentShouldBeFound("content.contains=" + DEFAULT_CONTENT);

        // Get all the overheardCommentList where content contains UPDATED_CONTENT
        defaultOverheardCommentShouldNotBeFound("content.contains=" + UPDATED_CONTENT);
    }

    @Test
    @Transactional
    public void getAllOverheardCommentsByContentNotContainsSomething() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList where content does not contain DEFAULT_CONTENT
        defaultOverheardCommentShouldNotBeFound("content.doesNotContain=" + DEFAULT_CONTENT);

        // Get all the overheardCommentList where content does not contain UPDATED_CONTENT
        defaultOverheardCommentShouldBeFound("content.doesNotContain=" + UPDATED_CONTENT);
    }


    @Test
    @Transactional
    public void getAllOverheardCommentsByDateIsEqualToSomething() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList where date equals to DEFAULT_DATE
        defaultOverheardCommentShouldBeFound("date.equals=" + DEFAULT_DATE);

        // Get all the overheardCommentList where date equals to UPDATED_DATE
        defaultOverheardCommentShouldNotBeFound("date.equals=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    public void getAllOverheardCommentsByDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList where date not equals to DEFAULT_DATE
        defaultOverheardCommentShouldNotBeFound("date.notEquals=" + DEFAULT_DATE);

        // Get all the overheardCommentList where date not equals to UPDATED_DATE
        defaultOverheardCommentShouldBeFound("date.notEquals=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    public void getAllOverheardCommentsByDateIsInShouldWork() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList where date in DEFAULT_DATE or UPDATED_DATE
        defaultOverheardCommentShouldBeFound("date.in=" + DEFAULT_DATE + "," + UPDATED_DATE);

        // Get all the overheardCommentList where date equals to UPDATED_DATE
        defaultOverheardCommentShouldNotBeFound("date.in=" + UPDATED_DATE);
    }

    @Test
    @Transactional
    public void getAllOverheardCommentsByDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList where date is not null
        defaultOverheardCommentShouldBeFound("date.specified=true");

        // Get all the overheardCommentList where date is null
        defaultOverheardCommentShouldNotBeFound("date.specified=false");
    }

    @Test
    @Transactional
    public void getAllOverheardCommentsByRankingIsEqualToSomething() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList where ranking equals to DEFAULT_RANKING
        defaultOverheardCommentShouldBeFound("ranking.equals=" + DEFAULT_RANKING);

        // Get all the overheardCommentList where ranking equals to UPDATED_RANKING
        defaultOverheardCommentShouldNotBeFound("ranking.equals=" + UPDATED_RANKING);
    }

    @Test
    @Transactional
    public void getAllOverheardCommentsByRankingIsNotEqualToSomething() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList where ranking not equals to DEFAULT_RANKING
        defaultOverheardCommentShouldNotBeFound("ranking.notEquals=" + DEFAULT_RANKING);

        // Get all the overheardCommentList where ranking not equals to UPDATED_RANKING
        defaultOverheardCommentShouldBeFound("ranking.notEquals=" + UPDATED_RANKING);
    }

    @Test
    @Transactional
    public void getAllOverheardCommentsByRankingIsInShouldWork() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList where ranking in DEFAULT_RANKING or UPDATED_RANKING
        defaultOverheardCommentShouldBeFound("ranking.in=" + DEFAULT_RANKING + "," + UPDATED_RANKING);

        // Get all the overheardCommentList where ranking equals to UPDATED_RANKING
        defaultOverheardCommentShouldNotBeFound("ranking.in=" + UPDATED_RANKING);
    }

    @Test
    @Transactional
    public void getAllOverheardCommentsByRankingIsNullOrNotNull() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList where ranking is not null
        defaultOverheardCommentShouldBeFound("ranking.specified=true");

        // Get all the overheardCommentList where ranking is null
        defaultOverheardCommentShouldNotBeFound("ranking.specified=false");
    }

    @Test
    @Transactional
    public void getAllOverheardCommentsByRankingIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList where ranking is greater than or equal to DEFAULT_RANKING
        defaultOverheardCommentShouldBeFound("ranking.greaterThanOrEqual=" + DEFAULT_RANKING);

        // Get all the overheardCommentList where ranking is greater than or equal to UPDATED_RANKING
        defaultOverheardCommentShouldNotBeFound("ranking.greaterThanOrEqual=" + UPDATED_RANKING);
    }

    @Test
    @Transactional
    public void getAllOverheardCommentsByRankingIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList where ranking is less than or equal to DEFAULT_RANKING
        defaultOverheardCommentShouldBeFound("ranking.lessThanOrEqual=" + DEFAULT_RANKING);

        // Get all the overheardCommentList where ranking is less than or equal to SMALLER_RANKING
        defaultOverheardCommentShouldNotBeFound("ranking.lessThanOrEqual=" + SMALLER_RANKING);
    }

    @Test
    @Transactional
    public void getAllOverheardCommentsByRankingIsLessThanSomething() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList where ranking is less than DEFAULT_RANKING
        defaultOverheardCommentShouldNotBeFound("ranking.lessThan=" + DEFAULT_RANKING);

        // Get all the overheardCommentList where ranking is less than UPDATED_RANKING
        defaultOverheardCommentShouldBeFound("ranking.lessThan=" + UPDATED_RANKING);
    }

    @Test
    @Transactional
    public void getAllOverheardCommentsByRankingIsGreaterThanSomething() throws Exception {
        // Initialize the database
        overheardCommentRepository.saveAndFlush(overheardComment);

        // Get all the overheardCommentList where ranking is greater than DEFAULT_RANKING
        defaultOverheardCommentShouldNotBeFound("ranking.greaterThan=" + DEFAULT_RANKING);

        // Get all the overheardCommentList where ranking is greater than SMALLER_RANKING
        defaultOverheardCommentShouldBeFound("ranking.greaterThan=" + SMALLER_RANKING);
    }


    @Test
    @Transactional
    public void getAllOverheardCommentsByUserIsEqualToSomething() throws Exception {
        // Get already existing entity
        User user = overheardComment.getUser();
        overheardCommentRepository.saveAndFlush(overheardComment);
        Long userId = user.getId();

        // Get all the overheardCommentList where user equals to userId
        defaultOverheardCommentShouldBeFound("userId.equals=" + userId);

        // Get all the overheardCommentList where user equals to userId + 1
        defaultOverheardCommentShouldNotBeFound("userId.equals=" + (userId + 1));
    }


    @Test
    @Transactional
    public void getAllOverheardCommentsByPostIsEqualToSomething() throws Exception {
        // Get already existing entity
        Post post = overheardComment.getPost();
        overheardCommentRepository.saveAndFlush(overheardComment);
        Long postId = post.getId();

        // Get all the overheardCommentList where post equals to postId
        defaultOverheardCommentShouldBeFound("postId.equals=" + postId);

        // Get all the overheardCommentList where post equals to postId + 1
        defaultOverheardCommentShouldNotBeFound("postId.equals=" + (postId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultOverheardCommentShouldBeFound(String filter) throws Exception {
        restOverheardCommentMockMvc.perform(get("/api/overheard-comments?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(overheardComment.getId().intValue())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].ranking").value(hasItem(DEFAULT_RANKING)));

        // Check, that the count call also returns 1
        restOverheardCommentMockMvc.perform(get("/api/overheard-comments/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultOverheardCommentShouldNotBeFound(String filter) throws Exception {
        restOverheardCommentMockMvc.perform(get("/api/overheard-comments?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restOverheardCommentMockMvc.perform(get("/api/overheard-comments/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingOverheardComment() throws Exception {
        // Get the overheardComment
        restOverheardCommentMockMvc.perform(get("/api/overheard-comments/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateOverheardComment() throws Exception {
        // Initialize the database
        overheardCommentService.save(overheardComment);

        int databaseSizeBeforeUpdate = overheardCommentRepository.findAll().size();

        // Update the overheardComment
        OverheardComment updatedOverheardComment = overheardCommentRepository.findById(overheardComment.getId()).get();
        // Disconnect from session so that the updates on updatedOverheardComment are not directly saved in db
        em.detach(updatedOverheardComment);
        updatedOverheardComment
            .content(UPDATED_CONTENT)
            .date(UPDATED_DATE)
            .ranking(UPDATED_RANKING);

        restOverheardCommentMockMvc.perform(put("/api/overheard-comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedOverheardComment)))
            .andExpect(status().isOk());

        // Validate the OverheardComment in the database
        List<OverheardComment> overheardCommentList = overheardCommentRepository.findAll();
        assertThat(overheardCommentList).hasSize(databaseSizeBeforeUpdate);
        OverheardComment testOverheardComment = overheardCommentList.get(overheardCommentList.size() - 1);
        assertThat(testOverheardComment.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testOverheardComment.getDate()).isEqualTo(UPDATED_DATE);
        assertThat(testOverheardComment.getRanking()).isEqualTo(UPDATED_RANKING);

        // Validate the OverheardComment in Elasticsearch
        verify(mockOverheardCommentSearchRepository, times(2)).save(testOverheardComment);
    }

    @Test
    @Transactional
    public void updateNonExistingOverheardComment() throws Exception {
        int databaseSizeBeforeUpdate = overheardCommentRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOverheardCommentMockMvc.perform(put("/api/overheard-comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(overheardComment)))
            .andExpect(status().isBadRequest());

        // Validate the OverheardComment in the database
        List<OverheardComment> overheardCommentList = overheardCommentRepository.findAll();
        assertThat(overheardCommentList).hasSize(databaseSizeBeforeUpdate);

        // Validate the OverheardComment in Elasticsearch
        verify(mockOverheardCommentSearchRepository, times(0)).save(overheardComment);
    }

    @Test
    @Transactional
    public void deleteOverheardComment() throws Exception {
        // Initialize the database
        overheardCommentService.save(overheardComment);

        int databaseSizeBeforeDelete = overheardCommentRepository.findAll().size();

        // Delete the overheardComment
        restOverheardCommentMockMvc.perform(delete("/api/overheard-comments/{id}", overheardComment.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<OverheardComment> overheardCommentList = overheardCommentRepository.findAll();
        assertThat(overheardCommentList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the OverheardComment in Elasticsearch
        verify(mockOverheardCommentSearchRepository, times(1)).deleteById(overheardComment.getId());
    }

    @Test
    @Transactional
    public void searchOverheardComment() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        overheardCommentService.save(overheardComment);
        when(mockOverheardCommentSearchRepository.search(queryStringQuery("id:" + overheardComment.getId())))
            .thenReturn(Collections.singletonList(overheardComment));

        // Search the overheardComment
        restOverheardCommentMockMvc.perform(get("/api/_search/overheard-comments?query=id:" + overheardComment.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(overheardComment.getId().intValue())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT)))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())))
            .andExpect(jsonPath("$.[*].ranking").value(hasItem(DEFAULT_RANKING)));
    }
}
