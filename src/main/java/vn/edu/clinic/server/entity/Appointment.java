package vn.edu.clinic.server.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Patient patient;

    @ManyToOne(optional = false)
    private UserAccount doctor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.WAITING;

    private LocalDateTime createdAt = LocalDateTime.now();
    private String note;

    public Appointment() {
    }

    public Appointment(Patient patient, UserAccount doctor, String note) {
        this.patient = patient;
        this.doctor = doctor;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public UserAccount getDoctor() {
        return doctor;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    @Override
    public String toString() {
        return id + " | " + patient.getPatientCode() + " - " + patient.getFullName() + " | BS: " + doctor.getFullName() + " | " + status + " | " + note + " | " + createdAt;
    }
}
