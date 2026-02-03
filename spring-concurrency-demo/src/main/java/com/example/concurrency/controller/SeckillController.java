package com.example.concurrency.controller;

import com.example.concurrency.service.SeckillService;
import com.example.concurrency.service.SeckillService.SeckillResult;
import com.example.concurrency.service.SeckillService.ProductStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 秒杀API控制器
 * 提供高并发测试的RESTful接口
 */
@RestController
@RequestMapping("/api/v1/seckill")
public class SeckillController {
    
    @Autowired
    private SeckillService seckillService;
    
    /**
     * 秒杀接口 - 核心测试接口
     * POST /api/v1/seckill/order
     */
    @PostMapping("/order")
    public ResponseEntity<SeckillResult> doSeckill(
            @RequestParam Long userId,
            @RequestParam String productCode,
            @RequestParam(defaultValue = "1") Integer quantity) {
        
        try {
            SeckillResult result = seckillService.doSeckill(userId, productCode, quantity);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(SeckillResult.failed("服务器内部错误：" + e.getMessage()));
        }
    }
    
    /**
     * 异步秒杀接口 - 用于极高并发测试
     * POST /api/v1/seckill/order/async
     */
    @PostMapping("/order/async")
    public CompletableFuture<ResponseEntity<SeckillResult>> doSeckillAsync(
            @RequestParam Long userId,
            @RequestParam String productCode,
            @RequestParam(defaultValue = "1") Integer quantity) {
        
        return seckillService.doSeckillAsync(userId, productCode, quantity)
                .thenApply(result -> {
                    if (result.isSuccess()) {
                        return ResponseEntity.ok(result);
                    } else {
                        return ResponseEntity.badRequest().body(result);
                    }
                })
                .exceptionally(throwable -> 
                    ResponseEntity.status(500)
                            .body(SeckillResult.failed("异步处理错误：" + throwable.getMessage())));
    }
    
    /**
     * 查询商品信息
     * GET /api/v1/seckill/product/{productCode}
     */
    @GetMapping("/product/{productCode}")
    public ResponseEntity<?> getProduct(@PathVariable String productCode) {
        try {
            return seckillService.getProductByCode(productCode)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(createErrorResponse("查询商品失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取所有活跃商品
     * GET /api/v1/seckill/products
     */
    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts() {
        try {
            List<?> products = seckillService.getActiveProducts();
            return ResponseEntity.ok(createSuccessResponse(products));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(createErrorResponse("获取商品列表失败：" + e.getMessage()));
        }
    }
    
    /**
     * 批量查询商品信息
     * GET /api/v1/seckill/products/batch
     */
    @GetMapping("/products/batch")
    public ResponseEntity<?> getProductsBatch(@RequestParam List<String> productCodes) {
        try {
            List<?> products = seckillService.getProductsByCodes(productCodes);
            return ResponseEntity.ok(createSuccessResponse(products));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(createErrorResponse("批量查询商品失败：" + e.getMessage()));
        }
    }
    
    /**
     * 查询用户订单
     * GET /api/v1/seckill/orders/user/{userId}
     */
    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<?> getUserOrders(@PathVariable Long userId) {
        try {
            List<?> orders = seckillService.getUserOrders(userId);
            return ResponseEntity.ok(createSuccessResponse(orders));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(createErrorResponse("查询用户订单失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取商品销售统计
     * GET /api/v1/seckill/stats/product/{productId}
     */
    @GetMapping("/stats/product/{productId}")
    public ResponseEntity<?> getProductStats(@PathVariable Long productId) {
        try {
            ProductStats stats = seckillService.getProductStats(productId);
            if (stats != null) {
                return ResponseEntity.ok(createSuccessResponse(stats));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(createErrorResponse("获取商品统计失败：" + e.getMessage()));
        }
    }
    
    /**
     * 健康检查接口
     * GET /api/v1/seckill/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(createSuccessResponse(
            "Spring Boot Concurrency Demo is running at " + LocalDateTime.now()
        ));
    }
    
    /**
     * 获取系统状态
     * GET /api/v1/seckill/status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getSystemStatus() {
        try {
            // 简单的系统状态检查
            int activeProducts = seckillService.getActiveProducts().size();
            
            return ResponseEntity.ok(createSuccessResponse(
                "System Status",
                new SystemStatus(
                    "RUNNING",
                    LocalDateTime.now().toString(),
                    activeProducts + " active products",
                    "API v1.0"
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(createErrorResponse("获取系统状态失败：" + e.getMessage()));
        }
    }
    
    /**
     * 压测专用接口 - 快速响应无业务逻辑
     * GET /api/v1/seckill/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
    
    /**
     * 压测专用接口 - 带延迟响应
     * GET /api/v1/seckill/ping/slow
     */
    @GetMapping("/ping/slow")
    public ResponseEntity<String> pingSlow(@RequestParam(defaultValue = "100") int delay) {
        try {
            Thread.sleep(delay);
            return ResponseEntity.ok("pong with " + delay + "ms delay");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(500).body("interrupted");
        }
    }
    
    // 内部辅助方法
    private ApiResponse createSuccessResponse(Object data) {
        return new ApiResponse("SUCCESS", "操作成功", data);
    }
    
    private ApiResponse createSuccessResponse(String message, Object data) {
        return new ApiResponse("SUCCESS", message, data);
    }
    
    private ApiResponse createErrorResponse(String error) {
        return new ApiResponse("ERROR", error, null);
    }
    
    // 内部类定义
    public static class ApiResponse {
        private String status;
        private String message;
        private Object data;
        
        public ApiResponse(String status, String message, Object data) {
            this.status = status;
            this.message = message;
            this.data = data;
        }
        
        // Getters
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
    }
    
    public static class SystemStatus {
        private String status;
        private String timestamp;
        private String products;
        private String version;
        
        public SystemStatus(String status, String timestamp, String products, String version) {
            this.status = status;
            this.timestamp = timestamp;
            this.products = products;
            this.version = version;
        }
        
        // Getters
        public String getStatus() { return status; }
        public String getTimestamp() { return timestamp; }
        public String getProducts() { return products; }
        public String getVersion() { return version; }
    }
}