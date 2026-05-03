package com.ascendia.ascendia.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestSessionRepository extends JpaRepository<TestSessionEntity, UUID> {

    @Query("""
    SELECT s FROM TestSessionEntity s
    WHERE s.user.id = :userId
    AND s.finishedAt IS NOT NULL
    ORDER BY s.startedAt DESC
    """)
    List<TestSessionEntity> findFinishedSessionsByUserId(@Param("userId") UUID userId);

}
