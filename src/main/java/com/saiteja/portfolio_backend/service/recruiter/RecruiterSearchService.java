package com.saiteja.portfolio_backend.service.recruiter;

import com.saiteja.portfolio_backend.service.ai.CandidateRerankService;
import com.saiteja.portfolio_backend.service.ai.QueryRewriteService;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruiterSearchService {

    private static final Logger logger = LoggerFactory.getLogger(RecruiterSearchService.class);

    private final EmbeddingModel embeddingModel;
    private final MongoTemplate mongoTemplate;
    private final QueryRewriteService queryRewriteService;
    private final CandidateRerankService rerankService;

    public List<Document> searchCandidates(String query) {

        String rewrittenQuery = queryRewriteService.rewriteQuery(query);

        logger.info("Rewritten Query - {}", rewrittenQuery);

        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(rewrittenQuery));

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
                        .append("embeddingText", 1)
                        .append("score", new Document("$meta", "vectorSearchScore"))
        );

        List<Document> pipeline = List.of(vectorSearch, project);

        logger.debug("Executing MongoDB vector search pipeline");
        List<Document> results = mongoTemplate
                .getCollection("ai_summaries")
                .aggregate(pipeline)
                .into(new ArrayList<>());

        // commenting out as it is not needed now
//        List<Document> reorderedResults = getReorderedResults(query, results);

        return results;
    }

    @NonNull
    private List<Document> getReorderedResults(String query, List<Document> results) {
        List<String> summaries = results.stream()
                .map(doc -> doc.getString("embeddingText"))
                .toList();


        String ranking = rerankService.rerankCandidates(query, summaries);

        System.out.println("LLM Ranking -> " + ranking);

        String[] parts = ranking.split(",");

        List<Document> reorderedResults = new ArrayList<>();

        for (String part : parts) {

            int index = Integer.parseInt(part.trim()) - 1;

            if (index >= 0 && index < results.size()) {
                reorderedResults.add(results.get(index));
            }
        }


        logger.info("Candidate search completed - Query: {} - Results: {} ",
                query, reorderedResults.size());
        return reorderedResults;
    }
}
