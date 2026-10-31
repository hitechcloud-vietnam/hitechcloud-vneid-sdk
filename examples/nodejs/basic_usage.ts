/**
 * Basic usage of the HiTechCloud VNeID SDK (TypeScript).
 * Set VNEID_API_KEY and VNEID_TARGET_CODE in the environment.
 */
import { VNeIDClient, HiTechCloudVNeIDError, RateLimitError } from "@hitechcloud-vietnam/vneid-sdk";

async function main(): Promise<void> {
  const client = new VNeIDClient({
    apiKey: process.env.VNEID_API_KEY ?? "YOUR_API_KEY",
    targetCode: process.env.VNEID_TARGET_CODE ?? "YOUR_TARGET_CODE",
    sandbox: true,
  });

  try {
    // 1) Start a data-share transaction
    const init = await client.dataShare.init({ citizenPid: "036188001958" });
    const txnId = init.data.txnId;
    console.log("txnId:", txnId);

    // 2) Redirect the citizen to VNeID (App-to-App)
    console.log("Open on VNeID:", client.consent.buildUniversalLink(txnId));

    // 3) After the citizen consents, fetch the data
    const tx = await client.dataShare.getTransaction(txnId);

    // 4) Verify the JWS and read the payload
    const c06PublicKey = process.env.C06_PUBLIC_KEY;
    if (c06PublicKey && tx.userInfo) {
      const userInfo = client.signature.verifyJws<{ fullName: string }>(tx.userInfo, c06PublicKey);
      console.log("fullName:", userInfo.fullName);
    }
  } catch (err) {
    if (err instanceof RateLimitError) {
      console.log("Rate limited, retry after", err.retryAfter, "seconds");
    } else if (err instanceof HiTechCloudVNeIDError) {
      console.log("API error:", err.message, "(HTTP", err.statusCode, ")");
    } else {
      throw err;
    }
  }
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
