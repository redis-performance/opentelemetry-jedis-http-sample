package com.redislabs.opentelemetry.jedis.sample;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 *
 * @author sazzadul
 */
public class JavaHttpServer {

  public static final int HTTP_PORT = 7777;

  public static final String REDIS_HOST = "redis";
  public static final int REDIS_PORT = 6379;

  public static final String JAEGER_HOST = "jaeger";
//  public static final int JAEGER_PORT = 14268;
  public static final int JAEGER_PORT = 14250;

  static final TracerProvider tracerProvider = OpenTelemetry.getGlobalTracerProvider();
  static final Tracer tracer = tracerProvider.get("com.redislabs.opentelemetry.jedis.sample.JavaHttpServer");

  public static void main(String[] args) throws Exception {
    setupJaegerExporter();
    HttpServer server = HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
    server.createContext(AuthorHandler.AUTHOR_CONTEXT_PATH, new AuthorHandler());
    server.setExecutor(null); // creates a default executor
    server.start();
    System.out.println("HTTP server started.");
  }

  private static void setupJaegerExporter() {
    // Create a channel towards Jaeger end point
    ManagedChannel jaegerChannel = ManagedChannelBuilder.forAddress(JAEGER_HOST, JAEGER_PORT).usePlaintext().build();
    // Export traces to Jaeger
    // Export traces to Jaeger
    JaegerGrpcSpanExporter jaegerExporter
        = JaegerGrpcSpanExporter.builder()
            .setServiceName("otel-jedis")
            .setChannel(jaegerChannel)
            .setDeadlineMs(30000)
            .build();

    // Set to process the spans by the Jaeger Exporter
    OpenTelemetrySdk.getGlobalTracerManagement().addSpanProcessor(SimpleSpanProcessor.builder(jaegerExporter).build());
  }

  // graceful shutdown
  public static void shutdown() {
    OpenTelemetrySdk.getGlobalTracerManagement().shutdown();
  }

  private static class AuthorHandler implements HttpHandler {

    private static final String AUTHOR_CONTEXT_PATH = "/author/";
    private static final String NAME_KEY = "Name";
    private static final String USERNAME_KEY = "Username";
    private static final String ABOUT_KEY = "About";

    private final JedisPool jedisPool = new JedisPool(REDIS_HOST, REDIS_PORT);

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
//      Span span = tracer.spanBuilder("my span").startSpan();
      try {
//      try (Scope scope = span.makeCurrent()) {
        String requestUri = httpExchange.getRequestURI().toString();
        String param = requestUri.substring(requestUri.indexOf(AUTHOR_CONTEXT_PATH) + AUTHOR_CONTEXT_PATH.length());

        if (param.matches("\\d+")) {
          if ("GET".equals(httpExchange.getRequestMethod())) {
            handleGet(httpExchange, param);
            return;
          } else if ("POST".equals(httpExchange.getRequestMethod())) {
            handlePost(httpExchange, param);
            handleGet(httpExchange, param);
            return;
          }
        }

        handleResponse(httpExchange, 400, "Bad request");

      } catch (IOException ioe) {
        throw ioe;
      } catch (Exception ex) {
//        span.setStatus(StatusCode.ERROR, ex.getMessage());
        handleResponse(httpExchange, 500, "Server error");
//      } finally {
//        span.end();
      }
    }

    private void handleGet(HttpExchange httpExchange, String param) throws IOException {
      Map<String, String> map;
      Span span = tracer.spanBuilder("redis/hgetall").startSpan();
      try (Scope scope = span.makeCurrent();
          Jedis jedis = jedisPool.getResource()) {
        map = jedis.hgetAll(getAuthorKey(param));
      } finally {
        span.end();
      }
      String response = toJson(map).toString();
      handleResponse(httpExchange, 200, response);
    }

    private void handlePost(HttpExchange httpExchange, String param) throws IOException {
      JSONObject object = buildBody(httpExchange.getRequestBody());
      Map<String, String> map = toMap(object);
      Span span = tracer.spanBuilder("redis/hset").startSpan();
      try (Scope scope = span.makeCurrent();
          Jedis jedis = jedisPool.getResource()) {
        jedis.hset(getAuthorKey(param), map);
      } finally {
        span.end();
      }
    }

    private void handleResponse(HttpExchange httpExchange, int code, String response) throws IOException {
      httpExchange.sendResponseHeaders(code, response.length());
      try (OutputStream os = httpExchange.getResponseBody()) {
        os.write(response.getBytes());
      }
    }

    private JSONObject buildBody(InputStream stream) throws IOException {
      StringBuilder builder = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
        String line;
        while ((line = reader.readLine()) != null) {
          builder.append(line);
        }
      }
      return new JSONObject(builder.toString());
    }

    private static Map<String, String> toMap(JSONObject object) {
      Map<String, String> map = new HashMap<>();
      if (object.has(NAME_KEY)) {
        map.put(NAME_KEY, object.getString(NAME_KEY));
      }
      if (object.has(USERNAME_KEY)) {
        map.put(USERNAME_KEY, object.getString(USERNAME_KEY));
      }
      if (object.has(ABOUT_KEY)) {
        map.put(ABOUT_KEY, object.getString(ABOUT_KEY));
      }
      return map;
    }

    private static JSONObject toJson(Map<String, String> map) {
      JSONObject json = new JSONObject();
      if (map.containsKey(NAME_KEY)) {
        json.put(NAME_KEY, map.get(NAME_KEY));
      }
      if (map.containsKey(USERNAME_KEY)) {
        json.put(USERNAME_KEY, map.get(USERNAME_KEY));
      }
      if (map.containsKey(ABOUT_KEY)) {
        json.put(ABOUT_KEY, map.get(ABOUT_KEY));
      }
      return json;
    }

    private static String getAuthorKey(String id) {
      return "author:" + id;
    }

  }

}
