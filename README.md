# HiTechCloud eKYC — VNeID SDK

> Official multi-language SDK for the **HiTechCloud eKYC — Xác thực VNeID** gateway
> (`https://ekyc-c06.hitechcloud.vn`). Verify chip CCCD, share & verify citizen data
> from VNeID, biometric face matching, VNeID SSO, organization identity and digital
> certificate (CTS/SmartCA) issuance — through the HiTechCloud Agent Gateway (AgentGW).

## Overview

HiTechCloud operates an **Agent Gateway** that connects your application (TCDN — Đơn vị
khai thác) to the Ministry of Public Security identity platform (C06 / RAR center) and the
VNeID app. These SDKs wrap the real gateway endpoints and implement the two authentication
layers the platform requires:

- **eID (chip CCCD)** — requests are signed with your **CSK private key** (RSA-SHA256).
- **VNeID services** — authenticated with an **`api_key`** plus a per-service **`target_code`**;
  SSO uses OAuth2 Bearer tokens.

Citizen data (`userInfo`) and biometric results (`biometricResult`) are always returned as a
**JWS (ES256)** string. The SDK includes a `Signature` helper in every language to sign eID
requests and to verify + parse the JWS payload with the C06 public key.

## Supported languages

| Language | Package | Manager |
|----------|---------|---------|
| Node.js (TypeScript) | `@hitechcloud-vietnam/vneid-sdk` | npm |
| Python | `hitechcloud-vneid` | PyPI |
| PHP | `hitechcloud-vietnam/vneid-sdk` | Composer |
| Go | `github.com/hitechcloud-vietnam/hitechcloud-vneid-sdk/go` | Go modules |
| Java | `vn.hitechcloud:vneid-sdk` | Maven |

## Resources & endpoints

| Resource | Methods | Endpoint |
|----------|---------|----------|
| `eid` | `verifyChip` | `POST /eid/api/verify/request` |
| `dataShare` | `init`, `getTransaction` | `POST /api/tcdn/share-info/api/agent/init`, `.../api/get-transaction` |
| `biometric` | `faceMatching`, `getTransactionV2` | `POST /api/tcdn/{code}/v2/api/get-transaction` |
| `verifyInfo` | `init`, `getTransaction` | `POST /api/tcdn/verify-info/...` |
| `organization` | `init`, `getTransaction` | `POST /api/tcdn/share-info/...` (`type=ORG`) |
| `sso` | `buildAuthorizeUrl`, `exchangeToken`, `getUserInfo` | `POST /api/tcdn/sso-token/api/vneid/form/sso`, `GET /api/tcdn/sso-userinfo/api/vneid/sso` |
| `cts` | `init`, `reportResult` | `POST /api/tcdn/{code}/...`, `.../create-smartca-account/.../receive-result` |
| `consent` | `receiveResult`, `withdrawConsentResult`, `buildUniversalLink` | `POST /api/tcdn/{code}/agent/account/receive-result` |

See [`openapi.json`](openapi.json) and [`docs/api-reference.md`](docs/api-reference.md) for the full contract.

## Environments

| Environment | Base URL |
|-------------|----------|
| Production | `https://ekyc-c06.hitechcloud.vn` |
| Sandbox | `https://ekyc-c06-sandbox.hitechcloud.vn` |

## Quick start

### Node.js (TypeScript)

```bash
npm install @hitechcloud-vietnam/vneid-sdk
```

```ts
import { VNeIDClient } from "@hitechcloud-vietnam/vneid-sdk";

const client = new VNeIDClient({
  apiKey: process.env.VNEID_API_KEY!,
  targetCode: process.env.VNEID_TARGET_CODE!,
  sandbox: true,
});

const init = await client.dataShare.init({ citizenPid: "036188001958" });
const txnId = init.data.txnId;
const url = client.consent.buildUniversalLink(txnId);
// ...after consent:
const tx = await client.dataShare.getTransaction(init.data.txnId);
const userInfo = client.signature.verifyJws(tx.userInfo, process.env.C06_PUBLIC_KEY!);
```

### Python

```bash
pip install hitechcloud-vneid
```

```python
from hitechcloud_vneid import VNeIDClient

client = VNeIDClient(api_key="...", target_code="...", sandbox=True)
init = client.data_share.init(citizen_pid="036188001958")
tx = client.data_share.get_transaction(init["data"]["txnId"])
user_info = client.signature.verify_jws(tx["userInfo"], c06_public_key_pem)
```

### PHP

```bash
composer require hitechcloud-vietnam/vneid-sdk
```

```php
use HiTechCloud\VNeID\VNeIDClient;

$client = new VNeIDClient(['api_key' => '...', 'target_code' => '...', 'sandbox' => true]);
$init = $client->dataShare->init(['citizenPid' => '036188001958']);
$tx = $client->dataShare->getTransaction($init['data']['txnId']);
```

### Go

```bash
go get github.com/hitechcloud-vietnam/hitechcloud-vneid-sdk/go
```

```go
client := vneid.NewClient(vneid.WithAPIKey("..."), vneid.WithTargetCode("..."), vneid.WithSandbox())
init, _ := client.DataShare.Init(map[string]interface{}{"citizenPid": "036188001958"})
```

### Java

```java
VNeIDClient client = VNeIDClient.builder()
    .apiKey("...").targetCode("...").sandbox(true).build();
Map<String,Object> init = client.dataShare().init(Map.of("citizenPid", "036188001958"));
```

## eID chip verification (RSA-SHA256 signed)

```python
from hitechcloud_vneid import VNeIDClient

client = VNeIDClient(
    api_key="...",
    license_id="LIC-HTC-0001",
    device_type="WEB",
    csk_private_key=open("csk-private.key").read(),
)
res = client.eid.verify_chip(id_card="001099008060", dscert="<base64>", dia_chi="hanoi")
# res["responds"]["exitcode"] == 0 means a valid DS Certificate
```

## Security

- Never commit CSK private keys, client certificates, `api_key` or `license_id`. See `.gitignore`.
- Production requires **mTLS** (TLS 1.3) and IP/Domain whitelisting.
- Always verify the JWS signature of `userInfo` / `biometricResult` with the C06 public key
  before trusting the data.
- Compliant with Luật Bảo vệ dữ liệu cá nhân 2025 (91/2025/QH15), Nghị định 356/2025/NĐ-CP,
  Thông tư 95/2025/TT-BCA.

## License

[MIT](LICENSE) © HiTechCloud Vietnam
