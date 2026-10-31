"""Basic usage of the HiTechCloud VNeID SDK (Python).

Run against the sandbox. Set VNEID_API_KEY and VNEID_TARGET_CODE in the environment.
No real network call is made unless you supply valid credentials.
"""

import os

from hitechcloud_vneid import VNeIDClient
from hitechcloud_vneid.exceptions import HiTechCloudVNeIDError, RateLimitError


def main():
    client = VNeIDClient(
        api_key=os.environ.get("VNEID_API_KEY", "YOUR_API_KEY"),
        target_code=os.environ.get("VNEID_TARGET_CODE", "YOUR_TARGET_CODE"),
        sandbox=True,
    )

    try:
        # 1) Start a data-share transaction
        init = client.data_share.init(citizen_pid="036188001958")
        txn_id = init["data"]["txnId"]
        print("txnId:", txn_id)

        # 2) Redirect the citizen to VNeID (App-to-App)
        print("Open on VNeID:", client.consent.build_universal_link(txn_id))

        # 3) After the citizen consents, fetch the data
        tx = client.data_share.get_transaction(txn_id)

        # 4) Verify the JWS and read the payload
        c06_public_key = os.environ.get("C06_PUBLIC_KEY", "")
        if c06_public_key and tx.get("userInfo"):
            user_info = client.signature.verify_jws(tx["userInfo"], c06_public_key)
            print("fullName:", user_info.get("fullName"))

    except RateLimitError as e:
        print("Rate limited, retry after", e.retry_after, "seconds")
    except HiTechCloudVNeIDError as e:
        print("API error:", e.message, "(HTTP", e.status_code, ")")


if __name__ == "__main__":
    main()
