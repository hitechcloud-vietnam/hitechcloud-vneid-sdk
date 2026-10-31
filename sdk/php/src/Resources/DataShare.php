<?php

namespace HiTechCloud\VNeID\Resources;

/** Data share resource — share-info service. */
class DataShare extends BaseResource
{
    public const SERVICE_CODE = 'share-info';

    /**
     * POST /api/tcdn/share-info/api/agent/init
     *
     * @param array $params Must include 'citizenPid'; optional 'fullName', 'birthDate'.
     */
    public function init(array $params, ?string $targetCode = null): ?array
    {
        $headers = $this->vneidHeaders(['targetCode' => $targetCode]);
        $path = $this->tcdnPath(self::SERVICE_CODE, '/api/agent/init');
        return $this->http->postJson($path, $params, $headers);
    }

    /** POST /api/tcdn/share-info/api/get-transaction */
    public function getTransaction(string $transactionId, ?string $targetCode = null): ?array
    {
        $headers = $this->vneidHeaders(['targetCode' => $targetCode]);
        $path = $this->tcdnPath(self::SERVICE_CODE, '/api/get-transaction');
        return $this->http->postJson($path, ['transactionId' => $transactionId], $headers);
    }
}
