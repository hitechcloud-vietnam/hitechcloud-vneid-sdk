<?php

namespace HiTechCloud\VNeID\Resources;

/** Consent resource — receive-result, withdraw-consent, universal link. */
class Consent extends BaseResource
{
    public const UNIVERSAL_LINK_BASE = 'https://universal.dancuquocgia.com/share';

    /** Return the VNeID universal link for a transaction (App-to-App). */
    public function buildUniversalLink(string $transactionCode): string
    {
        return self::UNIVERSAL_LINK_BASE . '/' . $transactionCode;
    }

    /** POST /api/tcdn/{serviceCode}/agent/account/receive-result */
    public function receiveResult(string $serviceCode, array $body, ?string $targetCode = null): ?array
    {
        $headers = $this->vneidHeaders(['targetCode' => $targetCode]);
        $path = $this->tcdnPath($serviceCode, '/agent/account/receive-result');
        return $this->http->postJson($path, $body, $headers);
    }

    /** POST /api/tcdn/withdraw-consent-result/agent/account/receive-result */
    public function withdrawConsentResult(
        string $requestStatus,
        ?string $reasonRequestCode = null,
        ?string $reasonRequestText = null,
        ?string $targetCode = null
    ): ?array {
        $body = [
            'requestStatus' => $requestStatus,
            'reasonRequestCode' => $reasonRequestCode,
            'reasonRequestText' => $reasonRequestText,
        ];
        $headers = $this->vneidHeaders(['targetCode' => $targetCode]);
        $path = $this->tcdnPath('withdraw-consent-result', '/agent/account/receive-result');
        return $this->http->postJson($path, $body, $headers);
    }
}
