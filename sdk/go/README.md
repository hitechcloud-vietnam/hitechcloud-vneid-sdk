# HiTechCloud VNeID SDK — Go

Official Go client for the HiTechCloud eKYC — VNeID gateway.

## Install

```bash
go get github.com/hitechcloud-vietnam/hitechcloud-vneid-sdk/go
```

## Usage

```go
package main

import (
    "fmt"
    vneid "github.com/hitechcloud-vietnam/hitechcloud-vneid-sdk/go"
)

func main() {
    client := vneid.NewClient(
        vneid.WithAPIKey("YOUR_API_KEY"),
        vneid.WithTargetCode("YOUR_TARGET_CODE"),
        vneid.WithSandbox(),
    )

    init, err := client.DataShare.Init(map[string]interface{}{"citizenPid": "036188001958"})
    if err != nil {
        panic(err)
    }
    data := init["data"].(map[string]interface{})
    txnID := data["txnId"].(string)
    fmt.Println(client.Consent.BuildUniversalLink(txnID))

    tx, _ := client.DataShare.GetTransaction(txnID)
    userInfo, _ := client.Signature.VerifyJWS(tx["userInfo"].(string), c06PublicKeyPEM)
    fmt.Println(userInfo["fullName"])
}
```

### eID chip verification

```go
client := vneid.NewClient(
    vneid.WithAPIKey("..."),
    vneid.WithLicenseID("LIC-HTC-0001"),
    vneid.WithDeviceType("WEB"),
    vneid.WithCSKPrivateKey(cskPEM),
)
res, err := client.Eid.VerifyChip("001099008060", "<base64>", "hanoi", nil)
```

## Resources

`Eid`, `DataShare`, `Biometric`, `VerifyInfo`, `Organization`, `Sso`, `Cts`, `Consent`.

See the repository [`docs/`](../../docs) for full documentation.
