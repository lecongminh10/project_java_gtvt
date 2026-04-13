package com.example.project.repository;

import com.example.project.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByCode(String code);

    @Query("""
            select s from Student s
            where (:name is null or lower(s.name) like lower(concat('%', :name, '%')))
              and (:code is null or lower(s.code) like lower(concat('%', :code, '%')))
              and (:status is null or s.status = :status)
              and (:classId is null or exists (
                    select 1 from ClassStudent cs
                    where cs.student = s
                      and cs.trainingClass.id = :classId
                      and cs.leaveDate is null
              ))
            order by s.id desc
            """)
    List<Student> filterStudents(@Param("name") String name,
                                @Param("code") String code,
                                @Param("status") com.example.project.entity.StudentStatus status,
                                @Param("classId") Long classId);

    @Query("""
            select s from Student s
            where lower(s.code) like lower(concat('%', :q, '%'))
               or lower(s.name) like lower(concat('%', :q, '%'))
               or lower(coalesce(s.parentName, '')) like lower(concat('%', :q, '%'))
               or lower(coalesce(s.parentPhone, '')) like lower(concat('%', :q, '%'))
               or lower(coalesce(s.parentEmail, '')) like lower(concat('%', :q, '%'))
               or exists (
                    select 1
                    from ClassStudent cs
                    join cs.trainingClass c
                    where cs.student = s
                      and cs.leaveDate is null
                      and (lower(c.code) like lower(concat('%', :q, '%'))
                           or lower(c.name) like lower(concat('%', :q, '%')))
               )
            order by s.id desc
            """)
    List<Student> search(@Param("q") String keyword);

    @Query("select coalesce(max(s.id), 0) + 1 from Student s")
    Long findNextId();
}
