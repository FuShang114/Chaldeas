package com.example.concurrency.service;

import com.example.concurrency.entity.SeckillProduct;
import com.example.concurrency.entity.SeckillOrder;
import com.example.concurrency.repository.SeckillOrderRepository;
import com.example.concurrency.repository.SeckillProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 秒杀服务类
 * 实现高并发场景下的库存扣减和订单处理
 */
@Service
@Transactional
public class SeckillService {
    
    @Autowired
    private SeckillProductRepository productRepository;
    
    @Autowired
    private SeckillOrderRepository orderRepository;
    
    @Autowired
    private RedisService redisService;
    
    /**
     * 秒杀下单核心方法
     * 使用乐观锁和Redis分布式锁保证数据一致性
     */
    public SeckillResult doSeckill(Long userId, String productCode, Integer quantity) {
        // 1. 参数验证
        if (userId == null || productCode == null || quantity == null || quantity <= 0) {
            return SeckillResult.failed("参数错误");
        }
        
        // 2. 检查用户购买限制（每人限购1件）
        if (quantity > 1) {
            return SeckillResult.failed("每人限购1件");
        }
        
        // 3. 生成分布式锁key
        String lockKey = "seckill:lock:" + productCode + ":" + userId;
        String lockValue = String.valueOf(System.currentTimeMillis());
        
        try {
            // 4. 尝试获取分布式锁
            boolean locked = redisService.tryLock(lockKey, lockValue, 10000);
            if (!locked) {
                return SeckillResult.failed("请求过于频繁，请稍后再试");
            }
            
            // 5. 获取商品信息
            Optional<SeckillProduct> productOpt = productRepository.findByProductCode(productCode);
            if (!productOpt.isPresent()) {
                return SeckillResult.failed("商品不存在");
            }
            
            SeckillProduct product = productOpt.get();
            
            // 6. 检查商品状态
            if (!product.isInSeckillTime()) {
                return SeckillResult.failed("商品不在秒杀时间内");
            }
            
            if (!product.hasStock()) {
                return SeckillResult.failed("商品已售罄");
            }
            
            // 7. 检查用户是否已经购买过
            long userPurchases = orderRepository.countUserPurchases(userId, product.getId());
            if (userPurchases > 0) {
                return SeckillResult.failed("您已经抢购过该商品");
            }
            
            // 8. 执行库存扣减和订单创建
            return executeSeckill(userId, product, quantity);
            
        } catch (Exception e) {
            return SeckillResult.failed("系统异常：" + e.getMessage());
        } finally {
            // 9. 释放分布式锁
            redisService.releaseLock(lockKey, lockValue);
        }
    }
    
    /**
     * 执行秒杀核心逻辑
     */
    @Transactional
    public SeckillResult executeSeckill(Long userId, SeckillProduct product, Integer quantity) {
        // 1. 使用乐观锁扣减库存
        int affectedRows = productRepository.deductStockWithVersion(
                product.getId(), quantity, product.getVersion());
        
        if (affectedRows == 0) {
            return SeckillResult.failed("库存不足，抢购失败");
        }
        
        // 2. 创建订单
        String orderNo = generateOrderNo();
        SeckillOrder order = new SeckillOrder(
                orderNo, userId, product.getId(), quantity,
                new BigDecimal(product.getSeckillPrice()).divide(new BigDecimal(100)),
                SeckillOrder.OrderStatus.SUCCESS
        );
        
        // 3. 保存订单
        SeckillOrder savedOrder = orderRepository.save(order);
        
        // 4. 更新缓存
        String stockKey = "seckill:stock:" + product.getProductCode();
        redisService.set(stockKey, String.valueOf(product.getAvailableStock() - quantity), 300);
        
        // 5. 记录成功日志
        String successLog = String.format("秒杀成功 - 用户ID:%d, 商品:%s, 订单号:%s", 
                userId, product.getProductName(), orderNo);
        System.out.println(successLog);
        
        return SeckillResult.success(savedOrder);
    }
    
    /**
     * 异步执行秒杀（用于高并发场景）
     */
    @Async("seckillExecutor")
    public CompletableFuture<SeckillResult> doSeckillAsync(Long userId, String productCode, Integer quantity) {
        try {
            SeckillResult result = doSeckill(userId, productCode, quantity);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(SeckillResult.failed("异步执行失败：" + e.getMessage()));
        }
    }
    
    /**
     * 查询商品信息
     */
    @Cacheable(value = "products", key = "#productCode")
    public Optional<SeckillProduct> getProductByCode(String productCode) {
        return productRepository.findByProductCode(productCode);
    }
    
    /**
     * 获取所有活跃的秒杀商品
     */
    public List<SeckillProduct> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }
    
    /**
     * 批量查询商品信息
     */
    public List<SeckillProduct> getProductsByCodes(List<String> productCodes) {
        return productRepository.findAll().stream()
                .filter(p -> productCodes.contains(p.getProductCode()))
                .toList();
    }
    
    /**
     * 查询用户订单
     */
    public List<SeckillOrder> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    /**
     * 获取商品销售统计
     */
    public ProductStats getProductStats(Long productId) {
        Long totalSales = orderRepository.countTotalSales(productId);
        Long successOrders = orderRepository.countSuccessOrders(productId);
        Optional<SeckillProduct> productOpt = productRepository.findById(productId);
        
        if (!productOpt.isPresent()) {
            return null;
        }
        
        SeckillProduct product = productOpt.get();
        return new ProductStats(
                productId,
                product.getProductName(),
                totalSales.intValue(),
                successOrders.intValue(),
                product.getTotalStock(),
                product.getAvailableStock(),
                new BigDecimal(product.getSeckillPrice()).divide(new BigDecimal(100))
        );
    }
    
    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
        return "SK" + timestamp + random;
    }
    
    // 内部静态类
    public static class SeckillResult {
        private boolean success;
        private String message;
        private Object data;
        
        private SeckillResult(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        public static SeckillResult success(Object data) {
            return new SeckillResult(true, "success", data);
        }
        
        public static SeckillResult failed(String message) {
            return new SeckillResult(false, message, null);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
    }
    
    public static class ProductStats {
        private Long productId;
        private String productName;
        private int totalSales;
        private int successOrders;
        private int totalStock;
        private int availableStock;
        private BigDecimal price;
        
        public ProductStats(Long productId, String productName, int totalSales, int successOrders,
                          int totalStock, int availableStock, BigDecimal price) {
            this.productId = productId;
            this.productName = productName;
            this.totalSales = totalSales;
            this.successOrders = successOrders;
            this.totalStock = totalStock;
            this.availableStock = availableStock;
            this.price = price;
        }
        
        // Getters
        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public int getTotalSales() { return totalSales; }
        public int getSuccessOrders() { return successOrders; }
        public int getTotalStock() { return totalStock; }
        public int getAvailableStock() { return availableStock; }
        public BigDecimal getPrice() { return price; }
    }
}