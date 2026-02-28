package com.saiteja.portfolio_backend.service.recruiter;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruiterSearchService {

    private final EmbeddingModel embeddingModel;
    private final MongoTemplate mongoTemplate;

    public List<Document> searchCandidates(String query) {

        EmbeddingResponse response =
                embeddingModel.embedForResponse(List.of(query));

        float[] queryEmbedding = response.getResults().getFirst().getOutput();

        List<Double> embeddingList = new ArrayList<>(queryEmbedding.length);

        for (float value : queryEmbedding) {
            embeddingList.add((double) value);
        }

        System.out.println("Embedding list --> " + embeddingList);

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

        return mongoTemplate
                .getCollection("ai_summaries")
                .aggregate(pipeline)
                .into(new ArrayList<>());
    }
}
