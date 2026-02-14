package com.saiteja.portfolio_backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/professional")
    public String professionalOnly() {
        return "Hello Professional";
    }

    @GetMapping("/recruiter")
    public String recruiterOnly() {
        return "Hello Recruiter";
    }
}

