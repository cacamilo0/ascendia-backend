package com.ascendia.ascendia.session;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatEntryResponse {
    private int correct;
    private int total;
    private int percentage;

    public void incrementTotal() { this.total++; }
    public void incrementCorrect() { this.correct++; }
    public void calculatePercentage() { this.percentage = (correct * 100) / total; }
}