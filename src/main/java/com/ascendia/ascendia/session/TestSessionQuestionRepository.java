package com.ascendia.ascendia.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TestSessionQuestionRepository extends JpaRepository<TestSessionQuestionEntity, Long> {

    // Para obtener IDs de preguntas usadas en los últimos N tests del usuario
    @Query("""
        SELECT tsq.question.id
        FROM TestSessionQuestionEntity tsq
        WHERE tsq.session.user.id = :userId
        ORDER BY tsq.session.startedAt DESC
        LIMIT :limit
    """)
    List<Long> findRecentlyUsedQuestionIds(@Param("userId") UUID userId, @Param("limit") int limit);

    List<TestSessionQuestionEntity> findBySessionId(UUID sessionId);

    List<TestSessionQuestionEntity> findBySessionIdOrderByDisplayOrderAsc(UUID sessionId);

    @Query("""
       SELECT tsq.question.id
       FROM TestSessionQuestionEntity tsq
       WHERE tsq.session.id = :sessionId
       ORDER BY tsq.question.id ASC
    """)
    List<Long> findQuestionIdsBySessionId(UUID sessionId);

    boolean existsBySessionIdAndQuestionId(UUID sessionId, Long questionId);

    int countBySessionId(UUID sessionId);

}
