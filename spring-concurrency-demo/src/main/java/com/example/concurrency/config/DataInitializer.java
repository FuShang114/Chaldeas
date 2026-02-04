package com.example.concurrency.config;

import com.example.concurrency.entity.SeckillProduct;
import com.example.concurrency.repository.SeckillProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据初始化器
 * 应用启动时自动添加测试数据
 * @Lazy 注解用于压测环境优化，按需初始化数据
 */
@Component
@Lazy
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SeckillProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已有数据
        if (productRepository.count() > 0) {
            System.out.println("数据库已有数据，跳过初始化");
            return;
        }

        System.out.println("开始初始化测试数据...");

        // 添加测试秒杀商品
        initSeckillProducts();

        System.out.println("测试数据初始化完成！");
    }

    private void initSeckillProducts() {
        // 商品1: iPhone 15 Pro
        SeckillProduct product1 = new SeckillProduct(
                "IPHONE15PRO",
                "iPhone 15 Pro 128GB 原色钛金属",
                50,                    // 总库存
                699900,               // 秒杀价格 (6999.00元)
                799900,               // 原价 (7999.00元)
                LocalDateTime.now().minusMinutes(30),  // 开始时间：30分钟前
                LocalDateTime.now().plusHours(2)       // 结束时间：2小时后
        );

        // 商品2: MacBook Air
        SeckillProduct product2 = new SeckillProduct(
                "MACBOOKAIR",
                "MacBook Air 13英寸 M2芯片 8GB+256GB",
                20,                    // 总库存
                799900,               // 秒杀价格 (7999.00元)
                899900,               // 原价 (8999.00元)
                LocalDateTime.now().minusMinutes(15),  // 开始时间：15分钟前
                LocalDateTime.now().plusHours(1)       // 结束时间：1小时后
        );

        // 商品3: AirPods Pro
        SeckillProduct product3 = new SeckillProduct(
                "AIRPODSPRO",
                "AirPods Pro (第2代) 配MagSafe充电盒",
                100,                   // 总库存
                139900,               // 秒杀价格 (1399.00元)
                189900,               // 原价 (1899.00元)
                LocalDateTime.now().minusMinutes(10),  // 开始时间：10分钟前
                LocalDateTime.now().plusMinutes(30)    // 结束时间：30分钟后
        );

        // 商品4: iPad Air
        SeckillProduct product4 = new SeckillProduct(
                "IPADAIR",
                "iPad Air 10.9英寸 WiFi 64GB 蓝色",
                30,                    // 总库存
                389900,               // 秒杀价格 (3899.00元)
                439900,               // 原价 (4399.00元)
                LocalDateTime.now().minusMinutes(5),   // 开始时间：5分钟前
                LocalDateTime.now().plusMinutes(50)    // 结束时间：50分钟后
        );

        // 商品5: 小米13 Ultra
        SeckillProduct product5 = new SeckillProduct(
                "XIAOMI13ULTRA",
                "小米13 Ultra 12GB+256GB 黑色",
                80,                    // 总库存
                499900,               // 秒杀价格 (4999.00元)
                599900,               // 原价 (5999.00元)
                LocalDateTime.now().minusMinutes(20),  // 开始时间：20分钟前
                LocalDateTime.now().plusHours(3)      // 结束时间：3小时后
        );

        // 保存所有商品
        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        productRepository.save(product4);
        productRepository.save(product5);

        System.out.println("已添加5个秒杀商品：");
        System.out.println("1. " + product1.getProductName() + " - 库存:" + product1.getTotalStock());
        System.out.println("2. " + product2.getProductName() + " - 库存:" + product2.getTotalStock());
        System.out.println("3. " + product3.getProductName() + " - 库存:" + product3.getTotalStock());
        System.out.println("4. " + product4.getProductName() + " - 库存:" + product4.getTotalStock());
        System.out.println("5. " + product5.getProductName() + " - 库存:" + product5.getTotalStock());
    }
}