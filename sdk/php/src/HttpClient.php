<?php

namespace HiTechCloud\VNeID;

use GuzzleHttp\Client;
use GuzzleHttp\Exception\RequestException;
use GuzzleHttp\Exception\ConnectException;

class HttpClient
{
    private Client $client;
    private int $retries;

    public function __construct(string $baseUrl, int $timeout = 30, int $retries = 3, ?array $clientCert = null)
    {
        $this->retries = $retries;
        $config = [
            'base_uri' => rtrim($baseUrl, '/') . '/',
            'timeout' => $timeout,
            'http_errors' => false,
            'headers' => [
                'User-Agent' => 'HiTechCloud-VNeID-PHP-SDK/1.0.0',
                'Accept' => 'application/json',
            ],
        ];
        if ($clientCert !== null) {
            // ['cert' => 'client.crt', 'key' => 'client.key'] (mTLS)
            if (isset($clientCert['cert'])) {
                $config['cert'] = $clientCert['cert'];
            }
            if (isset($clientCert['key'])) {
                $config['ssl_key'] = $clientCert['key'];
            }
        }
        $this->client = new Client($config);
    }

    public function request(string $method, string $path, array $options = []): ?array
    {
        $path = ltrim($path, '/');
        $lastException = null;

        for ($attempt = 0; $attempt <= $this->retries; $attempt++) {
            try {
                $response = $this->client->request($method, $path, $options);
                $status = $response->getStatusCode();

                if ($status === 429) {
                    $retryAfter = (int) ($response->getHeader('Retry-After')[0] ?? 60);
                    if ($attempt < $this->retries) {
                        sleep($retryAfter);
                        continue;
                    }
                    throw new RateLimitException('Rate limit exceeded', $retryAfter);
                }

                if ($status >= 500 && $attempt < $this->retries) {
                    sleep(2 ** $attempt);
                    continue;
                }

                $body = (string) $response->getBody();
                $decoded = $body === '' ? null : json_decode($body, true);

                if ($status >= 400) {
                    $this->raiseError($status, $decoded);
                }
                return $decoded;
            } catch (ConnectException $e) {
                $lastException = $e;
                if ($attempt < $this->retries) {
                    sleep(2 ** $attempt);
                    continue;
                }
                throw new HiTechCloudVNeIDException('Request failed: ' . $e->getMessage());
            } catch (RequestException $e) {
                throw new HiTechCloudVNeIDException('Request failed: ' . $e->getMessage());
            }
        }

        throw new HiTechCloudVNeIDException(
            'Request failed after retries: ' . ($lastException ? $lastException->getMessage() : 'unknown')
        );
    }

    private function raiseError(int $status, ?array $body): void
    {
        $message = $body['description'] ?? $body['message'] ?? $body['error'] ?? "HTTP {$status}";
        $responseCode = $body['responseCode'] ?? null;

        switch ($status) {
            case 400:
                throw new ValidationException($message, $responseCode, $body);
            case 401:
                throw new AuthenticationException($message, $responseCode, $body);
            case 403:
                throw new AuthorizationException($message, $responseCode, $body);
            case 404:
                throw new NotFoundException($message, $responseCode, $body);
            case 501:
                throw new BiometricException($message, $responseCode, $body);
            default:
                if ($status >= 500) {
                    throw new ServerException($message, $status, $responseCode, $body);
                }
                throw new HiTechCloudVNeIDException($message, $status, $responseCode, $body);
        }
    }

    public function postJson(string $path, array $json, array $headers): ?array
    {
        return $this->request('POST', $path, ['json' => $json, 'headers' => $headers]);
    }

    public function postForm(string $path, array $form, array $headers): ?array
    {
        return $this->request('POST', $path, ['form_params' => $form, 'headers' => $headers]);
    }

    public function get(string $path, array $headers): ?array
    {
        return $this->request('GET', $path, ['headers' => $headers]);
    }
}
