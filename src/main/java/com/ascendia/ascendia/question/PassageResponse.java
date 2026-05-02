package com.ascendia.ascendia.question;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PassageResponse {
    private UUID id;
    private String title;
    private String text;
}
