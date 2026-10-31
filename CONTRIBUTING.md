# Contributing to the HiTechCloud VNeID/eKYC SDK

Cảm ơn bạn đã quan tâm đóng góp cho SDK. Tài liệu này mô tả quy ước và quy trình phát triển.

## Nguyên tắc chung

- SDK bao bọc các API thật của nền tảng eKYC — Xác thực VNeID (`https://ekyc-c06.hitechcloud.vn`).
  Mỗi phương thức SDK phải ánh xạ tới một endpoint có thật trong `openapi.json` và tài liệu `docs/`.
- **Không bao giờ** commit khoá bí mật (`*.key`, `*.pem`), chứng chỉ client, `api_key`
  hay `license_id` thật. File `.gitignore` đã chặn các đuôi này.
- Giữ thiết kế đồng nhất giữa các ngôn ngữ: cùng tên resource, cùng tên phương thức
  (theo quy ước của từng ngôn ngữ), cùng mô hình lỗi và retry.

## Cấu trúc kho

```
sdk/
  nodejs/   TypeScript  (@hitechcloud-vietnam/vneid-sdk)
  python/   Python 3.8+ (hitechcloud-vneid)
  php/      PHP 8.0+    (hitechcloud-vietnam/vneid-sdk)
  go/       Go 1.21+    (github.com/hitechcloud-vietnam/hitechcloud-vneid-sdk/go)
  java/     Java 11+    (vn.hitechcloud:vneid-sdk)
docs/       Tài liệu tiếng Việt
examples/   Ví dụ cơ bản mỗi ngôn ngữ
mcp/        MCP server (Node)
```

## Quy ước

| Hạng mục | Quy ước |
|----------|---------|
| Client ctor | `apiKey`, `baseUrl`, `timeout`, `retries` (+ `licenseId`, `deviceType`, `cskPrivateKey` cho eID) |
| Lỗi | Phân cấp từ `HiTechCloudVNeIDError`; retry 429/5xx |
| Ký eID | RSA-SHA256 trên chuỗi nối bằng `|` |
| Xác minh dữ liệu | JWS ES256 (`userInfo`, `biometricResult`) |

## Phát triển & kiểm thử

```bash
# Python
cd sdk/python && python -m py_compile hitechcloud_vneid/**/*.py && pytest

# Node.js
cd sdk/nodejs && npm install && npm run build && npm test

# PHP
cd sdk/php && composer install && ./vendor/bin/phpunit

# Go
cd sdk/go && gofmt -l . && go vet ./... && go test ./...

# Java
cd sdk/java && mvn -q test
```

## Pull request

1. Fork và tạo nhánh từ `main`.
2. Thêm/điều chỉnh test cho thay đổi của bạn.
3. Cập nhật `CHANGELOG.md`.
4. Mở PR mô tả rõ endpoint liên quan.

## Giấy phép

Đóng góp của bạn được phát hành theo giấy phép [MIT](LICENSE).
