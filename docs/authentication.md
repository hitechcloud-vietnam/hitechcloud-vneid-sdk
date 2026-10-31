# Xác thực & Ký yêu cầu

Nền tảng dùng **hai cơ chế xác thực** cho hai lớp dịch vụ. SDK tự động áp dụng đúng cơ chế
theo từng resource.

## 1. Dịch vụ eID — Ký bằng khoá CSK

- **Thuật toán**: SHA256withRSA (RSA-SHA256), khoá 2048-bit, encoding Base64.
- **Chuỗi ký**: nối các trường bằng `|` theo đúng thứ tự:

```text
request_id | request_time | license_id | device_type | dia_chi | id_card | dscert
```

- **Công thức**: `signature = Base64( RSA-SHA256( signing_string, CSK_private_key ) )`.

Các header bắt buộc:

| Header | Mô tả |
|--------|-------|
| `content-type` | `application/json; charset=utf-8` |
| `request_id` | UUID v4 do TCDN sinh |
| `request_time` | `yyyyMMddHHmmss` (GMT+7) |
| `license_id` | Mã giấy phép do RAR cấp |
| `device_type` | Nền tảng triển khai (WEB, APP…) |
| `dia_chi` | Địa chỉ cấp tỉnh, viết liền không dấu |
| `signature` | Chữ ký RSA-SHA256 |

SDK tạo chuỗi ký và chữ ký cho bạn:

```python
sig = client.signature.sign_eid(
    request_id, request_time, license_id, device_type, dia_chi, id_card, dscert
)
```

> `request_time` của dịch vụ eID dùng `yyyyMMddHHmmss`.

## 2. Dịch vụ VNeID — `api_key` & `target_code`

| Header | Mô tả |
|--------|-------|
| `Content-Type` | `application/json; charset=utf-8` |
| `api_key` | Khoá API do HiTechCloud cấp (32–64 ký tự) |
| `target_code` | Mã mục đích chia sẻ/xử lý, hiển thị trên VNeID |
| `request_id` | Mã yêu cầu duy nhất do TCDN sinh |
| `request_time` | `ddMMyyyyHHmmss` |
| `request_type` | Chỉ dịch vụ sinh trắc: `sdk-liveness-match` hoặc `sdk-match` |

> **Lưu ý**: `request_time` của dịch vụ VNeID dùng `ddMMyyyyHHmmss` (khác eID). Lệch quá
> **2 phút** so với giờ nền tảng sẽ bị coi là timeout. SDK sinh `request_id` và `request_time`
> tự động nếu bạn không truyền.

## 3. SSO (OAuth2/OIDC)

- Đổi code lấy token: `POST /api/tcdn/sso-token/api/vneid/form/sso` (x-www-form-urlencoded).
- Lấy userinfo: `GET /api/tcdn/sso-userinfo/api/vneid/sso` với `Authorization: Bearer <access_token>`.

## 4. Xác minh dữ liệu trả về (JWS)

`userInfo` và `biometricResult` là chuỗi JWS `<header>.<payload>.<signature>` (thuật toán
`ES256`, `typ: JOSE/JWS`). Dùng helper `Signature` với public key C06:

```python
payload = client.signature.verify_jws(tx["userInfo"], c06_public_key_pem)
```

Header JWS mẫu: `{ "alg": "ES256", "typ": "JOSE/JWS" }`.

## 5. mTLS & whitelist

Mọi kết nối Production đi qua mTLS (TLS 1.3). Truyền chứng chỉ client khi khởi tạo:

```python
client = VNeIDClient(api_key="...", client_cert=("partner-client.crt", "partner-client.key"))
```

Thiếu chứng chỉ client hợp lệ hoặc IP ngoài whitelist sẽ bị từ chối ở tầng TLS hoặc trả HTTP 403.
