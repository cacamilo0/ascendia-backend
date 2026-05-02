package com.ascendia.ascendia.question;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BlockResponse {
    private String type; // "PASSAGE" o "STANDALONE"
    private PassageResponse passage; // null si es STANDALONE
    private List<QuestionResponse> questions;
}