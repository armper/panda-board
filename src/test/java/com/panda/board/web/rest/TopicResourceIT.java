package com.panda.board.web.rest;

import com.panda.board.PandaBoardApp;
import com.panda.board.domain.Topic;
import com.panda.board.domain.Post;
import com.panda.board.domain.User;
import com.panda.board.repository.TopicRepository;
import com.panda.board.repository.search.TopicSearchRepository;
import com.panda.board.service.TopicService;
import com.panda.board.service.dto.TopicCriteria;
import com.panda.board.service.TopicQueryService;

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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link TopicResource} REST controller.
 */
@SpringBootTest(classes = PandaBoardApp.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
public class TopicResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private TopicService topicService;

    /**
     * This repository is mocked in the com.panda.board.repository.search test package.
     *
     * @see com.panda.board.repository.search.TopicSearchRepositoryMockConfiguration
     */
    @Autowired
    private TopicSearchRepository mockTopicSearchRepository;

    @Autowired
    private TopicQueryService topicQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTopicMockMvc;

    private Topic topic;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Topic createEntity(EntityManager em) {
        Topic topic = new Topic()
            .title(DEFAULT_TITLE);
        // Add required entity
        User user = UserResourceIT.createEntity(em);
        em.persist(user);
        em.flush();
        topic.setUser(user);
        return topic;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Topic createUpdatedEntity(EntityManager em) {
        Topic topic = new Topic()
            .title(UPDATED_TITLE);
        // Add required entity
        User user = UserResourceIT.createEntity(em);
        em.persist(user);
        em.flush();
        topic.setUser(user);
        return topic;
    }

    @BeforeEach
    public void initTest() {
        topic = createEntity(em);
    }

    @Test
    @Transactional
    public void createTopic() throws Exception {
        int databaseSizeBeforeCreate = topicRepository.findAll().size();
        // Create the Topic
        restTopicMockMvc.perform(post("/api/topics")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(topic)))
            .andExpect(status().isCreated());

        // Validate the Topic in the database
        List<Topic> topicList = topicRepository.findAll();
        assertThat(topicList).hasSize(databaseSizeBeforeCreate + 1);
        Topic testTopic = topicList.get(topicList.size() - 1);
        assertThat(testTopic.getTitle()).isEqualTo(DEFAULT_TITLE);

        // Validate the Topic in Elasticsearch
        verify(mockTopicSearchRepository, times(1)).save(testTopic);
    }

    @Test
    @Transactional
    public void createTopicWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = topicRepository.findAll().size();

        // Create the Topic with an existing ID
        topic.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restTopicMockMvc.perform(post("/api/topics")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(topic)))
            .andExpect(status().isBadRequest());

        // Validate the Topic in the database
        List<Topic> topicList = topicRepository.findAll();
        assertThat(topicList).hasSize(databaseSizeBeforeCreate);

        // Validate the Topic in Elasticsearch
        verify(mockTopicSearchRepository, times(0)).save(topic);
    }


    @Test
    @Transactional
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = topicRepository.findAll().size();
        // set the field null
        topic.setTitle(null);

        // Create the Topic, which fails.


        restTopicMockMvc.perform(post("/api/topics")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(topic)))
            .andExpect(status().isBadRequest());

        List<Topic> topicList = topicRepository.findAll();
        assertThat(topicList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllTopics() throws Exception {
        // Initialize the database
        topicRepository.saveAndFlush(topic);

        // Get all the topicList
        restTopicMockMvc.perform(get("/api/topics?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(topic.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)));
    }
    
    @Test
    @Transactional
    public void getTopic() throws Exception {
        // Initialize the database
        topicRepository.saveAndFlush(topic);

        // Get the topic
        restTopicMockMvc.perform(get("/api/topics/{id}", topic.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(topic.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE));
    }


    @Test
    @Transactional
    public void getTopicsByIdFiltering() throws Exception {
        // Initialize the database
        topicRepository.saveAndFlush(topic);

        Long id = topic.getId();

        defaultTopicShouldBeFound("id.equals=" + id);
        defaultTopicShouldNotBeFound("id.notEquals=" + id);

        defaultTopicShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultTopicShouldNotBeFound("id.greaterThan=" + id);

        defaultTopicShouldBeFound("id.lessThanOrEqual=" + id);
        defaultTopicShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllTopicsByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        topicRepository.saveAndFlush(topic);

        // Get all the topicList where title equals to DEFAULT_TITLE
        defaultTopicShouldBeFound("title.equals=" + DEFAULT_TITLE);

        // Get all the topicList where title equals to UPDATED_TITLE
        defaultTopicShouldNotBeFound("title.equals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    public void getAllTopicsByTitleIsNotEqualToSomething() throws Exception {
        // Initialize the database
        topicRepository.saveAndFlush(topic);

        // Get all the topicList where title not equals to DEFAULT_TITLE
        defaultTopicShouldNotBeFound("title.notEquals=" + DEFAULT_TITLE);

        // Get all the topicList where title not equals to UPDATED_TITLE
        defaultTopicShouldBeFound("title.notEquals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    public void getAllTopicsByTitleIsInShouldWork() throws Exception {
        // Initialize the database
        topicRepository.saveAndFlush(topic);

        // Get all the topicList where title in DEFAULT_TITLE or UPDATED_TITLE
        defaultTopicShouldBeFound("title.in=" + DEFAULT_TITLE + "," + UPDATED_TITLE);

        // Get all the topicList where title equals to UPDATED_TITLE
        defaultTopicShouldNotBeFound("title.in=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    public void getAllTopicsByTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        topicRepository.saveAndFlush(topic);

        // Get all the topicList where title is not null
        defaultTopicShouldBeFound("title.specified=true");

        // Get all the topicList where title is null
        defaultTopicShouldNotBeFound("title.specified=false");
    }
                @Test
    @Transactional
    public void getAllTopicsByTitleContainsSomething() throws Exception {
        // Initialize the database
        topicRepository.saveAndFlush(topic);

        // Get all the topicList where title contains DEFAULT_TITLE
        defaultTopicShouldBeFound("title.contains=" + DEFAULT_TITLE);

        // Get all the topicList where title contains UPDATED_TITLE
        defaultTopicShouldNotBeFound("title.contains=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    public void getAllTopicsByTitleNotContainsSomething() throws Exception {
        // Initialize the database
        topicRepository.saveAndFlush(topic);

        // Get all the topicList where title does not contain DEFAULT_TITLE
        defaultTopicShouldNotBeFound("title.doesNotContain=" + DEFAULT_TITLE);

        // Get all the topicList where title does not contain UPDATED_TITLE
        defaultTopicShouldBeFound("title.doesNotContain=" + UPDATED_TITLE);
    }


    @Test
    @Transactional
    public void getAllTopicsByPostIsEqualToSomething() throws Exception {
        // Initialize the database
        topicRepository.saveAndFlush(topic);
        Post post = PostResourceIT.createEntity(em);
        em.persist(post);
        em.flush();
        topic.addPost(post);
        topicRepository.saveAndFlush(topic);
        Long postId = post.getId();

        // Get all the topicList where post equals to postId
        defaultTopicShouldBeFound("postId.equals=" + postId);

        // Get all the topicList where post equals to postId + 1
        defaultTopicShouldNotBeFound("postId.equals=" + (postId + 1));
    }


    @Test
    @Transactional
    public void getAllTopicsByUserIsEqualToSomething() throws Exception {
        // Get already existing entity
        User user = topic.getUser();
        topicRepository.saveAndFlush(topic);
        Long userId = user.getId();

        // Get all the topicList where user equals to userId
        defaultTopicShouldBeFound("userId.equals=" + userId);

        // Get all the topicList where user equals to userId + 1
        defaultTopicShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultTopicShouldBeFound(String filter) throws Exception {
        restTopicMockMvc.perform(get("/api/topics?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(topic.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)));

        // Check, that the count call also returns 1
        restTopicMockMvc.perform(get("/api/topics/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultTopicShouldNotBeFound(String filter) throws Exception {
        restTopicMockMvc.perform(get("/api/topics?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restTopicMockMvc.perform(get("/api/topics/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingTopic() throws Exception {
        // Get the topic
        restTopicMockMvc.perform(get("/api/topics/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateTopic() throws Exception {
        // Initialize the database
        topicService.save(topic);

        int databaseSizeBeforeUpdate = topicRepository.findAll().size();

        // Update the topic
        Topic updatedTopic = topicRepository.findById(topic.getId()).get();
        // Disconnect from session so that the updates on updatedTopic are not directly saved in db
        em.detach(updatedTopic);
        updatedTopic
            .title(UPDATED_TITLE);

        restTopicMockMvc.perform(put("/api/topics")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedTopic)))
            .andExpect(status().isOk());

        // Validate the Topic in the database
        List<Topic> topicList = topicRepository.findAll();
        assertThat(topicList).hasSize(databaseSizeBeforeUpdate);
        Topic testTopic = topicList.get(topicList.size() - 1);
        assertThat(testTopic.getTitle()).isEqualTo(UPDATED_TITLE);

        // Validate the Topic in Elasticsearch
        verify(mockTopicSearchRepository, times(2)).save(testTopic);
    }

    @Test
    @Transactional
    public void updateNonExistingTopic() throws Exception {
        int databaseSizeBeforeUpdate = topicRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTopicMockMvc.perform(put("/api/topics")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(topic)))
            .andExpect(status().isBadRequest());

        // Validate the Topic in the database
        List<Topic> topicList = topicRepository.findAll();
        assertThat(topicList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Topic in Elasticsearch
        verify(mockTopicSearchRepository, times(0)).save(topic);
    }

    @Test
    @Transactional
    public void deleteTopic() throws Exception {
        // Initialize the database
        topicService.save(topic);

        int databaseSizeBeforeDelete = topicRepository.findAll().size();

        // Delete the topic
        restTopicMockMvc.perform(delete("/api/topics/{id}", topic.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Topic> topicList = topicRepository.findAll();
        assertThat(topicList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Topic in Elasticsearch
        verify(mockTopicSearchRepository, times(1)).deleteById(topic.getId());
    }

    @Test
    @Transactional
    public void searchTopic() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        topicService.save(topic);
        when(mockTopicSearchRepository.search(queryStringQuery("id:" + topic.getId())))
            .thenReturn(Collections.singletonList(topic));

        // Search the topic
        restTopicMockMvc.perform(get("/api/_search/topics?query=id:" + topic.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(topic.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)));
    }
}
