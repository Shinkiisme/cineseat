# CineSeat: sơ đồ chọn ghế rạp chiếu phim

Bài thi giữa kỳ môn **Phát triển phần mềm cho thiết bị di động**, chủ đề *Interactive Spatial Map*, đề tài số 2: **Sơ đồ phòng chiếu rạp phim**.

CineSeat là ứng dụng Android mô phỏng màn hình chọn ghế khi đặt vé xem phim. Toàn bộ phòng chiếu (208 ghế, màn chiếu, lối đi, nhãn hàng, nhãn phân khu) được vẽ trên **một Canvas duy nhất**. Người dùng kéo để di chuyển, chụm/mở hai ngón để phóng to/thu nhỏ quanh tâm hai ngón, và chạm vào từng ghế để chọn hoặc xem thông tin. Dữ liệu là dữ liệu giả lập (mock data); không có backend, API hay cơ sở dữ liệu.

- Video demo: https://youtu.be/sNmorqEI0z0
- Repository: https://github.com/Shinkiisme/cineseat

## 1. Tính năng

| Nhóm | Tính năng |
|---|---|
| Sơ đồ | 14 hàng A–N, 208 ghế: 128 Standard (A–H), 64 VIP (I–L), 16 ghế đôi Couple (M–N). Ba khối 4 \| 8 \| 4 tách bởi hai lối đi; ba phân khu có nhãn tên và giá; nhãn hàng ở hai bên; màn chiếu có vầng sáng. |
| Trạng thái ghế | Còn trống (viền theo màu loại ghế), Đang chọn (đỏ đặc, có dấu tick), Đã đặt (xám, có dấu ×), Không khả dụng (viền nét đứt, gạch chéo). Phân biệt bằng cả màu lẫn hình, không chỉ dựa vào màu. |
| Pan | Kéo một ngón theo mọi hướng, có giới hạn để sơ đồ không bị kéo ra khỏi màn hình. |
| Pinch-to-Zoom | Thu phóng quanh tâm hai ngón, liên tục, có tỷ lệ tối thiểu và tối đa. Pan và zoom chạy cùng lúc trong một cử chỉ. |
| Hit-testing | Chạm chọn hoặc bỏ chọn ghế trống; chạm ghế đã đặt hoặc bị khóa để xem lý do; chạm vùng trống để bỏ ghế đang xem. Đúng ở mọi mức pan/zoom và khi chạm sát biên ghế. Ghế đôi có vùng chạm bằng đúng kích thước thật. |
| Reset View | Nút đưa sơ đồ về vị trí và tỷ lệ mặc định có animation; không xóa ghế đang chọn hay bộ lọc. |
| Legend | Nút (i) bật/tắt bảng chú thích nhỏ ở góc: 3 loại ghế (kèm giá) và 4 trạng thái. Biểu tượng được vẽ bằng chính hàm vẽ ghế của Canvas. |
| Bộ lọc | Chip lọc theo loại ghế và theo trạng thái. Ghế không khớp bị làm mờ, không bị ẩn, nên bố cục không đổi. Có dòng "Đang lọc: … · N ghế phù hợp", nút "Xóa lọc", và thông báo khi không ghế nào khớp. |
| Bottom Sheet | Khi thu gọn: số ghế đã chọn, mã ghế, tổng tiền, ghế đang xem. Khi mở rộng: danh sách cuộn được (tên, loại, giá, nút định vị, nút xóa), tổng cộng, "Bỏ chọn hết", "Đặt vé". Chiều cao tối đa khoảng 60% màn hình để luôn còn thấy bản đồ. |
| Đồng bộ hai chiều | Chọn hoặc bỏ chọn trên Canvas thì sheet cập nhật ngay. Xóa trên sheet thì ghế trên Canvas trở về trạng thái cũ. Chạm một ghế trong danh sách thì bản đồ trượt và phóng tới ghế đó kèm vòng sáng nhấp nháy. |
| Đặt vé giả lập | Các ghế đang chọn chuyển sang "Đã đặt", có thông báo thành công. Không gọi mạng. |
| Xoay màn hình | Giữ nguyên ghế đang chọn, ghế đã đặt, bộ lọc, ghế đang xem và trạng thái sheet. Bố cục riêng cho chế độ ngang (header và chip trên một hàng, sheet thấp hơn). |

## 2. Công nghệ

Kotlin, Jetpack Compose, Material 3 (`BottomSheetScaffold`, `FilterChip`), Compose `Canvas` / `DrawScope`, `ViewModel` + `StateFlow` + `SavedStateHandle`. Build bằng Gradle Kotlin DSL (Gradle 9.6, Android Gradle Plugin 9.4.1, Kotlin Compose plugin 2.2.10), `compileSdk 37`, `minSdk 26`.

Không dùng thư viện bên thứ ba ngoài AndroidX. Thư viện test: JUnit 4 (unit test), `androidx.compose.ui:ui-test-junit4` (test cử chỉ chạy trên thiết bị).

## 3. Build và chạy

Yêu cầu: Android SDK có platform 37, JDK 17 trở lên, một thiết bị hoặc emulator Android 8.0+.

```bash
# Tạo local.properties trỏ tới SDK, nếu chưa có
echo "sdk.dir=/đường/dẫn/Android/Sdk" > local.properties

./gradlew :app:assembleDebug          # build APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:installDebug           # cài lên thiết bị/emulator đang kết nối
./gradlew :app:testDebugUnitTest      # 20 unit test (hình học, hit-testing, ViewModel)
./gradlew :app:connectedDebugAndroidTest   # 4 test cử chỉ trên thiết bị (pinch, pan, tap)
```

Hoặc mở thư mục này bằng Android Studio và nhấn Run.

Lưu ý: `connectedDebugAndroidTest` gỡ ứng dụng khỏi thiết bị sau khi chạy xong, nên cần `installDebug` lại trước khi dùng thử.

## 4. Kiến trúc mã nguồn

```
app/src/main/java/app/cineseat/
├── MainActivity.kt                    Khởi tạo theme và CinemaScreen
├── data/
│   ├── model/Seat.kt                  Seat, SeatType (giá), SeatStatus
│   └── repository/SeatRepository.kt   Sinh 208 ghế mock data (seed cố định)
├── ui/
│   ├── state/CinemaUiState.kt         Trạng thái giao diện + SeatFilter (nguồn dữ liệu duy nhất)
│   ├── viewmodel/CinemaViewModel.kt   Nghiệp vụ: chọn/bỏ chọn, lọc, đặt vé; lưu vào SavedStateHandle
│   ├── canvas/
│   │   ├── CinemaCanvas.kt            Vẽ toàn bộ phòng chiếu + nhận cử chỉ
│   │   └── ViewportController.kt      "Camera" pan/zoom, animation reset/định vị
│   ├── components/
│   │   ├── SelectionBottomSheet.kt    Nội dung Bottom Sheet
│   │   ├── FilterBar.kt               Chip lọc + dòng điều kiện đang lọc
│   │   └── LegendCard.kt              Bảng chú thích, SeatIcon
│   ├── screen/CinemaScreen.kt         Ghép header, bộ lọc, bản đồ, nút nổi, sheet; bố cục dọc/ngang
│   ├── theme/Theme.kt                 Màu tối, màu ghế dùng chung
│   └── Format.kt                      Định dạng tiền VND, tên ghế
└── util/geometry/
    ├── CinemaLayout.kt                Tọa độ world của từng ghế, lối đi, nhãn
    ├── ViewportTransform.kt           Viewport (world ↔ screen, zoom quanh tâm), ViewportLimits (giới hạn pan/zoom)
    └── SeatHitTester.kt               Hit-testing trong tọa độ world
```

Ba lớp được tách riêng:

- **Dữ liệu** (`data/`): ghế chỉ có mã, hàng, số, loại, trạng thái gốc, cột và độ rộng (số ô). Không chứa thông tin vẽ.
- **Trạng thái và nghiệp vụ** (`CinemaViewModel`, `CinemaUiState`): Canvas, bộ lọc và Bottom Sheet cùng đọc một `StateFlow<CinemaUiState>`. Mọi thay đổi đi qua ViewModel.
- **Vẽ và hình học** (`ui/canvas/`, `util/geometry/`): `CinemaLayout` tính hình chữ nhật của từng ghế một lần. Canvas vẽ đúng các hình chữ nhật đó, và hit-tester kiểm tra trên chính các hình chữ nhật đó, nên hình vẽ và vùng chạm luôn trùng nhau.

Pan/zoom là trạng thái *giao diện*, không phải dữ liệu, nên nằm trong `ViewportController` (cấp màn hình) chứ không nằm trong ViewModel. Khi xoay màn hình, kích thước canvas đổi nên góc nhìn tính lại về mặc định cho kích thước mới. Ghế đang chọn và bộ lọc thì vẫn giữ nguyên.

## 5. Tổ chức dữ liệu giả lập

`SeatRepository` sinh danh sách ghế một lần (`object`, khởi tạo lười):

- Hàng A–H là Standard (75.000đ), I–L là VIP (105.000đ), M–N là Couple (190.000đ). Mỗi hàng Standard/VIP có 16 ghế; mỗi hàng Couple có 8 ghế đôi, mỗi ghế chiếm 2 cột.
- Trạng thái gốc sinh bằng `Random(2024)`, nên **cố định** qua mỗi lần mở app, recomposition hay xoay màn hình. Khối giữa có xác suất đã đặt cao hơn (42% so với 22% ở hai khối bên), giống rạp thật. Khoảng 4% ghế "đang bảo trì". A1, A2, A15, A16 là "khu vực kỹ thuật". Mỗi ghế không khả dụng có lý do (`note`) hiển thị khi chạm.
- `SELECTED` **không bao giờ được ghi vào dữ liệu ghế**. ViewModel giữ danh sách mã ghế đang chọn (`selectedIds`, giữ thứ tự chọn) và tập ghế đã đặt trong phiên (`bookedIds`). Trạng thái hiển thị được suy ra:

```kotlin
fun statusOf(seat) = when {
    seat.id in selectedIds -> SELECTED
    seat.id in bookedIds   -> BOOKED
    else                   -> seat.baseStatus
}
```

Nhờ vậy, khi bỏ chọn, ghế luôn trở về đúng trạng thái trước đó, và các ghế khác không bị ảnh hưởng.

## 6. Công thức chuyển đổi tọa độ

Mỗi ghế có tọa độ **world** cố định (đơn vị: 1 world = 1 px khi tỷ lệ bằng 1). Viewport gồm `scale` và `offset` (px):

```
screen = world × scale + offset
world  = (screen − offset) / scale
```

Canvas vẽ phần hình học bên trong `withTransform { translate(offset); scale(scale, pivot = (0,0)) }`, tức đúng công thức trên. Chữ (nhãn hàng, số ghế) được vẽ sau, ở vị trí `toScreen(...)`, nên giữ cỡ dễ đọc ở mọi mức zoom.

**Zoom quanh tâm hai ngón** (`Viewport.zoomAndPan`): Compose `detectTransformGestures` trả về `centroid` là vị trí *trước đó* của tâm hai ngón (thư viện gọi `calculateCentroid(useCurrent = false)`, đã kiểm tra trong bytecode), `pan` là độ dịch kể từ lần trước, và `zoom` là tỷ lệ thay đổi khoảng cách giữa hai ngón. Mỗi bước:

```
scale' = clamp(scale × zoom, minScale, maxScale)
anchor = (centroid − offset) / scale           // điểm world đang nằm dưới các ngón tay
offset' = (centroid + pan) − anchor × scale'   // điểm đó đi theo các ngón tới vị trí mới
```

Điểm world dưới các ngón tay nằm yên dưới các ngón tay sau mỗi bước, kể cả khi vừa kéo vừa zoom. Sau đó `ViewportLimits.clamp` áp giới hạn.

**Giới hạn** (`ViewportLimits`): `view` là phần canvas không bị sheet thu gọn che, `fitScale = min(view.w / world.w, view.h / world.h)` (tỷ lệ vừa khít, chính là góc nhìn mặc định), `minScale = 0.8 × fitScale`, `maxScale = 6 × fitScale`. Mỗi trục được giới hạn riêng:

- Sơ đồ **nhỏ hơn** khung nhìn: phải nằm trọn trong khung, `offset ∈ [viewStart − start·s, viewEnd − end·s]`.
- Sơ đồ **lớn hơn** khung nhìn: mép sơ đồ không được thụt vào quá `edge` (48dp), `offset ∈ [viewEnd − edge − end·s, viewStart + edge − start·s]`.

Code dùng một công thức `lo = min(...)`, `hi = max(...)` bao cả hai trường hợp và liên tục khi chuyển giữa chúng. Vì vậy sơ đồ không bao giờ bị kéo ra khỏi màn hình.

## 7. Hit-testing

1. Chuyển điểm chạm sang world: `world = viewport.toWorld(tap)`.
2. `SeatHitTester.hitTest` duyệt 208 hình chữ nhật (duyệt tuyến tính là đủ nhanh với số lượng này). Mỗi hình được nới thêm `tolerance = GAP / 2 = 3` đơn vị world mỗi phía, nên chạm hơi lệch mép vẫn trúng. Vì khoảng cách giữa hai ghế là `GAP`, các vùng đã nới chỉ chạm nhau ở đường giữa chứ không chồng lên nhau, nên không thể chọn nhầm ghế bên cạnh. Nếu vẫn có nhiều ứng viên, ghế có tâm gần nhất thắng.
3. Ghế đôi dùng đúng hình chữ nhật rộng gấp đôi cộng khoảng hở giữa, nên chạm vào giữa ghế đôi vẫn trúng.
4. Vì hit-testing làm trong world, kết quả không phụ thuộc pan/zoom.

Phân biệt chạm với kéo: modifier `detectTransformGestures` nằm trong `detectTapGestures`, nên nhận sự kiện trước. Khi ngón tay di chuyển quá touch slop, nó tiêu thụ (consume) sự kiện, và `detectTapGestures` thấy sự kiện đã bị tiêu thụ nên hủy cú chạm. Kéo vì thế không bao giờ bị hiểu thành chạm.

## 8. Quản lý trạng thái

- `CinemaViewModel` giữ một `MutableStateFlow<CinemaUiState>`. Mọi hàm (`onSeatTapped`, `removeSelection`, `toggleType`, `confirmBooking`, …) tạo trạng thái mới rồi **ghi vào `SavedStateHandle`**: danh sách ghế chọn, ghế đã đặt, ghế đang xem, bộ lọc.
  - Xoay màn hình: ViewModel còn sống, nên trạng thái giữ nguyên.
  - Hệ thống kill process khi app ở nền: ViewModel mới đọc lại từ `SavedStateHandle` (unit test `stateComesBackFromSavedStateHandle` kiểm tra đúng luồng này).
- Bộ lọc chỉ làm mờ ghế, không đổi dữ liệu và không đổi danh sách chọn. Ghế đang chọn mà bị lọc mờ vẫn ở trong sheet, có ghi chú "ngoài bộ lọc". Chạm một ghế bị làm mờ chỉ xem thông tin, không chọn, để bộ lọc không làm thay đổi lựa chọn ngoài ý muốn.
- Trạng thái thuần giao diện (camera, sheet mở/đóng, legend mở/đóng) nằm ở Compose: `ViewportController`, `rememberBottomSheetScaffoldState` và `rememberSaveable` (hai cái sau cũng được giữ khi xoay).

## 9. Tối ưu hiệu năng

- Một `Canvas` cho cả 208 ghế, không có composable riêng cho từng ghế.
- Hình chữ nhật của ghế, `Path` màn chiếu, `Brush` vầng sáng, `Stroke`/`PathEffect`, và toàn bộ chữ (đo trước bằng `TextMeasurer`: chữ hàng, số 1–16, nhãn phân khu) đều được tạo **một lần** trong `remember`, không tạo trong khối vẽ.
- Viewport được đọc **trong khối vẽ**, nên mỗi khung pan/zoom chỉ chạy lại pha vẽ, không recompose màn hình.
- Bỏ qua (cull) các ghế nằm ngoài vùng nhìn thấy.
- Tập các hàng có ghế đang chọn được tính lại chỉ khi danh sách chọn đổi.
- Số ghế chỉ vẽ khi ghế đủ lớn (≥ 22dp trên màn hình). Chữ tự co khi zoom quá nhỏ (ví dụ ở chế độ ngang) để không chồng lên nhau.

## 10. Hướng dẫn sử dụng

1. Kéo một ngón để di chuyển; chụm/mở hai ngón để thu phóng (trên emulator: giữ Ctrl rồi kéo chuột).
2. Chạm ghế trống để chọn, chạm lại để bỏ chọn. Chạm ghế xám hoặc ghế nét đứt để xem vì sao không đặt được.
3. Nút (i): bật/tắt chú thích. Nút ngắm: Reset View.
4. Chip phía trên: lọc theo loại hoặc trạng thái; "Xóa lọc" để bỏ.
5. Kéo sheet lên (hoặc nhấn mũi tên) để xem danh sách. Biểu tượng định vị hoặc chạm vào dòng: bản đồ tới ghế đó. Dấu ×: bỏ ghế. "Đặt vé": đặt giả lập.

## 11. Kiểm thử

Đã chạy thật trên máy phát triển (Linux, emulator Pixel 9, Android 16):

| Bộ test | Số test | Nội dung | Kết quả |
|---|---|---|---|
| `GeometryTest` (JVM) | 12 | world↔screen là nghịch đảo nhau; zoom giữ điểm dưới ngón tay; pan + zoom đồng thời; giới hạn min/max; góc nhìn mặc định nằm trọn và căn giữa (dọc + ngang); không thể kéo sơ đồ ra khỏi màn hình ở 4 mức scale × 3 hướng; ghế không chồng nhau; ghế đôi rộng đúng; mọi ghế trúng ở tâm và sát 4 mép; chạm giữa hai ghế chọn ghế gần hơn; lối đi, màn chiếu, khoảng giữa phân khu không trúng ghế; **mọi ghế × 3 điểm × 4 viewport (sau pan/zoom)** đều trúng đúng ghế | 12/12 qua |
| `CinemaViewModelTest` (JVM) | 8 | đủ 208 ghế đúng tỷ lệ; mock data cố định; chọn/bỏ chọn trả về trạng thái cũ; ghế đã đặt/khóa chỉ xem được; tổng tiền và xóa từ sheet; lọc không đổi lựa chọn, đếm 0 kết quả; đặt vé; khôi phục từ `SavedStateHandle` | 8/8 qua |
| `CanvasGestureTest` (trên emulator) | 4 | cử chỉ chạm thật vào Canvas: chạm 6 ghế + lối đi ở góc nhìn mặc định; **pinch hai ngón** làm tỷ lệ tăng hơn 2 lần và điểm dưới ngón lệch dưới 1 đơn vị world, sau đó chạm mọi ghế trên màn hình đều trúng; pinch rồi kéo quá biên nhiều lần thì sơ đồ dừng đúng ở giới hạn, sau đó chạm vẫn trúng; zoom dừng ở max, Reset trở về mặc định | 4/4 qua (chạy 2 lần liên tiếp) |

Kiểm tra thủ công trên emulator bằng `adb` (chạm/kéo một ngón và chụp màn hình): hiển thị 208 ghế và phân khu; chọn D1, I1, M1 cho tổng 370.000đ; chạm ghế đã đặt hiện lý do; mở sheet; định vị M1 từ danh sách; chạm K4 sau khi định vị (đã pan + zoom) chọn đúng K4; Reset giữ nguyên lựa chọn; xoay ngang/dọc khi sheet mở vẫn giữ lựa chọn, tổng tiền và sheet mở; lọc VIP cho "64 ghế phù hợp"; đặt vé chuyển ghế sang "Đã đặt".

Chưa kiểm chứng: pinch bằng tay trên máy thật; khôi phục sau khi hệ thống kill process trên thiết bị (`am kill` không kill được process trong lần thử, nên luồng này chỉ được kiểm bằng unit test); màn hình rất nhỏ hoặc máy tính bảng.

## 12. Nhật ký sử dụng AI Agent

**Agent:** Claude Code (model Claude Opus 5.5), chạy trong VS Code, có quyền đọc/ghi file trong thư mục dự án, chạy Gradle, `adb` và emulator.

**Nhiệm vụ đã giao:** đọc đề thi (PDF), chọn kiến trúc, viết toàn bộ mã nguồn, test, README và hướng dẫn quay video; commit theo từng mốc; không push.

**File do Agent tạo:** toàn bộ file trong repository (cấu hình Gradle, `AndroidManifest.xml`, icon, mọi file `.kt` trong `main/`, `test/`, `androidTest/`, `README.md`, `DEMO_CHECKLIST.md`). Phiên bản Gradle/AGP/Compose được lấy theo một dự án Android khác của sinh viên trên cùng máy để build offline được.

**Quy trình và các lỗi Agent tự phát hiện, sửa:**

| Bước | Vấn đề | Cách phát hiện | Cách sửa |
|---|---|---|---|
| Chạy lần đầu | Crash `NoSuchMethodException: CinemaViewModel.<init>`. Factory mặc định cần constructor chỉ nhận `SavedStateHandle`, trong khi Agent thêm một tham số có giá trị mặc định. | logcat | Bỏ tham số thừa |
| Định vị ghế | Bản đồ khi zoom vẽ tràn lên header và chip lọc (Compose `Canvas` không tự cắt theo khung) | Chụp màn hình | Thêm `clipToBounds()` |
| Pinch-to-Zoom | Công thức ban đầu coi `centroid` là vị trí *hiện tại* của ngón tay, nên khi vừa kéo vừa zoom thì `pan` bị tính hai lần (lệch nhỏ). Unit test ban đầu dùng cùng giả định sai nên vẫn qua. | Test pinch trên thiết bị báo lệch; Agent đọc bytecode `detectTransformGestures` và thấy `calculateCentroid(useCurrent = false)` | Sửa công thức thành `offset' = centroid + pan − anchor·scale'`; sửa unit test theo đúng ngữ nghĩa |
| Test pinch | Hai lỗi nằm ở chính test: so sánh `Viewport` bằng `==` (sai số float), và chọn ghế đã nằm ngoài màn hình | Đọc thông báo lỗi | So sánh gần đúng; chỉ chạm ghế đang trong khung nhìn |
| Chế độ ngang | Nhãn chữ cỡ cố định chồng lên ghế khi sơ đồ nhỏ; dòng bộ lọc làm sơ đồ nhỏ thêm; sheet mở rộng che gần hết bản đồ | Chụp màn hình khi xoay | Chữ tự co theo zoom; dòng bộ lọc thành thẻ nổi khi xoay ngang; giảm chiều cao tối đa của sheet |
| Giao diện | Ghế "đang chọn" thiếu dấu tick như đã mô tả; vạch chia ghế đôi cắt ngang số ghế; lý do bị lặp chữ ("Đã đặt: Đã có khách đặt") | Chụp màn hình | Thêm tick, bỏ vạch chia, viết lại câu |
| Kiểm thử thủ công | `connectedDebugAndroidTest` gỡ app sau khi chạy, nên một loạt lệnh chạm bằng adb rơi vào ứng dụng khác đang mở trên emulator | Chụp màn hình | Cài lại và kiểm tra app đang ở foreground trước khi chạm |

**Hạn chế còn lại:** góc nhìn (pan/zoom) không được giữ khi xoay màn hình mà quay về mặc định cho kích thước mới (có chủ đích); ở chế độ ngang trên điện thoại, sơ đồ mặc định khá nhỏ vì phòng chiếu có dạng dọc, nên người dùng cần zoom; không giới hạn số ghế tối đa mỗi lần đặt.
