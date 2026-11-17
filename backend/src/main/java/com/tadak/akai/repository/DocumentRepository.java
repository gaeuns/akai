package com.tadak.akai.repository;

import com.tadak.akai.domain.Document;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findAllByOrderByCreatedAtDesc();

    /**
     * F-04 · UC-05: 제목·요약 키워드·태그 이름에 검색어가 들어 있는 문서를 분류로 걸러 최신순으로 찾는다.
     * keyword가 null이면 검색어 조건을, categoryId가 null이면 분류 조건을 적용하지 않는다.
     */
    @Query("select distinct d from Document d left join d.summary s left join d.tags t "
            + "where (:categoryId is null or d.category.id = :categoryId) "
            + "and (:keyword is null "
            + "or lower(d.title) like lower(concat('%', :keyword, '%')) "
            + "or lower(s.keywords) like lower(concat('%', :keyword, '%')) "
            + "or lower(t.name) like lower(concat('%', :keyword, '%'))) "
            + "order by d.createdAt desc")
    List<Document> search(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);

    /** 태그가 붙은 문서. 태그를 지울 때 연결만 끊는 데 쓴다. */
    @Query("select d from Document d join d.tags t where t.id = :tagId")
    List<Document> findAllByTagId(@Param("tagId") Long tagId);

    /** 분류를 지울 때 그 분류의 문서를 미분류로 바꾼다. 문서는 남는다(집합). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Document d set d.category = null where d.category.id = :categoryId")
    int clearCategory(@Param("categoryId") Long categoryId);
}
