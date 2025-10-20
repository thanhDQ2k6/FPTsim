-- Sample test data for FPTsim
-- Run this after creating the database schema

-- Insert test users
INSERT INTO nguoidung (Email, Password, HoTen, SDT, NgaySinh, DiaChi, VaiTro, created_at, updated_at)
VALUES
    ('admin@fptsim.com', 'admin123', 'Administrator', '0901234567', '1990-01-01', 'Hanoi, Vietnam', 'Admin', NOW(), NOW()),
    ('staff@fptsim.com', 'staff123', 'Staff Member', '0902345678', '1995-05-15', 'Ho Chi Minh City, Vietnam', 'NhanVien', NOW(), NOW()),
    ('customer@fptsim.com', 'customer123', 'Customer User', '0903456789', '2000-10-20', 'Da Nang, Vietnam', 'KhachHang', NOW(), NOW()),
    ('root@mail.com', 'root', 'Root User', '0904567890', '1985-03-10', 'Hanoi, Vietnam', 'Admin', NOW(), NOW());

-- Insert sample SIM cards
INSERT INTO sim (iccid, msisdn, NhaMang, GiaBan, LoaiSim, TrangThai, created_at, updated_at)
VALUES
    ('89840000000000001', '0901234567', 'Viettel', 50000.00, 'TraTruoc', 'SanSang', NOW(), NOW()),
    ('89840000000000002', '0902345678', 'Viettel', 75000.00, 'TraTruoc', 'SanSang', NOW(), NOW()),
    ('89840000000000003', '0903456789', 'Mobiphone', 60000.00, 'TraSau', 'SanSang', NOW(), NOW()),
    ('89840000000000004', '0904567890', 'Mobiphone', 55000.00, 'TraTruoc', 'SanSang', NOW(), NOW()),
    ('89840000000000005', '0905678901', 'Vinaphone', 70000.00, 'TraTruoc', 'SanSang', NOW(), NOW()),
    ('89840000000000006', '0906789012', 'Vinaphone', 65000.00, 'TraSau', 'SanSang', NOW(), NOW()),
    ('89840000000000007', '0907890123', 'Viettel', 80000.00, 'NgoaiDia', 'SanSang', NOW(), NOW()),
    ('89840000000000008', '0908901234', 'Viettel', 45000.00, 'TraTruoc', 'DaBan', NOW(), NOW()),
    ('89840000000000009', '0909012345', 'Mobiphone', 90000.00, 'TraSau', 'SanSang', NOW(), NOW()),
    ('89840000000000010', '0900123456', 'Vinaphone', 85000.00, 'TraTruoc', 'SanSang', NOW(), NOW());

-- Insert sample promotions
INSERT INTO uudai (MaUuDai, TenUuDai, MoTa, PhanTramGiamGia, NgayBatDau, NgayKetThuc, created_at, updated_at)
VALUES
    ('NEWCUST2025', 'New Customer Discount', 'Special discount for new customers', 15.00, '2025-01-01', '2025-12-31', NOW(), NOW()),
    ('SUMMER2025', 'Summer Sale', 'Summer promotion for all SIM cards', 10.00, '2025-06-01', '2025-08-31', NOW(), NOW()),
    ('LOYAL2025', 'Loyalty Bonus', 'Reward for loyal customers', 20.00, '2025-01-01', '2025-12-31', NOW(), NOW()),
    ('STUDENT2025', 'Student Discount', 'Special offer for students', 25.00, '2025-01-01', '2025-12-31', NOW(), NOW());

-- Insert sample SIM import batch
INSERT INTO nhapsim (MaNhanVien, NhaCungCap, NgayNhap, GhiChu, created_at)
VALUES
    ('staff@fptsim.com', 'Viettel Official', '2025-01-15 10:00:00', 'First batch import', NOW()),
    ('staff@fptsim.com', 'Mobiphone Distributor', '2025-01-20 14:30:00', 'Second batch import', NOW());

-- Get the IDs of the import batches (assuming auto-increment starts at 1)
-- Insert import details for first batch
INSERT INTO chitietnhapsim (MaNhapSim, iccid, GiaNhap, SoLuong, created_at)
VALUES
    (1, '89840000000000001', 40000.00, 1, NOW()),
    (1, '89840000000000002', 60000.00, 1, NOW()),
    (1, '89840000000000007', 65000.00, 1, NOW());

-- Insert import details for second batch
INSERT INTO chitietnhapsim (MaNhapSim, iccid, GiaNhap, SoLuong, created_at)
VALUES
    (2, '89840000000000003', 50000.00, 1, NOW()),
    (2, '89840000000000004', 45000.00, 1, NOW()),
    (2, '89840000000000009', 75000.00, 1, NOW());

-- Insert sample shopping cart items
INSERT INTO giohang (KhachHang_Email, iccid, SoLuong, NgayThem)
VALUES
    ('customer@fptsim.com', '89840000000000001', 1, NOW()),
    ('customer@fptsim.com', '89840000000000003', 1, NOW());

-- Notes:
-- 1. User Credentials:
--    - Admin: admin@fptsim.com / admin123
--    - Staff: staff@fptsim.com / staff123
--    - Customer: customer@fptsim.com / customer123
--    - Root: root@mail.com / root
--
-- 2. SIM Cards:
--    - 10 sample SIM cards from different providers
--    - Various prices and types
--    - One SIM card marked as "DaBan" (Sold)
--
-- 3. Promotions:
--    - 4 active promotions with different discount rates
--    - Valid for 2025
--
-- 4. Import History:
--    - 2 import batches
--    - Associated with staff user
--    - Each batch has 3 SIM cards
--
-- 5. Shopping Cart:
--    - Customer has 2 items in cart
--
-- WARNING: This uses plain text passwords for demonstration only.
--          In production, passwords should be hashed with BCrypt!
