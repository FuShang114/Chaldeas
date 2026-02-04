# 秒杀接口QPS100压测 - Arthas批处理命令
# 目标: 100并发，7分钟监控

# 1. 基础系统监控
dashboard -n 84 > arthas-dashboard-gradient2.log

# 2. 内存监控（每10秒收集一次）
memory > arthas-memory1.log
sleep 10000
memory > arthas-memory2.log
sleep 10000
memory > arthas-memory3.log
sleep 10000
memory > arthas-memory4.log
sleep 10000
memory > arthas-memory5.log

# 3. 线程监控（每30秒收集一次）
thread > arthas-thread1.log
sleep 30000
thread > arthas-thread2.log
sleep 30000
thread > arthas-thread3.log
sleep 30000
thread > arthas-thread4.log
sleep 30000
thread > arthas-thread5.log

# 4. 方法跟踪监控
trace com.example.concurrency.service.SeckillService doSeckill -j > arthas-trace-gradient2.log

# 5. 控制器方法监控
monitor com.example.concurrency.controller.SeckillController doSeckill -c 5 > arthas-monitor-gradient2.log

# 6. 方法参数观察
watch com.example.concurrency.service.SeckillService executeSeckill "{params,returnObj,throwExp}" -x 2 -b -f -s > arthas-watch-gradient2.log

# 7. JVM信息收集
jvm > arthas-jvm-gradient2.log

# 8. GC监控
gc > arthas-gc-gradient2.log

# 9. 系统属性查看
sysprop > arthas-sysprop-gradient2.log

# 10. 最后的内存快照
memory > arthas-memory-final.log