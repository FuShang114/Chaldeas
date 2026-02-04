# Arthas 监控命令集合

## 🚀 启动 Arthas
```bash
# 如果已经下载了arthas-boot.jar
java -jar arthas-boot.jar

# 或者选择指定进程ID（如果已知）
java -jar arthas-boot.jar <PID>
```

## 📊 基础监控命令

### 1. 实时系统监控
```bash
# JVM仪表板 - 每5秒刷新
dashboard

# 内存监控
memory

# 线程监控
thread

# Top 5 CPU消耗线程
thread -n 5

# 垃圾回收监控
gc

# JVM信息
jvm
```

### 2. 秒杀接口专项监控

#### 监控接口方法执行
```bash
# 监控SeckillController的seckill方法，每5秒统计一次
monitor com.example.concurrency.controller.SeckillController seckill -c 5

# 监控SeckillService的doSeckill方法，每5秒统计一次
monitor com.example.concurrency.service.SeckillService doSeckill -c 5
```

#### 跟踪方法执行时间
```bash
# 跟踪秒杀服务方法执行链路
trace com.example.concurrency.service.SeckillService doSeckill

# 跟踪控制器方法执行链路
trace com.example.concurrency.controller.SeckillController seckill
```

#### 观察方法参数和返回值
```bash
# 观察秒杀服务方法输入输出参数
watch com.example.concurrency.service.SeckillService doSeckill "{params,returnObj}" -x 2

# 观察库存扣减操作
watch com.example.concurrency.repository.SeckillProductRepository deductStockWithVersion "{params,returnObj}" -x 2
```

#### 监控异常
```bash
# 监控秒杀服务方法异常，每5秒统计一次
monitor com.example.concurrency.service.SeckillService doSeckill -c 5 -e
```

## 🎯 性能瓶颈识别

### 1. 慢SQL监控
```bash
# 监控数据库查询
sql

# 查看数据库连接池状态
dashboard
```

### 2. 线程状态分析
```bash
# 查看所有线程状态
thread

# 查看线程堆栈
thread 1

# 查看等待锁的线程
thread | grep BLOCKED
```

### 3. 内存分析
```bash
# 内存详细信息
memory

# 堆内存转储
heapdump /tmp/heap-dump.hprof

# 查看GC详情
gc
```

## 📈 监控数据保存

### 保存监控数据到文件
```bash
# 保存dashboard数据（1000次采样）
dashboard -n 1000 > dashboard.log

# 保存线程信息
thread -n 10 > thread.log

# 保存内存信息
memory > memory.log

# 保存方法追踪数据
trace com.example.concurrency.service.SeckillService doSeckill -j > trace.log

# 保存监控数据
monitor com.example.concurrency.service.SeckillService doSeckill -c 5 > monitor.log
```

### 带时间戳的文件名
```bash
# 创建时间戳文件名
echo "压测开始时间: $(date)" > pressure-test-$(date +%Y%m%d-%H%M%S).log
```

## 🔍 实时分析命令

### 1. 接口响应时间分析
```bash
# 持续跟踪方法执行时间，直到手动停止
trace com.example.concurrency.service.SeckillService doSeckill -n 1000
```

### 2. 参数变化监控
```bash
# 监控userId参数变化
watch com.example.concurrency.service.SeckillService doSeckill "params[0]" -x 2 -f
```

### 3. 异常堆栈分析
```bash
# 查看最近5个异常
monitor com.example.concurrency.service.SeckillService doSeckill -c 5 -e | grep Exception

# 查看指定线程的堆栈
thread 1
```

## 🚨 压测期间监控重点

### 高并发场景监控
1. **数据库连接池**: 查看是否有连接耗尽
2. **线程阻塞**: 查看blocked/waiting线程数量
3. **内存使用**: 监控堆内存使用率
4. **GC频率**: 监控垃圾回收频率和耗时
5. **接口响应时间**: 监控P95/P99响应时间

### 关键指标关注
- **Success Rate**: 成功率应≥99%
- **平均响应时间**: 应<100ms
- **CPU使用率**: 应<80%
- **内存使用率**: 应<80%
- **线程数量**: 不应超过线程池配置

## 📝 监控数据解读

### 正常指标范围
- **QPS**: 预期目标500±50
- **平均RT**: <100ms
- **P95 RT**: <200ms
- **P99 RT**: <500ms
- **CPU**: <80%
- **内存**: <80%
- **GC时间**: <100ms

### 警告指标
- **成功率**: <99%
- **平均RT**: >200ms
- **P99 RT**: >1000ms
- **CPU**: >90%
- **内存**: >90%

### 压测停止条件
- 接口成功率<95%
- 出现OOM错误
- 系统无响应>10秒
- P99响应时间>2000ms
