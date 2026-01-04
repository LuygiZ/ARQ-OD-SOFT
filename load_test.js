import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomString } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const options = {
    stages: [
        { duration: '10s', target: 50 }, // Ramp up to 50 users
        { duration: '30s', target: 50 }, // Stay at 50 users
        { duration: '10s', target: 0 },  // Scale down
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'], // 95% of requests must complete below 2s
        http_req_failed: ['rate<0.05'],    // <5% errors
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
    // 1. Login as Librarian
    const loginPayload = JSON.stringify({
        username: 'maria@gmail.com',
        password: 'Mariaroberta!123',
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const loginRes = http.post(`${BASE_URL}/api/auth/login`, loginPayload, params);

    check(loginRes, {
        'login successful': (r) => r.status === 200,
        'token received': (r) => r.json('token') !== undefined,
    });

    const token = loginRes.json('token');
    if (!token) {
        console.error('Login failed, no token');
        return;
    }

    const authHeaders = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
        },
    };

    // 2. Create Reader
    const readerEmail = `reader_${randomString(8)}@test.com`;
    const readerPayload = JSON.stringify({
        username: readerEmail,
        password: 'Password1!',
        fullName: `Test Reader ${randomString(5)}`,
        interests: ['fiction', 'technology'],
        gdprConsent: true
    });

    const createRes = http.post(`${BASE_URL}/api/readers`, readerPayload, authHeaders);

    check(createRes, {
        'create reader status 201': (r) => r.status === 201,
    });

    sleep(1);
}
