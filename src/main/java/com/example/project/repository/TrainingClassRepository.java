package com.example.project.repository;

import com.example.project.entity.TrainingClass;
import com.example.project.entity.ClassStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrainingClassRepository extends JpaRepository<TrainingClass, Long> {

    boolean existsByCourse_IdAndStatusNot(Long courseId, ClassStatus status);

    List<TrainingClass> findByCourse_Id(Long courseId);

    @Query("select c from TrainingClass c " +
           "join c.course cr " +
           "left join c.teacher t " +
           "where lower(c.code) like lower(concat('%', :q, '%')) " +
           "or lower(c.name) like lower(concat('%', :q, '%')) " +
           "or lower(cr.name) like lower(concat('%', :q, '%')) " +
           "or (t is not null and lower(t.name) like lower(concat('%', :q, '%'))) ")
    List<TrainingClass> search(@Param("q") String keyword);
}
