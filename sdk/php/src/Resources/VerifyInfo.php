<?php

namespace HiTechCloud\VNeID\Resources;

/** Verify-info resource — True/False verification against the population DB. */
class VerifyInfo extends BaseResource
{
    public const SERVICE_CODE = 'verify-info';

    /** POST /api/tcdn/verify-info/api/agent/init */
    public function init(array $params, ?string $targetCode = null): ?array
    {
        $headers = $this->vneidHeaders(['targetCode' => $targetCode]);
        $path = $this->tcdnPath(self::SERVICE_CODE, '/api/agent/init');
        return $this->http->postJson($path, $params, $headers);
    }

    /** POST /api/tcdn/verify-info/api/get-transaction */
    public function getTransaction(string $transactionId, ?string $targetCode = null): ?array
    {
        $headers = $this->vneidHeaders(['targetCode' => $targetCode]);
        $path = $this->tcdnPath(self::SERVICE_CODE, '/api/get-transaction');
        return $this->http->postJson($path, ['transactionId' => $transactionId], $headers);
    }
}
