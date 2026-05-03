package com.ascendia.ascendia.session;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final AnswerService answerService;

    @GetMapping("/{sessionId}/questions/{questionId}/tip")
    public TipResponse getTip(@PathVariable UUID sessionId, @PathVariable Long questionId) {
        return answerService.getTip(sessionId, questionId);
    }

    @PostMapping("/finish")
    public FinishSessionResponse finish(@RequestBody FinishSessionRequest request) {
        return sessionService.finish(request);
    }

    @GetMapping("/{sessionId}/review")
    public SessionReviewResponse getReview(@PathVariable UUID sessionId) {
        return sessionService.getReview(sessionId);
    }

    @PostMapping("/start")
    public TestSessionResponse start(@RequestBody StartSessionRequest request) {
        return sessionService.start(request);
    }

    @GetMapping("/history")
    public SessionHistoryResponse getHistory(@RequestParam UUID userId) {
        return sessionService.getHistory(userId);
    }

}