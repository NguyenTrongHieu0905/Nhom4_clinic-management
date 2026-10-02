# Hệ thống quản lý đặt lịch khám — Nhóm 4

Bài tập lớn môn Lập trình Phân tán với Công nghệ Java.

## Công nghệ

- Java 17/21, giao diện Java Swing.
- Java Persistence API (JPA), Hibernate và database H2.
- Giao tiếp Client/Server qua TCP Socket.

## Chức năng

- Đăng nhập, xem hồ sơ và đổi mật khẩu.
- Tiếp nhận, cập nhật, tìm kiếm bệnh nhân và đặt lịch khám.
- Khám bệnh, chỉ định xét nghiệm và kê toa.
- Phát thuốc theo toa, cập nhật tồn kho.
- Quản lý tài khoản và hồ sơ bệnh nhân.

## Cách chạy

Máy cần có JDK 17 hoặc 21. Thư viện đã có trong thư mục `lib`;
không cần cài Neo4j hoặc database riêng.

### Chạy trực tiếp

Nhấp đúp `fix-and-start.cmd` để khởi động server và mở giao diện.
File này sẽ dừng server phòng khám cũ nếu đang giữ cổng 8888.

### Chạy trong Eclipse

1. Import project bằng **General → Existing Projects into Workspace**.
2. Chọn project và nhấn **F5**.
3. Vào **Run → External Tools → External Tools Configurations → Program**.
4. Chạy `03-Server-Eclipse`, chờ thông báo cổng 8888.
5. Chạy `05-Giao-Dien` để mở cửa sổ đăng nhập.

Chỉ chạy một server tại một thời điểm.
Địa chỉ kết nối mặc định: `localhost:8888`.

## Tài khoản mẫu

| Vai trò | Tài khoản | Mật khẩu |
|---|---|---|
| Tiếp nhận | reception | 123 |
| Bác sĩ | doctor | 123 |
| Phát thuốc | pharmacy | 123 |
| Quản lý | manager | 123 |

## Build và dữ liệu

- Chạy `build.cmd` sau khi chỉnh sửa source.
- File chạy: `dist/clinic-desktop-v2.jar`.
- Database: `data/clinicdb-v2.mv.db`.
- Log khi chạy bằng `fix-and-start.cmd`: thư mục `logs`.

Đóng ứng dụng trước khi build lại. Không xóa thư mục `lib`
hoặc file database nếu muốn giữ dữ liệu.
