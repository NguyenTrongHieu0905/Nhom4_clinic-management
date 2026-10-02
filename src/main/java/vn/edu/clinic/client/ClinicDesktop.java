package vn.edu.clinic.client;

import vn.edu.clinic.common.Request;
import vn.edu.clinic.common.Response;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public final class ClinicDesktop extends JPanel {
    private static final Color INK = new Color(33, 46, 51);
    private static final Color TEAL = new Color(0, 112, 105);
    private static final Color MUTED = new Color(106, 120, 128);
    private static final Color LINE = new Color(223, 231, 232);
    private static final Color BG = new Color(245, 248, 249);
    private final JLabel status = label("Sẵn sàng", 13, MUTED);
    private final JPanel body = new JPanel(new BorderLayout(0, 18));
    private final DefaultTableModel model = new DefaultTableModel() {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField search = new JTextField(24);
    private final JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final Map<String, JButton> navigation = new LinkedHashMap<>();
    private final Map<String, String[]> columns = Map.of(
            "patients", new String[]{"ID", "Mã bệnh nhân", "Họ và tên", "Ngày sinh", "Giới tính", "Điện thoại", "Địa chỉ", "Khám gần nhất"},
            "appointments", new String[]{"Mã lịch", "Mã bệnh nhân", "Bệnh nhân", "Bác sĩ", "Trạng thái", "Ghi chú", "Tiếp nhận lúc"},
            "labs", new String[]{"Mã lịch", "Bệnh nhân", "Xét nghiệm", "Kết quả", "Yêu cầu lúc", "Hoàn tất lúc"},
            "prescriptions", new String[]{"Mã toa", "Mã lịch", "Bệnh nhân", "Bác sĩ", "Đã phát", "Ngày kê toa"},
            "medicines", new String[]{"ID", "Tên thuốc", "Đơn vị", "Tồn kho", "Đơn giá (đ)"},
            "accounts", new String[]{"ID", "Tên đăng nhập", "Họ và tên", "Vai trò", "Điện thoại", "Email", "Hoạt động"});
    private String host = "localhost";
    private int port = 8888;
    private Map<String, String> user;
    private String view = "patients";
    private JLabel count;
    private JTextField loginUser;
    private JPasswordField loginPassword;
    private JButton loginButton;
    private boolean busy;
    private long generation;

    public ClinicDesktop() {
        super(new BorderLayout());
        setBackground(BG);
        showLogin();
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        for (Object key : Collections.list(UIManager.getDefaults().keys())) {
            if (UIManager.get(key) instanceof javax.swing.plaf.FontUIResource)
                UIManager.put(key, new javax.swing.plaf.FontUIResource("Segoe UI", Font.PLAIN, 14));
        }
        UIManager.put("Button.focus", TEAL);
        if (args.length > 0 && args[0].equals("--preview")) {
            preview(args);
            return;
        }
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Phòng khám Nhóm 4 | Quản lý khám bệnh");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ClinicDesktop app = new ClinicDesktop();
            if (args.length > 0) app.host = args[0];
            frame.setContentPane(app);
            frame.setSize(1280, 800);
            frame.setMinimumSize(new Dimension(1000, 660));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static JLabel label(String text, int size, Color color) {
        JLabel result = new JLabel(text);
        result.setFont(new Font("Segoe UI", Font.PLAIN, size));
        result.setForeground(color);
        return result;
    }

    private JButton button(String title, Runnable action, boolean primary) {
        JButton b = new JButton(title);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(primary ? Color.WHITE : INK);
        b.setBackground(primary ? TEAL : Color.WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primary ? TEAL : LINE), new EmptyBorder(9, 14, 9, 14)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> { if (!busy) action.run(); });
        return b;
    }

    private JPanel padded(LayoutManager layout, int pad) {
        JPanel p = new JPanel(layout);
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(pad, pad, pad, pad));
        return p;
    }

    private void showLogin() {
        removeAll();
        JPanel header = padded(new BorderLayout(), 24);
        JLabel brand = label("+   PHÒNG KHÁM NHÓM 4", 20, TEAL);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD));
        header.add(brand, BorderLayout.WEST);
        header.add(label("Tiếp nhận  /  Khám bệnh  /  Phát thuốc", 13, MUTED), BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(BG);
        JPanel form = padded(new GridBagLayout(), 36);
        form.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), new EmptyBorder(36, 36, 36, 36)));
        form.setPreferredSize(new Dimension(460, 490));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(0, 0, 10, 0);
        form.add(label("Đăng nhập", 28, INK), c);
        c.gridy++; form.add(label("Tài khoản phòng khám", 14, MUTED), c);
        loginUser = field(""); loginPassword = new JPasswordField(); styleField(loginPassword);
        c.gridy++; c.insets = new Insets(20, 0, 8, 0); form.add(label("Tên đăng nhập", 13, INK), c);
        c.gridy++; c.insets = new Insets(0, 0, 8, 0); form.add(loginUser, c);
        c.gridy++; form.add(label("Mật khẩu", 13, INK), c);
        c.gridy++; form.add(loginPassword, c);
        JTextField serverHost = field(host);
        JSpinner serverPort = new JSpinner(new SpinnerNumberModel(port, 1, 65535, 1));
        serverPort.setEditor(new JSpinner.NumberEditor(serverPort, "#"));
        JPanel connection = new JPanel(new BorderLayout(8, 0)); connection.setOpaque(false);
        connection.add(serverHost, BorderLayout.CENTER); connection.add(serverPort, BorderLayout.EAST);
        c.gridy++; c.insets = new Insets(16, 0, 8, 0); form.add(label("Máy chủ / Cổng", 13, MUTED), c);
        c.gridy++; c.insets = new Insets(0, 0, 8, 0); form.add(connection, c);
        loginButton = button("Đăng nhập", () -> {
            host = serverHost.getText().trim(); port = (Integer) serverPort.getValue();
            if (loginUser.getText().isBlank() || loginPassword.getPassword().length == 0) { error("Nhập tài khoản và mật khẩu."); return; }
            loginButton.setEnabled(false);
            call("GUI_LOGIN", data("username", loginUser.getText().trim(), "password", new String(loginPassword.getPassword())), response -> {
                loginButton.setEnabled(true);
                if (response.isSuccess()) {
                    user = stringMap(response.getPayload()); loginPassword.setText(""); showWorkspace();
                } else { error(response.getMessage()); }
            });
        }, true);
        c.gridy++; c.insets = new Insets(18, 0, 0, 0); form.add(loginButton, c);
        loginPassword.addActionListener(e -> loginButton.doClick());
        center.add(form); add(center, BorderLayout.CENTER);
        add(footer(), BorderLayout.SOUTH);
        revalidate(); repaint();
    }

    private JPanel footer() {
        JPanel footer = padded(new BorderLayout(), 14);
        footer.add(status, BorderLayout.WEST);
        footer.add(label("NHÓM 4  •  PHÒNG KHÁM", 11, MUTED), BorderLayout.EAST);
        return footer;
    }

    private void showWorkspace() {
        removeAll(); navigation.clear();
        JPanel sidebar = padded(new BorderLayout(0, 24), 20);
        sidebar.setPreferredSize(new Dimension(218, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, LINE), new EmptyBorder(28, 18, 20, 18)));
        JPanel branding = new JPanel(new GridLayout(2, 1, 0, 6)); branding.setOpaque(false);
        JLabel brand = label("+  PHÒNG KHÁM", 19, TEAL); brand.setFont(brand.getFont().deriveFont(Font.BOLD));
        branding.add(brand); branding.add(label("NHÓM 4", 12, MUTED)); sidebar.add(branding, BorderLayout.NORTH);
        JPanel nav = new JPanel(); nav.setOpaque(false); nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        String role = user.get("role");
        if (role.equals("MANAGER") || role.equals("RECEPTIONIST")) nav(nav, "patients", "Bệnh nhân");
        if (!role.equals("PHARMACIST")) nav(nav, "appointments", "Lịch khám");
        if (role.equals("MANAGER") || role.equals("DOCTOR")) nav(nav, "labs", "Xét nghiệm");
        if (role.equals("MANAGER") || role.equals("PHARMACIST")) nav(nav, "prescriptions", "Toa thuốc");
        nav(nav, "medicines", "Danh mục thuốc");
        if (role.equals("MANAGER")) nav(nav, "accounts", "Tài khoản");
        nav(nav, "profile", "Hồ sơ cá nhân");
        sidebar.add(nav, BorderLayout.CENTER);
        sidebar.add(button("Đăng xuất", () -> { user = null; generation++; showLogin(); }, false), BorderLayout.SOUTH);
        add(sidebar, BorderLayout.WEST);
        JPanel right = new JPanel(new BorderLayout()); right.setBackground(BG);
        JPanel header = padded(new BorderLayout(), 22);
        header.add(label("Quản lý khám bệnh", 18, INK), BorderLayout.WEST);
        header.add(label(user.get("fullName") + "   /   " + roleName(role), 13, TEAL), BorderLayout.EAST);
        right.add(header, BorderLayout.NORTH);
        body.setBackground(BG); body.setBorder(new EmptyBorder(26, 28, 24, 28));
        right.add(body, BorderLayout.CENTER); right.add(footer(), BorderLayout.SOUTH);
        add(right, BorderLayout.CENTER);
        view = navigation.keySet().iterator().next(); openView(view);
        revalidate(); repaint();
    }

    private void nav(JPanel nav, String key, String title) {
        JButton b = button(title, () -> openView(key), false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        navigation.put(key, b); nav.add(b); nav.add(Box.createVerticalStrut(9));
    }

    private void openView(String key) {
        view = key; generation++; body.removeAll();
        navigation.forEach((id, b) -> { b.setBackground(id.equals(key) ? new Color(225, 243, 239) : Color.WHITE); b.setForeground(id.equals(key) ? TEAL : INK); });
        if (key.equals("profile")) { profile(); body.revalidate(); body.repaint(); return; }
        JPanel heading = new JPanel(new BorderLayout(0, 18)); heading.setOpaque(false);
        JPanel title = new JPanel(new BorderLayout()); title.setOpaque(false);
        title.add(label(navigation.get(key).getText(), 24, INK), BorderLayout.WEST);
        count = label("Đang tải…", 13, MUTED); title.add(count, BorderLayout.EAST);
        heading.add(title, BorderLayout.NORTH);
        tools.removeAll(); tools.setOpaque(false);
        JPanel controls = new JPanel(); controls.setOpaque(false); controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        if (key.equals("patients")) {
            JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); searchBar.setOpaque(false);
            styleField(search); search.setPreferredSize(new Dimension(260, 36)); searchBar.add(search);
            searchBar.add(button("Tìm kiếm", this::refresh, false));
            controls.add(searchBar); controls.add(Box.createVerticalStrut(12));
            for (ActionListener l : search.getActionListeners()) search.removeActionListener(l);
            search.addActionListener(e -> refresh());
            tools.add(button("+ Thêm bệnh nhân", () -> patientForm(false), true));
            tools.add(button("Sửa hồ sơ", () -> patientForm(true), false));
            tools.add(button("Đặt lịch", this::book, false));
            if (user.get("role").equals("MANAGER")) tools.add(button("Xóa hồ sơ cũ", this::deleteOld, false));
        } else if (key.equals("appointments")) {
            if (user.get("role").equals("DOCTOR") || user.get("role").equals("MANAGER")) {
                tools.add(button("Khám / Kê toa", this::examine, true));
                tools.add(button("Kê toa sau xét nghiệm", () -> prescription(true), false));
            }
        } else if (key.equals("labs")) tools.add(button("Cập nhật kết quả", this::completeLab, true));
        else if (key.equals("prescriptions")) {
            tools.add(button("Xem chi tiết", this::prescriptionDetails, false));
            tools.add(button("Phát thuốc", () -> selected(row -> {
                if (row.get(4).equals("true")) { error("Toa này đã được phát thuốc."); return; }
                if (confirm("Phát thuốc cho " + row.get(2) + "?")) mutate("DISPENSE", data("prescriptionId", row.get(0)));
            }), true));
        } else if (key.equals("accounts")) {
            tools.add(button("+ Tạo tài khoản", this::accountForm, true));
            tools.add(button("Khóa tài khoản", () -> selected(row -> {
                if (row.get(0).equals(user.get("id"))) { error("Không thể khóa tài khoản đang sử dụng."); return; }
                if (confirm("Khóa tài khoản " + row.get(1) + "?")) mutate("DEACTIVATE_ACCOUNT", data("accountId", row.get(0)));
            }), false));
        }
        tools.add(button("Làm mới", this::refresh, false)); controls.add(tools); heading.add(controls, BorderLayout.CENTER);
        body.add(heading, BorderLayout.NORTH);
        model.setDataVector(new Object[0][], columns.get(key));
        table.setRowHeight(42); table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); table.setAutoCreateRowSorter(true);
        table.setShowGrid(false); table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(219, 240, 235)); table.setSelectionForeground(INK);
        table.setFillsViewportHeight(true); table.setBackground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(233, 239, 240)); table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean selected, boolean focus, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, display(v), selected, false, r, c);
                l.setBorder(new EmptyBorder(0, 10, 0, 10));
                l.setToolTipText(display(v));
                if (!selected) { l.setBackground(r % 2 == 0 ? Color.WHITE : new Color(249, 251, 251)); l.setForeground(INK); }
                if (String.valueOf(v).equals("WAITING_LAB")) l.setForeground(new Color(161, 93, 13));
                if (String.valueOf(v).equals("DISPENSED") || String.valueOf(v).equals("LAB_DONE")) l.setForeground(TEAL);
                return l;
            }
        });
        JScrollPane scroll = new JScrollPane(table); scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.setColumnHeaderView(table.getTableHeader());
        body.add(scroll, BorderLayout.CENTER);
        JLabel note = label("", 12, MUTED); body.add(note, BorderLayout.SOUTH);
        if (table.getSelectionModel() instanceof DefaultListSelectionModel selection) {
            for (javax.swing.event.ListSelectionListener listener : selection.getListSelectionListeners()) selection.removeListSelectionListener(listener);
        }
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) note.setText(table.getSelectedRow() < 0 ? "" : "Đang chọn bản ghi #" + model.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 0));
        });
        body.revalidate(); body.repaint(); refresh();
    }

    private void refresh() {
        if (view.equals("profile")) return;
        long ticket = generation;
        call("GUI_DATA", data("type", view, "keyword", search.getText().trim(), "doctorId", user.get("role").equals("DOCTOR") ? user.get("id") : "0"), response -> {
            if (ticket != generation) return;
            if (!response.isSuccess()) { error(response.getMessage()); return; }
            List<List<String>> rows = rows(response.getPayload()); model.setRowCount(0);
            for (List<String> row : rows) model.addRow(row.toArray());
            count.setText(rows.size() + " bản ghi"); status.setText("Đã cập nhật  •  " + host + ":" + port);
        });
    }

    private void selected(Consumer<List<String>> action) {
        if (table.getSelectedRow() < 0) { error("Chọn một dòng trong bảng trước."); return; }
        int index = table.convertRowIndexToModel(table.getSelectedRow());
        List<String> row = new ArrayList<>();
        for (int c = 0; c < model.getColumnCount(); c++) row.add(String.valueOf(model.getValueAt(index, c)));
        action.accept(row);
    }

    private JTextField field(String value) { JTextField f = new JTextField(value); styleField(f); return f; }
    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14)); field.setForeground(INK);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), new EmptyBorder(9, 10, 9, 10)));
    }
    private JPanel form(Object... pairs) {
        JPanel p = padded(new GridBagLayout(), 12); GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.gridx = 0; c.insets = new Insets(4, 0, 4, 0);
        for (int i = 0; i < pairs.length; i += 2) {
            c.gridy = i; p.add(label(String.valueOf(pairs[i]), 13, MUTED), c);
            c.gridy = i + 1; p.add((Component) pairs[i + 1], c);
        }
        p.setPreferredSize(new Dimension(480, p.getPreferredSize().height)); return p;
    }

    private void dialog(String title, JPanel content, Runnable save) {
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), title, Dialog.ModalityType.APPLICATION_MODAL);
        JPanel p = padded(new BorderLayout(0, 10), 18); p.add(content, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false);
        actions.add(button("Hủy", d::dispose, false));
        actions.add(button("Lưu", () -> { try { save.run(); d.dispose(); } catch (IllegalArgumentException ex) { error(ex.getMessage()); } }, true));
        p.add(actions, BorderLayout.SOUTH); d.setContentPane(p); d.pack(); d.setLocationRelativeTo(this); d.setVisible(true);
    }

    private void patientForm(boolean edit) {
        if (edit) selected(row -> patientFields(row)); else patientFields(Collections.nCopies(8, ""));
    }
    private void patientFields(List<String> row) {
        JTextField code = field(row.get(1)), name = field(row.get(2)), dob = field(row.get(3)), phone = field(row.get(5)), address = field(row.get(6));
        code.setEditable(row.get(0).isBlank());
        JComboBox<String> gender = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"}); if (!row.get(4).isBlank()) gender.setSelectedItem(row.get(4));
        dialog(row.get(0).isBlank() ? "Thêm bệnh nhân" : "Cập nhật hồ sơ", form("Mã bệnh nhân", code, "Họ và tên", name, "Ngày sinh (yyyy-MM-dd)", dob, "Giới tính", gender, "Điện thoại", phone, "Địa chỉ", address), () -> {
            required(code.getText(), name.getText(), dob.getText());
            try { if (LocalDate.parse(dob.getText().trim()).isAfter(LocalDate.now())) throw new IllegalArgumentException("Ngày sinh không được ở tương lai."); }
            catch (java.time.format.DateTimeParseException e) { throw new IllegalArgumentException("Ngày sinh cần đúng dạng yyyy-MM-dd."); }
            mutate("SAVE_PATIENT", data("code", code.getText().trim(), "fullName", name.getText().trim(), "dob", dob.getText().trim(), "gender", gender.getSelectedItem().toString(), "phone", phone.getText().trim(), "address", address.getText().trim()));
        });
    }

    private record Choice(String id, String name) { @Override public String toString() { return name; } }
    private void book() {
        selected(patient -> call("GUI_DATA", data("type", "doctors", "keyword", "", "doctorId", "0"), response -> {
            if (!response.isSuccess()) { error(response.getMessage()); return; }
            JComboBox<Choice> doctors = new JComboBox<>();
            for (List<String> row : rows(response.getPayload())) doctors.addItem(new Choice(row.get(0), row.get(1)));
            if (doctors.getItemCount() == 0) { error("Chưa có bác sĩ hoạt động."); return; }
            JTextField note = field("");
            dialog("Tiếp nhận khám • " + patient.get(2), form("Bác sĩ phụ trách", doctors, "Ghi chú", note), () -> {
                mutate("ADD_APPOINTMENT", data("patientCode", patient.get(1), "doctorId", ((Choice) doctors.getSelectedItem()).id(), "note", note.getText().trim()));
            });
        }));
    }

    private void examine() {
        selected(appointment -> {
            if (!appointment.get(4).equals("WAITING")) { error("Chọn lịch ở trạng thái Chờ khám."); return; }
            JTextField symptoms = field(""), diagnosis = field(""); JCheckBox lab = new JCheckBox("Chuyển xét nghiệm"); lab.setOpaque(false);
            JTextField labName = field(""); labName.setEnabled(false); lab.addActionListener(e -> labName.setEnabled(lab.isSelected()));
            JPanel f = form("Bệnh nhân", label(appointment.get(2), 16, INK), "Triệu chứng", symptoms, "Chẩn đoán", diagnosis, "Chỉ định", lab, "Tên xét nghiệm", labName);
            // The prescription editor preserves the same visit fields when no lab is requested.
            dialog("Khám bệnh", f, () -> {
                required(symptoms.getText(), diagnosis.getText());
                if (lab.isSelected()) {
                    required(labName.getText());
                    mutate("EXAMINE", data("appointmentId", appointment.get(0), "symptoms", symptoms.getText(), "diagnosis", diagnosis.getText(), "labName", labName.getText(), "prescription", ""));
                } else {
                    SwingUtilities.invokeLater(() -> prescriptionEditor(appointment.get(0), false, symptoms.getText(), diagnosis.getText()));
                }
            });
        });
    }

    private void prescription(boolean afterLab) {
        selected(row -> {
            if (afterLab && !row.get(4).equals("LAB_DONE")) { error("Chọn lịch đã có kết quả xét nghiệm."); return; }
            prescriptionEditor(row.get(0), afterLab, "", "");
        });
    }

    private void prescriptionEditor(String appointmentId, boolean afterLab, String symptoms, String diagnosis) {
        call("GUI_DATA", data("type", "medicines", "keyword", "", "doctorId", "0"), response -> {
            if (!response.isSuccess()) { error(response.getMessage()); return; }
            JComboBox<Choice> medicine = new JComboBox<>();
            for (List<String> row : rows(response.getPayload())) medicine.addItem(new Choice(row.get(1), row.get(1) + "  •  tồn " + row.get(3)));
            JSpinner quantity = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
            JTextField instruction = field("");
            DefaultTableModel items = new DefaultTableModel(new String[]{"Thuốc", "Số lượng", "Cách dùng"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
            JTable lines = new JTable(items); lines.setRowHeight(32); lines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JPanel editor = padded(new BorderLayout(0, 10), 4);
            editor.add(form("Thuốc", medicine, "Số lượng", quantity, "Cách dùng", instruction), BorderLayout.NORTH);
            JScrollPane scroll = new JScrollPane(lines); scroll.setPreferredSize(new Dimension(550, 180)); editor.add(scroll, BorderLayout.CENTER);
            JPanel commands = new JPanel(new FlowLayout(FlowLayout.LEFT)); commands.setOpaque(false);
            commands.add(button("+ Thêm vào toa", () -> {
                if (medicine.getSelectedItem() == null || instruction.getText().isBlank()) { error("Chọn thuốc và nhập cách dùng."); return; }
                String name = ((Choice) medicine.getSelectedItem()).id();
                if (instruction.getText().contains("|") || instruction.getText().contains(";")) { error("Cách dùng không được chứa ký tự | hoặc ;."); return; }
                items.addRow(new Object[]{name, quantity.getValue(), instruction.getText().trim()}); instruction.setText("");
            }, false));
            commands.add(button("Xóa dòng", () -> { if (lines.getSelectedRow() >= 0) items.removeRow(lines.getSelectedRow()); }, false));
            JTextField result = field("");
            JPanel bottom = new JPanel(new BorderLayout()); bottom.setOpaque(false); bottom.add(commands, BorderLayout.NORTH);
            if (afterLab) bottom.add(form("Nhận định kết quả xét nghiệm", result), BorderLayout.CENTER);
            editor.add(bottom, BorderLayout.SOUTH);
            dialog("Kê toa • Lịch #" + appointmentId, editor, () -> {
                if (items.getRowCount() == 0) throw new IllegalArgumentException("Thêm ít nhất một thuốc vào toa.");
                StringJoiner prescription = new StringJoiner(";");
                for (int i = 0; i < items.getRowCount(); i++) prescription.add(items.getValueAt(i, 0) + "|" + items.getValueAt(i, 1) + "|" + items.getValueAt(i, 2));
                if (afterLab) { required(result.getText()); mutate("PRESCRIBE_AFTER_LAB", data("appointmentId", appointmentId, "labResult", result.getText(), "prescription", prescription.toString())); }
                else mutate("EXAMINE", data("appointmentId", appointmentId, "symptoms", symptoms, "diagnosis", diagnosis, "labName", "", "prescription", prescription.toString()));
            });
        });
    }

    private void completeLab() {
        selected(row -> {
            if (!row.get(5).isBlank()) { error("Xét nghiệm đã có kết quả."); return; }
            JTextField result = field("");
            dialog("Kết quả • " + row.get(1), form("Xét nghiệm", label(row.get(2), 16, INK), "Kết quả", result), () -> {
                required(result.getText()); mutate("COMPLETE_LAB", data("appointmentId", row.get(0), "result", result.getText().trim()));
            });
        });
    }

    private void prescriptionDetails() {
        selected(row -> call("GUI_PRESCRIPTION", data("id", row.get(0)), response -> {
            if (!response.isSuccess()) { error(response.getMessage()); return; }
            JTable detail = new JTable(new DefaultTableModel(rows(response.getPayload()).stream().map(List::toArray).toArray(Object[][]::new), new String[]{"Thuốc", "Số lượng", "Đơn vị", "Cách dùng"}) { @Override public boolean isCellEditable(int r, int c) { return false; } });
            detail.setRowHeight(36); JScrollPane scroll = new JScrollPane(detail); scroll.setPreferredSize(new Dimension(650, 250));
            JOptionPane.showMessageDialog(this, scroll, "Toa #" + row.get(0) + " • " + row.get(2), JOptionPane.PLAIN_MESSAGE);
        }));
    }

    private void accountForm() {
        JTextField username = field(""), name = field(""), phone = field(""), email = field(""); JPasswordField password = new JPasswordField(); styleField(password);
        JComboBox<Choice> role = new JComboBox<>(new Choice[]{new Choice("RECEPTIONIST", "Tiếp nhận"), new Choice("DOCTOR", "Bác sĩ"), new Choice("PHARMACIST", "Phát thuốc"), new Choice("MANAGER", "Quản lý")});
        dialog("Tạo tài khoản", form("Tên đăng nhập", username, "Họ tên", name, "Mật khẩu", password, "Vai trò", role, "Điện thoại", phone, "Email", email), () -> {
            required(username.getText(), name.getText(), new String(password.getPassword()));
            mutate("CREATE_ACCOUNT", data("username", username.getText().trim(), "password", new String(password.getPassword()), "fullName", name.getText().trim(), "role", ((Choice) role.getSelectedItem()).id(), "phone", phone.getText().trim(), "email", email.getText().trim()));
        });
    }

    private void deleteOld() {
        if (confirm("Xóa các hồ sơ hơn 3 năm chưa tái khám? Thao tác không thể hoàn tác.")) mutate("DELETE_OLD_PATIENTS", Map.of());
    }

    private void profile() {
        JPanel panel = padded(new BorderLayout(0, 24), 24);
        panel.add(label("Hồ sơ cá nhân", 24, INK), BorderLayout.NORTH);
        panel.add(form("Họ và tên", label(user.get("fullName"), 18, INK), "Tên đăng nhập", label(user.get("username"), 15, INK), "Vai trò", label(roleName(user.get("role")), 15, INK), "Điện thoại", label(user.get("phone"), 15, INK), "Email", label(user.get("email"), 15, INK)), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT)); bottom.setOpaque(false);
        bottom.add(button("Đổi mật khẩu", () -> {
            JPasswordField old = new JPasswordField(), next = new JPasswordField(), again = new JPasswordField(); styleField(old); styleField(next); styleField(again);
            dialog("Đổi mật khẩu", form("Mật khẩu hiện tại", old, "Mật khẩu mới", next, "Nhập lại mật khẩu mới", again), () -> {
                String value = new String(next.getPassword()); required(new String(old.getPassword()), value);
                if (!value.equals(new String(again.getPassword()))) throw new IllegalArgumentException("Hai mật khẩu mới chưa khớp.");
                mutate("CHANGE_PASSWORD", data("username", user.get("username"), "oldPassword", new String(old.getPassword()), "newPassword", value));
            });
        }, true)); panel.add(bottom, BorderLayout.SOUTH);
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false); top.add(panel, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(top); scroll.setBorder(null); scroll.getVerticalScrollBar().setUnitIncrement(18); body.add(scroll, BorderLayout.CENTER);
    }

    private void required(String... values) { for (String value : values) if (value.isBlank()) throw new IllegalArgumentException("Điền đầy đủ các trường bắt buộc."); }
    private boolean confirm(String message) { return JOptionPane.showConfirmDialog(this, message, "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION; }
    private void error(String message) {
        String text = message == null ? "Không thể xử lý yêu cầu." : message;
        if (text.contains("Lenh khong ho tro")) text = "Server đang chạy bản cũ. Dừng server cũ và chạy lại 03-Server-Eclipse.";
        JOptionPane.showMessageDialog(this, text, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }
    private void mutate(String action, Map<String, String> data) {
        call(action, data, response -> {
            if (response.isSuccess()) { status.setText("Đã lưu thay đổi"); refresh(); }
            else error(response.getMessage());
        });
    }

    private Response send(String action, Map<String, String> data) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 3000); socket.setSoTimeout(12000);
            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                out.writeObject(new Request(action, data)); out.flush(); return (Response) in.readObject();
            }
        } catch (Exception e) { return Response.fail("Không kết nối được máy chủ " + host + ":" + port + ". Kiểm tra server đã chạy và địa chỉ kết nối."); }
    }

    private void call(String action, Map<String, String> data, Consumer<Response> callback) {
        if (busy) return;
        busy = true; setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); status.setText("Đang xử lý…");
        new SwingWorker<Response, Void>() {
            @Override protected Response doInBackground() { return send(action, data); }
            @Override protected void done() {
                busy = false; setCursor(Cursor.getDefaultCursor());
                try { Response response = get(); status.setText(response.isSuccess() ? "Đã kết nối máy chủ" : "Yêu cầu không thành công"); callback.accept(response); }
                catch (Exception ex) { error(ex.getMessage()); }
            }
        }.execute();
    }

    private static Map<String, String> data(String... pairs) { Map<String, String> d = new HashMap<>(); for (int i = 0; i < pairs.length; i += 2) d.put(pairs[i], pairs[i + 1]); return d; }
    @SuppressWarnings("unchecked") private static Map<String, String> stringMap(Object payload) { return (Map<String, String>) payload; }
    @SuppressWarnings("unchecked") private static List<List<String>> rows(Object payload) { return (List<List<String>>) payload; }
    private static String roleName(String role) { return switch (role) { case "DOCTOR" -> "Bác sĩ"; case "MANAGER" -> "Quản lý"; case "PHARMACIST" -> "Phát thuốc"; default -> "Tiếp nhận"; }; }
    private static String display(Object value) {
        String text = String.valueOf(value);
        return switch (text) { case "WAITING" -> "Chờ khám"; case "IN_EXAM" -> "Đang khám"; case "WAITING_LAB" -> "Chờ xét nghiệm"; case "LAB_DONE" -> "Có kết quả"; case "PRESCRIBED" -> "Đã kê toa"; case "DISPENSED" -> "Đã phát thuốc"; case "CANCELLED" -> "Đã hủy"; case "true" -> "Có"; case "false" -> "Không"; case "DOCTOR", "MANAGER", "PHARMACIST", "RECEPTIONIST" -> roleName(text); default -> text; };
    }

    private static void preview(String[] args) throws Exception {
        ClinicDesktop[] app = new ClinicDesktop[1];
        SwingUtilities.invokeAndWait(() -> { app[0] = new ClinicDesktop(); app[0].setSize(1280, 800); });
        capture(app[0], args[1] + "-login.png");
        app[0].port = Integer.parseInt(args[2]);
        Response response = app[0].send("GUI_LOGIN", data("username", "manager", "password", "123"));
        if (!response.isSuccess()) throw new IllegalStateException(response.getMessage());
        SwingUtilities.invokeAndWait(() -> { app[0].user = stringMap(response.getPayload()); app[0].showWorkspace(); });
        for (int i = 0; i < 100; i++) { Thread.sleep(100); final boolean[] done = {false}; SwingUtilities.invokeAndWait(() -> done[0] = !app[0].busy); if (done[0]) break; }
        capture(app[0], args[1] + "-patients.png");
        SwingUtilities.invokeAndWait(() -> app[0].setSize(1000, 660));
        capture(app[0], args[1] + "-compact.png");
        SwingUtilities.invokeAndWait(() -> app[0].setSize(1280, 800));
        SwingUtilities.invokeAndWait(() -> { if (app[0].model.getRowCount() == 0) throw new IllegalStateException("GUI patient table is empty"); app[0].openView("appointments"); });
        for (int i = 0; i < 100; i++) { Thread.sleep(100); final boolean[] done = {false}; SwingUtilities.invokeAndWait(() -> done[0] = !app[0].busy); if (done[0]) break; }
        capture(app[0], args[1] + "-appointments.png");
        System.out.println("PASS: GUI login, structured data, patient table and rendered screens");
    }
    private static void capture(ClinicDesktop panel, String path) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            layout(panel);
            java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(panel.getWidth(), panel.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            panel.printAll(g); g.dispose();
            try { javax.imageio.ImageIO.write(image, "png", new File(path)); } catch (IOException e) { throw new RuntimeException(e); }
        });
    }
    private static void layout(Container container) { container.doLayout(); for (Component child : container.getComponents()) if (child instanceof Container c) layout(c); }
}
