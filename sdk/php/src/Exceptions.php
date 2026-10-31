<?php

namespace HiTechCloud\VNeID;

class HiTechCloudVNeIDException extends \Exception
{
    protected $responseCode;
    protected $data;

    public function __construct(string $message = "", int $statusCode = 0, $responseCode = null, $data = null)
    {
        parent::__construct($message, $statusCode);
        $this->responseCode = $responseCode;
        $this->data = $data;
    }

    public function getStatusCode(): int
    {
        return $this->getCode();
    }

    public function getResponseCode()
    {
        return $this->responseCode;
    }

    public function getData()
    {
        return $this->data;
    }
}

class AuthenticationException extends HiTechCloudVNeIDException
{
    public function __construct(string $message = "Authentication failed", $responseCode = null, $data = null)
    {
        parent::__construct($message, 401, $responseCode, $data);
    }
}

class AuthorizationException extends HiTechCloudVNeIDException
{
    public function __construct(string $message = "Access denied", $responseCode = null, $data = null)
    {
        parent::__construct($message, 403, $responseCode, $data);
    }
}

class NotFoundException extends HiTechCloudVNeIDException
{
    public function __construct(string $message = "Not found", $responseCode = null, $data = null)
    {
        parent::__construct($message, 404, $responseCode, $data);
    }
}

class ValidationException extends HiTechCloudVNeIDException
{
    public function __construct(string $message = "Validation error", $responseCode = null, $data = null)
    {
        parent::__construct($message, 400, $responseCode, $data);
    }
}

class RateLimitException extends HiTechCloudVNeIDException
{
    private int $retryAfter;

    public function __construct(string $message = "Rate limit exceeded", int $retryAfter = 60, $responseCode = null, $data = null)
    {
        parent::__construct($message, 429, $responseCode, $data);
        $this->retryAfter = $retryAfter;
    }

    public function getRetryAfter(): int
    {
        return $this->retryAfter;
    }
}

class BiometricException extends HiTechCloudVNeIDException
{
    public function __construct(string $message = "Biometric error", $responseCode = null, $data = null)
    {
        parent::__construct($message, 501, $responseCode, $data);
    }
}

class ServerException extends HiTechCloudVNeIDException
{
    public function __construct(string $message = "Server error", int $statusCode = 500, $responseCode = null, $data = null)
    {
        parent::__construct($message, $statusCode, $responseCode, $data);
    }
}

class SignatureException extends HiTechCloudVNeIDException
{
    public function __construct(string $message = "Signature error")
    {
        parent::__construct($message, 0, null, null);
    }
}
