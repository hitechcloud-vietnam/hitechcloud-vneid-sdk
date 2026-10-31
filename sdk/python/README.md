# HiTechCloud VNeID SDK — Python

Official Python client for the HiTechCloud eKYC — VNeID gateway.

## Install

```bash
pip install hitechcloud-vneid
```

## Usage

```python
from hitechcloud_vneid import VNeIDClient

client = VNeIDClient(api_key="YOUR_API_KEY", target_code="YOUR_TARGET_CODE", sandbox=True)

# 1) Initialize a data-share transaction
init = client.data_share.init(citizen_pid="036188001958")
txn_id = init["data"]["txnId"]

# 2) Redirect the citizen to VNeID
print(client.consent.build_universal_link(txn_id))

# 3) After consent, fetch and verify the data
tx = client.data_share.get_transaction(txn_id)
user_info = client.signature.verify_jws(tx["userInfo"], c06_public_key_pem)
print(user_info["fullName"], user_info["citizenPid"])
```

### eID chip verification

```python
client = VNeIDClient(
    api_key="YOUR_API_KEY",
    license_id="LIC-HTC-0001",
    device_type="WEB",
    csk_private_key=open("csk-private.key").read(),
    client_cert=("partner-client.crt", "partner-client.key"),  # mTLS
)
res = client.eid.verify_chip(id_card="001099008060", dscert="<base64>", dia_chi="hanoi")
assert res["responds"]["exitcode"] == 0
```

## Resources

`eid`, `data_share`, `biometric`, `verify_info`, `organization`, `sso`, `cts`, `consent`.

## Errors

```python
from hitechcloud_vneid.exceptions import RateLimitError, AuthenticationError

try:
    client.data_share.get_transaction(txn_id)
except RateLimitError as e:
    print("retry after", e.retry_after)
```

See the repository [`docs/`](../../docs) for full documentation.
