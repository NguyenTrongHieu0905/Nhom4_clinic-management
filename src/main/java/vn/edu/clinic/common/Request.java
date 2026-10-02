package vn.edu.clinic.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Request implements Serializable {
    private final String action;
    private final Map<String, String> data;

    public Request(String action) {
        this(action, new HashMap<>());
    }

    public Request(String action, Map<String, String> data) {
        this.action = action;
        this.data = data == null ? new HashMap<>() : new HashMap<>(data);
    }

    public String getAction() {
        return action;
    }

    public Map<String, String> getData() {
        return data;
    }

    public String value(String key) {
        return data.getOrDefault(key, "");
    }
}
