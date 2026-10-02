package vn.edu.clinic.server.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String patientCode;

    @Column(nullable = false)
    private String fullName;

    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String address;
    private LocalDate lastVisitDate;

    public Patient() {
    }

    public Patient(String patientCode, String fullName, LocalDate dateOfBirth, String gender, String phone, String address) {
        this.patientCode = patientCode;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public LocalDate getLastVisitDate() {
        return lastVisitDate;
    }

    public void update(String fullName, LocalDate dateOfBirth, String gender, String phone, String address) {
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
    }

    public void setLastVisitDate(LocalDate lastVisitDate) {
        this.lastVisitDate = lastVisitDate;
    }

    @Override
    public String toString() {
        return id + " | " + patientCode + " | " + fullName + " | " + dateOfBirth + " | " + gender + " | " + phone + " | " + address + " | lan cuoi: " + lastVisitDate;
    }
}
