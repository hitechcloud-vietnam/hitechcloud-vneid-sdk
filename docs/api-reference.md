# Tham chiếu API

Tất cả endpoint dưới đây là endpoint thật của AgentGW. Xem thêm [`openapi.json`](../openapi.json).

Base URL: `https://ekyc-c06.hitechcloud.vn` (Production) · `https://ekyc-c06-sandbox.hitechcloud.vn` (Sandbox).

## Mẫu đường dẫn

```text
POST /eid/api/verify/request                                   # Xác thực chip CCCD (eID)
POST /api/tcdn/{service-code}/api/agent/init                   # Khởi tạo consent
POST /api/tcdn/{service-code}/api/get-transaction             # Lấy dữ liệu (không ảnh mặt)
POST /api/tcdn/{service-code}/v2/api/get-transaction          # Lấy dữ liệu + sinh trắc học
POST /api/tcdn/{service-code}/agent/account/receive-result    # Trả kết quả
POST /api/tcdn/{service-code}/api/vneid/form/sso              # Đổi code lấy token (SSO)
GET  /api/tcdn/{service-code}/api/vneid/sso                   # Lấy userinfo (SSO)
```

## Resource: `eid`

| Phương thức | Endpoint | Mô tả |
|-------------|----------|-------|
| `verifyChip(id_card, dscert, dia_chi)` | `POST /eid/api/verify/request` | Xác thực DS Certificate trên chip CCCD |

Phản hồi: `{ responds: { responseId, exitcode, result, message, time }, signature }`.
`exitcode = 0` và `result = true` nghĩa là chứng thư hợp lệ.

## Resource: `dataShare` (service-code `share-info`)

| Phương thức | Endpoint |
|-------------|----------|
| `init({ citizenPid, fullName?, birthDate? })` | `POST /api/tcdn/share-info/api/agent/init` |
| `getTransaction(transactionId)` | `POST /api/tcdn/share-info/api/get-transaction` |

`init` trả `{ data: { txnId, requestId } }`. `getTransaction` trả `{ userInfo (JWS), data }`.

Các trường `userInfo` tiêu biểu: `citizenPid`, `fullName`, `birthDate`, `nationalityCode`,
`indentifyNumber`, `issuingAuthority`, `genderCode`, `permanentAddress`, `permanentCityCode`,
`livingPlaceAddress`, `idCardExpireDate`.

## Resource: `biometric` (service-code `face-matching`, `biometric-share-info`)

| Phương thức | Endpoint |
|-------------|----------|
| `faceMatching(transactionId, faceImage, requestType='sdk-liveness-match')` | `POST /api/tcdn/face-matching/v2/api/get-transaction` |
| `getTransactionV2(serviceCode, { transactionId, faceImage }, requestType)` | `POST /api/tcdn/{service-code}/v2/api/get-transaction` |

Header `request_type`: `sdk-liveness-match` (chống giả mạo + so khớp) hoặc `sdk-match` (chỉ so khớp).
Phản hồi gồm `biometricResult` (JWS) và `userInfo` (JWS).

## Resource: `verifyInfo` (service-code `verify-info`)

| Phương thức | Endpoint |
|-------------|----------|
| `init({ citizenPid, fullName, birthDate, ... })` | `POST /api/tcdn/verify-info/api/agent/init` |
| `getTransaction(transactionId)` | `POST /api/tcdn/verify-info/api/get-transaction` |

Trường gửi lên khi khởi tạo trả về `true`/`false` (khớp/không khớp CSDL dân cư).

## Resource: `organization` (type=ORG)

| Phương thức | Endpoint |
|-------------|----------|
| `init({ shareOrgNumber, citizenPid? })` | `POST /api/tcdn/share-info/api/agent/init` (`type=ORG`) |
| `getTransaction(transactionId)` | `POST /api/tcdn/share-info/api/get-transaction` |

Dữ liệu tổ chức: `orgPid`, `orgName`, `taxCode`, `foundingDate`, `business[]`, `represents[]`.

## Resource: `sso`

| Phương thức | Endpoint |
|-------------|----------|
| `buildAuthorizeUrl({ clientId, redirectUri, scope='openid' })` | — (URL điều hướng) |
| `exchangeToken({ code, clientId, clientSecret, redirectUri })` | `POST /api/tcdn/sso-token/api/vneid/form/sso` |
| `getUserInfo(accessToken)` | `GET /api/tcdn/sso-userinfo/api/vneid/sso` |

`exchangeToken` trả `{ access_token, refresh_token, id_token, token_type, expires_in, scope }`.

## Resource: `cts` (service-code `create-smartca-account`)

| Phương thức | Endpoint |
|-------------|----------|
| `init({ citizenPid, type, shareOrgNumber?, feeCode?, listOrgCode?, keyData? })` | `POST /api/tcdn/share-info/api/agent/init` |
| `reportResult(body)` | `POST /api/tcdn/create-smartca-account/agent/account/receive-result` |

`type`: `PER` (cá nhân), `ORG` (tổ chức), `PER-ORG` (cá nhân thuộc tổ chức). Kết quả có
`serialNumber`, `expirationDateFrom/To`, `status` (`GOOD`/`REVOKED`/`EXPIRE`/`PAUSING`).

## Resource: `consent`

| Phương thức | Endpoint |
|-------------|----------|
| `receiveResult(serviceCode, body)` | `POST /api/tcdn/{service-code}/agent/account/receive-result` |
| `withdrawConsentResult({ requestStatus, reasonRequestCode?, reasonRequestText? })` | `POST /api/tcdn/withdraw-consent-result/agent/account/receive-result` |
| `buildUniversalLink(txnId)` | `https://universal.dancuquocgia.com/share/<txnId>` |

`withdraw-consent`: `requestStatus = DELETED` (đã xoá) hoặc `REJECT` (từ chối, cần `reasonRequestText`).
TCDN xử lý tối đa 72 giờ.

## Bảng mã dịch vụ (service-code)

`sso-token`, `sso-userinfo`, `share-info`, `face-matching`, `biometric-share-info`,
`verify-info`, `create-account`, `create-credit-card`, `create-ewallet-account`,
`create-account-result`, `create-smartca-account`, `create-sim`, `billing-history`,
`buy-medicine-history`, `withdraw-consent`, `withdraw-consent-result`, `payment-result`,
`vnpay-querydr`.
