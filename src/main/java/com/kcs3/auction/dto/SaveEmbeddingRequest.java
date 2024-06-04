package com.kcs3.auction.dto;


import lombok.Data;

@Data
public class SaveEmbeddingRequest {
    private double[] embedding;
    private double[] thEmbedding;
    private double[] categoryEmbedding;
    private double[] detailEmbedding;
}
