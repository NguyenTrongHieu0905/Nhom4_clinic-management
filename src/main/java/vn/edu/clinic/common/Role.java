package vn.edu.clinic.common;

public enum Role {
    RECEPTIONIST("Nhan vien tiep nhan"),
    DOCTOR("Bac si"),
    PHARMACIST("Nhan vien phat thuoc"),
    MANAGER("Quan ly");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
