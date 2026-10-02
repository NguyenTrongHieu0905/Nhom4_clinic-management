package vn.edu.clinic.server.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class LabTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Appointment appointment;

    private String testName;

    @Column(length = 1000)
    private String result;

    private LocalDateTime requestedAt = LocalDateTime.now();
    private LocalDateTime completedAt;

    public LabTest() {
    }

    public LabTest(Appointment appointment, String testName) {
        this.appointment = appointment;
        this.testName = testName;
    }

    public void complete(String result) {
        this.result = result;
        this.completedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return id + " | lich " + appointment.getId() + " | " + testName + " | ket qua: " + result + " | tao: " + requestedAt + " | xong: " + completedAt;
    }
}
