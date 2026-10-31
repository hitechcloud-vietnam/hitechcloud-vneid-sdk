<?php

namespace HiTechCloud\VNeID\Tests\Unit;

use HiTechCloud\VNeID\VNeIDClient;
use HiTechCloud\VNeID\Signature;
use PHPUnit\Framework\TestCase;

class VNeIDClientTest extends TestCase
{
    public function testBaseUrlDefaults(): void
    {
        $this->assertSame(VNeIDClient::PRODUCTION_URL, (new VNeIDClient(['api_key' => 'k']))->getBaseUrl());
        $this->assertSame(VNeIDClient::SANDBOX_URL, (new VNeIDClient(['api_key' => 'k', 'sandbox' => true]))->getBaseUrl());
    }

    public function testResourcesPresent(): void
    {
        $c = new VNeIDClient(['api_key' => 'k', 'target_code' => 't']);
        foreach (['eid', 'dataShare', 'biometric', 'verifyInfo', 'organization', 'sso', 'cts', 'consent'] as $name) {
            $this->assertNotNull($c->$name);
        }
    }

    public function testUniversalLink(): void
    {
        $c = new VNeIDClient(['api_key' => 'k']);
        $this->assertSame('https://universal.dancuquocgia.com/share/abc', $c->consent->buildUniversalLink('abc'));
    }

    public function testEidSigningStringOrder(): void
    {
        $s = Signature::buildEidSigningString('rid', '20261101093000', 'LIC', 'WEB', 'hanoi', '001', 'dscert');
        $this->assertSame('rid|20261101093000|LIC|WEB|hanoi|001|dscert', $s);
    }

    public function testAuthorizeUrl(): void
    {
        $c = new VNeIDClient(['api_key' => 'k']);
        $url = $c->sso->buildAuthorizeUrl('https://sso.vneid.gov.vn', 'cid', 'https://app/cb');
        $this->assertStringStartsWith('https://sso.vneid.gov.vn/auth?', $url);
        $this->assertStringContainsString('client_id=cid', $url);
    }
}
