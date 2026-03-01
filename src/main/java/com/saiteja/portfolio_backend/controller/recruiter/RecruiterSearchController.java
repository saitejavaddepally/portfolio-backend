package com.saiteja.portfolio_backend.controller.recruiter;

import com.saiteja.portfolio_backend.service.recruiter.RecruiterSearchService;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recruiter")
@RequiredArgsConstructor
public class RecruiterSearchController {

    private static final Logger logger = LoggerFactory.getLogger(RecruiterSearchController.class);

    private final RecruiterSearchService recruiterSearchService;

    @GetMapping("/search")
    @PreAuthorize("hasRole('RECRUITER')")
    public List<Document> search(@RequestParam String query, Authentication authentication) {
        String recruiterEmail = authentication != null ? authentication.getName() : "UNKNOWN";
        logger.info("Search request received from recruiter: {} - Query: {}", recruiterEmail, query);

        List<Document> results = recruiterSearchService.searchCandidates(query);

        logger.info("Search completed for recruiter: {} - Query: {} - Results found: {}",
            recruiterEmail, query, results.size());

        return results;
    }
}
