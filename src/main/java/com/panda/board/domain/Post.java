package com.panda.board.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.springframework.data.elasticsearch.annotations.FieldType;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * A Post.
 */
@Entity
@Table(name = "post")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "post")
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Size(min = 2, max = 80)
    @Column(name = "title", length = 80, nullable = false)
    private String title;

    @NotNull
    @Size(min = 2, max = 4096)
    @Column(name = "content", length = 4096, nullable = false)
    private String content;

    @NotNull
    @Column(name = "date", nullable = false)
    private Instant date;

    @Column(name = "rank_one")
    private Integer rankOne;

    @Column(name = "rank_two")
    private Integer rankTwo;

    @Column(name = "rank_three")
    private Integer rankThree;

    @Column(name = "rank_four")
    private Integer rankFour;

    @Column(name = "rank_five")
    private Integer rankFive;

    @Column(name = "rank_six")
    private Integer rankSix;

    @Column(name = "rank_seven")
    private Integer rankSeven;

    @OneToMany(mappedBy = "post")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<OverheardComment> overheardComments = new HashSet<>();

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = "posts", allowSetters = true)
    private User user;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = "posts", allowSetters = true)
    private Topic topic;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public Post title(String title) {
        this.title = title;
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public Post content(String content) {
        this.content = content;
        return this;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getDate() {
        return date;
    }

    public Post date(Instant date) {
        this.date = date;
        return this;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public Integer getRankOne() {
        return rankOne;
    }

    public Post rankOne(Integer rankOne) {
        this.rankOne = rankOne;
        return this;
    }

    public void setRankOne(Integer rankOne) {
        this.rankOne = rankOne;
    }

    public Integer getRankTwo() {
        return rankTwo;
    }

    public Post rankTwo(Integer rankTwo) {
        this.rankTwo = rankTwo;
        return this;
    }

    public void setRankTwo(Integer rankTwo) {
        this.rankTwo = rankTwo;
    }

    public Integer getRankThree() {
        return rankThree;
    }

    public Post rankThree(Integer rankThree) {
        this.rankThree = rankThree;
        return this;
    }

    public void setRankThree(Integer rankThree) {
        this.rankThree = rankThree;
    }

    public Integer getRankFour() {
        return rankFour;
    }

    public Post rankFour(Integer rankFour) {
        this.rankFour = rankFour;
        return this;
    }

    public void setRankFour(Integer rankFour) {
        this.rankFour = rankFour;
    }

    public Integer getRankFive() {
        return rankFive;
    }

    public Post rankFive(Integer rankFive) {
        this.rankFive = rankFive;
        return this;
    }

    public void setRankFive(Integer rankFive) {
        this.rankFive = rankFive;
    }

    public Integer getRankSix() {
        return rankSix;
    }

    public Post rankSix(Integer rankSix) {
        this.rankSix = rankSix;
        return this;
    }

    public void setRankSix(Integer rankSix) {
        this.rankSix = rankSix;
    }

    public Integer getRankSeven() {
        return rankSeven;
    }

    public Post rankSeven(Integer rankSeven) {
        this.rankSeven = rankSeven;
        return this;
    }

    public void setRankSeven(Integer rankSeven) {
        this.rankSeven = rankSeven;
    }

    public Set<OverheardComment> getOverheardComments() {
        return overheardComments;
    }

    public Post overheardComments(Set<OverheardComment> overheardComments) {
        this.overheardComments = overheardComments;
        return this;
    }

    public Post addOverheardComment(OverheardComment overheardComment) {
        this.overheardComments.add(overheardComment);
        overheardComment.setPost(this);
        return this;
    }

    public Post removeOverheardComment(OverheardComment overheardComment) {
        this.overheardComments.remove(overheardComment);
        overheardComment.setPost(null);
        return this;
    }

    public void setOverheardComments(Set<OverheardComment> overheardComments) {
        this.overheardComments = overheardComments;
    }

    public User getUser() {
        return user;
    }

    public Post user(User user) {
        this.user = user;
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Topic getTopic() {
        return topic;
    }

    public Post topic(Topic topic) {
        this.topic = topic;
        return this;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Post)) {
            return false;
        }
        return id != null && id.equals(((Post) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Post{" +
            "id=" + getId() +
            ", title='" + getTitle() + "'" +
            ", content='" + getContent() + "'" +
            ", date='" + getDate() + "'" +
            ", rankOne=" + getRankOne() +
            ", rankTwo=" + getRankTwo() +
            ", rankThree=" + getRankThree() +
            ", rankFour=" + getRankFour() +
            ", rankFive=" + getRankFive() +
            ", rankSix=" + getRankSix() +
            ", rankSeven=" + getRankSeven() +
            "}";
    }
}
