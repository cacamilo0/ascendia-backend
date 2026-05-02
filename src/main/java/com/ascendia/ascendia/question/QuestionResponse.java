package com.ascendia.ascendia.question;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionResponse {
    private Long id;
    private String text;
    private String category;
    private String difficulty;
    private List<OptionResponse> options;
}
