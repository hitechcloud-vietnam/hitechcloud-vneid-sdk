# Xử lý lỗi

Nền tảng dùng nhiều lớp mã lỗi: HTTP status, `exitcode` (eID), và `responseCode`/`resultCode`
(VNeID). Hãy dựa vào **mã** (ổn định) thay vì message.

## Phân cấp exception của SDK

| Lớp | Khi nào |
|-----|---------|
| `HiTechCloudVNeIDError` | Lớp cơ sở cho mọi lỗi SDK |
| `AuthenticationError` | HTTP 401 — token/chữ ký/license_id không hợp lệ |
| `AuthorizationError` | HTTP 403 — IP ngoài whitelist, tạm ngừng kết nối |
| `NotFoundError` | HTTP 404 |
| `ValidationError` | HTTP 400 — thiếu/sai định dạng trường |
| `RateLimitError` | HTTP 429 — có thuộc tính `retry_after` |
| `BiometricError` | HTTP 501 — lỗi xác thực sinh trắc học |
| `ServerError` | HTTP 500/503 |

```python
from hitechcloud_vneid import VNeIDClient
from hitechcloud_vneid.exceptions import (
    AuthenticationError, RateLimitError, ValidationError, HiTechCloudVNeIDError,
)

try:
    tx = client.data_share.get_transaction(txn_id)
except RateLimitError as e:
    print("Thử lại sau", e.retry_after, "giây")
except AuthenticationError:
    print("api_key hoặc chữ ký không hợp lệ")
except HiTechCloudVNeIDError as e:
    print("Lỗi:", e.message, e.status_code)
```

## Mã HTTP

| HTTP | Ý nghĩa |
|------|---------|
| 200 | Thành công |
| 400 | Thiếu trường bắt buộc / sai định dạng (eID) |
| 401 | Token hết hạn; chữ ký/license_id không hợp lệ |
| 403 | Không có quyền; IP ngoài whitelist; tạm ngừng kết nối |
| 404 | Địa chỉ yêu cầu không tồn tại |
| 500 | Lỗi máy chủ |
| 501 | Lỗi sinh trắc học |
| 503 | Dịch vụ tạm thời không khả dụng |

## `exitcode` — nghiệp vụ eID (HTTP 200)

| exitcode | Nội dung |
|----------|----------|
| 0 | Thành công — DS Certificate hợp lệ |
| 1 | Số CCCD không hợp lệ |
| 2 | license_id sai định dạng |
| 3 | device_type không hợp lệ |
| 4 | dia_chi không hợp lệ |
| 6 | Chứng thư trên chip không hợp lệ |
| 7 | Tài khoản kết nối không hợp lệ |
| 9 | Giấy phép hết hạn |
| 11 | Invalid credentials |

## `responseCode` / `resultCode` — dịch vụ VNeID

| Code | Ý nghĩa |
|------|---------|
| 0 | Thành công |
| 1 | Công dân đồng ý chia sẻ |
| 2 | Công dân không đồng ý chia sẻ |
| -1 | Không lấy được thông tin |
| -4 | Giao dịch đang xử lý, thử lại sau |
| -5 | Đầu vào không hợp lệ |
| -7 | Không tìm thấy dịch vụ |
| -9 | Không tìm thấy mã giao dịch |
| -11 | Công dân không đồng ý chia sẻ (khi get-transaction) |
| -14 | Timeout |
| -16 | Vượt hạn mức dịch vụ |
| -17 | Thông tin công dân không hợp lệ (Mức 1, hết hạn CCCD…) |

## Chiến lược retry

- **Không retry**: 400/401/403 và các mã `-5..-9`, `-17..-20`.
- **Có thể retry (backoff)**: 500/503 và `-4`, `-14`. SDK tự retry 429/5xx theo cấu hình
  `retries` (mặc định 3) với exponential backoff; 429 tôn trọng header `Retry-After`.
