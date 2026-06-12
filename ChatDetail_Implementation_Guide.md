# Hướng dẫn Triển khai Màn hình Chi tiết Chat (Nông Lâm Share)

Tài liệu này ghi lại các bước xây dựng tính năng nhắn tin chi tiết giữa Người mua và Người bán trong dự án **Nông Lâm Share**.

---

## 1. Thành phần cấu trúc (Architecture)

Tính năng được xây dựng dựa trên mô hình chuẩn của Android:
- **Model:** `Message.java` - Định nghĩa cấu trúc một tin nhắn.
- **Layout:** `activity_chat_detail.xml` và các Item XML.
- **Adapter:** `MessageAdapter.java` - Xử lý hiển thị tin nhắn trái/phải.
- **Activity:** `ChatDetailActivity.java` - Điều khiển chính và xử lý dữ liệu.

---

## 2. Các bước thực hiện chi tiết

### Bước 1: Tạo Model và Giao diện Bong bóng (Bubble)
Đầu tiên, tạo file `Message.java` để lưu dữ liệu. Sau đó, tạo các file drawable để làm bong bóng chat:
- `bg_bubble_sent.xml`: Bong bóng màu xanh (Teal) cho người gửi, bo góc đặc trưng.
- `bg_bubble_received.xml`: Bong bóng màu xám nhạt cho người nhận.

### Bước 2: Thiết kế Layout cho Item
Vì màn hình chat có 2 loại giao diện (tin nhắn mình gửi và tin nhắn người khác gửi), ta cần 2 file layout riêng:
- `item_message_sent.xml`: Căn lề phải, dùng background màu xanh.
- `item_message_received.xml`: Căn lề trái, có thêm ảnh đại diện (Avatar), dùng background xám.

### Bước 3: Xây dựng MessageAdapter (Đa giao diện)
Đây là phần quan trọng nhất. Trong Adapter, ta sử dụng hàm `getItemViewType()` để kiểm tra:
- Nếu `senderId` là "me" -> Trả về `TYPE_SENT`.
- Ngược lại -> Trả về `TYPE_RECEIVED`.
Dựa vào đó, hàm `onCreateViewHolder` sẽ nạp đúng file XML tương ứng.

### Bước 4: Hoàn thiện ChatDetailActivity
- **Khởi tạo:** Ánh xạ các View từ XML (RecyclerView, EditText, Button).
- **Cấu hình RecyclerView:** Sử dụng `setStackFromEnd(true)` trong LayoutManager để danh sách luôn ưu tiên hiển thị từ dưới lên (đúng chuẩn app Chat).
- **Xử lý gửi tin:** Khi nhấn nút gửi, thêm một đối tượng `Message` mới vào danh sách, gọi `notifyItemInserted` và cuộn xuống cuối cùng.

---

## 3. Luồng dữ liệu (Data Flow) trong Activity

1. **Khởi tạo:** `loadNongLamChatData()` nạp danh sách tin nhắn mẫu (mượn đồ thí nghiệm Khu B).
2. **Hiển thị:** `MessageAdapter` nhận danh sách, phân loại "me" hoặc "partner" để vẽ giao diện.
3. **Tương tác:** 
   - Người dùng nhập text -> Nhấn Gửi.
   - `sendMessage()` được gọi -> Cập nhật List -> Adapter báo hiệu UI thay đổi.
   - RecyclerView tự động cuộn xuống tin nhắn mới nhất.

---

## 4. Ghi chú cho Lập trình viên
- **Xử lý thời gian:** Hiện tại đang dùng String cứng (ví dụ: "19:01"). Sau này nên dùng `System.currentTimeMillis()` và format lại.
- **Bàn phím:** Nên cấu hình `android:windowSoftInputMode="adjustResize"` trong `AndroidManifest.xml` để bàn phím không che mất ô nhập liệu.
- **Tính năng tiếp theo:** Kết nối với Firebase Realtime Database hoặc Socket.io để tin nhắn nhảy lên thời gian thực mà không cần load lại.

---
*Tài liệu hướng dẫn nội bộ cho dự án Nông Lâm Share.*
