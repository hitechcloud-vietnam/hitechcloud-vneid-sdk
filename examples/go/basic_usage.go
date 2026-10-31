// Basic usage of the HiTechCloud VNeID SDK (Go).
// Set VNEID_API_KEY and VNEID_TARGET_CODE in the environment.
package main

import (
	"errors"
	"fmt"
	"os"

	vneid "github.com/hitechcloud-vietnam/hitechcloud-vneid-sdk/go"
)

func getenv(k, fallback string) string {
	if v := os.Getenv(k); v != "" {
		return v
	}
	return fallback
}

func main() {
	client := vneid.NewClient(
		vneid.WithAPIKey(getenv("VNEID_API_KEY", "YOUR_API_KEY")),
		vneid.WithTargetCode(getenv("VNEID_TARGET_CODE", "YOUR_TARGET_CODE")),
		vneid.WithSandbox(),
	)

	// 1) Start a data-share transaction
	init, err := client.DataShare.Init(map[string]interface{}{"citizenPid": "036188001958"})
	if err != nil {
		var rl *vneid.RateLimitError
		if errors.As(err, &rl) {
			fmt.Println("Rate limited, retry after", rl.RetryAfter, "seconds")
			return
		}
		fmt.Println("API error:", err)
		return
	}
	data := init["data"].(map[string]interface{})
	txnID := data["txnId"].(string)
	fmt.Println("txnId:", txnID)

	// 2) Redirect the citizen to VNeID (App-to-App)
	fmt.Println("Open on VNeID:", client.Consent.BuildUniversalLink(txnID))

	// 3) After the citizen consents, fetch the data
	tx, err := client.DataShare.GetTransaction(txnID)
	if err != nil {
		fmt.Println("get-transaction error:", err)
		return
	}

	// 4) Verify the JWS and read the payload
	c06PublicKey := os.Getenv("C06_PUBLIC_KEY")
	if c06PublicKey != "" {
		if jws, ok := tx["userInfo"].(string); ok {
			userInfo, verr := client.Signature.VerifyJWS(jws, c06PublicKey)
			if verr == nil {
				fmt.Println("fullName:", userInfo["fullName"])
			}
		}
	}
}
