package com.saiteja.portfolio_backend.controller.recruiter;

import com.saiteja.portfolio_backend.service.recruiter.RecruiterSearchService;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recruiter")
@RequiredArgsConstructor
public class RecruiterSearchController {

    private final RecruiterSearchService recruiterSearchService;

    @GetMapping("/search")
    public List<Document> search(@RequestParam String query) {
        return recruiterSearchService.searchCandidates(query);
    }
}
