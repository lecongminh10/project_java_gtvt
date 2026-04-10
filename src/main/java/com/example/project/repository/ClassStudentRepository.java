package com.example.project.repository;

import com.example.project.entity.ClassStudent;
import com.example.project.entity.ClassStudentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface ClassStudentRepository extends JpaRepository<ClassStudent, ClassStudentId> {

    @Query("""
            select cs from ClassStudent cs
            join fetch cs.trainingClass tc
            where cs.student.id = :studentId
            order by cs.joinedDate desc, tc.id desc
            """)
    List<ClassStudent> findMembershipsByStudentId(@Param("studentId") Long studentId);

    @Query("""
            select cs from ClassStudent cs
            join fetch cs.trainingClass tc
            join fetch cs.student st
            where st.id in :studentIds
              and cs.leaveDate is null
            order by cs.joinedDate desc, tc.id desc
            """)
    List<ClassStudent> findActiveMembershipsByStudentIds(@Param("studentIds") Collection<Long> studentIds);

    @Modifying
    @Transactional
    @Query("delete from ClassStudent cs where cs.student.id = :studentId")
    void deleteByStudentId(@Param("studentId") Long studentId);
}
