# Ung dung quan ly dat lich kham

## Ban sua cau hinh moi nhat

Nhan dup fix-and-start.cmd trong thu muc du an. Script chi dung tien trinh
ClinicServer dang giu cong 8888, mo server moi va giao dien. Khong xoa database.
Script can chay tu File Explorer de Windows cho phep dung server do Eclipse mo.
Neu cong 8888 thuoc chuong trinh khac, script dung lai va hien thong bao.
Log server duoc luu trong logs. Runtime moi: dist/clinic-desktop-v2.jar.
Tat ca cau hinh client (02, 04, 05) deu mo giao dien Swing.
ClinicClient main mac dinh mo giao dien; dung --console de chay ban dong lenh.

## Giao dien Swing

Ban moi co giao dien tieng Viet: dang nhap, benh nhan, lich kham, xet nghiem,
toa thuoc, danh muc thuoc, tai khoan va ho so ca nhan. Dieu huong theo vai tro.
Dung dist/clinic-desktop.jar cho ca server va client giao dien.

Trong Eclipse: nhan F5, dung Run > External Tools > External Tools Configurations
> Program > 03-Server-Eclipse. Dung server cu truoc neu no dang chay. Sau khi
co thong bao cong 8888, chay 05-Giao-Dien de mo cua so Swing.
Co the chay run-server.cmd va run-gui.cmd ngoai Eclipse.

Khi dang nhap, co the doi may chu va cong de ket noi trong mang LAN.
Them/sua benh nhan bang bieu mau; chon dong benh nhan roi bam Dat lich.
Bac si chon lich Cho kham de kham/chuyen xet nghiem. Toa thuoc duoc them
tung dong bang danh sach thuoc, so luong va cach dung. Chon toa de xem chi tiet
hoac phat thuoc. Cap nhat xet nghiem tai tab Xet nghiem; chon lich Co ket qua
de ke toa sau xet nghiem.
Sau khi sua source, chay build.cmd; JAR moi la dist/clinic-desktop.jar.

## Chay Eclipse sau khi sua truc tiep

Cap nhat: dung 03-Server-Eclipse.launch va 04-Client-Eclipse.launch.
Nhan F5 tren project, vao Run > External Tools > External Tools Configurations
> Program > 03-Server-Eclipse > Run. Cho thong bao cong 8888, sau do chay
04-Client-Eclipse trong cung danh sach Program. Nhap tai khoan o Console.
Hai cau hinh nay goi truc tiep Java voi classpath tuong doi, khong qua
bo tao classpath/modulepath cua Java Application trong Eclipse.
Da kiem tra dung executable va arguments cua hai file: server khoi dong,
client dang nhap reception/123 va xem thong tin thanh cong.
Huong dan 01/02 ben duoi la cau hinh cu.

Chon project va nhan F5 de refresh. Chuot phai 01-Clinic-Server.launch >
Run As > 01-Clinic-Server. Cho thong bao cong 8888, sau do chuot phai
02-Clinic-Client.launch > Run As > 02-Clinic-Client.
Hai cau hinh moi co classpath rieng va working directory dung thu muc du an,
su dung che do classpath-only JAR cho duong dan Windows co dau va khoang trang.
Khong dung nut Run de chay lai cau hinh ClinicServer (1) cu.
Neu sua source, chay build.cmd truoc de cap nhat dist/clinic-management.jar.

Do an mon Lap trinh Phan tan voi Cong nghe Java.

## Cong nghe

- Java 17
- Java Persistence API voi Hibernate
- Co so du lieu H2 file database
- Network Programming Client/Server bang Socket
- Maven

## Chay chuong trinh

Can JDK 17 hoac 21 (java va javac trong PATH). Ban ZIP kem thu muc lib,
khong can cai Maven hoac tai thu vien khi chay bang cac file CMD.
Ban ZIP cung kem dist/clinic-management.jar da bien dich cho Java 17.
Neu sua source, chay build.cmd de tao lai JAR truoc khi chay cac file CMD.

Giai nen toan bo ZIP. Chay run-server.cmd truoc, cho dong
"Clinic server dang chay tai cong 8888", sau do chay run-client.cmd.
Dang nhap bang tai khoan mau ben duoi. Khong xoa thu muc lib.
Database ban sua duoc tao tai data/clinicdb-v2.mv.db, tach khoi database
cu de tranh loi nhan dien ten bang khi Hibernate khoi dong lai.

Neu chay trong Eclipse: File > Import > General > Existing Projects into
Workspace, chon thu muc clinic-management. Ten project moi la
clinic-management-fixed de tranh trung project cu. Chon JDK 17/21,
Project > Clean, sau do Run As > Java Application cho ClinicServer,
va ClinicClient. Ban offline da co .project, .classpath va thu vien lib.
Build Path cung kem dist/clinic-management.jar de tim duoc lop main khi
Eclipse chua tao target/classes. Neu dang dung ban ZIP truoc, co the sua
truc tiep: chuot phai project > Build Path > Configure Build Path >
Libraries > Classpath > Add JARs > dist/clinic-management.jar > Apply and Close.
Run > Run Configurations > Java Application > chon ClinicServer > Classpath
(hoac Dependencies) > Restore Default Entries > Apply > Run.
Kiem tra tab Main: Project = clinic-management-fixed va Main class =
vn.edu.clinic.server.ClinicServer. Khong chon project cu clinic-management.

Neu chon import bang Maven: File > Import > Maven > Existing Maven Projects,
sau do Maven > Update Project va Project > Clean. Khong them src/main/java
lan thu hai vao Build Path. Chi chay khi tab Problems khong con Errors.

Chay client tren may khac trong mang LAN:

```bat
run-client.cmd 192.168.1.10
```

Thay IP bang IP may server. Cong TCP la 8888.

Neu dung Maven, mo 2 terminal tai thu muc du an.

Terminal 1 chay server:

```bash
mvn compile exec:java "-Dexec.mainClass=vn.edu.clinic.server.ClinicServer"
```

Terminal 2 chay client:

```bash
mvn compile exec:java "-Dexec.mainClass=vn.edu.clinic.client.ClinicClient"
```

## Tai khoan mau

| Vai tro | Username | Password |
|---|---|---|
| Nhan vien tiep nhan | reception | 123 |
| Bac si | doctor | 123 |
| Nhan vien phat thuoc | pharmacy | 123 |
| Quan ly | manager | 123 |

## Dinh dang toa thuoc

Khi bac si ke toa, nhap theo mau:

```text
Paracetamol 500mg|10|Uong 2 vien/ngay sau an;Vitamin C|6|Uong 1 vien/ngay
```

## Cac chuc nang chinh

- Dang nhap, xem thong tin tai khoan, doi mat khau.
- Tiep nhan them, sua, tim benh nhan va dua benh nhan vao danh sach cho cua bac si.
- Bac si xem danh sach cho, ghi nhan trieu chung, chan doan, chuyen xet nghiem hoac ke toa.
- Cap nhat ket qua xet nghiem va ke toa sau xet nghiem.
- Nhan vien phat thuoc xem toa chua phat va cap nhat trang thai da phat.
- Quan ly xem/tao/khoa tai khoan, xem toa thuoc, xem danh muc thuoc va xoa benh nhan hon 3 nam khong tai kham.

## Kiem thu ban sua

Da khoi dong server Hibernate 6.5.2 + H2, chay client console dang nhap,
xem thong tin va tim BN001. Da vuot qua 31 kiem tra qua Socket, bao gom
kham truc tiep va luong xet nghiem, tu choi so luong thuoc am, kham lai
lich da ke toa, ke toa khi chua co ket qua, va phat thuoc hai lan.

Co the chay test.cmd khi server dang chay (database demo va tai khoan mau).
Kiem thu se them benh nhan TEST va tru mot it thuoc trong database demo.
