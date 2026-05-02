package com.ascendia.ascendia.question;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "passages")
@Getter
@Setter
public class PassageEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "title")
    private String title;

    @Column(name = "text", columnDefinition = "text")
    private String text;

    @OneToMany(mappedBy = "passage", fetch = FetchType.LAZY)
    private List<QuestionEntity> questions;

}
