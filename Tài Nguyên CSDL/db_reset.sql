USE fptsim;

-- Procedures (xóa tất cả, ta sẽ làm bằng JPA/Service)
DROP PROCEDURE IF EXISTS AddToCart;
DROP PROCEDURE IF EXISTS CreateOrderFromCart;
DROP PROCEDURE IF EXISTS ImportSimBatch;
DROP PROCEDURE IF EXISTS SearchSimAdvanced;
DROP PROCEDURE IF EXISTS UpdateOrderStatus;
DROP PROCEDURE IF EXISTS GetOrderDetails;
DROP PROCEDURE IF EXISTS ThongKeDoanhThu;
DROP PROCEDURE IF EXISTS ThongKeSanPhamBanChay;
DROP PROCEDURE IF EXISTS AuthenticateUser;

-- Triggers (xóa tất cả, logic chuyển sang Service)
DROP TRIGGER IF EXISTS after_create_order_from_cart;
DROP TRIGGER IF EXISTS before_insert_giohang;
DROP TRIGGER IF EXISTS before_update_hoadon_check;
DROP TRIGGER IF EXISTS after_complete_order;

-- Events (xóa luôn; nếu cần sẽ chuyển sang scheduler trong app)
DROP EVENT IF EXISTS update_uudai_status;