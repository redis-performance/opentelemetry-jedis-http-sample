import http from 'k6/http';

const url = 'http://localhost:7777';

export const options = {
    duration: '30s',
    vus: 50,
    thresholds: {
        // http errors should be less than 1%
        http_req_failed: ['rate<0.01'],
        // 95 percent of response times must be below 500ms
        http_req_duration: ['p(95)<500'],
    },
};

export function setup() {
    console.log("Preloading with user data");
    let data = { Name: 'John Doe', Username: 'johndoe', About: 'Redis Geek' };

    let res = http.post(url + '/author/1', JSON.stringify(data), {
        headers: { 'Content-Type': 'application/json' },
    });
}

export default function () {
    const res = http.get(url + '/author/1');
}