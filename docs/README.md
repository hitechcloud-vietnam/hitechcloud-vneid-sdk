# Tài liệu SDK eKYC — VNeID

Chào mừng bạn đến với tài liệu của HiTechCloud eKYC — VNeID SDK.

## Mục lục

- [Bắt đầu nhanh](getting-started.md)
- [Xác thực & Ký yêu cầu](authentication.md)
- [Tham chiếu API](api-reference.md)
- [Xử lý lỗi](error-handling.md)
- [Giới hạn tần suất (Rate limiting)](rate-limiting.md)

## Liên kết nhanh

- [Đặc tả OpenAPI](../openapi.json)
- [Kho GitHub](https://github.com/hitechcloud-vietnam/hitechcloud-vneid-sdk)
- [Cổng eKYC](https://ekyc-c06.hitechcloud.vn)

## Hai lớp dịch vụ

| Lớp | Dịch vụ | Xác thực |
|-----|---------|----------|
| eID (chip CCCD) | Xác thực DS Certificate trên chip | Ký RSA-SHA256 bằng khoá CSK + `license_id` |
| VNeID (AgentGW) | Chia sẻ dữ liệu, sinh trắc học, SSO, định danh tổ chức, CTS | `api_key` + `target_code` theo `service-code`; SSO dùng OAuth2 Bearer |

## Môi trường

| Môi trường | Base URL |
|------------|----------|
| Production | `https://ekyc-c06.hitechcloud.vn` |
| Sandbox | `https://ekyc-c06-sandbox.hitechcloud.vn` |
