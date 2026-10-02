package vn.edu.clinic.common;

import java.io.Serializable;

public class Response implements Serializable {
    private final boolean success;
    private final String message;
    private final Object payload;

    private Response(boolean success, String message, Object payload) {
        this.success = success;
        this.message = message;
        this.payload = payload;
    }

    public static Response ok(String message, Object payload) {
        return new Response(true, message, payload);
    }

    public static Response fail(String message) {
        return new Response(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getPayload() {
        return payload;
    }
}
