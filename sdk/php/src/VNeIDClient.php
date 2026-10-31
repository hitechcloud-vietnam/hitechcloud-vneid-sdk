<?php

namespace HiTechCloud\VNeID;

use HiTechCloud\VNeID\Resources\Eid;
use HiTechCloud\VNeID\Resources\DataShare;
use HiTechCloud\VNeID\Resources\Biometric;
use HiTechCloud\VNeID\Resources\VerifyInfo;
use HiTechCloud\VNeID\Resources\Organization;
use HiTechCloud\VNeID\Resources\Sso;
use HiTechCloud\VNeID\Resources\Cts;
use HiTechCloud\VNeID\Resources\Consent;

/**
 * HiTechCloud eKYC — VNeID client.
 *
 * Options: api_key, target_code, base_url, sandbox, timeout, retries,
 *          license_id, device_type, csk_private_key, client_cert (['cert'=>,'key'=>]).
 */
class VNeIDClient
{
    public const PRODUCTION_URL = 'https://ekyc-c06.hitechcloud.vn';
    public const SANDBOX_URL = 'https://ekyc-c06-sandbox.hitechcloud.vn';

    private HttpClient $http;
    private Config $config;
    private string $baseUrl;

    public Signature $signature;
    public Eid $eid;
    public DataShare $dataShare;
    public Biometric $biometric;
    public VerifyInfo $verifyInfo;
    public Organization $organization;
    public Sso $sso;
    public Cts $cts;
    public Consent $consent;

    public function __construct(array $options = [])
    {
        $sandbox = $options['sandbox'] ?? false;
        $this->baseUrl = $options['base_url'] ?? ($sandbox ? self::SANDBOX_URL : self::PRODUCTION_URL);

        $this->config = new Config(
            $options['api_key'] ?? null,
            $options['target_code'] ?? null,
            $options['license_id'] ?? null,
            $options['device_type'] ?? null,
            $options['csk_private_key'] ?? null
        );
        $this->http = new HttpClient(
            $this->baseUrl,
            $options['timeout'] ?? 30,
            $options['retries'] ?? 3,
            $options['client_cert'] ?? null
        );
        $this->signature = new Signature();

        $this->eid = new Eid($this->http, $this->config, $this->signature);
        $this->dataShare = new DataShare($this->http, $this->config, $this->signature);
        $this->biometric = new Biometric($this->http, $this->config, $this->signature);
        $this->verifyInfo = new VerifyInfo($this->http, $this->config, $this->signature);
        $this->organization = new Organization($this->http, $this->config, $this->signature);
        $this->sso = new Sso($this->http, $this->config, $this->signature);
        $this->cts = new Cts($this->http, $this->config, $this->signature);
        $this->consent = new Consent($this->http, $this->config, $this->signature);
    }

    public function getBaseUrl(): string
    {
        return $this->baseUrl;
    }
}
