@echo off
REM 秒杀接口压测 - 梯度2 Arthas监控脚本
REM 目标QPS=100，并发100

echo ====================================
echo 秒杀接口QPS100压测 - Arthas监控
echo 开始时间: %date% %time%
echo ====================================

REM 1. 基础系统监控 - 每5秒收集一次，持续7分钟
echo [INFO] 开始基础系统监控...
dashboard -n 84 > arthas-dashboard-gradient2.log
echo [INFO] 基础监控数据已保存到 arthas-dashboard-gradient2.log

REM 2. 内存监控 - 每10秒收集一次
echo [INFO] 开始内存监控...
memory > arthas-memory-gradient2.log
sleep 10
memory >> arthas-memory-gradient2.log
sleep 10
memory >> arthas-memory-gradient2.log
sleep 10
memory >> arthas-memory-gradient2.log
sleep 10
memory >> arthas-memory-gradient2.log

REM 3. 线程监控 - 每30秒收集一次
echo [INFO] 开始线程监控...
thread > arthas-thread-gradient2.log
sleep 30
thread >> arthas-thread-gradient2.log
sleep 30
thread >> arthas-thread-gradient2.log
sleep 30
thread >> arthas-thread-gradient2.log
sleep 30
thread >> arthas-thread-gradient2.log

REM 4. 秒杀接口方法跟踪
echo [INFO] 开始秒杀接口方法跟踪...
trace com.example.concurrency.service.SeckillService doSeckill -j > arthas-trace-gradient2.log

REM 5. 秒杀控制器监控 - 每5秒监控一次
echo [INFO] 开始秒杀控制器监控...
monitor com.example.concurrency.controller.SeckillController doSeckill -c 5 > arthas-monitor-gradient2.log

REM 6. 订单服务监控
echo [INFO] 开始订单服务监控...
watch com.example.concurrency.service.SeckillService executeSeckill "{params,returnObj,throwExp}" -x 2 -b -f -s > arthas-watch-gradient2.log

echo ====================================
echo Arthas监控完成
echo 结束时间: %date% %time%
echo ====================================

REM 保存所有日志到统一目录
mkdir -p logs-gradient2
move arthas-*-gradient2.log logs-gradient2/

echo [INFO] 所有监控日志已保存到 logs-gradient2/ 目录