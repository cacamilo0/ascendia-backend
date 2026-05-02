package com.ascendia.ascendia.session;

import com.ascendia.ascendia.question.OptionEntity;
import com.ascendia.ascendia.question.QuestionEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;


@Entity
@Table(
        name = "user_answers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"session_id", "question_id"})
        }
)
@Getter
@Setter
public class UserAnswerEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private TestSessionEntity session;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionEntity question;

    @ManyToOne
    @JoinColumn(name = "selected_option_id", nullable = false)
    private OptionEntity selectedOption;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "answered_at", nullable = false)
    private OffsetDateTime answeredAt;

}
