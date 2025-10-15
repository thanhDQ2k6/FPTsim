# Hướng dẫn sử dụng JPA Entities - FPT SIM

## Tổng quan về JPA Entities

JPA (Java Persistence API) Entities là các lớp Java đại diện cho các bảng trong cơ sở dữ liệu. Trong dự án FPT SIM,
chúng ta sử dụng Spring Data JPA kết hợp với Hibernate để thao tác với cơ sở dữ liệu MySQL.

## Danh sách JPA Entities

Dự án FPT SIM có 12 entity chính:

1. `NguoiDung`: Thông tin người dùng hệ thống (khách hàng, nhân viên, admin)
2. `Sim`: Thông tin SIM điện thoại
3. `NhapSim`: Thông tin đợt nhập SIM
4. `ChiTietNhapSim`: Chi tiết SIM nhập trong mỗi đợt
5. `GioHang`: Giỏ hàng của khách hàng
6. `HoaDon`: Thông tin hóa đơn
7. `HoaDonChiTiet`: Chi tiết SIM trong mỗi hóa đơn
8. `ThongTinChuSim`: Thông tin chủ sở hữu của SIM
9. `UuDai`: Thông tin ưu đãi
10. `ApDungUuDai`: Thông tin áp dụng ưu đãi vào hóa đơn
11. `DanhGia`: Đánh giá của khách hàng
12. `LichSuGiaoDich`: Lịch sử giao dịch

## Các annotations quan trọng

### 1. Entity Annotations

```java

@Entity // Đánh dấu lớp là JPA entity
@Table(name = "sim", indexes = {
        @Index(name = "idx_sim_msisdn", columnList = "msisdn"),
        @Index(name = "idx_sim_nhamang", columnList = "NhaMang"),
        @Index(name = "idx_sim_trangthai", columnList = "TrangThai")
})
public class Sim {
    // ...
}
```

### 2. Id và Column Annotations

```java

@Id // Đánh dấu trường là khóa chính
@Column(name = "iccid", nullable = false)
private String iccid;

@Column(name = "msisdn", length = 20, unique = true)
private String msisdn;
```

### 3. Enum Annotations

```java

@Enumerated(EnumType.STRING)
@Column(name = "NhaMang", nullable = false)
private NhaMang nhaMang;

public enum NhaMang {
    Viettel, Mobiphone, Vinaphone
}
```

### 4. Relationship Annotations

```java
// One-to-Many
@OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
private Set<HoaDon> donHangDaMua = new HashSet<>();

// Many-to-One
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "MaKH", nullable = false)
private NguoiDung khachHang;

// One-to-One
@OneToOne(mappedBy = "sim", cascade = CascadeType.ALL, orphanRemoval = true)
private ThongTinChuSim thongTinChuSim;
```

## Mẫu Entity chuẩn

Dưới đây là ví dụ về các entity đã được tối ưu:

### 1. NguoiDung.java

```java
package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "nguoidung")
public class NguoiDung {
    @Id
    @Column(name = "Email", nullable = false)
    private String email;

    @Column(name = "Password", nullable = false)
    private String password;

    @Column(name = "HoTen", nullable = false)
    private String hoTen;

    @Column(name = "SDT", length = 20)
    private String sdt;

    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;

    @Column(name = "DiaChi")
    private String diaChi;

    @Enumerated(EnumType.STRING)
    @Column(name = "VaiTro", nullable = false)
    private VaiTro vaiTro = VaiTro.KhachHang;

    public enum VaiTro {
        KhachHang, NhanVien, Admin
    }

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "khachHang")
    private Set<ApDungUuDai> apDungUuDais = new HashSet<>();

    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GioHang> gioHangs = new HashSet<>();

    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    private Set<HoaDon> donHangDaMua = new HashSet<>();

    @OneToMany(mappedBy = "nhanVien", fetch = FetchType.LAZY)
    private Set<HoaDon> donHangDaXuLy = new HashSet<>();

    @OneToMany(mappedBy = "nguoiThucHien")
    private Set<LichSuGiaoDich> lichSuGiaoDichs = new HashSet<>();

    @OneToMany(mappedBy = "nhanVien")
    private Set<NhapSim> nhapSims = new HashSet<>();

    // Helper methods
    public void addToCart(Sim sim) {
        GioHang gioHang = new GioHang();
        gioHang.setKhachHang(this);
        gioHang.setSim(sim);
        gioHangs.add(gioHang);
    }

    public void removeFromCart(Sim sim) {
        gioHangs.removeIf(item -> item.getSim().equals(sim));
    }

    public boolean isAdmin() {
        return VaiTro.Admin.equals(this.vaiTro);
    }

    public boolean isNhanVien() {
        return VaiTro.NhanVien.equals(this.vaiTro) || isAdmin();
    }
}
```

### 2. Sim.java

```java
package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "sim", indexes = {
        @Index(name = "idx_sim_msisdn", columnList = "msisdn"),
        @Index(name = "idx_sim_nhamang", columnList = "NhaMang"),
        @Index(name = "idx_sim_trangthai", columnList = "TrangThai")
})
public class Sim {
    @Id
    @Column(name = "iccid", nullable = false)
    private String iccid;

    @Column(name = "msisdn", length = 20, unique = true)
    private String msisdn;

    @Enumerated(EnumType.STRING)
    @Column(name = "NhaMang", nullable = false)
    private NhaMang nhaMang;

    public enum NhaMang {
        Viettel, Mobiphone, Vinaphone
    }

    @Column(name = "GiaBan", nullable = false, precision = 10, scale = 2)
    private BigDecimal giaBan;

    @Enumerated(EnumType.STRING)
    @Column(name = "LoaiSim", nullable = false)
    private LoaiSim loaiSim;

    public enum LoaiSim {
        NgoaiDia, TraTruoc, TraSau
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThai", nullable = false)
    private TrangThai trangThai = TrangThai.SanSang;

    public enum TrangThai {
        SanSang, DaBan, HoatDong
    }

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "sim")
    private Set<ChiTietNhapSim> chiTietNhapSims = new HashSet<>();

    @OneToMany(mappedBy = "sim")
    private Set<GioHang> gioHangs = new HashSet<>();

    @OneToMany(mappedBy = "sim")
    private Set<HoaDonChiTiet> hoaDonChiTiets = new HashSet<>();

    @OneToOne(mappedBy = "sim", cascade = CascadeType.ALL, orphanRemoval = true)
    private ThongTinChuSim thongTinChuSim;

    // Phương thức để lấy giá nhập mới nhất
    public BigDecimal getGiaNhapMoiNhat() {
        return chiTietNhapSims.stream()
                .sorted((a, b) -> b.getNhapSim().getNgayNhap().compareTo(a.getNhapSim().getNgayNhap()))
                .map(ChiTietNhapSim::getGiaNhap)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    // Phương thức để kiểm tra SIM có thể bán được không
    public boolean isSellable() {
        return TrangThai.SanSang.equals(this.trangThai);
    }

    // Phương thức để lấy lợi nhuận
    public BigDecimal getProfit() {
        BigDecimal giaNhap = getGiaNhapMoiNhat();
        if (giaNhap.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        return giaBan.subtract(giaNhap);
    }
}
```

### 3. HoaDon.java

```java
package com.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "hoadon")
public class HoaDon {
    @Id
    @Column(name = "MaHD", nullable = false, length = 20)
    private String maHD;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaKH", nullable = false)
    private NguoiDung khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNV")
    private NguoiDung nhanVien;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Column(name = "NgayCapNhat")
    private LocalDateTime ngayCapNhat = LocalDateTime.now();

    @Column(name = "TongTien", precision = 10, scale = 2)
    private BigDecimal tongTien = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThaiDon", nullable = false)
    private TrangThaiDon trangThaiDon = TrangThaiDon.ChoXacNhan;

    public enum TrangThaiDon {
        ChoXacNhan, DangXuLy, DaHoanThanh, DaHuy
    }

    @Column(name = "GhiChu", columnDefinition = "TEXT")
    private String ghiChu;

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ApDungUuDai> apDungUuDais = new HashSet<>();

    @OneToOne(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true)
    private DanhGia danhGia;

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HoaDonChiTiet> hoaDonChiTiets = new HashSet<>();

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LichSuGiaoDich> lichSuGiaoDichs = new HashSet<>();

    @OneToMany(mappedBy = "hoaDon")
    private Set<ThongTinChuSim> thongTinChuSims = new HashSet<>();

    // Helper methods để duy trì tính nhất quán của mối quan hệ
    public void addChiTiet(HoaDonChiTiet chiTiet) {
        hoaDonChiTiets.add(chiTiet);
        chiTiet.setHoaDon(this);
    }

    public void removeChiTiet(HoaDonChiTiet chiTiet) {
        hoaDonChiTiets.remove(chiTiet);
        chiTiet.setHoaDon(null);
    }

    public void addUuDai(ApDungUuDai uuDai) {
        apDungUuDais.add(uuDai);
        uuDai.setHoaDon(this);
    }

    public void addLichSu(LichSuGiaoDich lichSu) {
        lichSuGiaoDichs.add(lichSu);
        lichSu.setHoaDon(this);
    }

    public boolean isEditable() {
        return !TrangThaiDon.DaHoanThanh.equals(this.trangThaiDon) &&
                !TrangThaiDon.DaHuy.equals(this.trangThaiDon);
    }

    public boolean canBeProcessedBy(NguoiDung user) {
        return user != null &&
                (user.isNhanVien() || user.getEmail().equals(this.khachHang.getEmail()));
    }
}
```

## Spring Data JPA Repository

Để thao tác với entity, tạo các repository interface kế thừa từ JpaRepository:

### 1. SimRepository.java

```java
package com.repository;

import com.model.Sim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SimRepository extends JpaRepository<Sim, String> {

    // Tìm kiếm theo số điện thoại
    List<Sim> findByMsisdnContaining(String msisdn);

    // Tìm kiếm theo nhà mạng và trạng thái
    List<Sim> findByNhaMangAndTrangThai(Sim.NhaMang nhaMang, Sim.TrangThai trangThai);

    // Tìm kiếm theo khoảng giá
    @Query("SELECT s FROM Sim s WHERE s.giaBan BETWEEN :minPrice AND :maxPrice")
    List<Sim> findByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    // Tìm kiếm phân trang
    Page<Sim> findByTrangThai(Sim.TrangThai trangThai, Pageable pageable);

    // Tìm kiếm kết hợp nhiều điều kiện
    @Query("SELECT s FROM Sim s WHERE " +
            "(:msisdn IS NULL OR s.msisdn LIKE %:msisdn%) AND " +
            "(:nhaMang IS NULL OR s.nhaMang = :nhaMang) AND " +
            "(:loaiSim IS NULL OR s.loaiSim = :loaiSim) AND " +
            "(:trangThai IS NULL OR s.trangThai = :trangThai)")
    Page<Sim> searchSims(
            @Param("msisdn") String msisdn,
            @Param("nhaMang") Sim.NhaMang nhaMang,
            @Param("loaiSim") Sim.LoaiSim loaiSim,
            @Param("trangThai") Sim.TrangThai trangThai,
            Pageable pageable
    );

    // Đếm số SIM theo nhà mạng
    @Query("SELECT s.nhaMang, COUNT(s) FROM Sim s GROUP BY s.nhaMang")
    List<Object[]> countByNhaMang();

    // Gọi stored procedure
    @Query(value = "CALL SearchSimAdvanced(:search, :nhaMang, :loaiSim, :giaMin, :giaMax, :soDep, :limit, :offset)",
            nativeQuery = true)
    List<Sim> searchSimAdvanced(
            @Param("search") String search,
            @Param("nhaMang") String nhaMang,
            @Param("loaiSim") String loaiSim,
            @Param("giaMin") BigDecimal giaMin,
            @Param("giaMax") BigDecimal giaMax,
            @Param("soDep") Boolean soDep,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );
}
```

### 2. HoaDonRepository.java

```java
package com.repository;

import com.model.HoaDon;
import com.model.NguoiDung;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, String> {

    // Tìm đơn hàng của một khách hàng
    List<HoaDon> findByKhachHang(NguoiDung khachHang);

    // Tìm đơn hàng theo trạng thái
    List<HoaDon> findByTrangThaiDon(HoaDon.TrangThaiDon trangThaiDon);

    // Tìm đơn hàng theo khoảng thời gian
    List<HoaDon> findByNgayTaoBetween(LocalDateTime start, LocalDateTime end);

    // Tìm đơn hàng cùng với thông tin chi tiết (eager loading)
    @Query("SELECT h FROM HoaDon h LEFT JOIN FETCH h.hoaDonChiTiets WHERE h.maHD = :maHD")
    Optional<HoaDon> findWithDetails(@Param("maHD") String maHD);

    // Tìm kiếm phân trang
    Page<HoaDon> findByKhachHangAndTrangThaiDon(
            NguoiDung khachHang,
            HoaDon.TrangThaiDon trangThaiDon,
            Pageable pageable
    );

    // Tìm kiếm đơn hàng theo nhiều điều kiện
    @Query("SELECT h FROM HoaDon h WHERE " +
            "(:maKH IS NULL OR h.khachHang.email = :maKH) AND " +
            "(:maNV IS NULL OR h.nhanVien.email = :maNV) AND " +
            "(:trangThai IS NULL OR h.trangThaiDon = :trangThai) AND " +
            "(cast(:tuNgay as date) IS NULL OR h.ngayTao >= :tuNgay) AND " +
            "(cast(:denNgay as date) IS NULL OR h.ngayTao <= :denNgay)")
    Page<HoaDon> searchHoaDon(
            @Param("maKH") String maKH,
            @Param("maNV") String maNV,
            @Param("trangThai") HoaDon.TrangThaiDon trangThai,
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay,
            Pageable pageable
    );

    // Thống kê doanh thu theo ngày
    @Query(value = "CALL ThongKeDoanhThu(:fromDate, :toDate, :groupBy)", nativeQuery = true)
    List<Object[]> thongKeDoanhThu(
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate,
            @Param("groupBy") String groupBy
    );
}
```

### 3. NguoiDungRepository.java

```java
package com.repository;

import com.model.NguoiDung;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, String> {

    // Tìm người dùng theo SDT
    Optional<NguoiDung> findBySdt(String sdt);

    // Tìm người dùng theo vai trò
    List<NguoiDung> findByVaiTro(NguoiDung.VaiTro vaiTro);

    // Tìm kiếm người dùng theo tên hoặc email
    @Query("SELECT u FROM NguoiDung u WHERE " +
            "LOWER(u.hoTen) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<NguoiDung> searchByNameOrEmail(@Param("search") String search, Pageable pageable);

    // Kiểm tra email đã tồn tại chưa
    boolean existsByEmail(String email);

    // Kiểm tra SDT đã tồn tại chưa
    boolean existsBySdt(String sdt);

    // Xác thực người dùng
    @Query(value = "CALL AuthenticateUser(:email, :password, @authenticated, @role)", nativeQuery = true)
    void authenticateUser(
            @Param("email") String email,
            @Param("password") String password
    );

    // Lấy kết quả xác thực
    @Query(value = "SELECT @authenticated, @role", nativeQuery = true)
    Object[] getAuthenticationResult();
}
```

### 4. GioHangRepository.java

```java
package com.repository;

import com.model.GioHang;
import com.model.NguoiDung;
import com.model.Sim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Integer> {

    // Tìm giỏ hàng của khách hàng
    List<GioHang> findByKhachHang(NguoiDung khachHang);

    // Tìm SIM trong giỏ hàng của khách hàng
    Optional<GioHang> findByKhachHangAndSim(NguoiDung khachHang, Sim sim);

    // Đếm số SIM trong giỏ hàng
    int countByKhachHang(NguoiDung khachHang);

    // Xóa SIM khỏi giỏ hàng
    @Modifying
    @Query("DELETE FROM GioHang g WHERE g.khachHang = :khachHang AND g.sim = :sim")
    void deleteBySim(@Param("khachHang") NguoiDung khachHang, @Param("sim") Sim sim);

    // Xóa toàn bộ giỏ hàng của khách hàng
    @Modifying
    @Query("DELETE FROM GioHang g WHERE g.khachHang = :khachHang")
    void deleteAllByKhachHang(@Param("khachHang") NguoiDung khachHang);

    // Gọi stored procedure thêm vào giỏ hàng
    @Query(value = "CALL AddToCart(:makh, :iccid, @success, @message)", nativeQuery = true)
    void addToCart(
            @Param("makh") String makh,
            @Param("iccid") String iccid
    );

    // Lấy kết quả thêm vào giỏ hàng
    @Query(value = "SELECT @success, @message", nativeQuery = true)
    Object[] getAddToCartResult();
}
```

## Service Layer

Sử dụng các repository trong service layer:

### 1. SimService.java

```java
package com.service;

import com.model.Sim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SimService {

    private final SimRepository simRepository;

    @Autowired
    public SimService(SimRepository simRepository) {
        this.simRepository = simRepository;
    }

    public List<Sim> findSimsByPhoneNumber(String phoneNumber) {
        return simRepository.findByMsisdnContaining(phoneNumber);
    }

    public List<Sim> findAvailableSimsByProvider(Sim.NhaMang nhaMang) {
        return simRepository.findByNhaMangAndTrangThai(nhaMang, Sim.TrangThai.SanSang);
    }

    @Transactional
    public Sim saveSim(Sim sim) {
        sim.setUpdatedAt(LocalDateTime.now());
        return simRepository.save(sim);
    }

    @Transactional
    public void updateSimStatus(String iccid, Sim.TrangThai trangThai) {
        Optional<Sim> optionalSim = simRepository.findById(iccid);
        if (optionalSim.isPresent()) {
            Sim sim = optionalSim.get();
            sim.setTrangThai(trangThai);
            sim.setUpdatedAt(LocalDateTime.now());
            simRepository.save(sim);
        }
    }

    public Page<Sim> searchSims(
            String msisdn,
            Sim.NhaMang nhaMang,
            Sim.LoaiSim loaiSim,
            Sim.TrangThai trangThai,
            Pageable pageable) {
        return simRepository.searchSims(msisdn, nhaMang, loaiSim, trangThai, pageable);
    }

    public List<Sim> searchAdvanced(
            String search,
            String nhaMang,
            String loaiSim,
            BigDecimal giaMin,
            BigDecimal giaMax,
            Boolean soDep,
            Integer limit,
            Integer offset) {
        return simRepository.searchSimAdvanced(
                search, nhaMang, loaiSim, giaMin, giaMax, soDep, limit, offset);
    }

    public List<Object[]> getProviderStats() {
        return simRepository.countByNhaMang();
    }
}
```

### 2. HoaDonService.java

```java
package com.service;

import com.model.HoaDon;
import com.model.HoaDonChiTiet;
import com.model.NguoiDung;
import com.model.Sim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class HoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository chiTietRepository;
    private final SimRepository simRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public HoaDonService(
            HoaDonRepository hoaDonRepository,
            HoaDonChiTietRepository chiTietRepository,
            SimRepository simRepository,
            JdbcTemplate jdbcTemplate) {
        this.hoaDonRepository = hoaDonRepository;
        this.chiTietRepository = chiTietRepository;
        this.simRepository = simRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<HoaDon> findOrdersByCustomer(NguoiDung khachHang) {
        return hoaDonRepository.findByKhachHang(khachHang);
    }

    public Optional<HoaDon> findOrderWithDetails(String maHD) {
        return hoaDonRepository.findWithDetails(maHD);
    }

    @Transactional
    public HoaDon createOrder(NguoiDung khachHang) {
        // Tạo mã hóa đơn
        String maHD = generateOrderCode();

        // Tạo hóa đơn mới
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHD(maHD);
        hoaDon.setKhachHang(khachHang);
        hoaDon.setTrangThaiDon(HoaDon.TrangThaiDon.ChoXacNhan);
        hoaDon.setNgayTao(LocalDateTime.now());

        return hoaDonRepository.save(hoaDon);
    }

    @Transactional
    public Map<String, Object> updateOrderStatus(String maHD, String employeeEmail, String newStatus, String note) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Sử dụng stored procedure để cập nhật trạng thái
            Map<String, Object> params = new HashMap<>();
            params.put("p_mahd", maHD);
            params.put("p_manv", employeeEmail);
            params.put("p_trangthai", newStatus);
            params.put("p_ghichu", note);

            // Gọi procedure và lấy kết quả
            Map<String, Object> resultMap = callUpdateOrderStatusProcedure(params);

            return resultMap;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi cập nhật trạng thái: " + e.getMessage());
            return result;
        }
    }

    @Transactional
    public HoaDonChiTiet addSimToOrder(HoaDon hoaDon, String iccid) {
        Optional<Sim> optionalSim = simRepository.findById(iccid);

        if (optionalSim.isEmpty() || !optionalSim.get().isSellable()) {
            throw new IllegalArgumentException("SIM không tồn tại hoặc không sẵn sàng để bán");
        }

        Sim sim = optionalSim.get();

        // Tạo chi tiết hóa đơn
        HoaDonChiTiet chiTiet = new HoaDonChiTiet();
        chiTiet.setHoaDon(hoaDon);
        chiTiet.setSim(sim);
        chiTiet.setGiaBan(sim.getGiaBan());
        chiTiet.setGiaCuoi(sim.getGiaBan());

        // Thêm vào hóa đơn
        hoaDon.addChiTiet(chiTiet);

        // Cập nhật tổng tiền
        updateOrderTotal(hoaDon);

        // Lưu chi tiết
        return chiTietRepository.save(chiTiet);
    }

    @Transactional
    public void removeSimFromOrder(HoaDon hoaDon, String iccid) {
        Optional<HoaDonChiTiet> optionalChiTiet = chiTietRepository.findByHoaDonAndSimIccid(hoaDon, iccid);

        if (optionalChiTiet.isPresent()) {
            HoaDonChiTiet chiTiet = optionalChiTiet.get();
            hoaDon.removeChiTiet(chiTiet);
            chiTietRepository.delete(chiTiet);

            // Cập nhật tổng tiền
            updateOrderTotal(hoaDon);
        }
    }

    private void updateOrderTotal(HoaDon hoaDon) {
        BigDecimal total = BigDecimal.ZERO;

        for (HoaDonChiTiet chiTiet : hoaDon.getHoaDonChiTiets()) {
            total = total.add(chiTiet.getGiaCuoi());
        }

        hoaDon.setTongTien(total);
        hoaDonRepository.save(hoaDon);
    }

    private String generateOrderCode() {
        // Tạo mã hóa đơn theo format: HDyyyyMMdd + số thứ tự
        String prefix = "HD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Đếm số đơn hàng trong ngày
        int count = hoaDonRepository.countByMaHDStartingWith(prefix) + 1;

        // Format số thứ tự thành chuỗi 3 chữ số (001, 002, ...)
        String countStr = String.format("%03d", count);

        return prefix + countStr;
    }

    private Map<String, Object> callUpdateOrderStatusProcedure(Map<String, Object> params) {
        // Implementation using jdbcTemplate to call stored procedure
        // ...
        return null; // Replace with actual implementation
    }

    public List<Object[]> getRevenueStats(String fromDate, String toDate, String groupBy) {
        return hoaDonRepository.thongKeDoanhThu(fromDate, toDate, groupBy);
    }
}
```

## Controller Layer

Sử dụng các service trong controller:

### 1. SimController.java

```java
package com.controller;

import com.model.Sim;
import com.service.GioHangService;
import com.service.SimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("/sim")
public class SimController {

    private final SimService simService;
    private final GioHangService gioHangService;

    @Autowired
    public SimController(SimService simService, GioHangService gioHangService) {
        this.simService = simService;
        this.gioHangService = gioHangService;
    }

    @GetMapping
    public String listSims(
            @RequestParam(required = false) String msisdn,
            @RequestParam(required = false) String nhaMang,
            @RequestParam(required = false) String loaiSim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // Chuyển đổi String params thành enum nếu không null
        Sim.NhaMang nhaMangEnum = nhaMang != null ? Sim.NhaMang.valueOf(nhaMang) : null;
        Sim.LoaiSim loaiSimEnum = loaiSim != null ? Sim.LoaiSim.valueOf(loaiSim) : null;

        // Chỉ tìm SIM đang sẵn sàng bán
        Pageable pageable = PageRequest.of(page, size);
        Page<Sim> simPage = simService.searchSims(
                msisdn, nhaMangEnum, loaiSimEnum, Sim.TrangThai.SanSang, pageable);

        model.addAttribute("simPage", simPage);
        model.addAttribute("msisdn", msisdn);
        model.addAttribute("nhaMang", nhaMang);
        model.addAttribute("loaiSim", loaiSim);
        model.addAttribute("nhaMangValues", Sim.NhaMang.values());
        model.addAttribute("loaiSimValues", Sim.LoaiSim.values());

        return "sim/list";
    }

    @GetMapping("/{iccid}")
    public String viewSim(@PathVariable String iccid, Model model) {
        simService.findById(iccid).ifPresent(sim -> {
            model.addAttribute("sim", sim);
        });

        return "sim/detail";
    }

    @PostMapping("/{iccid}/add-to-cart")
    @PreAuthorize("isAuthenticated()")
    public String addToCart(
            @PathVariable String iccid,
            Principal principal,
            @RequestHeader(required = false) String referer) {

        // Lấy email của người dùng đăng nhập
        String userEmail = principal.getName();

        // Thêm SIM vào giỏ hàng
        Map<String, Object> result = gioHangService.addToCart(userEmail, iccid);

        // Xử lý kết quả
        boolean success = (Boolean) result.get("success");
        String message = (String) result.get("message");

        // Quay lại trang trước đó hoặc trang danh sách SIM
        String redirectUrl = referer != null ? referer : "/sim";
        return "redirect:" + redirectUrl;
    }

    @GetMapping("/search")
    @ResponseBody
    public List<Sim> searchSims(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String nhaMang,
            @RequestParam(required = false) String loaiSim,
            @RequestParam(required = false) BigDecimal giaMin,
            @RequestParam(required = false) BigDecimal giaMax,
            @RequestParam(required = false, defaultValue = "false") Boolean soDep,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {

        return simService.searchAdvanced(
                search, nhaMang, loaiSim, giaMin, giaMax, soDep, limit, offset);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_NHANVIEN')")
    @GetMapping("/admin")
    public String adminList(Model model) {
        // Admin có thể xem tất cả SIM, bao gồm cả đã bán
        return "sim/admin-list";
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_NHANVIEN')")
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("sim", new Sim());
        model.addAttribute("nhaMangValues", Sim.NhaMang.values());
        model.addAttribute("loaiSimValues", Sim.LoaiSim.values());

        return "sim/add";
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_NHANVIEN')")
    @PostMapping("/add")
    public String addSim(@ModelAttribute Sim sim) {
        simService.saveSim(sim);
        return "redirect:/sim/admin";
    }
}
```

## Best Practices khi sử dụng JPA Entities

1. **Sử dụng FetchType.LAZY cho các relationship**:
   ```java
   @ManyToOne(fetch = FetchType.LAZY)
   private NguoiDung khachHang;
   ```

2. **Cẩn trọng với CascadeType**:
   ```java
   // Chỉ sử dụng cascade khi cần thiết
   @OneToMany(mappedBy = "hoaDon", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
   private Set<HoaDonChiTiet> hoaDonChiTiets = new HashSet<>();
   ```

3. **Sử dụng Set thay vì List cho @OneToMany**:
   ```java
   // Hiệu suất tốt hơn, tránh trùng lặp
   private Set<GioHang> gioHangs = new HashSet<>();
   ```

4. **Thêm helper methods để duy trì tính nhất quán**:
   ```java
   public void addChiTiet(HoaDonChiTiet chiTiet) {
       hoaDonChiTiets.add(chiTiet);
       chiTiet.setHoaDon(this);
   }
   ```

5. **Sử dụng @Transactional phù hợp**:
   ```java
   @Transactional
   public HoaDon createOrder(String maKH) {
       // Code xử lý transaction...
   }
   ```

6. **Xử lý N+1 query problem**:
   ```java
   @Query("SELECT h FROM HoaDon h JOIN FETCH h.hoaDonChiTiets WHERE h.maHD = :maHD")
   HoaDon findWithDetails(@Param("maHD") String maHD);
   ```

7. **Sử dụng Spring Data Specifications cho truy vấn phức tạp**:
   ```java
   public List<Sim> findByCriteria(String msisdn, NhaMang nhaMang, BigDecimal maxPrice) {
       return simRepository.findAll(
           Specification.where(msisdnContains(msisdn))
               .and(hasNhaMang(nhaMang))
               .and(priceLessThan(maxPrice))
       );
   }
   ```

8. **Sử dụng @EntityGraph để tối ưu fetch**:
   ```java
   @EntityGraph(attributePaths = {"hoaDonChiTiets", "khachHang"})
   Optional<HoaDon> findWithGraphById(String maHD);
   ```

9. **Sử dụng @Version để kiểm soát đồng thời**:
   ```java
   @Version
   private Long version;
   ```

10. **Sử dụng @Enumerated(EnumType.STRING) thay vì ORDINAL**:
    ```java
    @Enumerated(EnumType.STRING) // Lưu tên enum thay vì số thứ tự
    private TrangThai trangThai;
    ```

## Tips cho Spring Boot JPA

1. **Sử dụng Schema Generation Tools**: Tạo schema từ JPA entities
   ```properties
   spring.jpa.hibernate.ddl-auto=validate
   spring.jpa.properties.javax.persistence.schema-generation.scripts.action=create
   spring.jpa.properties.javax.persistence.schema-generation.scripts.create-target=create.sql
   ```

2. **Spring Data Rest**: Tự động tạo REST API cho entities
   ```java
   @RepositoryRestResource(path = "sims")
   public interface SimRepository extends JpaRepository<Sim, String> {
       // methods...
   }
   ```

3. **JPA Auditing**: Tự động theo dõi thông tin người dùng tạo/sửa entity
   ```java
   @CreatedBy
   @Column(name = "created_by")
   private String createdBy;
   
   @LastModifiedBy
   @Column(name = "last_modified_by")
   private String lastModifiedBy;
   
   @CreatedDate
   @Column(name = "created_date")
   private LocalDateTime createdDate;
   
   @LastModifiedDate
   @Column(name = "last_modified_date")
   private LocalDateTime lastModifiedDate;
   ```

4. **Batch Processing**: Xử lý dữ liệu lớn với batch
   ```properties
   spring.jpa.properties.hibernate.jdbc.batch_size=30
   spring.jpa.properties.hibernate.order_inserts=true
   spring.jpa.properties.hibernate.order_updates=true
   ```

5. **Profiles cho môi trường khác nhau**:
   ```properties
   # application-dev.properties
   spring.datasource.url=jdbc:mysql://localhost:3306/fptsim_dev
   
   # application-prod.properties
   spring.datasource.url=jdbc:mysql://production-server:3306/fptsim_prod
   ```

## Kết luận

JPA Entities là một phần quan trọng trong việc phát triển backend với Spring Boot. Hiểu rõ về cách sử dụng JPA giúp tạo
ra code hiệu quả và dễ bảo trì.

Trong dự án FPT SIM, việc áp dụng đúng các best practices của JPA sẽ giúp code backend mạnh mẽ và dễ mở rộng, đồng thời
tận dụng được sức mạnh của các stored procedures và triggers đã được thiết kế trong cơ sở dữ liệu.