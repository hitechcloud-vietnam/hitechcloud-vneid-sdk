<?php

namespace HiTechCloud\VNeID\Resources;

/** CTS resource — digital certificate (SmartCA) issuance. */
class Cts extends BaseResource
{
    public const RESULT_SERVICE_CODE = 'create-smartca-account';

    /**
     * POST /api/tcdn/{serviceCode}/api/agent/init with CTS fields.
     *
     * @param array{citizenPid:string,certType?:string,shareOrgNumber?:string,feeCode?:string,listOrgCode?:array,keyData?:string,serviceCode?:string,targetCode?:string} $params
     */
    public function init(array $params): ?array
    {
        $body = [
            'citizenPid' => $params['citizenPid'],
            'type' => $params['certType'] ?? 'PER',
        ];
        foreach (['shareOrgNumber', 'listOrgCode', 'feeCode', 'keyData'] as $k) {
            if (array_key_exists($k, $params)) {
                $body[$k] = $params[$k];
            }
        }
        $headers = $this->vneidHeaders(['targetCode' => $params['targetCode'] ?? null]);
        $path = $this->tcdnPath($params['serviceCode'] ?? 'share-info', '/api/agent/init');
        return $this->http->postJson($path, $body, $headers);
    }

    /** POST /api/tcdn/create-smartca-account/agent/account/receive-result */
    public function reportResult(array $result, ?string $targetCode = null): ?array
    {
        $headers = $this->vneidHeaders(['targetCode' => $targetCode]);
        $path = $this->tcdnPath(self::RESULT_SERVICE_CODE, '/agent/account/receive-result');
        return $this->http->postJson($path, $result, $headers);
    }
}
