package vn.edu.clinic.server.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    private Appointment appointment;

    private LocalDateTime createdAt = LocalDateTime.now();
    private boolean dispensed;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrescriptionItem> items = new ArrayList<>();

    public Prescription() {
    }

    public Prescription(Appointment appointment) {
        this.appointment = appointment;
    }

    public Long getId() {
        return id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public List<PrescriptionItem> getItems() {
        return items;
    }

    public boolean isDispensed() {
        return dispensed;
    }

    public void setDispensed(boolean dispensed) {
        this.dispensed = dispensed;
    }

    public void addItem(Medicine medicine, int quantity, String instruction) {
        items.add(new PrescriptionItem(this, medicine, quantity, instruction));
    }

    @Override
    public String toString() {
        return id + " | lich " + appointment.getId() + " | BN: " + appointment.getPatient().getFullName() + " | da phat: " + dispensed + " | " + createdAt + " | " + items;
    }
}
