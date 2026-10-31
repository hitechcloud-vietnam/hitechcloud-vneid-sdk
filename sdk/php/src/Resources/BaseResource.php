<?php

namespace HiTechCloud\VNeID\Resources;

use HiTechCloud\VNeID\HttpClient;
use HiTechCloud\VNeID\Config;
use HiTechCloud\VNeID\Signature;

class BaseResource
{
    protected HttpClient $http;
    protected Config $config;
    public Signature $signature;

    public function __construct(HttpClient $http, Config $config, Signature $signature)
    {
        $this->http = $http;
        $this->config = $config;
        $this->signature = $signature;
    }

    protected function newRequestId(): string
    {
        $d = random_bytes(16);
        $d[6] = chr((ord($d[6]) & 0x0f) | 0x40);
        $d[8] = chr((ord($d[8]) & 0x3f) | 0x80);
        return vsprintf('%s%s-%s-%s-%s-%s%s%s', str_split(bin2hex($d), 4));
    }

    /** ddMMyyyyHHmmss — VNeID services. */
    protected function vneidRequestTime(): string
    {
        return (new \DateTime('now', new \DateTimeZone('Asia/Ho_Chi_Minh')))->format('dmYHis');
    }

    /** yyyyMMddHHmmss — eID services. */
    protected function eidRequestTime(): string
    {
        return (new \DateTime('now', new \DateTimeZone('Asia/Ho_Chi_Minh')))->format('YmdHis');
    }

    /**
     * @param array{targetCode?:string,requestType?:string,requestId?:string,contentType?:string,bearer?:string,includeTargetCode?:bool} $opts
     */
    protected function vneidHeaders(array $opts = []): array
    {
        $headers = [
            'Content-Type' => $opts['contentType'] ?? 'application/json; charset=utf-8',
            'request_id' => $opts['requestId'] ?? $this->newRequestId(),
            'request_time' => $this->vneidRequestTime(),
        ];
        if ($this->config->apiKey !== null) {
            $headers['api_key'] = $this->config->apiKey;
        }
        if (($opts['includeTargetCode'] ?? true) === true) {
            $tc = $opts['targetCode'] ?? $this->config->targetCode;
            if ($tc !== null) {
                $headers['target_code'] = $tc;
            }
        }
        if (!empty($opts['requestType'])) {
            $headers['request_type'] = $opts['requestType'];
        }
        if (!empty($opts['bearer'])) {
            $headers['Authorization'] = 'Bearer ' . $opts['bearer'];
        }
        return $headers;
    }

    protected function tcdnPath(string $serviceCode, string $suffix): string
    {
        return "/api/tcdn/{$serviceCode}{$suffix}";
    }
}
