# Rà soát báo cáo và bài thuyết trình VnNews

Đối chiếu mã nguồn tại commit `777952c`, phiên bản ứng dụng 1.5.0.
Đã xem đủ 12 slide sau khi xuất ảnh bằng Microsoft PowerPoint và kiểm tra bản Word sau khi xuất bằng Microsoft Word. File `VnNews.pptx` được giữ nguyên; các nội dung dưới đây là đề xuất để người trình bày lựa chọn.

## Những nội dung đã sửa trong Word

- Cập nhật repository sang `TienHoan-05/VnNews`, đường dẫn release v1.5.0 và versionCode 150.
- Bỏ nhắc Hình 3–4 không còn trong file, giữ đúng hai ảnh sáng/tối đang dùng trong tài liệu tải về.
- Mô tả rõ `HomeViewModel` tải song song, gộp, sắp xếp và loại URL trùng; `NewsRepository` tải một feed hoặc làm việc với Room.
- Phân biệt URL ảnh với tệp ảnh. Bỏ lưu cập nhật `isBookmarked = false`, không xóa vật lý bản ghi.
- Bổ sung dữ liệu `ArticleEntity`, phạm vi lưu của DataStore, trạng thái màn hình, vòng đời WebView, định vị và lịch WorkManager.
- Nêu đúng thông báo nền hiện dùng VnExpress; chu kỳ 12 giờ không phải giờ gửi cố định.
- Cập nhật đủ 10 nhóm unit test với tổng 69 ca; nêu phần instrumentation, TalkBack và cỡ chữ lớn còn cần kiểm tra.
- Thay kết luận “hiện thực hóa đầy đủ và chính xác toàn bộ kiến thức” bằng kết quả và hạn chế cụ thể. Chỉnh lại bảng và phân trang để nội dung không tràn cuối trang.

## Đề xuất cho bộ slide hiện tại

Số slide trong bảng dưới đây là vị trí thật trong file, không phải số đang hiện ở góc slide.

| Slide | Nhận xét | Đề xuất cụ thể |
| --- | --- | --- |
| Toàn bộ | Có 12 slide nhưng số ở góc đi tới 16, bỏ qua nhiều số. | Đánh lại 02–12 ở các slide nội dung, hoặc bỏ toàn bộ số nếu không muốn duy trì thủ công. |
| 1 | Bìa đủ thông tin sinh viên và giảng viên. Hình bên phải là minh họa, không phải màn hình VnNews. | Có thể giữ. Nếu cần nhấn đúng phiên bản, thêm dòng nhỏ “Phiên bản 1.5.0” hoặc logo đã dùng trong app; không cần thêm lời giới thiệu dài. |
| 2 | Mục tiêu và ba ý chính rõ, chữ dễ đọc. | Giữ nguyên; trình bày trong khoảng 30–40 giây. |
| 3 | Hai ảnh thực tế giúp người nghe nhận ra ứng dụng. | Giữ ảnh và ngày chụp. Chỉ nói về các thao tác chính; phần chi tiết cỡ chữ và bố cục để trong Word hoặc demo. |
| 4 | Sơ đồ MVVM đủ để giới thiệu luồng chung nhưng gom các repository thành một khối. | Giữ sơ đồ ở mức tổng quát; khi nói, làm rõ có NewsRepository, WeatherRepository và UserPreferencesRepository. Việc gộp tin nhiều nguồn nằm ở HomeViewModel. |
| 5 | Quy trình RSS đúng hướng và không quá nhiều chữ. | Giữ. Có thể nói thêm “tải song song; loại trùng theo URL” thay vì đọc tên toàn bộ thư viện trên slide. |
| 6 | Cụm “lưu ảnh đại diện” và hình đám mây dễ khiến người nghe hiểu là lưu tệp ảnh hoặc đồng bộ cloud. | Đổi thành “Lưu tiêu đề, mô tả, ngày đăng, URL bài và URL ảnh”. Thay hình cloud bằng ảnh mục Đã lưu hoặc bỏ hình. Nói rõ Room lưu cục bộ, Coil quản lý cache ảnh riêng. |
| 7 | Danh sách ba màn hình trùng một phần với slide 3, 8 và phần demo. | Nếu giữ 12 slide, có thể chuyển phần này sang lời dẫn lúc demo và dùng chỗ trống cho slide kết quả kiểm thử. |
| 8 | “WebChromeClient ... hỗ trợ trải nghiệm đọc” khá chung; lớp này hiện chỉ được gán bản mặc định. | Đổi thành “AndroidView nhúng WebView; WebViewClient theo dõi tải trang; BackHandler xử lý quay lại”. Giữ phần không gọi lại loadUrl và bốn mức cỡ chữ vì có thao tác demo được. |
| 9 | Mô tả tám thành phố và quyền vị trí phù hợp mã nguồn. | Giữ. Chỉ nói tọa độ không ghi vào DataStore; không nói rằng ứng dụng không dùng tọa độ để gọi dịch vụ thời tiết. |
| 10 | “12 giờ mỗi chu kỳ” có thể bị hiểu là thông báo gửi chính xác 12 giờ một lần. Cũng chưa nói nguồn thông báo. | Dùng “Chu kỳ đăng ký 12 giờ, hệ điều hành quyết định lúc chạy” và “Hiện lấy điểm tin VnExpress”. Không chờ thông báo định kỳ trong demo. |
| 11 | Kịch bản demo có đủ các thao tác kiểm chứng được. | Giữ. Lưu sẵn một bài và tải xong tiêu đề/mô tả trước khi tắt mạng; bật mạng trở lại trước phần thời tiết. Không hứa đọc toàn văn khi mất mạng. |
| 12 | Kết luận hiện chỉ tóm tắt công nghệ, chưa có hạn chế cụ thể. | Thêm tối đa hai ý: “Đã có đọc tin đa nguồn và lưu tóm tắt” và “Chưa lưu toàn văn; cần kiểm thử thêm trên thiết bị”. Giữ lời cảm ơn. |

### Nội dung ngắn có thể dùng cho slide kết quả kiểm thử

Tiêu đề: **Kết quả kiểm thử và phần còn hạn chế**

- 69 unit test đạt; tác vụ kiểm thử và lint hoàn tất.
- Đã có bản APK 1.5.0; toàn văn bài gốc vẫn cần mạng.
- Kiểm thử giao diện, TalkBack và cỡ chữ 200% cần được kiểm tra tiếp.

Không đưa cả bảng 10 file test của Word lên slide. Nếu giảng viên hỏi, mở phần kiểm thử trong báo cáo hoặc kết quả Gradle để giải thích.

## Thứ tự gợi ý nếu muốn giữ tổng số 12 slide

Giả định bài nói khoảng 10 phút, chưa tính hỏi đáp. Có thể bỏ slide liệt kê điều hướng hiện tại, lồng phần đó vào demo và thêm một slide kết quả kiểm thử.

| Thứ tự | Nội dung | Thời lượng gợi ý |
| --- | --- | --- |
| 1 | Bìa đề tài | 15 giây |
| 2 | Mục tiêu | 35 giây |
| 3 | Giao diện thực tế | 45 giây |
| 4 | Kiến trúc và luồng dữ liệu | 60 giây |
| 5 | Xử lý nhiều nguồn RSS | 60 giây |
| 6 | Room và DataStore | 45 giây |
| 7 | Đọc bài gốc và giữ trạng thái | 55 giây |
| 8 | Thời tiết và vị trí | 35 giây |
| 9 | Thông báo nền | 30 giây |
| 10 | Kết quả kiểm thử và hạn chế | 45 giây |
| 11 | Trình diễn ứng dụng | 100 giây |
| 12 | Kết luận và trao đổi | 35 giây |

Tổng phần chuẩn bị là 9 phút 20 giây, còn khoảng 40 giây cho chuyển màn hình. Đây là gợi ý phân bổ, không phải giới hạn thời gian do giảng viên đưa ra.

## Chuẩn bị trước khi trình bày

1. Cài sẵn bản APK, cấp quyền cần thiết và tải bản tin khi mạng ổn định.
2. Chọn một bài có trang gốc mở được để thử cỡ chữ và quay lại danh sách.
3. Lưu một bài trước khi ngắt mạng; chỉ kiểm tra tiêu đề và mô tả ngoại tuyến.
4. Chọn sẵn một thành phố cho thời tiết để vẫn demo được nếu thiết bị không lấy được vị trí.
5. Chuẩn bị ảnh dự phòng cho thao tác demo và không chờ WorkManager chạy đủ chu kỳ trong giờ thuyết trình.
