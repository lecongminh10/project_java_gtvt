package com.example.project.repository;

import com.example.project.entity.TrainingClass;
import com.example.project.entity.ClassStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrainingClassRepository extends JpaRepository<TrainingClass, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByCourse_IdAndStatusNot(Long courseId, ClassStatus status);

    List<TrainingClass> findByCourse_Id(Long courseId);

    List<TrainingClass> findByTeacher_User_UsernameOrderByCreatedAtDesc(String username);

    java.util.Optional<TrainingClass> findByIdAndTeacher_User_Username(Long id, String username);

    List<TrainingClass> findByTeacher_IdOrderByCreatedAtDesc(Long teacherId);

    java.util.Optional<TrainingClass> findByIdAndTeacher_Id(Long id, Long teacherId);

    @Query("select c from TrainingClass c " +
           "join c.course cr " +
           "left join c.teacher t " +
           "where lower(c.code) like lower(concat('%', :q, '%')) " +
           "or lower(c.name) like lower(concat('%', :q, '%')) " +
           "or lower(cr.name) like lower(concat('%', :q, '%')) " +
            "or (t is not null and lower(t.name) like lower(concat('%', :q, '%'))) " +
            "order by c.createdAt desc, c.id desc")
    List<TrainingClass> search(@Param("q") String keyword);

    List<TrainingClass> findTop3ByOrderByCreatedAtDesc();
}
