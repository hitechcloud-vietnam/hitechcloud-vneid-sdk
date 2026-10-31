<?php

namespace HiTechCloud\VNeID;

/** Client configuration shared with resources. */
class Config
{
    public ?string $apiKey;
    public ?string $targetCode;
    public ?string $licenseId;
    public ?string $deviceType;
    public ?string $cskPrivateKey;

    public function __construct(
        ?string $apiKey = null,
        ?string $targetCode = null,
        ?string $licenseId = null,
        ?string $deviceType = null,
        ?string $cskPrivateKey = null
    ) {
        $this->apiKey = $apiKey;
        $this->targetCode = $targetCode;
        $this->licenseId = $licenseId;
        $this->deviceType = $deviceType;
        $this->cskPrivateKey = $cskPrivateKey;
    }
}
