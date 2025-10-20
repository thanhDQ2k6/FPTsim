-- Comprehensive Sample Data for FPTsim
-- Run this after creating the database schema with db_create.sql
-- This provides 20+ records for comprehensive testing

-- ============================================
-- USERS (10 users: 2 admins, 3 staff, 5 customers)
-- ============================================
INSERT INTO nguoidung (Email, Password, HoTen, SDT, NgaySinh, DiaChi, VaiTro, created_at, updated_at)
VALUES
    -- Admins
    ('admin@fptsim.com', 'admin123', 'Nguyen Van Admin', '0901234567', '1990-01-01', '123 Nguyen Hue, Hanoi', 'Admin', NOW(), NOW()),
    ('root@mail.com', 'root', 'Super Admin', '0904567890', '1985-03-10', '456 Le Loi, Hanoi', 'Admin', NOW(), NOW()),
    
    -- Staff Members
    ('staff1@fptsim.com', 'staff123', 'Tran Thi Nhan Vien', '0902345678', '1995-05-15', '789 Tran Phu, Ho Chi Minh City', 'NhanVien', NOW(), NOW()),
    ('staff2@fptsim.com', 'staff123', 'Le Van Staff', '0909876543', '1993-08-20', '321 Hai Ba Trung, Da Nang', 'NhanVien', NOW(), NOW()),
    ('staff3@fptsim.com', 'staff123', 'Pham Thi Staff', '0908765432', '1996-11-25', '654 Ba Dinh, Hanoi', 'NhanVien', NOW(), NOW()),
    
    -- Customers
    ('customer1@fptsim.com', 'customer123', 'Hoang Van Customer', '0903456789', '2000-10-20', '147 Nguyen Trai, Da Nang', 'KhachHang', NOW(), NOW()),
    ('customer2@fptsim.com', 'customer123', 'Vu Thi Customer', '0907654321', '1998-04-15', '258 Le Duan, Ho Chi Minh City', 'KhachHang', NOW(), NOW()),
    ('customer3@fptsim.com', 'customer123', 'Do Van Customer', '0906543210', '1999-07-08', '369 Phan Chu Trinh, Hanoi', 'KhachHang', NOW(), NOW()),
    ('customer4@fptsim.com', 'customer123', 'Bui Thi Customer', '0905432109', '2001-02-14', '741 Dong Khoi, Ho Chi Minh City', 'KhachHang', NOW(), NOW()),
    ('customer5@fptsim.com', 'customer123', 'Dang Van Customer', '0904321098', '1997-12-30', '852 Ly Thuong Kiet, Da Nang', 'KhachHang', NOW(), NOW());

-- ============================================
-- SIM CARDS (30 SIM cards with variety)
-- ============================================
INSERT INTO sim (iccid, msisdn, NhaMang, GiaBan, LoaiSim, TrangThai, created_at, updated_at)
VALUES
    -- Viettel SIMs (10 cards)
    ('89840000000000001', '0901234567', 'Viettel', 50000.00, 'TraTruoc', 'SanSang', DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),
    ('89840000000000002', '0902345678', 'Viettel', 75000.00, 'TraTruoc', 'SanSang', DATE_SUB(NOW(), INTERVAL 25 DAY), NOW()),
    ('89840000000000003', '0903456789', 'Viettel', 120000.00, 'TraSau', 'SanSang', DATE_SUB(NOW(), INTERVAL 20 DAY), NOW()),
    ('89840000000000004', '0904567890', 'Viettel', 55000.00, 'TraTruoc', 'HoatDong', DATE_SUB(NOW(), INTERVAL 15 DAY), NOW()),
    ('89840000000000005', '0905678901', 'Viettel', 80000.00, 'NgoaiDia', 'SanSang', DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
    ('89840000000000006', '0906789012', 'Viettel', 45000.00, 'TraTruoc', 'SanSang', DATE_SUB(NOW(), INTERVAL 8 DAY), NOW()),
    ('89840000000000007', '0907890123', 'Viettel', 150000.00, 'TraSau', 'SanSang', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
    ('89840000000000008', '0908901234', 'Viettel', 65000.00, 'TraTruoc', 'DaBan', DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
    ('89840000000000009', '0909012345', 'Viettel', 90000.00, 'TraSau', 'SanSang', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
    ('89840000000000010', '0900123456', 'Viettel', 85000.00, 'TraTruoc', 'SanSang', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
    
    -- Mobiphone SIMs (10 cards)
    ('89840000000000011', '0911234567', 'Mobiphone', 60000.00, 'TraTruoc', 'SanSang', DATE_SUB(NOW(), INTERVAL 28 DAY), NOW()),
    ('89840000000000012', '0912345678', 'Mobiphone', 70000.00, 'TraSau', 'SanSang', DATE_SUB(NOW(), INTERVAL 24 DAY), NOW()),
    ('89840000000000013', '0913456789', 'Mobiphone', 100000.00, 'TraSau', 'HoatDong', DATE_SUB(NOW(), INTERVAL 18 DAY), NOW()),
    ('89840000000000014', '0914567890', 'Mobiphone', 52000.00, 'TraTruoc', 'SanSang', DATE_SUB(NOW(), INTERVAL 16 DAY), NOW()),
    ('89840000000000015', '0915678901', 'Mobiphone', 95000.00, 'NgoaiDia', 'SanSang', DATE_SUB(NOW(), INTERVAL 12 DAY), NOW()),
    ('89840000000000016', '0916789012', 'Mobiphone', 48000.00, 'TraTruoc', 'SanSang', DATE_SUB(NOW(), INTERVAL 9 DAY), NOW()),
    ('89840000000000017', '0917890123', 'Mobiphone', 130000.00, 'TraSau', 'SanSang', DATE_SUB(NOW(), INTERVAL 7 DAY), NOW()),
    ('89840000000000018', '0918901234', 'Mobiphone', 62000.00, 'TraTruoc', 'DaBan', DATE_SUB(NOW(), INTERVAL 4 DAY), NOW()),
    ('89840000000000019', '0919012345', 'Mobiphone', 88000.00, 'TraSau', 'SanSang', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
    ('89840000000000020', '0910123456', 'Mobiphone', 78000.00, 'TraTruoc', 'SanSang', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
    
    -- Vinaphone SIMs (10 cards)
    ('89840000000000021', '0921234567', 'Vinaphone', 58000.00, 'TraTruoc', 'SanSang', DATE_SUB(NOW(), INTERVAL 27 DAY), NOW()),
    ('89840000000000022', '0922345678', 'Vinaphone', 68000.00, 'TraSau', 'SanSang', DATE_SUB(NOW(), INTERVAL 22 DAY), NOW()),
    ('89840000000000023', '0923456789', 'Vinaphone', 110000.00, 'TraSau', 'SanSang', DATE_SUB(NOW(), INTERVAL 19 DAY), NOW()),
    ('89840000000000024', '0924567890', 'Vinaphone', 54000.00, 'TraTruoc', 'HoatDong', DATE_SUB(NOW(), INTERVAL 17 DAY), NOW()),
    ('89840000000000025', '0925678901', 'Vinaphone', 92000.00, 'NgoaiDia', 'SanSang', DATE_SUB(NOW(), INTERVAL 13 DAY), NOW()),
    ('89840000000000026', '0926789012', 'Vinaphone', 46000.00, 'TraTruoc', 'SanSang', DATE_SUB(NOW(), INTERVAL 11 DAY), NOW()),
    ('89840000000000027', '0927890123', 'Vinaphone', 140000.00, 'TraSau', 'SanSang', DATE_SUB(NOW(), INTERVAL 6 DAY), NOW()),
    ('89840000000000028', '0928901234', 'Vinaphone', 64000.00, 'TraTruoc', 'DaBan', DATE_SUB(NOW(), INTERVAL 4 DAY), NOW()),
    ('89840000000000029', '0929012345', 'Vinaphone', 86000.00, 'TraSau', 'SanSang', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
    ('89840000000000030', '0920123456', 'Vinaphone', 82000.00, 'TraTruoc', 'SanSang', NOW(), NOW());

-- ============================================
-- PROMOTIONS (6 active promotions)
-- ============================================
INSERT INTO uudai (MaUD, MoTa, GiaTriGiam, LoaiGiam, NgayBatDau, NgayHetHan, TrangThai)
VALUES
    ('NEWCUST2025', 'New Customer Welcome Discount', 15.00, 'PhanTram', '2025-01-01', '2025-12-31', TRUE),
    ('SUMMER2025', 'Summer Hot Sale', 20.00, 'PhanTram', '2025-06-01', '2025-08-31', TRUE),
    ('LOYAL2025', 'Loyalty Customer Reward', 25.00, 'PhanTram', '2025-01-01', '2025-12-31', TRUE),
    ('STUDENT2025', 'Student Special Discount', 30.00, 'PhanTram', '2025-01-01', '2025-12-31', TRUE),
    ('FLASH50K', 'Flash Sale 50K Off', 50000.00, 'TienMat', '2025-01-01', '2025-12-31', TRUE),
    ('VIP2025', 'VIP Customer Exclusive', 35.00, 'PhanTram', '2025-01-01', '2025-12-31', TRUE);

-- ============================================
-- SIM IMPORT BATCHES (5 batches)
-- ============================================
INSERT INTO nhapsim (MaNhanVien, NhaCungCap, NgayNhap, GhiChu, created_at)
VALUES
    ('staff1@fptsim.com', 'Viettel Official Distributor', DATE_SUB(NOW(), INTERVAL 30 DAY), 'First major import batch', DATE_SUB(NOW(), INTERVAL 30 DAY)),
    ('staff1@fptsim.com', 'Mobiphone Official Partner', DATE_SUB(NOW(), INTERVAL 20 DAY), 'Premium SIM collection', DATE_SUB(NOW(), INTERVAL 20 DAY)),
    ('staff2@fptsim.com', 'Vinaphone Authorized Dealer', DATE_SUB(NOW(), INTERVAL 15 DAY), 'Mixed type SIM import', DATE_SUB(NOW(), INTERVAL 15 DAY)),
    ('staff2@fptsim.com', 'Viettel Regional Office', DATE_SUB(NOW(), INTERVAL 10 DAY), 'International SIM batch', DATE_SUB(NOW(), INTERVAL 10 DAY)),
    ('staff3@fptsim.com', 'Multi-Provider Import', DATE_SUB(NOW(), INTERVAL 5 DAY), 'Latest inventory update', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- ============================================
-- SIM IMPORT DETAILS
-- ============================================
INSERT INTO chitietnhapsim (MaNhapSim, iccid, GiaNhap, SoLuong, created_at)
VALUES
    -- Batch 1 (staff1 - Viettel)
    (1, '89840000000000001', 40000.00, 1, DATE_SUB(NOW(), INTERVAL 30 DAY)),
    (1, '89840000000000002', 60000.00, 1, DATE_SUB(NOW(), INTERVAL 30 DAY)),
    (1, '89840000000000003', 95000.00, 1, DATE_SUB(NOW(), INTERVAL 30 DAY)),
    (1, '89840000000000004', 45000.00, 1, DATE_SUB(NOW(), INTERVAL 30 DAY)),
    (1, '89840000000000005', 65000.00, 1, DATE_SUB(NOW(), INTERVAL 30 DAY)),
    (1, '89840000000000006', 38000.00, 1, DATE_SUB(NOW(), INTERVAL 30 DAY)),
    
    -- Batch 2 (staff1 - Mobiphone)
    (2, '89840000000000011', 48000.00, 1, DATE_SUB(NOW(), INTERVAL 20 DAY)),
    (2, '89840000000000012', 56000.00, 1, DATE_SUB(NOW(), INTERVAL 20 DAY)),
    (2, '89840000000000013', 80000.00, 1, DATE_SUB(NOW(), INTERVAL 20 DAY)),
    (2, '89840000000000014', 42000.00, 1, DATE_SUB(NOW(), INTERVAL 20 DAY)),
    (2, '89840000000000015', 75000.00, 1, DATE_SUB(NOW(), INTERVAL 20 DAY)),
    
    -- Batch 3 (staff2 - Vinaphone)
    (3, '89840000000000021', 46000.00, 1, DATE_SUB(NOW(), INTERVAL 15 DAY)),
    (3, '89840000000000022', 54000.00, 1, DATE_SUB(NOW(), INTERVAL 15 DAY)),
    (3, '89840000000000023', 88000.00, 1, DATE_SUB(NOW(), INTERVAL 15 DAY)),
    (3, '89840000000000024', 44000.00, 1, DATE_SUB(NOW(), INTERVAL 15 DAY)),
    (3, '89840000000000025', 72000.00, 1, DATE_SUB(NOW(), INTERVAL 15 DAY)),
    
    -- Batch 4 (staff2 - Viettel)
    (4, '89840000000000007', 120000.00, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
    (4, '89840000000000008', 52000.00, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
    (4, '89840000000000009', 72000.00, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
    (4, '89840000000000010', 68000.00, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
    
    -- Batch 5 (staff3 - Mixed)
    (5, '89840000000000016', 38000.00, 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (5, '89840000000000017', 104000.00, 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (5, '89840000000000018', 50000.00, 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (5, '89840000000000026', 36000.00, 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (5, '89840000000000027', 112000.00, 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (5, '89840000000000028', 51000.00, 1, DATE_SUB(NOW(), INTERVAL 5 DAY));

-- ============================================
-- ORDERS (8 completed orders for testing)
-- ============================================
INSERT INTO hoadon (MaHD, MaKH, MaNV, NgayTao, NgayCapNhat, TongTien, TrangThaiDon, GhiChu)
VALUES
    ('HD2025001', 'customer1@fptsim.com', 'staff1@fptsim.com', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY), 150000.00, 'DaHoanThanh', 'First order'),
    ('HD2025002', 'customer2@fptsim.com', 'staff1@fptsim.com', DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY), 200000.00, 'DaHoanThanh', 'Premium purchase'),
    ('HD2025003', 'customer3@fptsim.com', 'staff2@fptsim.com', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY), 110000.00, 'DaHoanThanh', 'Quick buy'),
    ('HD2025004', 'customer1@fptsim.com', 'staff2@fptsim.com', DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY), 140000.00, 'DaHoanThanh', 'Second purchase'),
    ('HD2025005', 'customer4@fptsim.com', 'staff3@fptsim.com', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), 180000.00, 'DaHoanThanh', 'Bundle deal'),
    ('HD2025006', 'customer5@fptsim.com', 'staff3@fptsim.com', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), 130000.00, 'DaHoanThanh', 'Standard order'),
    ('HD2025007', 'customer2@fptsim.com', NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 90000.00, 'ChoXacNhan', 'Pending approval'),
    ('HD2025008', 'customer3@fptsim.com', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 160000.00, 'ChoXacNhan', 'New order');

-- ============================================
-- ORDER DETAILS
-- ============================================
INSERT INTO hoadonchitiet (MaHD, iccid, GiaBan, GiaCuoi)
VALUES
    -- HD2025001
    ('HD2025001', '89840000000000004', 55000.00, 55000.00),
    ('HD2025001', '89840000000000008', 65000.00, 65000.00),
    -- HD2025002
    ('HD2025002', '89840000000000013', 100000.00, 100000.00),
    -- HD2025003
    ('HD2025003', '89840000000000024', 54000.00, 54000.00),
    -- HD2025004
    ('HD2025004', '89840000000000018', 62000.00, 62000.00),
    ('HD2025004', '89840000000000028', 64000.00, 64000.00),
    -- HD2025005
    ('HD2025005', '89840000000000001', 50000.00, 50000.00),
    ('HD2025005', '89840000000000011', 60000.00, 60000.00),
    -- HD2025006
    ('HD2025006', '89840000000000021', 58000.00, 58000.00);

-- ============================================
-- SIM OWNER INFO (for completed orders)
-- ============================================
INSERT INTO thongtinchusim (iccid, MaHD, HoTen, CCCD, NgaySinh, DiaChi, SDT)
VALUES
    ('89840000000000004', 'HD2025001', 'Hoang Van Customer', '001099001234', '2000-10-20', '147 Nguyen Trai, Da Nang', '0903456789'),
    ('89840000000000008', 'HD2025001', 'Hoang Van Customer', '001099001234', '2000-10-20', '147 Nguyen Trai, Da Nang', '0903456789'),
    ('89840000000000013', 'HD2025002', 'Vu Thi Customer', '001098002345', '1998-04-15', '258 Le Duan, Ho Chi Minh City', '0907654321'),
    ('89840000000000024', 'HD2025003', 'Do Van Customer', '001099003456', '1999-07-08', '369 Phan Chu Trinh, Hanoi', '0906543210'),
    ('89840000000000018', 'HD2025004', 'Hoang Van Customer', '001099001234', '2000-10-20', '147 Nguyen Trai, Da Nang', '0903456789'),
    ('89840000000000028', 'HD2025004', 'Hoang Van Customer', '001099001234', '2000-10-20', '147 Nguyen Trai, Da Nang', '0903456789'),
    ('89840000000000001', 'HD2025005', 'Bui Thi Customer', '002001004567', '2001-02-14', '741 Dong Khoi, Ho Chi Minh City', '0905432109'),
    ('89840000000000011', 'HD2025005', 'Bui Thi Customer', '002001004567', '2001-02-14', '741 Dong Khoi, Ho Chi Minh City', '0905432109'),
    ('89840000000000021', 'HD2025006', 'Dang Van Customer', '001097005678', '1997-12-30', '852 Ly Thuong Kiet, Da Nang', '0904321098');

-- ============================================
-- RATINGS (5 ratings for completed orders)
-- ============================================
INSERT INTO danhgia (MaHD, Sao, NoiDung, NgayDanhGia)
VALUES
    ('HD2025001', 5, 'Excellent service! SIMs activated quickly and staff was very helpful.', DATE_SUB(NOW(), INTERVAL 18 DAY)),
    ('HD2025002', 4, 'Good quality SIM cards. Delivery was fast but could improve packaging.', DATE_SUB(NOW(), INTERVAL 16 DAY)),
    ('HD2025003', 5, 'Very satisfied with the purchase. Great prices and friendly staff!', DATE_SUB(NOW(), INTERVAL 13 DAY)),
    ('HD2025004', 3, 'Average experience. SIMs work fine but processing took longer than expected.', DATE_SUB(NOW(), INTERVAL 10 DAY)),
    ('HD2025005', 5, 'Perfect! Got exactly what I needed. Will definitely buy again.', DATE_SUB(NOW(), INTERVAL 8 DAY));

-- ============================================
-- SHOPPING CART (current cart items)
-- ============================================
INSERT INTO giohang (KhachHang_Email, iccid, NgayThem)
VALUES
    ('customer1@fptsim.com', '89840000000000002', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    ('customer1@fptsim.com', '89840000000000012', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    ('customer2@fptsim.com', '89840000000000003', NOW()),
    ('customer3@fptsim.com', '89840000000000022', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    ('customer3@fptsim.com', '89840000000000023', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- ============================================
-- SUMMARY
-- ============================================
-- Users: 10 total (2 admins, 3 staff, 5 customers)
-- SIM Cards: 30 total (10 each provider)
--   - Available: 24
--   - Sold: 3
--   - Active: 3
-- Promotions: 6 active
-- Import Batches: 5 batches (26 SIMs imported)
-- Orders: 8 total (6 completed, 2 pending)
-- Ratings: 5 ratings on completed orders
-- Cart Items: 5 items across 3 customers
--
-- LOGIN CREDENTIALS:
-- Admin: admin@fptsim.com / admin123
-- Admin: root@mail.com / root
-- Staff: staff1@fptsim.com / staff123
-- Staff: staff2@fptsim.com / staff123
-- Staff: staff3@fptsim.com / staff123
-- Customer: customer1@fptsim.com / customer123
-- Customer: customer2@fptsim.com / customer123
-- Customer: customer3@fptsim.com / customer123
-- Customer: customer4@fptsim.com / customer123
-- Customer: customer5@fptsim.com / customer123
--
-- WARNING: This uses plain text passwords for demonstration only!
--          In production, passwords MUST be hashed with BCrypt!
