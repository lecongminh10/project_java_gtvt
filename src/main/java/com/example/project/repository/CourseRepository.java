package com.example.project.repository;

import com.example.project.entity.ClassStatus;
import com.example.project.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
        @Query("""
                        select c from Course c
                        where c.deleted = false
                            and (:keyword is null or lower(c.code) like lower(concat('%', :keyword, '%'))
                                     or lower(c.name) like lower(concat('%', :keyword, '%')))
                            and (:status is null or c.status = :status)
                        order by c.createdAt desc, c.id desc
                        """)
        List<Course> searchCourses(@Param("keyword") String keyword, @Param("status") ClassStatus status);

    List<Course> findAllByDeletedFalse();

    List<Course> findAllByDeletedFalse(Sort sort);

    long countBySubjectIdAndDeletedFalse(Long subjectId);

    long countByDeletedFalse();
}
