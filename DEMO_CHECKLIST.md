# Kịch bản quay video demo (3–4 phút)

Đề yêu cầu video 1–5 phút thể hiện: Pan, Pinch-to-Zoom, chọn/bỏ chọn, bộ lọc hoặc tìm kiếm, Bottom Sheet, đồng bộ trạng thái, Reset View và giữ trạng thái khi xoay màn hình. Kịch bản dưới đây đi qua đủ các mục theo thứ tự rubric.

## Chuẩn bị

- Cài bản mới: `./gradlew :app:installDebug`. Nếu vừa chạy `connectedDebugAndroidTest` thì phải cài lại, vì lệnh đó gỡ app.
- Mở lại app từ đầu (vuốt tắt rồi mở) để không còn ghế chọn từ lần trước.
- Emulator: bật xoay tự động, hoặc dùng nút xoay trên thanh công cụ emulator. Để pinch trên emulator, giữ **Ctrl** rồi kéo chuột.
- Bật "Show taps" (Developer options → Show taps) để người xem thấy điểm chạm.
- Quay bằng nút Screen record của emulator hoặc `adb shell screenrecord /sdcard/demo.mp4` (tối đa 3 phút mỗi file).

## Kịch bản

| # | Thời điểm | Thao tác | Nói / chỉ ra | Mục rubric |
|---|---|---|---|---|
| 1 | 0:00 | Mở app, để yên 3 giây | "Toàn bộ 208 ghế vẽ trên một Canvas: 128 Standard, 64 VIP, 16 ghế đôi; ba phân khu có nhãn và giá, hai lối đi, nhãn hàng hai bên, màn chiếu phía trên." | Canvas, bố cục |
| 2 | 0:15 | Nhấn nút (i) | Chỉ 3 loại ghế và 4 trạng thái: trống, đang chọn (tick), đã đặt (×), không khả dụng (nét đứt, gạch chéo). Nhấn (i) lần nữa để tắt. | Legend, trạng thái |
| 3 | 0:30 | Kéo một ngón theo 4 hướng, kéo mạnh ra quá mép | "Pan bị giới hạn, sơ đồ không bị kéo ra khỏi màn hình." | Pan |
| 4 | 0:45 | Ctrl + kéo để pinch, đặt tâm lên một ghế ở góc rồi phóng to; phóng tới tối đa, rồi thu nhỏ tới tối thiểu | "Zoom quanh tâm hai ngón, ghế dưới con trỏ đứng yên; có giới hạn min/max. Số ghế hiện ra khi đủ lớn." | Pinch-to-Zoom |
| 5 | 1:05 | Đang zoom, chạm một ghế trống, chạm ghế ngay bên cạnh, chạm sát mép ghế, chạm lại để bỏ chọn | "Hit-testing đúng sau pan/zoom, chạm sát biên vẫn đúng ghế, không nhầm ghế bên cạnh." | Hit-testing |
| 6 | 1:20 | Chạm một ghế đã đặt (×) và một ghế nét đứt | Sheet hiện lý do ("khách khác đã đặt trước", "ghế đang bảo trì"); ghế không được chọn. | Trạng thái, hit-testing |
| 7 | 1:30 | Chạm một ghế đôi (hàng M/N), chạm vào giữa ghế | "Ghế đôi có vùng chạm bằng đúng kích thước thật." | Hit-testing |
| 8 | 1:40 | Nhấn nút Reset (biểu tượng ngắm) | "Về góc nhìn mặc định có animation; các ghế đã chọn vẫn giữ nguyên." | Reset View |
| 9 | 1:50 | Chọn thêm 1 Standard, 1 VIP; chỉ vào sheet thu gọn | "Sheet cập nhật ngay: số ghế, mã ghế, tổng tiền." | Bottom Sheet thu gọn, đồng bộ Canvas → Sheet |
| 10 | 2:05 | Kéo sheet lên (hoặc nhấn mũi tên) | Danh sách ghế: tên, loại, giá, tổng cộng; cuộn được khi nhiều ghế; bản đồ vẫn hiện phía trên. | Bottom Sheet mở rộng |
| 11 | 2:15 | Nhấn × ở một dòng | "Ghế đó trên Canvas trở về trạng thái trống, tổng tiền giảm." | Đồng bộ Sheet → Canvas |
| 12 | 2:25 | Mở lại sheet, nhấn biểu tượng định vị của một ghế | Sheet thu lại, bản đồ trượt và zoom tới ghế, vòng sáng nhấp nháy. | Định vị từ Sheet |
| 13 | 2:35 | Nhấn chip "VIP" | Ghế không phải VIP bị làm mờ; dòng "Đang lọc: VIP · 64 ghế phù hợp"; ghế Standard đang chọn vẫn ở trong sheet với ghi chú "ngoài bộ lọc". | Bộ lọc, nhất quán |
| 14 | 2:50 | Nhấn thêm chip "Đang chọn" (khi chưa chọn ghế VIP nào) | Hiện "Không có ghế nào khớp bộ lọc". Nhấn "Xóa lọc" để trở về hiển thị đầy đủ. | Kết quả lọc, empty state |
| 15 | 3:00 | Mở sheet, xoay sang ngang | "Xoay màn hình: ghế đang chọn, tổng tiền, sheet mở đều giữ nguyên; bố cục ngang riêng." Pan/zoom/chạm thử một lần ở chế độ ngang. | Giữ trạng thái khi xoay, landscape |
| 16 | 3:20 | Xoay lại dọc, nhấn "Đặt vé" | Thông báo thành công; các ghế vừa đặt chuyển sang "Đã đặt" (×); chạm vào thì thấy "bạn vừa đặt ghế này". | Đặt vé giả lập |
| 17 | 3:35 | (Tùy chọn) Chuyển sang Android Studio, mở `ViewportTransform.kt` | Chỉ công thức `screen = world × scale + offset` và `zoomAndPan`. | Kiến trúc, tọa độ |

## Kiểm tra trước khi nộp

- [ ] Video dài 1–5 phút, xem được (đặt quyền "Anyone with the link" nếu dùng Google Drive hoặc YouTube unlisted).
- [ ] Dán đường dẫn video và repository vào đầu `README.md`.
- [ ] `git log` có ít nhất 5–7 commit có ý nghĩa (hiện có 6; các chỉnh sửa của bạn sau này nên được commit riêng).
- [ ] `./gradlew :app:assembleDebug` build được từ một bản clone sạch.
