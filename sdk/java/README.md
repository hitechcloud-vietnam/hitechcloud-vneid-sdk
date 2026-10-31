# HiTechCloud VNeID SDK — Java

Official Java client for the HiTechCloud eKYC — VNeID gateway (Java 11+, Gson).

## Install (Maven)

```xml
<dependency>
    <groupId>vn.hitechcloud</groupId>
    <artifactId>vneid-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

```java
import vn.hitechcloud.vneid.VNeIDClient;
import java.util.Map;

VNeIDClient client = VNeIDClient.builder()
        .apiKey("YOUR_API_KEY")
        .targetCode("YOUR_TARGET_CODE")
        .sandbox(true)
        .build();

Map<String, Object> init = client.dataShare().init(Map.of("citizenPid", "036188001958"));
Map<String, Object> data = (Map<String, Object>) init.get("data");
String txnId = (String) data.get("txnId");

System.out.println(client.consent().buildUniversalLink(txnId));

Map<String, Object> tx = client.dataShare().getTransaction(txnId);
Map<String, Object> userInfo = client.signature().verifyJws((String) tx.get("userInfo"), c06PublicKeyPem);
System.out.println(userInfo.get("fullName"));
```

### eID chip verification

```java
VNeIDClient client = VNeIDClient.builder()
        .apiKey("...")
        .licenseId("LIC-HTC-0001")
        .deviceType("WEB")
        .cskPrivateKey(cskPem)
        .build();
Map<String, Object> res = client.eid().verifyChip("001099008060", "<base64>", "hanoi");
```

## Resources

`eid()`, `dataShare()`, `biometric()`, `verifyInfo()`, `organization()`, `sso()`, `cts()`, `consent()`.

## Build

```bash
mvn -q test
```

See the repository [`docs/`](../../docs) for full documentation.
