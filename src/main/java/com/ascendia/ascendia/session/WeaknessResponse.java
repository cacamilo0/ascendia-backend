package com.ascendia.ascendia.session;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WeaknessResponse {

    private String category;
    private int percentage;
    private String message;

}
