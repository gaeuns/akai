package com.tadak.akai.repository;

import com.tadak.akai.domain.Chunk;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkRepository extends JpaRepository<Chunk, Long> {

    List<Chunk> findByContentContainingIgnoreCase(String keyword);
}
