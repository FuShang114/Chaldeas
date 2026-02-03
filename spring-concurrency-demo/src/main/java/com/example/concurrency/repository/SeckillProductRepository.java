package com.example.concurrency.repository;

import com.example.concurrency.entity.SeckillProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 秒杀商品数据访问层
 * 包含高并发库存扣减优化
 */
@Repository
public interface SeckillProductRepository extends JpaRepository<SeckillProduct, Long> {
    
    /**
     * 根据商品编码查找商品
     */
    Optional<SeckillProduct> findByProductCode(String productCode);
    
    /**
     * 查找所有激活的秒杀商品
     */
    List<SeckillProduct> findByActiveTrue();
    
    /**
     * 查找指定时间范围内的秒杀商品
     */
    List<SeckillProduct> findByStartTimeBeforeAndEndTimeAfter(LocalDateTime now, LocalDateTime now2);
    
    /**
     * 优化版库存扣减 - 使用乐观锁
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return 影响的记录数
     */
    @Modifying
    @Query("UPDATE SeckillProduct sp SET sp.availableStock = sp.availableStock - :quantity, sp.version = sp.version + 1 " +
           "WHERE sp.id = :productId AND sp.availableStock >= :quantity AND sp.version = :version")
    int deductStockWithVersion(@Param("productId") Long productId, 
                              @Param("quantity") Integer quantity, 
                              @Param("version") Integer version);
    
    /**
     * 批量扣减库存
     */
    @Modifying
    @Query("UPDATE SeckillProduct sp SET sp.availableStock = sp.availableStock - :quantity " +
           "WHERE sp.id IN :productIds AND sp.availableStock >= :quantity")
    int batchDeductStock(@Param("productIds") List<Long> productIds, @Param("quantity") Integer quantity);
    
    /**
     * 恢复库存
     */
    @Modifying
    @Query("UPDATE SeckillProduct sp SET sp.availableStock = sp.availableStock + :quantity " +
           "WHERE sp.id = :productId AND sp.availableStock + :quantity <= sp.totalStock")
    int restoreStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    /**
     * 检查库存是否充足
     */
    @Query("SELECT CASE WHEN sp.availableStock >= :quantity THEN true ELSE false END " +
           "FROM SeckillProduct sp WHERE sp.id = :productId")
    boolean hasEnoughStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    /**
     * 统计活跃商品数量
     */
    @Query("SELECT COUNT(sp) FROM SeckillProduct sp WHERE sp.active = true")
    int countActiveProducts();
    
    /**
     * 查找低库存商品
     */
    @Query("SELECT sp FROM SeckillProduct sp WHERE sp.availableStock <= :threshold AND sp.active = true")
    List<SeckillProduct> findLowStockProducts(@Param("threshold") Integer threshold);
}