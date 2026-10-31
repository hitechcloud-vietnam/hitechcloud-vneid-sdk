<?php
/**
 * Basic usage of the HiTechCloud VNeID SDK (PHP).
 * Set VNEID_API_KEY and VNEID_TARGET_CODE in the environment.
 */

require __DIR__ . '/../../sdk/php/vendor/autoload.php';

use HiTechCloud\VNeID\VNeIDClient;
use HiTechCloud\VNeID\HiTechCloudVNeIDException;
use HiTechCloud\VNeID\RateLimitException;

$client = new VNeIDClient([
    'api_key'     => getenv('VNEID_API_KEY') ?: 'YOUR_API_KEY',
    'target_code' => getenv('VNEID_TARGET_CODE') ?: 'YOUR_TARGET_CODE',
    'sandbox'     => true,
]);

try {
    // 1) Start a data-share transaction
    $init = $client->dataShare->init(['citizenPid' => '036188001958']);
    $txnId = $init['data']['txnId'];
    echo "txnId: {$txnId}\n";

    // 2) Redirect the citizen to VNeID (App-to-App)
    echo 'Open on VNeID: ' . $client->consent->buildUniversalLink($txnId) . "\n";

    // 3) After the citizen consents, fetch the data
    $tx = $client->dataShare->getTransaction($txnId);

    // 4) Verify the JWS and read the payload
    $c06PublicKey = getenv('C06_PUBLIC_KEY') ?: '';
    if ($c06PublicKey && !empty($tx['userInfo'])) {
        $userInfo = $client->signature::verifyJws($tx['userInfo'], $c06PublicKey);
        echo 'fullName: ' . ($userInfo['fullName'] ?? '') . "\n";
    }
} catch (RateLimitException $e) {
    echo 'Rate limited, retry after ' . $e->getRetryAfter() . " seconds\n";
} catch (HiTechCloudVNeIDException $e) {
    echo 'API error: ' . $e->getMessage() . ' (HTTP ' . $e->getStatusCode() . ")\n";
}
