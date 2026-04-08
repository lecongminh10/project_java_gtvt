package com.example.project.repository;

import com.example.project.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
        List<Document> findAllByOrderByCreatedAtDesc();

        List<Document> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name);

        @Query("""
                        select d from Document d
                        where (:keyword is null or lower(d.name) like lower(concat('%', :keyword, '%')))
                            and (:subjectId is null or d.subject.id = :subjectId)
                            and (:courseId is null or d.course.id = :courseId)
                        order by d.createdAt desc
                        """)
        List<Document> search(@Param("keyword") String keyword,
                                                    @Param("subjectId") Long subjectId,
                                                    @Param("courseId") Long courseId);
}
