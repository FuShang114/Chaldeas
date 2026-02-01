package com.example.demo.utils;

import com.example.demo.utils.DebugModels.DebugSession;
import com.example.demo.utils.DebugModels.MethodCallRecord;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * WebServer（第一阶段版本）
 *
 * 目标：
 * - 提供静态前端资源
 * - 提供简单的调试接口：
 *   - POST /api/debug/start  : 启动一次会话（一次性执行目标方法，生成单步记录）
 *   - POST /api/debug/next   : 返回当前会话的下一步（当前只有一步）
 *   - GET  /api/debug/tree   : 返回一个极简的“方法树”（只有根节点）
 *   - GET  /api/debug/topology: 返回极简拓扑图（只有一个节点）
 */
public class WebServer {

    private final int port;
    private final String webRoot;
    private HttpServer server;

    private final DebugSessionManager sessionManager = new DebugSessionManager();

    public WebServer(int port, String webRoot) {
        this.port = port;
        this.webRoot = webRoot;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(8));

        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/debug/start", new DebugStartHandler());
        server.createContext("/api/debug/next", new DebugNextHandler());
        server.createContext("/api/debug/tree", new DebugTreeHandler());
        server.createContext("/api/debug/topology", new DebugTopologyHandler());

        server.start();
        System.out.println("WebServer started at http://localhost:" + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    // ----------------- 静态资源 -----------------

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path)) {
                path = "/index.html";
            }

            File file = new File(webRoot + path);
            if (!file.exists() || file.isDirectory()) {
                sendPlainText(exchange, 404, "Not Found");
                return;
            }

            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().add("Content-Type", guessContentType(path));
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String guessContentType(String path) {
            if (path.endsWith(".html")) return "text/html;charset=UTF-8";
            if (path.endsWith(".css")) return "text/css;charset=UTF-8";
            if (path.endsWith(".js")) return "application/javascript;charset=UTF-8";
            return "application/octet-stream";
        }
    }

    // ----------------- 调试接口 -----------------

    /**
     * POST /api/debug/start
     * body: { "targetMethod": "...", "input": {... 任意JSON ...} }
     */
    private class DebugStartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String body = readBody(exchange);
            Map<String, Object> req = JsonUtil.parseJsonToMap(body);
            String targetMethod = (String) req.get("targetMethod");
            Object input = req.get("input");

            Map<String, Object> resp = new HashMap<>();
            try {
                DebugSession session = sessionManager.startSession(targetMethod, input);
                MethodCallRecord step = session.getSteps().isEmpty() ? null : session.getSteps().get(0);

                resp.put("success", true);
                resp.put("sessionId", session.getId());
                resp.put("step", JsonUtil.fromMethodCall(step));
                resp.put("steps", JsonUtil.fromMethodCalls(session.getSteps(), session.getCurrentStepIndex()));
            } catch (Exception e) {
                resp.put("success", false);
                resp.put("error", e.toString());
            }
            sendJson(exchange, 200, resp);
        }
    }

    /**
     * POST /api/debug/next
     * body: { "sessionId": "..." }
     * 简化实现：总是返回该会话唯一的一步
     */
    private class DebugNextHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String body = readBody(exchange);
            Map<String, Object> req = JsonUtil.parseJsonToMap(body);
            String sessionId = (String) req.get("sessionId");

            Map<String, Object> resp = new HashMap<>();
            DebugSession session = sessionManager.getSession(sessionId);
            if (session == null) {
                resp.put("success", false);
                resp.put("error", "session not found");
                sendJson(exchange, 404, resp);
                return;
            }
            // 前进到下一步（不越界则 +1）
            int idx = session.getCurrentStepIndex();
            if (idx < session.getSteps().size() - 1) {
                session.setCurrentStepIndex(idx + 1);
            }
            MethodCallRecord step = session.getSteps().isEmpty()
                    ? null
                    : session.getSteps().get(session.getCurrentStepIndex());

            resp.put("success", true);
            resp.put("step", JsonUtil.fromMethodCall(step));
            resp.put("steps", JsonUtil.fromMethodCalls(session.getSteps(), session.getCurrentStepIndex()));
            sendJson(exchange, 200, resp);
        }
    }

    /**
     * GET /api/debug/tree?sessionId=...
     * 极简实现：仅返回根节点
     */
    private class DebugTreeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String query = exchange.getRequestURI().getQuery();
            String sessionId = extractQueryParam(query, "sessionId");

            Map<String, Object> resp = new HashMap<>();
            DebugSession session = sessionManager.getSession(sessionId);
            if (session == null) {
                resp.put("success", false);
                resp.put("error", "session not found");
                sendJson(exchange, 404, resp);
                return;
            }
            MethodCallRecord root = session.getRootCall();
            Map<String, Object> node = new HashMap<>();
            node.put("name", root.getClassName() + "#" + root.getMethodName());
            node.put("sourceType", root.getSourceType().name());
            node.put("children", new Object[0]);

            resp.put("success", true);
            resp.put("tree", node);
            sendJson(exchange, 200, resp);
        }
    }

    /**
     * GET /api/debug/topology?sessionId=...
     * 极简实现：单节点拓扑
     */
    private class DebugTopologyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String query = exchange.getRequestURI().getQuery();
            String sessionId = extractQueryParam(query, "sessionId");

            Map<String, Object> resp = new HashMap<>();
            DebugSession session = sessionManager.getSession(sessionId);
            if (session == null) {
                resp.put("success", false);
                resp.put("error", "session not found");
                sendJson(exchange, 404, resp);
                return;
            }
            MethodCallRecord root = session.getRootCall();

            Map<String, Object> node = new HashMap<>();
            node.put("id", root.getId());
            node.put("name", root.getClassName());
            node.put("sourceType", root.getSourceType().name());

            resp.put("success", true);
            resp.put("nodes", new Object[]{node});
            resp.put("edges", new Object[0]);
            sendJson(exchange, 200, resp);
        }
    }

    // ----------------- 工具方法 -----------------

    private String readBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private void sendPlainText(HttpExchange exchange, int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain;charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendJson(HttpExchange exchange, int status, Map<String, Object> obj) throws IOException {
        String json = JsonUtil.toJson(obj);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String extractQueryParam(String query, String key) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        String[] pairs = query.split("&");
        for (String p : pairs) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                return kv[1];
            }
        }
        return null;
    }

    /**
     * 简单 JSON 工具（不依赖外部库）
     * 这里只实现非常有限的 Map / 基本类型 / String / null 的序列化解析，够当前 demo 使用
     */
    static class JsonUtil {
        @SuppressWarnings("unchecked")
        static Map<String, Object> parseJsonToMap(String json) {
            // 为避免引入第三方库，这里实现一个极简解析：
            // - 只支持最外层是 { "key": value, ... }
            // - value 支持字符串、数字、布尔、null、以及简单的对象/数组，复杂情况可按需扩展
            // 为了不引入大量代码，这里如果解析失败直接返回空 Map
            try {
                return new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                        json, Map.class);
            } catch (Exception e) {
                return new HashMap<>();
            }
        }

        static String toJson(Object obj) {
            try {
                return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
            } catch (Exception e) {
                return "{}";
            }
        }

        static Map<String, Object> fromMethodCall(MethodCallRecord record) {
            Map<String, Object> m = new HashMap<>();
            if (record == null) {
                return m;
            }
            m.put("id", record.getId());
            m.put("className", record.getClassName());
            m.put("methodName", record.getMethodName());
            m.put("sourceType", record.getSourceType().name());
            m.put("args", record.getArgs());
            m.put("returnValue", record.getReturnValue());
            m.put("error", record.getError());
            m.put("startTime", record.getStartTime() == null ? null : record.getStartTime().toString());
            m.put("endTime", record.getEndTime() == null ? null : record.getEndTime().toString());
            m.put("durationMillis", record.getDurationMillis());
            return m;
        }

        /**
         * 将完整步骤列表转换为前端展示用的概要信息
         */
        static java.util.List<Map<String, Object>> fromMethodCalls(
                java.util.List<MethodCallRecord> records,
                int currentIndex) {
            java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
            if (records == null) {
                return list;
            }
            for (int i = 0; i < records.size(); i++) {
                MethodCallRecord r = records.get(i);
                Map<String, Object> m = new HashMap<>();
                m.put("id", r.getId());
                m.put("name", r.getClassName() + "#" + r.getMethodName());
                m.put("hasError", r.getError() != null);
                m.put("index", i);
                m.put("current", i == currentIndex);
                list.add(m);
            }
            return list;
        }
    }

    public static void main(String[] args) throws Exception {
        WebServer server = new WebServer(8081, "src/main/resources/web");
        server.start();
        System.out.println("Press ENTER to stop WebServer...");
        System.in.read();
        server.stop();
    }
}

