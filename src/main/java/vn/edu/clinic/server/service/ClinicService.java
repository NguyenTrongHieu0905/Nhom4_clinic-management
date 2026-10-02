package vn.edu.clinic.server.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import vn.edu.clinic.common.Response;
import vn.edu.clinic.common.Role;
import vn.edu.clinic.server.JpaUtil;
import vn.edu.clinic.server.entity.*;

import java.time.LocalDate;
import java.util.List;

public class ClinicService {
    public Response guiLogin(String username, String password) {
        EntityManager em = JpaUtil.em();
        try {
            UserAccount user = em.createQuery("select u from UserAccount u where u.username=:u and u.password=:p and u.active=true", UserAccount.class)
                    .setParameter("u", username).setParameter("p", password).getSingleResult();
            java.util.Map<String, String> result = new java.util.LinkedHashMap<>();
            result.put("id", user.getId().toString());
            result.put("username", user.getUsername());
            result.put("fullName", user.getFullName());
            result.put("role", user.getRole().name());
            result.put("phone", java.util.Objects.toString(user.getPhone(), ""));
            result.put("email", java.util.Objects.toString(user.getEmail(), ""));
            return Response.ok("Dang nhap thanh cong", result);
        } catch (NoResultException ex) {
            return Response.fail("Sai tai khoan hoac mat khau");
        } finally { em.close(); }
    }

    public Response guiData(String type, String keyword, long doctorId) {
        EntityManager em = JpaUtil.em();
        try {
            String query = switch (type) {
                case "patients" -> "select p.id,p.patientCode,p.fullName,p.dateOfBirth,p.gender,p.phone,p.address,p.lastVisitDate from Patient p where lower(p.fullName) like :kw or lower(p.patientCode) like :kw order by p.id desc";
                case "doctors" -> "select u.id,u.fullName from UserAccount u where u.role=:role and u.active=true order by u.fullName";
                case "appointments" -> "select a.id,a.patient.patientCode,a.patient.fullName,a.doctor.fullName,a.status,a.note,a.createdAt from Appointment a" + (doctorId > 0 ? " where a.doctor.id=:doctor" : "") + " order by a.id desc";
                case "labs" -> "select l.appointment.id,l.appointment.patient.fullName,l.testName,l.result,l.requestedAt,l.completedAt from LabTest l" + (doctorId > 0 ? " where l.appointment.doctor.id=:doctor" : "") + " order by l.id desc";
                case "prescriptions" -> "select p.id,p.appointment.id,p.appointment.patient.fullName,p.appointment.doctor.fullName,p.dispensed,p.createdAt from Prescription p order by p.id desc";
                case "medicines" -> "select m.id,m.name,m.unit,m.stockQuantity,m.price from Medicine m order by m.name";
                case "accounts" -> "select u.id,u.username,u.fullName,u.role,u.phone,u.email,u.active from UserAccount u order by u.id";
                default -> throw new IllegalArgumentException("Loai danh sach khong hop le");
            };
            var q = em.createQuery(query, Object[].class);
            if (type.equals("patients")) q.setParameter("kw", "%" + keyword.toLowerCase(java.util.Locale.ROOT) + "%");
            if (type.equals("doctors")) q.setParameter("role", Role.DOCTOR);
            if (doctorId > 0 && (type.equals("appointments") || type.equals("labs"))) q.setParameter("doctor", doctorId);
            java.util.List<java.util.List<String>> rows = new java.util.ArrayList<>();
            for (Object[] row : q.getResultList()) {
                java.util.List<String> values = new java.util.ArrayList<>();
                for (Object value : row) values.add(java.util.Objects.toString(value, ""));
                rows.add(values);
            }
            return Response.ok("Du lieu", rows);
        } finally { em.close(); }
    }

    public Response guiPrescription(long id) {
        EntityManager em = JpaUtil.em();
        try {
            java.util.List<java.util.List<String>> rows = new java.util.ArrayList<>();
            for (Object[] item : em.createQuery("select i.medicine.name,i.quantity,i.medicine.unit,i.instruction from PrescriptionItem i where i.prescription.id=:id", Object[].class).setParameter("id", id).getResultList()) {
                java.util.List<String> values = new java.util.ArrayList<>();
                for (Object value : item) values.add(java.util.Objects.toString(value, ""));
                rows.add(values);
            }
            return Response.ok("Chi tiet toa", rows);
        } finally { em.close(); }
    }
    public Response login(String username, String password) {
        EntityManager em = JpaUtil.em();
        try {
            UserAccount user = em.createQuery("select u from UserAccount u where u.username = :username and u.password = :password and u.active = true", UserAccount.class)
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getSingleResult();
            return Response.ok("Dang nhap thanh cong", user.toString());
        } catch (NoResultException ex) {
            return Response.fail("Sai tai khoan hoac mat khau");
        } finally {
            em.close();
        }
    }

    public Response myInfo(String username) {
        EntityManager em = JpaUtil.em();
        try {
            UserAccount user = findUserByUsername(em, username);
            return Response.ok("Thong tin tai khoan", user.toString());
        } finally {
            em.close();
        }
    }

    public Response changePassword(String username, String oldPassword, String newPassword) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            UserAccount user = findUserByUsername(em, username);
            if (!user.getPassword().equals(oldPassword)) {
                em.getTransaction().rollback();
                return Response.fail("Mat khau cu khong dung");
            }
            user.setPassword(newPassword);
            em.getTransaction().commit();
            return Response.ok("Da doi mat khau", null);
        } catch (RuntimeException ex) {
            rollback(em);
            return Response.fail(ex.getMessage());
        } finally {
            em.close();
        }
    }

    public Response doctors() {
        EntityManager em = JpaUtil.em();
        try {
            List<UserAccount> doctors = em.createQuery("select u from UserAccount u where u.role = :role and u.active = true", UserAccount.class)
                    .setParameter("role", Role.DOCTOR)
                    .getResultList();
            return Response.ok("Danh sach bac si", join(doctors));
        } finally {
            em.close();
        }
    }

    public Response savePatient(String code, String fullName, String dob, String gender, String phone, String address) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            Patient patient = findPatientByCodeOrNull(em, code);
            LocalDate birthDate = LocalDate.parse(dob);
            if (patient == null) {
                patient = new Patient(code, fullName, birthDate, gender, phone, address);
                em.persist(patient);
            } else {
                patient.update(fullName, birthDate, gender, phone, address);
            }
            em.getTransaction().commit();
            return Response.ok("Da luu thong tin benh nhan", patient.toString());
        } catch (RuntimeException ex) {
            rollback(em);
            return Response.fail("Khong the luu benh nhan: " + ex.getMessage());
        } finally {
            em.close();
        }
    }

    public Response searchPatients(String keyword) {
        EntityManager em = JpaUtil.em();
        try {
            List<Patient> patients = em.createQuery("""
                    select p from Patient p
                    where lower(p.patientCode) like :kw or lower(p.fullName) like :kw
                    order by p.fullName
                    """, Patient.class)
                    .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                    .getResultList();
            return Response.ok("Ket qua tim kiem", join(patients));
        } finally {
            em.close();
        }
    }

    public Response addAppointment(String patientCode, long doctorId, String note) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            Patient patient = findPatientByCode(em, patientCode);
            UserAccount doctor = em.find(UserAccount.class, doctorId);
            if (doctor == null || doctor.getRole() != Role.DOCTOR) {
                em.getTransaction().rollback();
                return Response.fail("Khong tim thay bac si");
            }
            Appointment appointment = new Appointment(patient, doctor, note);
            em.persist(appointment);
            em.getTransaction().commit();
            return Response.ok("Da dua benh nhan vao danh sach cho", appointment.toString());
        } catch (RuntimeException ex) {
            rollback(em);
            return Response.fail(ex.getMessage());
        } finally {
            em.close();
        }
    }

    public Response waitingList(long doctorId) {
        EntityManager em = JpaUtil.em();
        try {
            List<Appointment> appointments = em.createQuery("""
                    select a from Appointment a
                    where a.doctor.id = :doctorId and (a.status = :waiting or a.status = :labDone)
                    order by a.id
                    """, Appointment.class)
                    .setParameter("doctorId", doctorId)
                    .setParameter("waiting", AppointmentStatus.WAITING)
                    .setParameter("labDone", AppointmentStatus.LAB_DONE)
                    .getResultList();
            return Response.ok("Danh sach cho kham", join(appointments));
        } finally {
            em.close();
        }
    }

    public Response examine(long appointmentId, String symptoms, String diagnosis, String labName, String prescriptionLine) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            Appointment appointment = em.find(Appointment.class, appointmentId);
            if (appointment == null) {
                em.getTransaction().rollback();
                return Response.fail("Khong tim thay lich kham");
            }
            Visit visit = new Visit(appointment, symptoms, diagnosis);
            if (appointment.getStatus() != AppointmentStatus.WAITING) {
                throw new IllegalArgumentException("Lich kham khong con o trang thai cho kham");
            }
            em.persist(visit);
            appointment.getPatient().setLastVisitDate(LocalDate.now());
            if (!labName.isBlank()) {
                appointment.setStatus(AppointmentStatus.WAITING_LAB);
                em.persist(new LabTest(appointment, labName));
                em.getTransaction().commit();
                return Response.ok("Da chuyen benh nhan sang bo phan xet nghiem", appointment.toString());
            }
            createPrescription(em, appointment, prescriptionLine);
            appointment.setStatus(AppointmentStatus.PRESCRIBED);
            em.getTransaction().commit();
            return Response.ok("Da kham va ke toa", appointment.toString());
        } catch (RuntimeException ex) {
            rollback(em);
            return Response.fail("Khong the kham benh: " + ex.getMessage());
        } finally {
            em.close();
        }
    }

    public Response completeLab(long appointmentId, String result) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            Appointment appointment = em.find(Appointment.class, appointmentId);
            if (appointment == null) {
                em.getTransaction().rollback();
                return Response.fail("Khong tim thay lich kham");
            }
            LabTest labTest = em.createQuery("select l from LabTest l where l.appointment.id = :id order by l.id desc", LabTest.class)
                    .setParameter("id", appointmentId)
                    .setMaxResults(1)
                    .getSingleResult();
            if (appointment.getStatus() != AppointmentStatus.WAITING_LAB) {
                throw new IllegalArgumentException("Lich kham khong cho xet nghiem");
            }
            labTest.complete(result);
            appointment.setStatus(AppointmentStatus.LAB_DONE);
            em.getTransaction().commit();
            return Response.ok("Da cap nhat ket qua xet nghiem", labTest.toString());
        } catch (RuntimeException ex) {
            rollback(em);
            return Response.fail(ex.getMessage());
        } finally {
            em.close();
        }
    }

    public Response prescribeAfterLab(long appointmentId, String labResult, String prescriptionLine) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            Appointment appointment = em.find(Appointment.class, appointmentId);
            if (appointment == null) {
                em.getTransaction().rollback();
                return Response.fail("Khong tim thay lich kham");
            }
            Visit visit = em.createQuery("select v from Visit v where v.appointment.id = :id order by v.id desc", Visit.class)
                    .setParameter("id", appointmentId)
                    .setMaxResults(1)
                    .getSingleResult();
            visit.setLabResult(labResult);
            if (appointment.getStatus() != AppointmentStatus.LAB_DONE) {
                throw new IllegalArgumentException("Chua co ket qua xet nghiem hoac da ke toa");
            }
            createPrescription(em, appointment, prescriptionLine);
            appointment.setStatus(AppointmentStatus.PRESCRIBED);
            em.getTransaction().commit();
            return Response.ok("Da cap nhat ket qua va ke toa", appointment.toString());
        } catch (RuntimeException ex) {
            rollback(em);
            return Response.fail(ex.getMessage());
        } finally {
            em.close();
        }
    }

    public Response prescriptionsToDispense() {
        EntityManager em = JpaUtil.em();
        try {
            List<Prescription> prescriptions = em.createQuery("select p from Prescription p where p.dispensed = false order by p.id", Prescription.class)
                    .getResultList();
            return Response.ok("Danh sach toa chua phat", join(prescriptions));
        } finally {
            em.close();
        }
    }

    public Response dispense(long prescriptionId) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            Prescription prescription = em.find(Prescription.class, prescriptionId);
            if (prescription == null) {
                em.getTransaction().rollback();
                return Response.fail("Khong tim thay toa thuoc");
            }
            if (prescription.isDispensed()) {
                throw new IllegalArgumentException("Toa thuoc da duoc phat");
            }
            for (PrescriptionItem item : prescription.getItems()) {
                item.getMedicine().decreaseStock(item.getQuantity());
            }
            prescription.setDispensed(true);
            prescription.getAppointment().setStatus(AppointmentStatus.DISPENSED);
            em.getTransaction().commit();
            return Response.ok("Da phat thuoc", prescription.toString());
        } catch (RuntimeException ex) {
            rollback(em);
            return Response.fail("Khong the phat thuoc: " + ex.getMessage());
        } finally {
            em.close();
        }
    }

    public Response allAccounts() {
        EntityManager em = JpaUtil.em();
        try {
            return Response.ok("Danh sach tai khoan", join(em.createQuery("select u from UserAccount u order by u.id", UserAccount.class).getResultList()));
        } finally {
            em.close();
        }
    }

    public Response createAccount(String username, String password, String fullName, String role, String phone, String email) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            UserAccount account = new UserAccount(username, password, fullName, Role.valueOf(role), phone, email);
            em.persist(account);
            em.getTransaction().commit();
            return Response.ok("Da tao tai khoan", account.toString());
        } catch (RuntimeException ex) {
            rollback(em);
            return Response.fail("Khong tao duoc tai khoan: " + ex.getMessage());
        } finally {
            em.close();
        }
    }

    public Response deactivateAccount(long accountId) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            UserAccount account = em.find(UserAccount.class, accountId);
            if (account == null) {
                em.getTransaction().rollback();
                return Response.fail("Khong tim thay tai khoan");
            }
            account.setActive(false);
            em.getTransaction().commit();
            return Response.ok("Da khoa tai khoan", account.toString());
        } catch (RuntimeException ex) {
            rollback(em);
            return Response.fail(ex.getMessage());
        } finally {
            em.close();
        }
    }

    public Response allPrescriptions() {
        EntityManager em = JpaUtil.em();
        try {
            return Response.ok("Danh sach toa thuoc", join(em.createQuery("select p from Prescription p order by p.id desc", Prescription.class).getResultList()));
        } finally {
            em.close();
        }
    }

    public Response allMedicines() {
        EntityManager em = JpaUtil.em();
        try {
            return Response.ok("Danh sach thuoc", join(em.createQuery("select m from Medicine m order by m.name", Medicine.class).getResultList()));
        } finally {
            em.close();
        }
    }

    public Response deleteOldPatients() {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();
            LocalDate cutoff = LocalDate.now().minusYears(3);
            List<Patient> oldPatients = em.createQuery("""
                    select p from Patient p
                    where p.lastVisitDate is not null and p.lastVisitDate < :cutoff
                    """, Patient.class).setParameter("cutoff", cutoff).getResultList();
            oldPatients.forEach(em::remove);
            em.getTransaction().commit();
            return Response.ok("Da xoa " + oldPatients.size() + " benh nhan qua 3 nam khong tai kham", null);
        } catch (RuntimeException ex) {
            rollback(em);
            return Response.fail(ex.getMessage());
        } finally {
            em.close();
        }
    }

    private void createPrescription(EntityManager em, Appointment appointment, String prescriptionLine) {
        Prescription prescription = new Prescription(appointment);
        if (prescriptionLine == null || prescriptionLine.isBlank()) {
            throw new IllegalArgumentException("Toa thuoc khong duoc rong");
        }
        for (String rawItem : prescriptionLine.split(";")) {
            String[] parts = rawItem.split("\\|");
            if (parts.length < 3) {
                throw new IllegalArgumentException("Dinh dang toa: ten thuoc|so luong|huong dan;...");
            }
            Medicine medicine = em.createQuery("select m from Medicine m where lower(m.name) = :name", Medicine.class)
                    .setParameter("name", parts[0].trim().toLowerCase())
                    .getSingleResult();
            int quantity = Integer.parseInt(parts[1].trim());
            if (quantity <= 0) {
                throw new IllegalArgumentException("So luong thuoc phai lon hon 0");
            }
            prescription.addItem(medicine, quantity, parts[2].trim());
        }
        em.persist(prescription);
    }

    private UserAccount findUserByUsername(EntityManager em, String username) {
        return em.createQuery("select u from UserAccount u where u.username = :username", UserAccount.class)
                .setParameter("username", username)
                .getSingleResult();
    }

    private Patient findPatientByCode(EntityManager em, String patientCode) {
        Patient patient = findPatientByCodeOrNull(em, patientCode);
        if (patient == null) {
            throw new IllegalArgumentException("Khong tim thay benh nhan");
        }
        return patient;
    }

    private Patient findPatientByCodeOrNull(EntityManager em, String patientCode) {
        List<Patient> patients = em.createQuery("select p from Patient p where p.patientCode = :code", Patient.class)
                .setParameter("code", patientCode)
                .getResultList();
        return patients.isEmpty() ? null : patients.get(0);
    }

    private void rollback(EntityManager em) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }

    private String join(List<?> rows) {
        if (rows.isEmpty()) {
            return "(khong co du lieu)";
        }
        StringBuilder builder = new StringBuilder();
        for (Object row : rows) {
            builder.append(row).append(System.lineSeparator());
        }
        return builder.toString();
    }
}
