package com.ascendia.ascendia.question;

import com.ascendia.ascendia.common.Area;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
public class QuestionEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 610)
    private String text;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    private List<OptionEntity> options;

    @ManyToOne
    @JoinColumn(name = "passage_id")
    private PassageEntity passage;

    @Enumerated(EnumType.STRING)
    private Area area;

}
