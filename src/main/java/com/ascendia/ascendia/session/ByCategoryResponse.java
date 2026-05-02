package com.ascendia.ascendia.session;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ByCategoryResponse {

    private int correct;
    private int total;
    private int percentage;

}
