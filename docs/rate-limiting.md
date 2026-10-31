# Giới hạn tần suất (Rate limiting)

## Nguyên tắc

- Mỗi `api_key` và mỗi dịch vụ (`service-code`) có hạn mức riêng do HiTechCloud cấu hình.
- Vượt hạn mức trả về **HTTP 429** kèm header `Retry-After` (giây).
- Mã nghiệp vụ `-16` ("Dịch vụ vượt quá hạn mức cho phép") cũng cho biết bạn đã chạm trần.

## Ràng buộc giao dịch

| Ràng buộc | Giá trị |
|-----------|---------|
| Hiệu lực `transactionId` | 30 phút |
| Số lần gọi `get-transaction` tối đa | 3 lần / giao dịch |
| Lệch thời gian `request_time` cho phép | ≤ 2 phút so với giờ nền tảng |
| Thời hạn trả kết quả rút đồng ý | ≤ 72 giờ |

## SDK tự động xử lý

SDK tự retry khi gặp **429** và **5xx** theo cấu hình `retries` (mặc định 3):

- Với 429: chờ đúng số giây trong `Retry-After` rồi thử lại.
- Với 5xx / lỗi kết nối: exponential backoff (`2^attempt` giây).
- Hết số lần retry: ném `RateLimitError` (có `retry_after`) hoặc `ServerError`.

```python
client = VNeIDClient(api_key="...", retries=5, timeout=30)
```

```ts
const client = new VNeIDClient({ apiKey: "...", retries: 5, timeout: 30000 });
```

## Khuyến nghị

- Dùng `request_id` **duy nhất** cho mỗi yêu cầu để tránh bị coi là trùng (`-2`).
- Đồng bộ đồng hồ máy chủ (NTP) để `request_time` không lệch quá 2 phút.
- Không gọi lại `get-transaction` quá 3 lần; cache kết quả đã lấy.
- Với mã `-4` / `-14`: chờ rồi thử lại; với `-5..-9`, `-17..-20`: dừng và khắc phục nguyên nhân.
