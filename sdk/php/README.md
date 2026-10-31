# HiTechCloud VNeID SDK — PHP

Official PHP client for the HiTechCloud eKYC — VNeID gateway (PHP 8.0+, Guzzle).

## Install

```bash
composer require hitechcloud-vietnam/vneid-sdk
```

## Usage

```php
use HiTechCloud\VNeID\VNeIDClient;

$client = new VNeIDClient([
    'api_key'     => getenv('VNEID_API_KEY'),
    'target_code' => getenv('VNEID_TARGET_CODE'),
    'sandbox'     => true,
]);

$init = $client->dataShare->init(['citizenPid' => '036188001958']);
$txnId = $init['data']['txnId'];

echo $client->consent->buildUniversalLink($txnId);

// After consent:
$tx = $client->dataShare->getTransaction($txnId);
$userInfo = $client->signature::verifyJws($tx['userInfo'], $c06PublicKeyPem);
echo $userInfo['fullName'];
```

### eID chip verification

```php
$client = new VNeIDClient([
    'api_key'         => '...',
    'license_id'      => 'LIC-HTC-0001',
    'device_type'     => 'WEB',
    'csk_private_key' => file_get_contents('csk-private.key'),
    'client_cert'     => ['cert' => 'partner-client.crt', 'key' => 'partner-client.key'],
]);
$res = $client->eid->verifyChip('001099008060', '<base64>', 'hanoi');
var_dump($res['responds']['exitcode'] === 0);
```

## Resources

`eid`, `dataShare`, `biometric`, `verifyInfo`, `organization`, `sso`, `cts`, `consent`.

See the repository [`docs/`](../../docs) for full documentation.
