package com.ascendia.ascendia.session;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @PostMapping("/submit")
    public TestResultResponse submit(@Valid @RequestBody TestSubmitRequest submitRequest) {
        return testService.submit(submitRequest);
    }

    @PostMapping("/start")
    public TestSessionResponse start(@RequestParam UUID userId) {
        return testService.start(userId);
    }

}
