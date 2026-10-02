package vn.edu.clinic;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import vn.edu.clinic.common.Request;
import vn.edu.clinic.common.Response;

/** Run against a local server with a fresh demo database. */
public final class SocketSmokeTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        String code = "TEST" + System.currentTimeMillis();
        check(false, call("LOGIN", "username", "doctor", "password", "wrong"));
        for (String username : new String[]{"reception", "doctor", "pharmacy", "manager"}) {
            check(true, call("LOGIN", "username", username, "password", "123"));
            check(true, call("MY_INFO", "username", username));
        }
        check(true, call("DOCTORS"));
        check(true, call("MEDICINES"));
        check(true, call("ACCOUNTS"));
        check(true, call("SAVE_PATIENT", "code", code, "fullName", "Socket Test Patient",
                "dob", "2000-01-01", "gender", "Nam", "phone", "0900000000", "address", "Test"));
        check(true, call("SEARCH_PATIENTS", "keyword", code));
        Response appointment = call("ADD_APPOINTMENT", "patientCode", code, "doctorId", "2", "note", "Test");
        check(true, appointment);
        String id = idOf(appointment);
        check(true, call("WAITING_LIST", "doctorId", "2"));
        check(false, call("EXAMINE", "appointmentId", id, "symptoms", "Test", "diagnosis", "Test",
                "labName", "", "prescription", "Vitamin C|-1|Test"));
        check(true, call("EXAMINE", "appointmentId", id, "symptoms", "Test", "diagnosis", "Test",
                "labName", "", "prescription", "Vitamin C|2|Test"));
        check(false, call("EXAMINE", "appointmentId", id, "symptoms", "Test", "diagnosis", "Test",
                "labName", "", "prescription", "Vitamin C|2|Test"));
        Response prescriptions = call("PRESCRIPTIONS");
        check(true, prescriptions);
        String prescriptionId = prescriptionFor(prescriptions, id);
        check(true, call("DISPENSE", "prescriptionId", prescriptionId));
        check(false, call("DISPENSE", "prescriptionId", prescriptionId));
        check(true, call("ALL_PRESCRIPTIONS"));
        appointment = call("ADD_APPOINTMENT", "patientCode", code, "doctorId", "2", "note", "Lab Test");
        check(true, appointment);
        id = idOf(appointment);
        check(true, call("EXAMINE", "appointmentId", id, "symptoms", "Test", "diagnosis", "Test",
                "labName", "Blood test", "prescription", ""));
        check(false, call("PRESCRIBE_AFTER_LAB", "appointmentId", id, "labResult", "Test",
                "prescription", "Vitamin C|1|Test"));
        check(true, call("COMPLETE_LAB", "appointmentId", id, "result", "Normal"));
        check(true, call("WAITING_LIST", "doctorId", "2"));
        check(true, call("PRESCRIBE_AFTER_LAB", "appointmentId", id, "labResult", "Normal",
                "prescription", "Vitamin C|1|Test"));
        check(true, call("DISPENSE", "prescriptionId", prescriptionFor(call("PRESCRIPTIONS"), id)));
        check(false, call("UNKNOWN"));
        System.out.println("PASS: " + checks + " Socket/JPA checks");
    }

    private static Response call(String action, String... pairs) throws Exception {
        Map<String, String> data = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) data.put(pairs[i], pairs[i + 1]);
        try (Socket socket = new Socket("localhost", Integer.getInteger("clinic.port", 8888));
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            socket.setSoTimeout(10000);
            out.writeObject(new Request(action, data));
            out.flush();
            return (Response) in.readObject();
        }
    }

    private static void check(boolean expected, Response response) {
        if (response.isSuccess() != expected) throw new AssertionError(response.getMessage());
        checks++;
    }

    private static String idOf(Response response) {
        return response.getPayload().toString().split("\\|", 2)[0].trim();
    }

    private static String prescriptionFor(Response response, String appointmentId) {
        for (String line : response.getPayload().toString().split("\\R")) {
            if (line.contains("| lich " + appointmentId + " |")) return line.split("\\|", 2)[0].trim();
        }
        throw new AssertionError("Prescription not found for appointment " + appointmentId);
    }
}
