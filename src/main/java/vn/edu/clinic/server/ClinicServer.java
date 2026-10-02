package vn.edu.clinic.server;

import vn.edu.clinic.common.Request;
import vn.edu.clinic.common.Response;
import vn.edu.clinic.server.service.ClinicService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClinicServer {
    private static final int PORT = Integer.getInteger("clinic.port", 8888);
    private final ClinicService service = new ClinicService();

    public static void main(String[] args) throws Exception {
        SeedData.ensure();
        new ClinicServer().start();
    }

    public void start() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Clinic server dang chay tai cong " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                pool.submit(() -> handle(socket));
            }
        }
    }

    private void handle(Socket socket) {
        try (socket;
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            Request request = (Request) in.readObject();
            out.writeObject(dispatch(request));
            out.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Response dispatch(Request request) {
        try {
            Map<String, String> d = request.getData();
            return switch (request.getAction()) {
                case "GUI_LOGIN" -> service.guiLogin(request.value("username"), request.value("password"));
                case "GUI_DATA" -> service.guiData(request.value("type"), request.value("keyword"), Long.parseLong(d.getOrDefault("doctorId", "0")));
                case "GUI_PRESCRIPTION" -> service.guiPrescription(Long.parseLong(request.value("id")));
                case "LOGIN" -> service.login(request.value("username"), request.value("password"));
                case "MY_INFO" -> service.myInfo(request.value("username"));
                case "CHANGE_PASSWORD" -> service.changePassword(request.value("username"), request.value("oldPassword"), request.value("newPassword"));
                case "DOCTORS" -> service.doctors();
                case "SAVE_PATIENT" -> service.savePatient(request.value("code"), request.value("fullName"), request.value("dob"), request.value("gender"), request.value("phone"), request.value("address"));
                case "SEARCH_PATIENTS" -> service.searchPatients(request.value("keyword"));
                case "ADD_APPOINTMENT" -> service.addAppointment(request.value("patientCode"), Long.parseLong(request.value("doctorId")), request.value("note"));
                case "WAITING_LIST" -> service.waitingList(Long.parseLong(request.value("doctorId")));
                case "EXAMINE" -> service.examine(Long.parseLong(request.value("appointmentId")), request.value("symptoms"), request.value("diagnosis"), request.value("labName"), request.value("prescription"));
                case "COMPLETE_LAB" -> service.completeLab(Long.parseLong(request.value("appointmentId")), request.value("result"));
                case "PRESCRIBE_AFTER_LAB" -> service.prescribeAfterLab(Long.parseLong(request.value("appointmentId")), request.value("labResult"), request.value("prescription"));
                case "PRESCRIPTIONS" -> service.prescriptionsToDispense();
                case "DISPENSE" -> service.dispense(Long.parseLong(request.value("prescriptionId")));
                case "ACCOUNTS" -> service.allAccounts();
                case "CREATE_ACCOUNT" -> service.createAccount(request.value("username"), request.value("password"), request.value("fullName"), request.value("role"), request.value("phone"), request.value("email"));
                case "DEACTIVATE_ACCOUNT" -> service.deactivateAccount(Long.parseLong(request.value("accountId")));
                case "MEDICINES" -> service.allMedicines();
                case "ALL_PRESCRIPTIONS" -> service.allPrescriptions();
                case "DELETE_OLD_PATIENTS" -> service.deleteOldPatients();
                default -> Response.fail("Lenh khong ho tro: " + request.getAction() + " " + d);
            };
        } catch (Exception ex) {
            return Response.fail("Loi xu ly yeu cau: " + ex.getMessage());
        }
    }
}
