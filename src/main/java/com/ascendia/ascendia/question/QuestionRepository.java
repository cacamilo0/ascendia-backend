package com.ascendia.ascendia.question;

import com.ascendia.ascendia.common.Area;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository  extends JpaRepository<QuestionEntity, Long> {


    List<QuestionEntity> findByCategoryAndDifficulty(Category category, Difficulty difficulty);

    List<QuestionEntity> findByAreaAndPassageIsNull(Area area);

}
