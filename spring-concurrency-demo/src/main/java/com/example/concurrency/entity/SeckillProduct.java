package com.example.concurrency.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 秒杀商品实体类
 * 支持高并发库存扣减操作
 */
@Entity
@Table(name = "seckill_products")
public class SeckillProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String productCode; // 商品编码
    
    @Column(nullable = false)
    private String productName; // 商品名称
    
    @Column(nullable = false)
    private Integer totalStock; // 总库存
    
    @Column(nullable = false)
    private Integer availableStock; // 可用库存
    
    @Column(nullable = false)
    private Integer seckillPrice; // 秒杀价格 (分)
    
    @Column(nullable = false)
    private Integer originalPrice; // 原价 (分)
    
    @Column(nullable = false)
    private LocalDateTime startTime; // 秒杀开始时间
    
    @Column(nullable = false)
    private LocalDateTime endTime; // 秒杀结束时间
    
    @Column(nullable = false)
    private Boolean active; // 是否激活
    
    @Column(nullable = false)
    private Integer version; // 版本号，用于乐观锁
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 构造函数
    public SeckillProduct() {}
    
    public SeckillProduct(String productCode, String productName, Integer totalStock, 
                         Integer seckillPrice, Integer originalPrice,
                         LocalDateTime startTime, LocalDateTime endTime) {
        this.productCode = productCode;
        this.productName = productName;
        this.totalStock = totalStock;
        this.availableStock = totalStock;
        this.seckillPrice = seckillPrice;
        this.originalPrice = originalPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = true;
        this.version = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 库存扣减操作
    public boolean deductStock(int quantity) {
        if (quantity <= 0 || quantity > availableStock) {
            return false;
        }
        this.availableStock -= quantity;
        this.updatedAt = LocalDateTime.now();
        return true;
    }
    
    // 库存恢复操作
    public void restoreStock(int quantity) {
        this.availableStock += quantity;
        this.updatedAt = LocalDateTime.now();
    }
    
    // 检查是否在秒杀时间内
    public boolean isInSeckillTime() {
        LocalDateTime now = LocalDateTime.now();
        return active && now.isAfter(startTime) && now.isBefore(endTime);
    }
    
    // 检查是否还有库存
    public boolean hasStock() {
        return availableStock > 0;
    }
    
    // JPA生命周期回调
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getProductCode() {
        return productCode;
    }
    
    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public Integer getTotalStock() {
        return totalStock;
    }
    
    public void setTotalStock(Integer totalStock) {
        this.totalStock = totalStock;
    }
    
    public Integer getAvailableStock() {
        return availableStock;
    }
    
    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }
    
    public Integer getSeckillPrice() {
        return seckillPrice;
    }
    
    public void setSeckillPrice(Integer seckillPrice) {
        this.seckillPrice = seckillPrice;
    }
    
    public Integer getOriginalPrice() {
        return originalPrice;
    }
    
    public void setOriginalPrice(Integer originalPrice) {
        this.originalPrice = originalPrice;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeckillProduct that = (SeckillProduct) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "SeckillProduct{" +
                "id=" + id +
                ", productCode='" + productCode + '\'' +
                ", productName='" + productName + '\'' +
                ", totalStock=" + totalStock +
                ", availableStock=" + availableStock +
                ", seckillPrice=" + seckillPrice +
                ", originalPrice=" + originalPrice +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", active=" + active +
                '}';
    }
}