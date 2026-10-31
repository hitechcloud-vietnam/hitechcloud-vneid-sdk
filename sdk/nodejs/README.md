# HiTechCloud VNeID SDK — Node.js (TypeScript)

Official TypeScript client for the HiTechCloud eKYC — VNeID gateway.

## Install

```bash
npm install @hitechcloud-vietnam/vneid-sdk
```

## Usage

```ts
import { VNeIDClient } from "@hitechcloud-vietnam/vneid-sdk";

const client = new VNeIDClient({
  apiKey: process.env.VNEID_API_KEY!,
  targetCode: process.env.VNEID_TARGET_CODE!,
  sandbox: true,
});

const init = await client.dataShare.init({ citizenPid: "036188001958" });
const txnId = init.data.txnId;

// Redirect the citizen to VNeID
console.log(client.consent.buildUniversalLink(txnId));

// After consent:
const tx = await client.dataShare.getTransaction(txnId);
const userInfo = client.signature.verifyJws(tx.userInfo!, process.env.C06_PUBLIC_KEY!);
console.log(userInfo.fullName, userInfo.citizenPid);
```

### eID chip verification

```ts
const client = new VNeIDClient({
  apiKey: "...",
  licenseId: "LIC-HTC-0001",
  deviceType: "WEB",
  cskPrivateKey: fs.readFileSync("csk-private.key", "utf8"),
  clientCert: {
    cert: fs.readFileSync("partner-client.crt"),
    key: fs.readFileSync("partner-client.key"),
  },
});
const res = await client.eid.verifyChip("001099008060", "<base64>", "hanoi");
console.log(res.responds.exitcode === 0);
```

## Resources

`eid`, `dataShare`, `biometric`, `verifyInfo`, `organization`, `sso`, `cts`, `consent`.

## Build

```bash
npm install && npm run build
```

See the repository [`docs/`](../../docs) for full documentation.
