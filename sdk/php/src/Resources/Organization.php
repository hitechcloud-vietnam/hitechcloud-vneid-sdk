<?php

namespace HiTechCloud\VNeID\Resources;

/** Organization resource — organization identity (type=ORG). */
class Organization extends BaseResource
{
    public const SERVICE_CODE = 'share-info';

    /**
     * POST /api/tcdn/share-info/api/agent/init with type=ORG.
     *
     * @param array $extra Additional body fields.
     */
    public function init(string $shareOrgNumber, string $citizenPid = '', array $extra = [], ?string $targetCode = null): ?array
    {
        $body = array_merge([
            'citizenPid' => $citizenPid,
            'type' => 'ORG',
            'shareOrgNumber' => $shareOrgNumber,
        ], $extra);
        $headers = $this->vneidHeaders(['targetCode' => $targetCode]);
        $path = $this->tcdnPath(self::SERVICE_CODE, '/api/agent/init');
        return $this->http->postJson($path, $body, $headers);
    }

    /** POST /api/tcdn/share-info/api/get-transaction — org data (orgPid, taxCode, ...). */
    public function getTransaction(string $transactionId, ?string $targetCode = null): ?array
    {
        $headers = $this->vneidHeaders(['targetCode' => $targetCode]);
        $path = $this->tcdnPath(self::SERVICE_CODE, '/api/get-transaction');
        return $this->http->postJson($path, ['transactionId' => $transactionId], $headers);
    }
}
