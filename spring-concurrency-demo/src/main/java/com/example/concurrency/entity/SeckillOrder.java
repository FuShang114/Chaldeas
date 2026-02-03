package com.example.concurrency.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 秒杀订单实体类
 */
@Entity
@Table(name = "seckill_orders")
public class SeckillOrder {
    
    public enum OrderStatus {
        PENDING,    // 待处理
        SUCCESS,    // 成功
        FAILED,     // 失败
        CANCELLED,  // 已取消
        TIMEOUT     // 超时
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String orderNo; // 订单号
    
    @Column(nullable = false)
    private Long userId; // 用户ID
    
    @Column(nullable = false)
    private Long productId; // 商品ID
    
    @Column(nullable = false)
    private Integer quantity; // 购买数量
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount; // 总金额
    
    @Column(nullable = false)
    private LocalDateTime orderTime; // 下单时间
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; // 订单状态
    
    @Column(length = 500)
    private String remark; // 备注
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 构造函数
    public SeckillOrder() {}
    
    public SeckillOrder(String orderNo, Long userId, Long productId, Integer quantity, 
                       BigDecimal totalAmount, OrderStatus status) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.orderTime = LocalDateTime.now();
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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
    
    public String getOrderNo() {
        return orderNo;
    }
    
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public LocalDateTime getOrderTime() {
        return orderTime;
    }
    
    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
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
        SeckillOrder that = (SeckillOrder) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "SeckillOrder{" +
                "id=" + id +
                ", orderNo='" + orderNo + '\'' +
                ", userId=" + userId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", totalAmount=" + totalAmount +
                ", orderTime=" + orderTime +
                ", status=" + status +
                '}';
    }
}