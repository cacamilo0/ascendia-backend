package com.ascendia.ascendia.question;

import com.ascendia.ascendia.common.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PassageRepository extends JpaRepository<PassageEntity, UUID> {

    @Query("""
    SELECT DISTINCT p
    FROM PassageEntity p
    JOIN QuestionEntity q ON q.passage = p
    WHERE q.area = :area
    """)
    List<PassageEntity> findByArea(Area area);

}
