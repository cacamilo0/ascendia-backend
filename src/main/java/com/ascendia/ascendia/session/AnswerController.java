package com.ascendia.ascendia.session;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/answers")
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping
    public AnswerResponse submitAnswer(@RequestBody @Valid AnswerRequest request) {
        return answerService.submitAnswer(request);
    }

}