# VnNews - Ứng Dụng Đọc Báo Việt Nam

## Phiên bản 1.5.0

- Logo mới dùng hình chữ V kết hợp trang báo, tông đen mực và trắng ngà; áp dụng cho biểu tượng launcher thông thường và adaptive icon.
- Bản phát hành kèm APK cài đặt và báo cáo Word cập nhật.
- Tải tại [GitHub Releases v1.5.0](https://github.com/TienHoan-05/VnNews/releases/tag/v1.5.0).

Đánh giá theo UI Skills MCP: [nguồn skill, thay đổi giao diện và kết quả kiểm tra](docs/UI-REVIEW.md).
Giao diện có chữ dòng tin lớn hơn, vùng bấm 48dp, bố cục thích ứng với cửa sổ/cỡ chữ,
mô tả bài đã lưu và menu cỡ chữ 100–150% khi đọc bài gốc.

## Bản cập nhật giao diện và bản tin đa nguồn

### Thiết kế tạp chí

- Bộ chữ mới kết hợp tiêu đề serif với chữ nội dung sans, nền giấy ấm, điểm nhấn xanh trầm và màu đất nung; hỗ trợ sáng/tối, đồng bộ màu biểu tượng thanh hệ thống.
- Tin mới nhất có thẻ ảnh lớn, các bài tiếp theo nằm trong mục **Dòng tin mới** với ảnh nhỏ và màu nhận diện nguồn báo. Ở màn hình rộng, ảnh tin chính và nội dung nằm cạnh nhau.
- Chuyên mục dạng chữ với gạch chỉ báo, ngày giờ/thời tiết nằm trên một hàng nhỏ, tìm kiếm và menu tùy chọn được thu gọn. Trong menu **…** có chế độ đọc lướt không ảnh và làm mới tin.
- Thanh điều hướng dưới gọn hơn; trang đã lưu dùng cùng phong cách. Giữ nguyên quyền vị trí, lọc nguồn, lưu bài và khôi phục vị trí cuộn.

### Sửa lỗi cuộn và bổ sung vị trí

- Quay lại từ bài báo giữ nguyên vị trí danh sách, bộ lọc, tìm kiếm và chế độ đọc lướt. Chỉ đưa danh sách về đầu khi người dùng đổi chuyên mục/nguồn hoặc nhập tìm kiếm.
- Chuẩn hóa Unicode tiếng Việt, giải mã HTML trong RSS và dùng lớp chữ riêng cho tiêu đề/mô tả để tránh chữ bị trộn khi tái sử dụng hàng tin lúc cuộn. Bỏ lớp bo góc cắt vào chữ đầu dòng; chỉ ảnh được bo góc. Nội dung đã xử lý được giữ lại, không phân tích HTML lại mỗi khung hình.
- Màn hình dọc có thanh ngày giờ/thời tiết nhỏ, ô tìm kiếm bật qua biểu tượng và các hàng tin có ảnh nhỏ. Nút đọc lướt cho phép ẩn ảnh. Bố cục ngang giữ tin đầu với ảnh lớn.
- Trong thời tiết, bấm **Dùng vị trí của tôi** để cấp quyền gần đúng hoặc chính xác. Ứng dụng lấy tọa độ thực tế khi mở bản tin; không yêu cầu quyền vị trí nền, không lưu tọa độ vào DataStore. Có thể trở lại chọn thành phố bất cứ lúc nào. Khi định vị tắt, hết thời gian chờ hoặc bị từ chối quyền, ứng dụng hướng dẫn thử lại/chọn thủ công.
- Bài báo có nút chia sẻ và mở bằng trình duyệt; WebView không tải lại trang gốc khi Compose cập nhật giao diện.

Kiểm thử giao diện trên máy ảo: `gradlew.bat :app:connectedDebugAndroidTest`. Các ca hồi quy kiểm tra vị trí sau khi mở/đóng bài, khôi phục trạng thái và tiêu đề tiếng Việt khi cuộn qua nhiều hàng.

- Tổng hợp RSS trực tiếp từ **VnExpress, Tuổi Trẻ, Thanh Niên và Dân trí**, lọc theo báo và 5 chuyên mục. Tin được sắp xếp theo thời gian xuất bản, loại bỏ URL trùng; nguồn lỗi không làm mất tin của các nguồn còn lại.
- Đồng hồ và ngày theo thiết bị; nhiệt độ, trạng thái thời tiết và thời điểm dữ liệu từ [Open-Meteo](https://open-meteo.com/). Chọn một trong 8 thành phố, mặc định TP. Hồ Chí Minh; thành phố được lưu bằng DataStore. Thời tiết làm mới mỗi 15 phút khi màn hình hoạt động, có trạng thái tải/lỗi và đánh dấu dữ liệu cũ.
- Tìm nhanh trong tiêu đề, mô tả và tên báo của bản tin đang xem; làm mới thủ công và lưu đọc sau.
- Giao diện tối giản với tông xanh trầm, tin đầu có ảnh lớn, các tin còn lại dạng hàng nhỏ, hỗ trợ sáng/tối. Màn hình đã lưu dùng cùng bố cục.
- Hỗ trợ Android 7.0+ (API 24); bật core library desugaring cho API ngày giờ. Bài đã lưu giữ tiêu đề và mô tả trên máy; đọc toàn bộ bài gốc cần mạng.

Kiểm tra: `gradlew.bat :app:testDebugUnitTest :app:assembleDebug`. APK ở `app/build/outputs/apk/debug/app-debug.apk`.

Danh mục RSS: [VnExpress](https://vnexpress.net/rss), [Tuổi Trẻ](https://tuoitre.vn/rss.htm), [Thanh Niên](https://thanhnien.vn/rss.html), [Dân trí](https://dantri.com.vn/rss.htm). Thời tiết: [Open-Meteo Forecast API](https://open-meteo.com/en/docs), dữ liệu cấp phép [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).

Ứng dụng đọc tin tức hiện đại viết bằng **Jetpack Compose**, đáp ứng 100% các tiêu chí kỹ thuật từ **Unit 4 đến Unit 8** của giáo trình **Android Basics with Compose (Google)**.

---

## 📌 Bảng Đối Chiếu Kiến Thức Giáo Trình (Checklist Chấm Điểm)

| Khối kiến thức (Unit) | Công nghệ sử dụng | Thành phần & File trong Project |
| :--- | :--- | :--- |
| **Unit 4: Architecture & Navigation** | • Mô hình **MVVM** + **Repository Pattern**<br>• Quản lý UI State bằng **StateFlow** (UDF)<br>• **Jetpack Navigation Compose** | • `ui/navigation/NewsNavGraph.kt`<br>• `ui/home/HomeViewModel.kt`<br>• `data/repository/NewsRepository.kt` |
| **Unit 5: Connect to Internet & Images** | • **Retrofit 2** + XML RSS trực tiếp từ 4 báo<br>• OkHttp tải thời tiết Open-Meteo<br>• **Coil (`AsyncImage`)** tải và cache ảnh | • `data/remote/NewsApiService.kt`<br>• `data/repository/NewsSource.kt`<br>• `data/repository/WeatherRepository.kt`<br>• `ui/home/HomeScreen.kt` |
| **Unit 6: Data Persistence** | • **Room Database** (Entity, DAO, Flow)<br>• **DataStore Preferences** lưu cấu hình người dùng | • `data/local/ArticleEntity.kt`<br>• `data/local/ArticleDao.kt`<br>• `data/local/AppDatabase.kt`<br>• `data/local/UserPreferencesRepository.kt` |
| **Unit 7: WorkManager** | • **PeriodicWorkRequest** chạy tác vụ nền<br>• Ràng buộc kết nối mạng (`Constraints`)<br>• Phát **Notification** thông báo tin nóng | • `worker/DailyNewsNotificationWorker.kt`<br>• Khởi tạo tại `NewsApplication.kt` |
| **Unit 8: Views in Compose (Interop)** | • Nhúng **`WebView`** truyền thống vào Compose bằng **`AndroidView`** | • `ui/detail/ArticleWebView.kt`<br>• `ui/detail/DetailScreen.kt` |

---

## 🚀 Các Tính Năng Chính
1. **Duyệt tin đa chuyên mục:** Mới nhất, Công nghệ (Số hóa), Kinh doanh, Thể thao, Giải trí.
2. **Đọc báo gốc trong ứng dụng:** Bấm vào bất kỳ tin nào để mở trang web chính thức của báo qua `AndroidView(WebView)`.
3. **Lưu đọc sau (Room Database):** Bấm biểu tượng Bookmark để lưu tiêu đề, mô tả và liên kết trên máy. Toàn văn bài báo được đọc qua trang gốc khi có mạng.
4. **Thông báo tin mới định kỳ (WorkManager):** Tự động kiểm tra và thông báo tin mới mỗi 12 tiếng.

---

## 🛠 Hướng Dẫn Cài Đặt & Chạy
1. Mở thư mục dự án bằng **Android Studio Ladybug / Meerkat**.
2. Chờ Gradle đồng bộ (Sync Project with Gradle Files).
3. Chọn máy ảo (Emulator Android 7.0+ / API 24+) hoặc máy thật cắm cáp USB.
4. Nhấn **Run 'app'** (`Shift + F10`).
