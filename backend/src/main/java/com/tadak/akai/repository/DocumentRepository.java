package com.tadak.akai.repository;

import com.tadak.akai.domain.Document;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findAllByOrderByCreatedAtDesc();

    /** 제목 또는 요약 키워드에 검색어가 들어 있는 문서 (F-04). 태그 검색은 태그 기능 구현 후 추가한다. */
    @Query("select distinct d from Document d left join d.summary s "
            + "where lower(d.title) like lower(concat('%', :keyword, '%')) "
            + "or lower(s.keywords) like lower(concat('%', :keyword, '%')) "
            + "order by d.createdAt desc")
    List<Document> searchByTitleOrKeyword(@Param("keyword") String keyword);
}
