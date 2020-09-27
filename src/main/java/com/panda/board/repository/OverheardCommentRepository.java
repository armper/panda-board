package com.panda.board.repository;

import com.panda.board.domain.OverheardComment;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data  repository for the OverheardComment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OverheardCommentRepository extends JpaRepository<OverheardComment, Long>, JpaSpecificationExecutor<OverheardComment> {

    @Query("select overheardComment from OverheardComment overheardComment where overheardComment.user.login = ?#{principal.username}")
    List<OverheardComment> findByUserIsCurrentUser();
}
