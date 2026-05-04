package com.example.project.repository;

import com.example.project.entity.Subject;
import com.example.project.entity.SubjectLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

        @Query("""
                        select s from Subject s
                        where (:keyword is null or lower(s.code) like lower(concat('%', :keyword, '%'))
                                     or lower(s.name) like lower(concat('%', :keyword, '%')))
                            and (:level is null or s.level = :level)
                                                order by s.id desc
                        """)
        List<Subject> searchSubjects(@Param("keyword") String keyword, @Param("level") SubjectLevel level);
}