<?php

namespace HiTechCloud\VNeID\Resources;

use HiTechCloud\VNeID\SignatureException;

/** eID resource — chip CCCD verification (RSA-SHA256 signed). */
class Eid extends BaseResource
{
    /**
     * POST /eid/api/verify/request
     *
     * @param array{requestId?:string,requestTime?:string} $opts
     */
    public function verifyChip(string $idCard, string $dscert, string $diaChi, array $opts = []): ?array
    {
        $cfg = $this->config;
        if ($cfg->licenseId === null || $cfg->deviceType === null || $cfg->cskPrivateKey === null) {
            throw new SignatureException('eID verification requires licenseId, deviceType and cskPrivateKey');
        }
        $requestId = $opts['requestId'] ?? $this->newRequestId();
        $requestTime = $opts['requestTime'] ?? $this->eidRequestTime();

        $signature = $this->signature::signEid(
            $requestId, $requestTime, $cfg->licenseId, $cfg->deviceType,
            $diaChi, $idCard, $dscert, $cfg->cskPrivateKey
        );
        $headers = [
            'content-type' => 'application/json; charset=utf-8',
            'request_id' => $requestId,
            'request_time' => $requestTime,
            'license_id' => $cfg->licenseId,
            'device_type' => $cfg->deviceType,
            'dia_chi' => $diaChi,
            'signature' => $signature,
        ];
        return $this->http->postJson('/eid/api/verify/request', ['id_card' => $idCard, 'dscert' => $dscert], $headers);
    }

    /** Verify the RSA-SHA256 signature of an eID response against the RAR public key. */
    public function verifyResponseSignature(array $response, string $publicKeyPem): bool
    {
        $signingString = json_encode($response['responds'] ?? [], JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        return $this->signature::verifyEidResponse($signingString, $response['signature'] ?? '', $publicKeyPem);
    }
}
