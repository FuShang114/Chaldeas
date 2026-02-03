package com.example.concurrency.repository;

import com.example.concurrency.entity.SeckillOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 秒杀订单数据访问层
 */
@Repository
public interface SeckillOrderRepository extends JpaRepository<SeckillOrder, Long> {
    
    /**
     * 根据订单号查找订单
     */
    Optional<SeckillOrder> findByOrderNo(String orderNo);
    
    /**
     * 根据用户ID查找订单
     */
    List<SeckillOrder> findByUserId(Long userId);
    
    /**
     * 根据商品ID查找订单
     */
    List<SeckillOrder> findByProductId(Long productId);
    
    /**
     * 根据状态查找订单
     */
    List<SeckillOrder> findByStatus(SeckillOrder.OrderStatus status);
    
    /**
     * 查找指定时间范围内的订单
     */
    List<SeckillOrder> findByOrderTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计用户购买数量
     */
    @Query("SELECT COALESCE(SUM(so.quantity), 0) FROM SeckillOrder so " +
           "WHERE so.userId = :userId AND so.productId = :productId AND so.status = 'SUCCESS'")
    Long countUserPurchases(@Param("userId") Long userId, @Param("productId") Long productId);
    
    /**
     * 统计商品总销售数量
     */
    @Query("SELECT COALESCE(SUM(so.quantity), 0) FROM SeckillOrder so " +
           "WHERE so.productId = :productId AND so.status = 'SUCCESS'")
    Long countTotalSales(@Param("productId") Long productId);
    
    /**
     * 统计订单成功率
     */
    @Query("SELECT COUNT(*) FROM SeckillOrder so WHERE so.productId = :productId AND so.status = 'SUCCESS'")
    Long countSuccessOrders(@Param("productId") Long productId);
    
    /**
     * 查找用户的成功订单
     */
    @Query("SELECT so FROM SeckillOrder so WHERE so.userId = :userId AND so.productId = :productId AND so.status = 'SUCCESS'")
    List<SeckillOrder> findUserSuccessOrders(@Param("userId") Long userId, @Param("productId") Long productId);
    
    /**
     * 统计时间范围内的订单数量
     */
    @Query("SELECT COUNT(so) FROM SeckillOrder so WHERE so.orderTime BETWEEN :startTime AND :endTime")
    Long countOrdersInTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}