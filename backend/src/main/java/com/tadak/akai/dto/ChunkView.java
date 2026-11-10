package com.tadak.akai.dto;

import com.tadak.akai.domain.Chunk;

public record ChunkView(Long id, int pageNo, int seq, String content) {

    public static ChunkView from(Chunk chunk) {
        return new ChunkView(chunk.getId(), chunk.getPageNo(), chunk.getSeq(), chunk.getContent());
    }
}
