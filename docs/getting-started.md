# Bắt đầu nhanh

Hướng dẫn này giúp bạn thực hiện giao dịch eKYC đầu tiên với nền tảng **eKYC — Xác thực VNeID**
của HiTechCloud thông qua SDK.

## 1. Đăng ký kết nối

1. Đăng ký làm Đơn vị khai thác (TCDN) với HiTechCloud để nhận `license_id`, `api_key`,
   `target_code` và `device_type`.
2. Tạo cặp khoá CSK (RSA 2048-bit); gửi **khoá công khai** cho HiTechCloud/RAR, giữ
   **khoá bí mật** để ký yêu cầu eID.
3. Tạo CSR cho chứng chỉ client (mTLS) và đăng ký IP tĩnh vào whitelist.
4. Nhận **public key của C06** để xác minh JWS (`userInfo`, `biometricResult`).

```bash
# Khoá CSK để KÝ yêu cầu eID
openssl genrsa -out csk-private.key 2048
openssl rsa -in csk-private.key -pubout -out csk-public.pem

# Khoá + CSR cho mTLS
openssl genrsa -out partner-client.key 2048
openssl req -new -key partner-client.key -out partner-client.csr \
  -subj "/C=VN/O=TEN_DOI_TAC/CN=partner.example.vn"
```

## 2. Cài đặt SDK

| Ngôn ngữ | Lệnh |
|----------|------|
| Node.js | `npm install @hitechcloud-vietnam/vneid-sdk` |
| Python | `pip install hitechcloud-vneid` |
| PHP | `composer require hitechcloud-vietnam/vneid-sdk` |
| Go | `go get github.com/hitechcloud-vietnam/hitechcloud-vneid-sdk/go` |
| Java | `vn.hitechcloud:vneid-sdk` (Maven) |

## 3. Khởi tạo client

```python
from hitechcloud_vneid import VNeIDClient

client = VNeIDClient(
    api_key="YOUR_API_KEY",
    target_code="YOUR_TARGET_CODE",
    sandbox=True,            # dùng https://ekyc-c06-sandbox.hitechcloud.vn
    timeout=30,
    retries=3,
)
```

## 4. Luồng chia sẻ dữ liệu (share-info)

```python
# Bước 1: khởi tạo giao dịch
init = client.data_share.init(citizen_pid="036188001958")
txn_id = init["data"]["txnId"]

# Bước 2: điều hướng công dân sang VNeID (chỉ trên APP VNeID)
link = client.consent.build_universal_link(txn_id)
print(link)  # https://universal.dancuquocgia.com/share/<txnId>

# Bước 3: sau khi công dân đồng ý, lấy dữ liệu
tx = client.data_share.get_transaction(txn_id)

# Bước 4: xác minh JWS và parse payload
user_info = client.signature.verify_jws(tx["userInfo"], c06_public_key_pem)
print(user_info["fullName"], user_info["citizenPid"])
```

## 5. Xác thực chip CCCD (eID)

```python
client = VNeIDClient(
    api_key="YOUR_API_KEY",
    license_id="LIC-HTC-0001",
    device_type="WEB",
    csk_private_key=open("csk-private.key").read(),
    client_cert=("partner-client.crt", "partner-client.key"),  # mTLS
)

res = client.eid.verify_chip(
    id_card="001099008060",
    dscert="<dscert-base64>",
    dia_chi="hanoi",
)
assert res["responds"]["exitcode"] == 0  # DS Certificate hợp lệ
```

## 6. Tiếp theo

- [Xác thực & Ký yêu cầu](authentication.md)
- [Tham chiếu API](api-reference.md)
- [Xử lý lỗi](error-handling.md)
