package vn.edu.clinic.client;

import vn.edu.clinic.common.Request;
import vn.edu.clinic.common.Response;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ClinicClient {
    private static String host = "localhost";
    private static final int PORT = 8888;
    private final Scanner scanner = new Scanner(System.in);
    private String username;

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "--console".equals(args[0])) {
            if (args.length > 1) host = args[1];
            new ClinicClient().run();
        } else {
            ClinicDesktop.main(args);
        }
    }

    private void run() {
        System.out.println("=== UNG DUNG QUAN LY DAT LICH KHAM ===");
        login();
        while (true) {
            System.out.println("""

                    1. Xem thong tin tai khoan
                    2. Doi mat khau
                    3. Tiep nhan: them/sua benh nhan
                    4. Tiep nhan: tim benh nhan
                    5. Tiep nhan: dua benh nhan vao danh sach kham
                    6. Bac si: xem danh sach cho
                    7. Bac si: kham benh va ke toa/chuyen xet nghiem
                    8. Xet nghiem: cap nhat ket qua
                    9. Bac si: ke toa sau xet nghiem
                    10. Phat thuoc: xem toa chua phat
                    11. Phat thuoc: phat thuoc
                    12. Quan ly: xem tai khoan
                    13. Quan ly: tao tai khoan
                    14. Quan ly: khoa tai khoan
                    15. Quan ly: xem thuoc
                    16. Quan ly: xem toa thuoc
                    17. Quan ly: xoa benh nhan qua 3 nam khong tai kham
                    0. Thoat
                    """);
            String choice = ask("Chon");
            if ("0".equals(choice)) {
                return;
            }
            handle(choice);
        }
    }

    private void login() {
        while (username == null) {
            String u = ask("Ten dang nhap");
            String p = ask("Mat khau");
            Response response = send("LOGIN", map("username", u, "password", p));
            print(response);
            if (response.isSuccess()) {
                username = u;
            }
        }
    }

    private void handle(String choice) {
        switch (choice) {
            case "1" -> print(send("MY_INFO", map("username", username)));
            case "2" -> print(send("CHANGE_PASSWORD", map("username", username, "oldPassword", ask("Mat khau cu"), "newPassword", ask("Mat khau moi"))));
            case "3" -> savePatient();
            case "4" -> print(send("SEARCH_PATIENTS", map("keyword", ask("Ma hoac ten benh nhan"))));
            case "5" -> addAppointment();
            case "6" -> print(send("WAITING_LIST", map("doctorId", ask("ID bac si"))));
            case "7" -> examine();
            case "8" -> print(send("COMPLETE_LAB", map("appointmentId", ask("ID lich kham"), "result", ask("Ket qua xet nghiem"))));
            case "9" -> print(send("PRESCRIBE_AFTER_LAB", map("appointmentId", ask("ID lich kham"), "labResult", ask("Ket qua can luu vao phieu kham"), "prescription", prescriptionInput())));
            case "10" -> print(send("PRESCRIPTIONS", Map.of()));
            case "11" -> print(send("DISPENSE", map("prescriptionId", ask("ID toa thuoc"))));
            case "12" -> print(send("ACCOUNTS", Map.of()));
            case "13" -> createAccount();
            case "14" -> print(send("DEACTIVATE_ACCOUNT", map("accountId", ask("ID tai khoan can khoa"))));
            case "15" -> print(send("MEDICINES", Map.of()));
            case "16" -> print(send("ALL_PRESCRIPTIONS", Map.of()));
            case "17" -> print(send("DELETE_OLD_PATIENTS", Map.of()));
            default -> System.out.println("Lua chon khong hop le");
        }
    }

    private void createAccount() {
        System.out.println("Role hop le: RECEPTIONIST, DOCTOR, PHARMACIST, MANAGER");
        print(send("CREATE_ACCOUNT", map(
                "username", ask("Ten dang nhap"),
                "password", ask("Mat khau"),
                "fullName", ask("Ho ten"),
                "role", ask("Role"),
                "phone", ask("So dien thoai"),
                "email", ask("Email")
        )));
    }

    private void savePatient() {
        print(send("SAVE_PATIENT", map(
                "code", ask("Ma benh nhan"),
                "fullName", ask("Ho ten"),
                "dob", ask("Ngay sinh yyyy-MM-dd"),
                "gender", ask("Gioi tinh"),
                "phone", ask("So dien thoai"),
                "address", ask("Dia chi")
        )));
    }

    private void addAppointment() {
        print(send("DOCTORS", Map.of()));
        print(send("ADD_APPOINTMENT", map(
                "patientCode", ask("Ma benh nhan"),
                "doctorId", ask("ID bac si"),
                "note", ask("Ghi chu")
        )));
    }

    private void examine() {
        String labName = ask("Ten xet nghiem neu can, bo trong neu khong");
        String prescription = "";
        if (labName.isBlank()) {
            print(send("MEDICINES", Map.of()));
            prescription = prescriptionInput();
        }
        print(send("EXAMINE", map(
                "appointmentId", ask("ID lich kham"),
                "symptoms", ask("Tinh trang/trieu chung"),
                "diagnosis", ask("Chan doan"),
                "labName", labName,
                "prescription", prescription
        )));
    }

    private String prescriptionInput() {
        print(send("MEDICINES", Map.of()));
        return ask("Toa thuoc theo dinh dang: Ten thuoc|So luong|Huong dan;Ten thuoc|So luong|Huong dan");
    }

    private Response send(String action, Map<String, String> data) {
        try (Socket socket = new Socket(host, PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(new Request(action, data));
            out.flush();
            return (Response) in.readObject();
        } catch (Exception ex) {
            return Response.fail("Khong ket noi duoc server: " + ex.getMessage());
        }
    }

    private void print(Response response) {
        System.out.println((response.isSuccess() ? "[OK] " : "[LOI] ") + response.getMessage());
        if (response.getPayload() != null) {
            System.out.println(response.getPayload());
        }
    }

    private String ask(String label) {
        System.out.print(label + ": ");
        return scanner.nextLine().trim();
    }

    private Map<String, String> map(String... pairs) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }
}
