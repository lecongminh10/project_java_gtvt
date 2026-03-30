package com.example.project.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "class_schedule")
public class ClassSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private TrainingClass trainingClass;

    @Enumerated(EnumType.STRING)
    private DayOfWeekEnum dayOfWeek;

    @Enumerated(EnumType.STRING)
    private Shift shift;

    private String room;

    public ClassSchedule() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TrainingClass getTrainingClass() {
        return trainingClass;
    }

    public void setTrainingClass(TrainingClass trainingClass) {
        this.trainingClass = trainingClass;
    }

    public DayOfWeekEnum getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeekEnum dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}
