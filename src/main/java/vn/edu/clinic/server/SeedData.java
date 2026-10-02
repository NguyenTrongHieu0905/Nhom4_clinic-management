package vn.edu.clinic.server;

import jakarta.persistence.EntityManager;
import vn.edu.clinic.common.Role;
import vn.edu.clinic.server.entity.Medicine;
import vn.edu.clinic.server.entity.Patient;
import vn.edu.clinic.server.entity.UserAccount;

import java.time.LocalDate;

public final class SeedData {
    private SeedData() {
    }

    public static void ensure() {
        EntityManager em = JpaUtil.em();
        try {
            Long count = em.createQuery("select count(u) from UserAccount u", Long.class).getSingleResult();
            if (count > 0) {
                return;
            }
            em.getTransaction().begin();
            em.persist(new UserAccount("reception", "123", "Nguyen Trong Hieu", Role.RECEPTIONIST, "0901000001", "reception@clinic.local"));
            em.persist(new UserAccount("doctor", "123", "Bui Trung Kien", Role.DOCTOR, "0901000002", "doctor@clinic.local"));
            em.persist(new UserAccount("pharmacy", "123", "Nguyen Thi Thanh Thuong", Role.PHARMACIST, "0901000003", "pharmacy@clinic.local"));
            em.persist(new UserAccount("manager", "123", "Quan ly phong kham", Role.MANAGER, "0901000004", "manager@clinic.local"));

            em.persist(new Medicine("Paracetamol 500mg", "vien", 300, 1500));
            em.persist(new Medicine("Amoxicillin 500mg", "vien", 180, 2500));
            em.persist(new Medicine("Vitamin C", "vien", 200, 1200));
            em.persist(new Medicine("Oresol", "goi", 100, 3000));

            Patient patient = new Patient("BN001", "Tran Van An", LocalDate.of(1998, 3, 12), "Nam", "0912345678", "Quan 1, TP HCM");
            patient.setLastVisitDate(LocalDate.now().minusMonths(2));
            em.persist(patient);
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }
}
