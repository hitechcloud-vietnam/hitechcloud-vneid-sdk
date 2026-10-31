<?php

namespace HiTechCloud\VNeID\Resources;

/** Biometric resource — face matching / liveness (v2/get-transaction). */
class Biometric extends BaseResource
{
    /** POST /api/tcdn/{serviceCode}/v2/api/get-transaction */
    public function getTransactionV2(
        string $serviceCode,
        string $transactionId,
        ?string $faceImage = null,
        string $requestType = 'sdk-liveness-match',
        ?string $targetCode = null
    ): ?array {
        $body = ['transactionId' => $transactionId];
        if ($faceImage !== null) {
            $body['faceImage'] = $faceImage;
        }
        $headers = $this->vneidHeaders(['targetCode' => $targetCode, 'requestType' => $requestType]);
        $path = $this->tcdnPath($serviceCode, '/v2/api/get-transaction');
        return $this->http->postJson($path, $body, $headers);
    }

    /** Convenience for service-code face-matching. */
    public function faceMatching(string $transactionId, string $faceImage, string $requestType = 'sdk-liveness-match', ?string $targetCode = null): ?array
    {
        return $this->getTransactionV2('face-matching', $transactionId, $faceImage, $requestType, $targetCode);
    }

    /** Convenience for service-code biometric-share-info. */
    public function shareWithBiometric(string $transactionId, string $faceImage, string $requestType = 'sdk-liveness-match', ?string $targetCode = null): ?array
    {
        return $this->getTransactionV2('biometric-share-info', $transactionId, $faceImage, $requestType, $targetCode);
    }
}
