package com.saiteja.portfolio_backend.service.recruiter;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruiterSearchService {

    private static final Logger logger = LoggerFactory.getLogger(RecruiterSearchService.class);

    private final EmbeddingModel embeddingModel;
    private final MongoTemplate mongoTemplate;

    public List<Document> searchCandidates(String query) {

        long startTime = System.currentTimeMillis();
        logger.info("Candidate search initiated - Query: {}", query);

        logger.debug("Generating embedding for search query");
        EmbeddingResponse response =
                embeddingModel.embedForResponse(List.of(query));

        float[] queryEmbedding = response.getResults().getFirst().getOutput();

        List<Double> embeddingList = new ArrayList<>(queryEmbedding.length);

        for (float value : queryEmbedding) {
            embeddingList.add((double) value);
        }

        logger.debug("Search embedding generated - Dimensions: {}", embeddingList.size());

        Document vectorSearch = new Document("$vectorSearch",
                new Document("index", "vector_index")
                        .append("path", "embedding")
                        .append("queryVector", embeddingList)
                        .append("numCandidates", 100)
                        .append("limit", 10)
        );

        Document project = new Document("$project",
                new Document("userEmail", 1)
                        .append("userId", 1)
                        .append("score", new Document("$meta", "vectorSearchScore"))
        );

        List<Document> pipeline = List.of(vectorSearch, project);

        logger.debug("Executing MongoDB vector search pipeline");
        List<Document> results = mongoTemplate
                .getCollection("ai_summaries")
                .aggregate(pipeline)
                .into(new ArrayList<>());

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Candidate search completed - Query: {} - Results: {} - Duration: {}ms",
            query, results.size(), duration);

        return results;
    }
}
