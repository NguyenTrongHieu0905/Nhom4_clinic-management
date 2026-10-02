package vn.edu.clinic.server.entity;

import jakarta.persistence.*;

@Entity
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String unit;
    private int stockQuantity;
    private double price;

    public Medicine() {
    }

    public Medicine(String name, String unit, int stockQuantity, double price) {
        this.name = name;
        this.unit = unit;
        this.stockQuantity = stockQuantity;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("So luong thuoc phai lon hon 0");
        }
        if (quantity > stockQuantity) {
            throw new IllegalArgumentException("Khong du ton kho thuoc " + name);
        }
        stockQuantity -= quantity;
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + unit + " | ton: " + stockQuantity + " | gia: " + price;
    }
}
