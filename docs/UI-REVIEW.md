# Đánh giá giao diện VnNews — 06/11/2026

## Nguồn và phạm vi

Đã gọi trực tiếp JSON-RPC `initialize`, `tools/list`, `tools/call` trên
[UI Skills MCP](https://www.ui-skills.com/mcp), phiên bản 0.2.4.
`list_skills` trả về 638 skill; đã đọc nội dung qua `get_skill`:

| Skill | Áp dụng cho VnNews |
| --- | --- |
| [Android Native UI](https://www.ui-skills.com/skills/kemiljk/android-native-ui/llms.txt) | Compose semantics, Material controls, cửa sổ thay đổi kích thước, font scale và kiểm chứng trên Android. Đây là hướng dẫn ưu tiên cho stack hiện tại. |
| [Layout](https://www.ui-skills.com/skills/pbakaus/layout/llms.txt) | Nhóm tiêu đề/ảnh và nguồn/giờ; ưu tiên không gian đọc; bố cục dựa vào container. |
| [Typeset](https://www.ui-skills.com/skills/pbakaus/typeset/llms.txt) | Phân cấp chữ, giãn dòng, metadata dễ đọc, tôn trọng cỡ chữ hệ thống. |
| [Colorize](https://www.ui-skills.com/skills/pbakaus/colorize/llms.txt) | Giữ bản sắc xanh trầm/nền giấy, kiểm tra tương phản, dùng màu theo vai trò. |
| [Baseline UI](https://www.ui-skills.com/skills/ibelick/baseline-ui/llms.txt) | Nhãn cho nút biểu tượng, lỗi tại chỗ, hành động rõ ở trạng thái trống. |

Các chỉ dẫn CSS/Tailwind/ARIA và script `impeccable detect` không áp dụng trực tiếp
cho dự án Kotlin. Không thêm framework web. Đã kiểm tra mã Compose, Android lint
và đo các cặp màu thay cho detector web. Các file tham chiếu phụ của skill không
được endpoint `get_skill` cung cấp; không coi việc đọc skill chính là kiểm chứng UI.

Đối chiếu tài liệu chính thức:
[touch target và semantics](https://developer.android.com/develop/ui/compose/accessibility/api-defaults),
[constraints/modifier order](https://developer.android.com/develop/ui/compose/layouts/constraints-modifiers),
[WindowInfo](https://developer.android.com/reference/kotlin/androidx/compose/ui/platform/WindowInfo).

## Đánh giá và thay đổi

Luồng đọc chính: chuyên mục/nguồn → tin nổi bật → dòng tin → bài gốc.
Giữ hai họ chữ đã có: serif cho tiêu đề chính, sans-serif cho dòng tin và thao tác.
Nền ấm, chữ xanh đen, điểm nhấn xanh trầm tiếp tục là hệ thống nhận diện.

| Vấn đề trong mã trước lần cải thiện này | Thay đổi |
| --- | --- |
| Tiêu đề dòng tin 15sp/22sp, nguồn và giờ 10sp/15sp | Tiêu đề 17sp/25sp; metadata tối thiểu 12sp/18sp; nguồn và giờ xuống hai dòng. |
| Nút lưu 32dp, theme 40dp, tab chuyên mục cao 44dp | Dành vùng bố cục 48dp; nút lưu dùng `IconToggleButton` có trạng thái checked. |
| Thông tin nguồn nằm trong cột hẹp cạnh ảnh | Đặt metadata và nút lưu dưới hàng tiêu đề/ảnh, dùng trọn chiều rộng. |
| Bố cục compact dựa vào hướng xoay thiết bị | Dùng `LocalWindowInfo.containerSize`; thẻ nổi bật kiểm tra chiều rộng container thực. |
| Ảnh chiếm chỗ khi cửa sổ hẹp hoặc chữ lớn | Bỏ thumbnail dưới 300dp hoặc font scale ≥1,5; tin nổi bật chuyển sang bố cục dọc khi chữ lớn. |
| Tiêu đề giới hạn ba dòng ở mọi cỡ chữ | Dòng tin tối đa bốn dòng, sáu dòng ở cỡ chữ lớn; tin nổi bật tối đa sáu dòng. |
| Thanh ngày/thời tiết luôn chia hai cột | Xếp dọc dưới 360dp hoặc font scale ≥1,5; giữ thông báo lỗi gần dữ liệu thời tiết. |
| Header và điều hướng có chiều cao cố định | App bar tăng chiều cao theo chữ; ẩn tagline trang trí khi chữ lớn; thanh dưới có chiều cao tối thiểu và đo theo nội dung. |
| Chữ phụ sáng hơi nhạt | Đổi `onSurfaceVariant` thành `#535F55`, tiếp tục dùng semantic color tokens ở cả hai theme. |
| Trang đã lưu không hiển thị mô tả dù thông báo hỗ trợ xem offline | Hiển thị tối đa ba dòng mô tả từ dữ liệu đã lưu. |
| Thư viện trống không có hành động tiếp theo | Thêm “Khám phá bản tin”, điều hướng về trang chủ và khôi phục trạng thái. |
| Bài gốc chỉ có zoom bằng cử chỉ | Thêm menu cỡ chữ 100/115/130/150%; cập nhật `WebSettings.textZoom` không gọi lại `loadUrl`, nhớ mức chọn khi khôi phục màn hình. |

Tìm kiếm có nhãn riêng; nhóm tab có `selectableGroup`; tiêu đề chính có semantics
heading. Giữ lớp `StableArticleText` hiện có để tránh tái phát lỗi glyph tiếng Việt
khi cuộn. RSS, lưu bài, quyền vị trí và kiến trúc dữ liệu không bị thay thế.

## Kiểm chứng

Đo tương phản từ giá trị sRGB thực trong `Theme.kt`, không ước lượng bằng mắt:

| Cặp chữ/nền | Theme sáng | Theme tối |
| --- | --- | --- |
| `onSurface` / `background` | 14,10:1 | 14,41:1 |
| `onSurfaceVariant` / `background` | 6,25:1 | 10,04:1 |
| `onSurfaceVariant` / `surface` | 6,64:1 | 8,54:1 |
| `onSurfaceVariant` / `surfaceContainerLow` | 5,85:1 | 9,08:1 |

Đã kiểm tra thêm `primary` và `tertiary` trên ba nền trên; mức thấp nhất là
4,69:1. Các số này xác nhận những cặp màu được đo, không phải chứng nhận WCAG
cho toàn ứng dụng hay trang báo bên thứ ba.

Lệnh kiểm tra:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest
```

69 kiểm thử đơn vị vượt qua, không lỗi/bỏ qua. APK ứng dụng và APK instrumentation
biên dịch được. Android lint không có error; còn các cảnh báo về dependency,
resource chưa dùng và khuyến nghị mã. `git diff --check` không phát hiện lỗi whitespace.

Sau bước build, đã tạo AVD tạm từ system image Android 37.2 có sẵn, cài APK và
chụp trang chủ với tin thực tế ở hai theme, kích thước 1080×2400, density 420:
[sáng](../screenshots/vnnews-light-2026-11-06.png),
[tối](../screenshots/vnnews-dark-2026-11-06.png).
Đã xem ảnh để kiểm tra tiêu đề, ngày/thời tiết, thẻ nổi bật và điều hướng dưới.
Chưa chạy instrumentation, TalkBack hoặc font scale 200%. Các ảnh mang tên
`screenshot_*` cũ trong thư mục không đại diện cho bản sửa này.

Khi có thiết bị, cần kiểm tra trang chủ/thư viện ở 320dp và ≥600dp, hai theme,
font scale 100/150/200%; bật bàn phím tìm kiếm; chuyển nguồn/chuyên mục; lưu/bỏ lưu;
đọc bài rồi quay lại; đổi cỡ chữ trên các nguồn báo khác nhau. Website nguồn có
thể áp dụng CSS riêng nên hiệu quả `textZoom` cần kiểm tra từng nguồn.
