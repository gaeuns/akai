package com.tadak.akai.repository;

import com.tadak.akai.domain.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChunkRepository extends JpaRepository<Chunk, Long> {

    /** F-09: 임베딩을 계산해 둔 조각 수 */
    long countByEmbeddingIsNotNull();

    /** 문서를 지우기 전에 그 문서 조각을 가리키는 답변 근거 연결을 끊는다. 답변(이력)은 남는다. */
    @Modifying
    @Query(value = "DELETE FROM answer_evidences WHERE chunk_id IN "
            + "(SELECT id FROM chunks WHERE document_id = :documentId)", nativeQuery = true)
    int deleteEvidenceLinksByDocumentId(@Param("documentId") Long documentId);
}
