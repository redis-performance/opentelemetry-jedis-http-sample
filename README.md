# OpenTelemetry Java HTTP Service + Jedis example


This demo showcases OpenTelemetry distributed tracing of a sample 
Java HTTP App that uses a Redis backend, 
using W3C TraceContext as the de-facto standard for trace propagation across process boundaries.

This setup relies on Jaeger to receive and visualize traces. 

Make sure to check https://opentelemetry.io/docs/java/ for further reference and examples.
 
## 1 ) How to build and the sample app locally (requires redis accessible on port 6379)
```
git clone github.com/filipecosta90/opentelemetry-java-http-sample
cd opentelemetry-java-http-sample
make
make run
```


## 2 ) Generating sample traces
You should now jave your sample app listening on port :7777, redis on port :6379, 
and Jaeger on ports :14268, :16686.
Let's generate some sample traces:

```
$ curl -X POST -d "{\"Name\":\"John Doe\", \"Username\":\"johndoe\", \"About\":\"Redis Geek\"}" http://localhost:7777/author/1
$ curl -X GET http://localhost:7777/author/1
```

## 3) Go look for your traces!
 
The Jaeger visualization URL is at (notice the port number):

http://[hostname]:16686/search 
 
Put in `sampleHTTPServer` value into the service name, and search for your recent traces!
Click one, and you should see the following:
 
![sample jaeger output](./docs/sample-jaeger-output.jpg)


# Sample Load Testing with k6

In this subsection, we will look into how to integrate performance testing in your development process with GitHub Actions and k6. 

Nonetheless, apart from your CI/CD, if you have installed k6 in your local machine, you can run the benchmarks locally in your terminal using the command:

```
make build-docker
make start-docker
make benchmark
```

Here's the expected output:

```
opentelemetry-jedis-http-sample % make benchmark
k6 run benchmarks/sample-get.js

          /\      |‾‾| /‾‾/   /‾‾/   
     /\  /  \     |  |/  /   /  /    
    /  \/    \    |     (   /   ‾‾\  
   /          \   |  |\  \ |  (‾)  | 
  / __________ \  |__| \__\ \_____/ .io

  execution: local
     script: benchmarks/sample-get.js
     output: -

  scenarios: (100.00%) 1 scenario, 50 max VUs, 1m0s max duration (incl. graceful stop):
           * default: 50 looping VUs for 30s (gracefulStop: 30s)

INFO[0000] Preloading with user data                     source=console

     █ setup

     data_received..................: 101 MB 3.4 MB/s
     data_sent......................: 65 MB  2.2 MB/s
     http_req_blocked...............: avg=576ns   min=0s       med=0s     max=2.66ms  p(90)=1µs    p(95)=1µs   
     http_req_connecting............: avg=103ns   min=0s       med=0s     max=2.65ms  p(90)=0s     p(95)=0s    
   ✓ http_req_duration..............: avg=2.01ms  min=86µs     med=1.97ms max=67.11ms p(90)=3.07ms p(95)=3.36ms
       { expected_response:true }...: avg=2.01ms  min=86µs     med=1.97ms max=67.11ms p(90)=3.07ms p(95)=3.36ms
   ✓ http_req_failed................: 0.00%  ✓ 0            ✗ 738272
     http_req_receiving.............: avg=10.84µs min=3µs      med=10µs   max=1.32ms  p(90)=16µs   p(95)=18µs  
     http_req_sending...............: avg=2µs     min=1µs      med=2µs    max=1.42ms  p(90)=3µs    p(95)=4µs   
     http_req_tls_handshaking.......: avg=0s      min=0s       med=0s     max=0s      p(90)=0s     p(95)=0s    
     http_req_waiting...............: avg=2ms     min=75µs     med=1.96ms max=66.84ms p(90)=3.06ms p(95)=3.35ms
     http_reqs......................: 738272 24603.69322/s
     iteration_duration.............: avg=2.02ms  min=100.41µs med=1.98ms max=68.24ms p(90)=3.08ms p(95)=3.37ms
     iterations.....................: 738271 24603.659894/s
     vus............................: 50     min=50         max=50  
     vus_max........................: 50     min=50         max=50  


running (0m30.0s), 00/50 VUs, 738271 complete and 0 interrupted iterations
default ✓ [======================================] 50 VUs  30s
```

## Configuring your thresholds

Within your k6 script, you have the capability to define SLOs as criteria for passing or failing, complete with predefined thresholds. 

Throughout the test execution, k6 diligently evaluates these criteria and promptly reports back on the results of these threshold assessments. In the event that any of these criteria fail to meet the specified thresholds, k6 will communicate this by returning a non-zero exit code, effectively signaling to your Continuous Integration (CI) tool on GitHub that the associated step has encountered a failure.

This way, you can use the pass/fail conditions to indicate whether the benchmark requirements are met for a specific pull request (PR) on GitHub.

