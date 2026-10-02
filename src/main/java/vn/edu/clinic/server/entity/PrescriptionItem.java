package vn.edu.clinic.server.entity;

import jakarta.persistence.*;

@Entity
public class PrescriptionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Prescription prescription;

    @ManyToOne(optional = false)
    private Medicine medicine;

    private int quantity;
    private String instruction;

    public PrescriptionItem() {
    }

    public PrescriptionItem(Prescription prescription, Medicine medicine, int quantity, String instruction) {
        this.prescription = prescription;
        this.medicine = medicine;
        this.quantity = quantity;
        this.instruction = instruction;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return medicine.getName() + " x " + quantity + " " + medicine.getUnit() + " (" + instruction + ")";
    }
}
