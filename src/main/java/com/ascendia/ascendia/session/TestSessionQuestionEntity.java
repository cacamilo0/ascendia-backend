package com.ascendia.ascendia.session;

import com.ascendia.ascendia.question.QuestionEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "test_session_questions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"session_id", "question_id"}),
                @UniqueConstraint(columnNames = {"session_id", "display_order"})
        }
)
@Getter
@Setter
public class TestSessionQuestionEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private TestSessionEntity session;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionEntity question;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

}
