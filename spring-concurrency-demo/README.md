# Spring Boot 高并发接口演示项目

这是一个专为压测和分析设计的Spring Boot高并发接口演示项目，支持各种性能测试和故障排查场景。

## 🚀 项目特性

### 核心功能
- **秒杀系统**：完整的高并发秒杀业务流程
- **分布式锁**：基于Redis的分布式锁实现
- **乐观锁**：数据库层面的并发控制
- **异步处理**：@Async异步任务执行
- **缓存机制**：Redis缓存和Spring Cache
- **健康检查**：完整的健康监控接口

### 压测优化
- **@Lazy最小化启动**：减少非必要组件加载
- **专用线程池**：独立的任务执行器
- **监控接口**：实时性能监控点

## 📦 项目结构

```
spring-concurrency-demo/
├── src/main/java/com/example/concurrency/
│   ├── ConcurrencyDemoApplication.java    # 主启动类 (@Lazy)
│   ├── entity/                           # 实体类
│   │   ├── SeckillProduct.java           # 秒杀商品实体
│   │   └── SeckillOrder.java             # 秒杀订单实体
│   ├── repository/                       # 数据访问层
│   │   ├── SeckillProductRepository.java # 商品数据访问
│   │   └── SeckillOrderRepository.java   # 订单数据访问
│   ├── service/                         # 业务逻辑层
│   │   ├── SeckillService.java          # 秒杀核心服务
│   │   └── RedisService.java            # Redis分布式锁服务
│   ├── controller/                      # RESTful接口
│   │   └── SeckillController.java       # 秒杀API控制器
│   └── config/                         # 配置类
│       ├── DataInitializer.java        # 测试数据初始化
│       └── RedisConfig.java            # Redis配置
├── src/main/resources/
│   └── application.properties          # 应用配置
├── src/test/java/                      # 测试类
│   └── SeckillControllerTest.java     # 接口测试
├── pom.xml                            # Maven配置
└── README.md                          # 项目说明
```

## 🔧 技术栈

- **Spring Boot 2.7.0**
- **Spring Data JPA**
- **Spring Data Redis**
- **H2内存数据库**
- **Lombok (可选)**
- **Maven**

## 🚀 快速开始

### 1. 环境要求
- JDK 8+
- Maven 3.6+
- Redis (可选，默认跳过Redis功能)

### 2. 启动应用

#### 方式一：直接启动
```bash
# 克隆或下载项目到本地
cd spring-concurrency-demo

# 编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run
```

#### 方式二：Maven打包启动
```bash
# 打包
mvn clean package -DskipTests

# 运行jar包
java -jar target/spring-concurrency-demo-1.0.0.jar
```

#### 方式三：IDE启动
直接运行`ConcurrencyDemoApplication`类的main方法

### 3. 验证启动

访问健康检查接口：
```bash
curl http://localhost:8080/api/v1/seckill/health
```

预期响应：
```json
{
  "status": "SUCCESS",
  "message": "操作成功",
  "data": "Spring Boot Concurrency Demo is running at 2024-02-03T14:30:00"
}
```

## 📋 API接口列表

### 核心压测接口

| 接口 | 方法 | 描述 | 压测适用性 |
|------|------|------|------------|
| `/api/v1/seckill/ping` | GET | 健康检查 | ⭐⭐⭐⭐⭐ |
| `/api/v1/seckill/ping/slow` | GET | 延迟响应 | ⭐⭐⭐⭐ |
| `/api/v1/seckill/health` | GET | 详细状态 | ⭐⭐⭐ |
| `/api/v1/seckill/status` | GET | 系统状态 | ⭐⭐⭐ |

### 业务接口

| 接口 | 方法 | 描述 | 参数 |
|------|------|------|------|
| `/api/v1/seckill/order` | POST | 秒杀下单 | userId, productCode, quantity |
| `/api/v1/seckill/order/async` | POST | 异步秒杀 | userId, productCode, quantity |
| `/api/v1/seckill/products` | GET | 获取所有商品 | - |
| `/api/v1/seckill/product/{code}` | GET | 查询商品信息 | productCode |
| `/api/v1/seckill/products/batch` | GET | 批量查询商品 | productCodes (List) |
| `/api/v1/seckill/orders/user/{userId}` | GET | 查询用户订单 | userId |
| `/api/v1/seckill/stats/product/{productId}` | GET | 商品销售统计 | productId |

### 测试数据

应用启动时会自动初始化以下测试商品：

1. **iPhone 15 Pro** (IPHONE15PRO) - 库存1000，秒杀价7999元
2. **MacBook Air M3** (MACBOOK_AIR) - 库存500，秒杀价8999元  
3. **华为Mate 60 Pro** (HUAWEI_MATE60) - 库存2000，秒杀价5999元
4. **小米14 Ultra** (XIAOMI14_ULTRA) - 库存1500，秒杀价4999元
5. **AirPods Pro 3代** (AIRPODS_PRO) - 库存3000，秒杀价1599元

## 🧪 压测示例

### 1. 简单健康检查压测
```bash
# 连续100次请求测试
for i in {1..100}; do
    curl -s http://localhost:8080/api/v1/seckill/ping > /dev/null
    echo "Request $i completed"
done
```

### 2. 秒杀接口压测
```bash
# 单次秒杀测试
curl -X POST "http://localhost:8080/api/v1/seckill/order" \
     -G -d "userId=1001" \
     -d "productCode=IPHONE15PRO" \
     -d "quantity=1"
```

### 3. JMeter脚本示例
```
测试计划: Spring Boot 高并发压测
线程组: 100个线程，Ramp-up时间10秒，循环1次

HTTP请求默认值:
- 服务器名称: localhost
- 端口号: 8080

HTTP请求1: 健康检查
- 路径: /api/v1/seckill/ping
- 方法: GET

HTTP请求2: 秒杀接口  
- 路径: /api/v1/seckill/order
- 方法: POST
- 参数: userId=${userId}, productCode=IPHONE15PRO, quantity=1
```

## 🔍 监控和故障排查

### 1. 健康检查
```bash
# 基本健康检查
curl http://localhost:8080/api/v1/seckill/health

# 系统状态检查
curl http://localhost:8080/api/v1/seckill/status

# 慢接口测试
curl http://localhost:8080/api/v1/seckill/ping/slow?delay=200
```

### 2. 日志监控
应用启动后会输出详细的日志信息：
- 数据库连接状态
- Redis连接状态（如配置了Redis）
- 测试数据初始化结果
- 秒杀操作执行日志

### 3. 常见问题排查

**问题1: 端口占用**
```bash
# 查看端口占用
netstat -ano | findstr :8080

# 修改端口（在application.properties中）
server.port=8081
```

**问题2: Redis连接失败**
应用会自动跳过Redis功能，使用本地锁替代分布式锁。

**问题3: 数据库初始化失败**
检查H2数据库配置，确保JPA设置正确。

## 📊 压测场景推荐

### 场景1: 基础性能测试
- 测试接口: `/api/v1/seckill/ping`
- 并发数: 50-200
- 目标: 验证基础响应性能

### 场景2: 业务逻辑测试  
- 测试接口: `/api/v1/seckill/order`
- 并发数: 10-100
- 目标: 验证秒杀业务正确性

### 场景3: 极限压力测试
- 测试接口: `/api/v1/seckill/order`
- 并发数: 200-1000
- 目标: 发现性能瓶颈

### 场景4: 长时间稳定性测试
- 测试接口: 混合所有接口
- 持续时间: 30分钟-2小时
- 目标: 验证系统稳定性

## 🎯 压测注意事项

1. **@Lazy启动**: 项目已配置@Lazy注解实现最小化启动
2. **数据一致性**: 秒杀接口使用乐观锁保证数据一致性  
3. **限流控制**: 每个用户只能抢购一次
4. **库存保护**: 防止超卖现象
5. **异常处理**: 完善的异常捕获和错误响应

## 📝 开发说明

### 添加新的压测接口
1. 在`SeckillController`中添加新的API方法
2. 使用`@GetMapping`或`@PostMapping`注解
3. 添加相应的业务逻辑
4. 更新测试用例

### 自定义压测参数
在`application.properties`中修改相关配置：
```properties
# 线程池配置
task.executor.core-size=20
task.executor.max-size=100

# 数据库连接池
spring.datasource.hikari.maximum-pool-size=20
```

## 🔗 相关链接

- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [JMeter官方下载](https://jmeter.apache.org/download_jmeter.cgi)
- [Arthas官方文档](https://arthas.aliyun.com/)
- [H2数据库文档](https://www.h2database.com/html/main.html)

---

**注意**: 此项目仅用于学习和测试目的，不适用于生产环境。