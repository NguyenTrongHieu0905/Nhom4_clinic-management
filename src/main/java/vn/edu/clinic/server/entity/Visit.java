package vn.edu.clinic.server.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Visit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    private Appointment appointment;

    @Column(length = 1000)
    private String symptoms;

    @Column(length = 1000)
    private String diagnosis;

    @Column(length = 1000)
    private String labResult;

    private LocalDateTime examinedAt = LocalDateTime.now();

    public Visit() {
    }

    public Visit(Appointment appointment, String symptoms, String diagnosis) {
        this.appointment = appointment;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
    }

    public Long getId() {
        return id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setLabResult(String labResult) {
        this.labResult = labResult;
    }

    @Override
    public String toString() {
        return id + " | lich " + appointment.getId() + " | trieu chung: " + symptoms + " | chan doan: " + diagnosis + " | xet nghiem: " + labResult + " | " + examinedAt;
    }
}
