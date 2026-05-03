package com.ascendia.ascendia.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAnswerRepository extends JpaRepository<UserAnswerEntity, Long> {

    Optional<UserAnswerEntity> findBySessionIdAndQuestionId(UUID sessionId, Long questionId);

    List<UserAnswerEntity> findBySessionId(UUID sessionId);
}
