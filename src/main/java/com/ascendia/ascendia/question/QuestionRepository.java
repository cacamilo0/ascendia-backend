package com.ascendia.ascendia.question;

import com.ascendia.ascendia.common.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository  extends JpaRepository<QuestionEntity, Long> {

    List<QuestionEntity> findByAreaAndPassageIsNull(Area area);

    @Query("""
    SELECT q FROM QuestionEntity q
    JOIN FETCH q.options
    WHERE q.id IN :ids
    """)
    List<QuestionEntity> findAllWithOptionsByIds(@Param("ids") List<Long> ids);

}
