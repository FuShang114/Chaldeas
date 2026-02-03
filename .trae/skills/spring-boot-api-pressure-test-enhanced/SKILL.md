---
name: "spring-boot-api-pressure-test-enhanced"
description: "Comprehensive Spring Boot API pressure testing with active questioning, tool deployment guidance, @Lazy startup, performance analysis, and standardized reporting. Invoke when Spring Boot APIs need performance testing with bottleneck identification."
---

# Spring Boot API Pressure Testing Skill (Enhanced Version)

This enhanced skill provides comprehensive Spring Boot RESTful API pressure testing analysis with active questioning capabilities, tool deployment guidance, standardized reporting, and performance bottleneck identification.

## Core Positioning & Applicability

**Target Environment:**
- Java Spring Boot projects (Spring ecosystem, supports @Lazy annotation)
- RESTful API interfaces requiring performance testing + bottleneck identification
- Seckill/Inventory/High-concurrency Query/Create interfaces
- Support single-machine/cluster pressure testing environments

**Core Problem Solving:**
- Missing key parameters before testing leading to unreasonable testing plans (no expected QPS, machine performance parameters)
- Testers haven't deployed JMeter/Arthas tools, lacking clear download deployment guidance
- Pressure testing scenarios don't match actual interface business, lacking targeted indicator statistics
- Only obtaining surface performance metrics, unable to locate JVM/interface underlying bottlenecks
- Full project startup causing pressure testing metric distortion (solved through @Lazy minimalist startup)

**Skill Goal:**
Enable large models to actively inquire key parameters → guide tool deployment → complete pressure testing analysis according to standardized process, output directly executable pressure testing plans/scripts/monitoring commands, and generate visualization reports containing "indicator statistics + bottleneck identification + optimization suggestions", all without requiring manual additional requirement sorting.

## Core Enhanced Capabilities: Pre-Testing Active Questioning Process (Must Execute First)

**Mandatory Rule**: Before starting any pressure testing steps, large models must actively inquire the following key parameters from users in sequence to ensure pressure testing plans match user expectations and actual environments. Do not start subsequent processes if complete parameters are not obtained.

### Questioning Sequence

| Question # | Question Content | Parameter Usage | Example Answer |
|------------|-----------------|----------------|----------------|
| 1 | Please provide the expected QPS target for the pressure testing interface (e.g., seckill interface expected QPS=1000, normal query interface expected QPS=200) | Used to design pressure testing concurrency gradients (e.g., expected QPS=1000, design 100/500/1000/1200 concurrency gradients to verify if targets are met) | Expected QPS=800 |
| 2 | Please provide hardware performance parameters of the pressure testing execution machine (CPU cores/memory size/OS, e.g., 4-core 8GB CentOS 7) | Used to evaluate pressure testing machine load capacity, avoid distortion of pressure testing results due to insufficient machine performance (e.g., 4-core 8GB machine cannot support 2000 concurrency, adjust gradient design accordingly) | 8-core 16GB Ubuntu 22.04 |
| 3 | Please confirm if there are clear business indicator requirements (e.g., oversell rate=0, cache hit rate≥95%, order success rate≥99.9%) | Used to customize pressure testing scenarios and indicator statistics logic, targeted monitoring of core business indicators | Oversell rate≤0.1%, inventory consistency rate=100% |

### Questioning Rules
- **Sequential Questioning**: Strictly follow sequence 1→2→3, ask next question only after user answers the current one
- **Clear Guidance**: Each question needs to explain "parameter usage + example" to reduce user understanding cost
- **Default Value Supplement**: If user has no clear requirements, provide reasonable defaults (e.g., expected QPS default=200; machine performance default=4-core 8GB; business indicators default=interface success rate≥99%)

## Core Enhanced Capabilities: JMeter/Arthas Tool Download Deployment Guidance (Must Pre-Check)

**Mandatory Rule**: Before generating pressure testing scripts/monitoring commands, large models must first check if users have installed JMeter and Arthas. If not installed, output detailed download, installation, and configuration steps to ensure users can quickly complete tool deployment.

### I. JMeter Download Deployment Guidance

#### 1. Prerequisites
- JDK 8/11/17 already installed (recommended JDK 8, best compatibility)
- Confirm JDK environment variables configured (execute `java -version` can normally output version)

#### 2. Download Steps
- Visit JMeter official download address: `https://jmeter.apache.org/download_jmeter.cgi`
- Choose Binary Distribution compressed package (e.g., apache-jmeter-5.6.3.zip), download to pressure testing machine
- Extract compressed package to any directory (e.g., /opt/apache-jmeter-5.6.3)

#### 3. Environment Configuration (Optional, Recommended)
Configure JMeter environment variables (Linux example):
```bash
# Edit environment variable configuration file
vi /etc/profile
# Add following content
export JMETER_HOME=/opt/apache-jmeter-5.6.3
export PATH=$PATH:$JMETER_HOME/bin
# Apply configuration
source /etc/profile
```

**Verify Installation**: Execute `jmeter -v`, output JMeter version information if configuration successful.

#### 4. Startup Instructions
- **GUI Startup** (Windows/Linux with desktop): Execute `jmeter`, directly open JMeter GUI
- **Command Line Startup** (Recommended for pressure testing): Execute `jmeter -n -t pressure_test_script.jmx -l test_results.jtl`, run without GUI

### II. Arthas Download Deployment Guidance

#### 1. Prerequisites
- Target Spring Boot project already started (need configured @Lazy minimalist startup)
- Pressure testing machine and target project machine network connectivity (if remote monitoring)
- JDK 8+ installed (consistent with project JDK version)

#### 2. Download Steps (Choose One of Two Methods)

**Method 1: One-Click Download Startup (Recommended)**
```bash
# Linux/Mac environment
curl -O https://arthas.aliyun.com/arthas-boot.jar
# Windows environment: directly access https://arthas.aliyun.com/arthas-boot.jar download
```

**Method 2: Manual Complete Package Download**
- Visit Arthas official download address: `https://arthas.aliyun.com/download.html`
- Download corresponding version compressed package (e.g., arthas-packaging-3.7.0-bin.zip)
- Extract to any directory (e.g., /opt/arthas)

#### 3. Startup and Connection

**Local Monitoring** (Arthas and project same machine):
```bash
# Start arthas-boot.jar
java -jar arthas-boot.jar
# Select Spring Boot process to monitor (input process number, press Enter)
```

**Remote Monitoring** (Arthas and project different machines):
```bash
# Target project machine startup with parameters added
java -jar your-project.jar --arthas-agent-id=test --arthas-telnet-port=3658 --arthas-http-port=8563
# Local machine connect to remote process
java -jar arthas-boot.jar --target-ip target-machine-ip --telnet-port 3658
```

**Verify Installation**: After startup, input `dashboard`, output JVM real-time monitoring data if successful.

#### 4. Common Commands Description
- `dashboard`: Real-time view JVM memory, CPU, threads, interface response time
- `thread -n 10`: View busiest 10 threads
- `trace class-full-path method-name`: Track interface execution chain response time
- `sql`: Monitor SQL execution status

## 8 Core Execution Steps (Strict Order, Pre-Questions + Tool Deployment Completed)

### Step 1: Project @Lazy Minimalist Startup Configuration (Mandatory Pre-Testing)

**Core Operation**:
Consistent with previous skills, must first add @Lazy annotation to Spring Boot startup class, mask non-pressure testing essential components (MQ/scheduled tasks/third-party services), ensure pressure testing metrics are real and effective.

**Annotation Specification Example**:
```java
@Lazy
@SpringBootApplication
public class SeckillApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class, args);
    }
}
```

**Additional Optimization**:
Add @Lazy annotation to non-pressure testing essential Beans (e.g., @Scheduled scheduled tasks, @FeignClient third-party interfaces), further reduce JVM load.

### Step 2: Target Pressure Testing Interface Implementation Logic Deep Analysis

Large model needs to analyze interface basic information, business process, technical dependencies, clarify interface type (seckill/query/create), provide basis for subsequent pressure testing scenario design and indicator statistics. Analysis content needs to form written records.

**Analysis Content**:
- Interface basic information (address, request method, parameter specification)
- Business process analysis (data flow, business logic, external dependencies)
- Technical dependency analysis (database, cache, MQ, third-party services)
- Performance bottleneck points prediction (potential slow SQL, lock contention, etc.)

### Step 3: Test Environment Real Data Acquisition and Preprocessing

**Core Principle**: Obtain basic business data, data distribution characteristics, baseline performance indicators through project native query interfaces. **Prohibited**: Manual database connection.

**Acquisition Process**:
1. **Business Data Acquisition**: Call project query interfaces to obtain test-related data (users, products, inventory, etc.)
2. **Data Distribution Analysis**: Analyze data characteristics (balance distribution, time distribution, etc.)
3. **Baseline Performance Acquisition**: Obtain normal operation performance indicators
4. **Data Preprocessing**: Generate valid data collection for pressure testing script parameterization design

**Data Validation**: Ensure obtained data authenticity and validity, mark data sources and acquisition time.

### Step 4: Customized Pressure Testing Plan Generation (Combining User Provided QPS/Machine Performance)

Based on user expected QPS, machine performance, interface type, generate standardized pressure testing plan. Core includes:

**Plan Components**:
- Pressure testing concurrency gradient design (e.g., expected QPS=800, design 500/800/1000 concurrency gradients)
- Pressure testing duration (each gradient includes 1-minute warmup + 5-minute pressure testing + 1-minute cooldown)
- Arthas monitoring instruction set (synchronous with pressure testing scenarios)
- Pressure testing stop conditions (stop immediately if interface success rate <95%, OOM occurs)

**Pressure Testing Plan Fixed Template (Already Integrated User Parameters)**:
```
# Pressure Testing Plan - Target Interface: {interface address}
## Basic Parameters (from user input)
- Expected QPS target: {user provided QPS}
- Pressure testing machine performance: {user provided CPU/memory/system}
- Core business indicator requirements: {user provided oversell rate/hit rate etc.}

## Concurrency Gradient Design
| Scenario # | Concurrency | Duration | Core Test Points | Expected QPS |
|------------|-------------|----------|-------------------|--------------|
| 1 | {expectedQPS*0.5} | 7 minutes | Basic performance verification | {expectedQPS*0.5} |
| 2 | {expectedQPS} | 7 minutes | Target QPS compliance verification | {expectedQPS} |
| 3 | {expectedQPS*1.2} | 7 minutes | Extreme pressure testing | {expectedQPS*1.2} |
```

### Step 5: Pressure Testing Script + Arthas Monitoring Command Generation

#### 5.1 Pressure Testing Script Generation
Based on user environment, choose JMeter script or Java native script. Scripts need to include:
- Parameterization design based on real data
- Concurrency control (matching pressure testing plan gradients)
- Automatic statistics of common indicators (QPS/RT/P99) and business indicators (oversell rate/hit rate)
- Detailed execution instructions (command line parameters/output paths)

**JMeter Script Example**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <TestPlan guiclass="TestPlanGui">
    <stringProp name="TestPlan.comments">Pressure Testing Script - Target Interface: /api/v1/seckill</stringProp>
    <boolProp name="TestPlan.functional_mode">false</boolProp>
    <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
    <elementProp name="TestPlan.arguments" elementType="Arguments">
      <collectionProp name="Arguments.arguments">
        <elementProp name="userId" elementType="Argument">
          <stringProp name="Argument.name">userId</stringProp>
          <stringProp name="Argument.value">${__CSVRead(test-data.csv,0)}</stringProp>
        </elementProp>
        <elementProp name="productId" elementType="Argument">
          <stringProp name="Argument.name">productId</stringProp>
          <stringProp name="Argument.value">${__CSVRead(test-data.csv,1)}</stringProp>
        </elementProp>
      </collectionProp>
    </elementProp>
  </TestPlan>
</jmeterTestPlan>
```

#### 5.2 Arthas Monitoring Command Set Generation
Organize commands according to "Basic monitoring→Interface-specific monitoring→Data saving". Example:

```bash
# Basic monitoring (mandatory)
dashboard -n 1000 > dashboard.log  # Collect 1000 dashboard data samples
thread -n 10 -i 2000 > thread.log   # Collect busiest threads every 2 seconds
memory > memory.log                # Collect memory information

# Interface-specific monitoring (seckill interface example)
trace com.seckill.service.SeckillService doSeckill -j > trace.log  # Track order placement method response time
watch com.seckill.dao.SeckillDao updateStock "{params,returnObj}" -x 2 > stock.log  # Monitor inventory deduction
monitor com.seckill.controller.SeckillController seckill -c 5 > monitor.log  # Monitor controller method execution

# Database monitoring (if applicable)
sql > sql.log  # Monitor SQL execution
```

### Step 6: Pressure Testing Execution + Real-time Monitoring (Simultaneous Start)

**Strictly Execute According to Pressure Testing Plan**. Core Requirements:
- **Simultaneous Start**: Pressure testing scripts and Arthas monitoring start simultaneously to ensure data collection consistency
- **Environment Recovery**: After each gradient pressure testing completed, wait for JVM to recover to baseline state (memory/CPU normal)
- **Data Saving**: Immediately save pressure testing indicators and Arthas monitoring logs after each gradient ends

**Execution Process**:
1. **Pre-execution Check**: Verify all tools installed and configured correctly
2. **Simultaneous Start**: Start pressure testing script and Arthas monitoring at the same time
3. **Real-time Monitoring**: Monitor interface success rate, response time, system resources during execution
4. **Environment Recovery**: Wait for system to recover after each gradient
5. **Data Collection**: Collect and save all metrics and logs

### Step 7: Multi-Dimensional Indicator Statistics + Performance Bottleneck Identification

#### 7.1 Indicator Statistics
Compare common performance indicators (QPS/RT/P99/success rate) and business indicators (oversell rate/cache hit rate) across concurrency gradients, verify if user expected targets are achieved.

**Statistics Dimensions**:
- **Common Performance**: QPS, Average RT, P95, P99, Success Rate, Error Rate
- **Business Metrics**: Oversell Rate, Cache Hit Rate, Inventory Consistency, Order Success Rate
- **System Resources**: CPU Usage, Memory Usage, GC Frequency, Thread Count

#### 7.2 Bottleneck Identification
Based on Arthas monitoring data, identify bottlenecks from dimensions like slow SQL, JVM memory, thread blocking, cache penetration, distributed lock contention. Identification results need data support (e.g., SQL response time logs, thread stack information).

**Identification Process**:
1. **Data Analysis**: Analyze collected Arthas monitoring data
2. **Bottleneck Classification**: Categorize bottlenecks by type and severity
3. **Root Cause Analysis**: Identify underlying causes of performance issues
4. **Impact Assessment**: Evaluate impact on overall system performance

### Step 8: Standardized Pressure Testing Analysis Report Generation

Generate Markdown/HTML format report. Core modules include:

**Report Structure**:
1. **Pressure Testing Basic Information** (user provided QPS/machine performance)
2. **Indicator Statistics Summary Table** (comparing all gradients)
3. **Bottleneck Deep Analysis** (with Arthas data screenshots)
4. **Priority-based Optimization Suggestions** (High/Medium/Low)
5. **Appendix** (tool deployment logs/pressure testing raw data)

**Report Template Example**:
```markdown
# Spring Boot API Pressure Testing Analysis Report

## Executive Summary
- **Test Target**: /api/v1/seckill (Seckill Interface)
- **Expected QPS**: 800
- **Test Duration**: 21 minutes (3 gradients × 7 minutes each)
- **Overall Result**: PASSED (target achieved in gradient 2)

## Test Environment
- **Machine**: 8-core 16GB Ubuntu 22.04
- **JDK Version**: OpenJDK 8
- **Database**: MySQL 8.0
- **Startup**: @Lazy minimal startup configured

## Indicator Statistics Summary
| Gradient | Concurrency | QPS | Avg RT(ms) | P99(ms) | Success Rate | Oversell Rate |
|----------|-------------|-----|------------|---------|--------------|---------------|
| 1 | 400 | 380 | 45 | 120 | 99.8% | 0.02% |
| 2 | 800 | 760 | 85 | 200 | 99.2% | 0.05% |
| 3 | 960 | 850 | 150 | 350 | 95.5% | 0.12% |

## Bottleneck Analysis
### High Priority Issues
1. **Database Connection Pool Exhaustion** (Gradient 3)
   - Evidence: `thread` command shows 80+ blocked threads
   - Impact: Success rate drops to 95.5%

2. **Inventory Deduction Bottleneck** (Gradient 2-3)
   - Evidence: `trace` shows SeckillService.doSeckill() taking 60-80ms
   - Impact: Limits maximum QPS to ~850

### Medium Priority Issues
1. **Cache Penetration** (All Gradients)
   - Evidence: Low cache hit rate (78%)
   - Impact: Increased database load

## Optimization Recommendations
### High Priority
1. **Increase Database Connection Pool Size**
   ```properties
   spring.datasource.hikari.maximum-pool-size=50
   ```
2. **Optimize Inventory Deduction Logic**
   - Consider batch processing
   - Implement optimistic locking

### Medium Priority
1. **Enhance Cache Strategy**
   - Implement multi-level caching
   - Add cache warming

## Appendix
- [Arthas Monitoring Logs](./logs/arthas-logs.zip)
- [JMeter Test Results](./results/jmeter-results.zip)
- [Tool Deployment Guide](./deployment/tool-setup.md)
```

## Core Execution Rules (Large Models Must Strictly Follow)

1. **Pre-questioning Rule**: Before obtaining user provided "expected QPS, machine performance, business indicators", prohibited to start subsequent pressure testing steps

2. **Tool Check Rule**: First confirm if user installed JMeter/Arthas. If not installed, prioritize output complete download deployment guidance

3. **@Lazy Startup Rule**: All pressure testing must be executed in @Lazy minimalist startup environment, prohibited to start project fully

4. **Sequential Execution Rule**: Steps 1→8 strictly execute in order, prohibited to skip steps (e.g., don't generate pressure testing plan without analyzing interface logic)

5. **Data Authenticity Rule**: Test data must come from project native query interfaces, prohibited to subjectively fabricate

6. **Report Standardization Rule**: Report must include all parameters provided by users, bottleneck analysis must have Arthas data support

## Large Model Usage Guide (Simplified, for Quick User Start)

1. **Large Model Active Questions**: Large model actively asks you for "expected QPS, machine performance, business indicators". Please answer according to actual requirements.

2. **Tool Installation**: If JMeter/Arthas not installed, large model will output. Complete tool installation detailed download deployment steps according to steps.

3. **Interface Information**: Provide basic information of target pressure testing interface (address/request method/business logic).

4. **Plan Execution**: Large model generates pressure testing plan, scripts, Arthas instructions. You only need to follow guidance for execution.

5. **Report Generation**: After pressure testing completion, large model generates standardized report containing indicators, bottlenecks, optimization suggestions.

## Detailed Tool Usage Guide (Post-Installation Instructions)

### 1. Test Data Preparation

#### 1.1 Data Collection Process
```bash
# Step 1: Get test data from application queries
curl -X GET "http://localhost:8080/api/v1/user/queryAll" > users.json
curl -X GET "http://localhost:8080/api/v1/product/queryAll" > products.json
curl -X GET "http://localhost:8080/api/v1/inventory/queryAll" > inventory.json

# Step 2: Process data for JMeter CSV format
# Extract user IDs for seckill testing
cat users.json | jq '.data[] | .userId' > test-data/userIds.txt
cat products.json | jq '.data[] | .productId' > test-data/productIds.txt
```

#### 1.2 Create JMeter CSV Data File
Create `test-data.csv` in the following format:
```csv
userId,productId,quantity
1001,5001,1
1002,5002,1
1003,5003,1
...
```

### 2. JMeter Script Preparation

#### 2.1 GUI Mode Script Creation (Recommended for Beginners)
```bash
# Start JMeter GUI
jmeter

# Manual creation steps:
# 1. Add Thread Group (configure thread numbers, ramp-up time)
# 2. Add HTTP Request Sampler (configure URL, method, parameters)
# 3. Add CSV Data Set Config (point to test-data.csv)
# 4. Add Response Assertions
# 5. Add Listeners (View Results Tree, Aggregate Report)
```

#### 2.2 Save and Convert to Non-GUI Format
```bash
# Save the test plan as: pressure-test-plan.jmx
# Convert to non-GUI format for command line execution:
jmeter -n -t pressure-test-plan.jmx -l results.jtl -e -o html-report/
```

### 3. Tool Startup Procedures

#### 3.1 JMeter Startup Commands

**GUI Mode (Development/Testing)**:
```bash
# Start JMeter with GUI for script development
jmeter

# Key configuration in GUI:
# Thread Group: Number of Threads (Users), Ramp-up period, Loop count
# HTTP Request: Server Name, Port, Path, Method
# CSV Data Set: Filename (test-data.csv), Variable names (userId,productId)
```

**Non-GUI Mode (Actual Pressure Testing)**:
```bash
# Execute pressure test with console output
jmeter -n -t pressure-test-plan.jmx -l test-results.jtl

# Execute with detailed HTML report generation
jmeter -n -t pressure-test-plan.jmx -l test-results.jtl -e -o html-report/

# Execute with specific results file
jmeter -n -t pressure-test-plan.jmx -l seckill-results.jtl -j jmeter.log
```

#### 3.2 Arthas Startup and Connection

**Local Connection**:
```bash
# Start Arthas and list Java processes
java -jar arthas-boot.jar

# After starting, Arthas will show available processes:
# [1] 1234 com.example.seckill.SeckillApplication
# [2] 5678 com.example.user.UserApplication

# Select process to monitor (enter number):
1

# Alternative: Connect to specific process directly
java -jar arthas-boot.jar 1234
```

**Remote Connection**:
```bash
# On target machine, start application with Arthas agent:
java -jar your-app.jar --arthas-agent-id=pressure-test --arthas-telnet-port=3658 --arthas-http-port=8563

# On monitoring machine, connect to remote:
java -jar arthas-boot.jar --target-ip 192.168.1.100 --telnet-port 3658
```

### 4. Monitoring and Result Viewing

#### 4.1 Arthas Monitoring Commands

**Real-time System Monitoring**:
```bash
# Basic dashboard (refresh every 5 seconds)
dashboard

# Memory monitoring
memory

# Thread monitoring
thread

# CPU monitoring
thread -n 5  # Top 5 CPU consuming threads

# Garbage collection monitoring
gc

# JVM information
jvm
```

**Interface-Specific Monitoring**:
```bash
# Trace method execution time
trace com.seckill.service.SeckillService doSeckill

# Monitor method execution (every 5 seconds)
monitor com.seckill.controller.SeckillController seckill -c 5

# Watch method input/output parameters
watch com.seckill.service.SeckillService doSeckill "{params,returnObj}" -x 2

# Monitor method exceptions
monitor com.seckill.service.SeckillService doSeckill -c 5 -e

# Stack trace for specific thread
thread 1

# Heap dump for memory analysis
heapdump /tmp/heap-dump.hprof
```

#### 4.2 JMeter Result Analysis

**Real-time Monitoring During Test**:
```bash
# View real-time results in console (requires jmeter.log)
tail -f jmeter.log

# Monitor results file (for large tests)
tail -f test-results.jtl
```

**Post-Test Analysis**:
```bash
# View HTML report (open in browser)
# Open: html-report/index.html

# Parse JMeter results with Python (optional)
python3 parse-jmeter-results.py test-results.jtl

# Key metrics to check in HTML report:
# - Average Response Time
# - 95th/99th Percentile Response Time  
# - Throughput (Requests/Second)
# - Error Rate
# - Concurrent Users
```

#### 4.3 Data Collection and Logging

**Save Arthas Monitoring Data**:
```bash
# Save dashboard data
dashboard -n 1000 > dashboard.log

# Save thread information
thread -n 10 > thread.log

# Save memory information
memory > memory.log

# Save method tracing data
trace com.seckill.service.SeckillService doSeckill -j > trace.log

# Save monitoring data to files with timestamps
dashboard > arthas-$(date +%Y%m%d-%H%M%S).log
```

**Combine JMeter and Arthas Data**:
```bash
# Create synchronized timestamp files
echo "Pressure Test Started: $(date)" > sync-log.txt
echo "Arthas Started: $(date)" >> sync-log.txt

# During testing, periodically collect Arthas data:
# Use a cron job or script to capture Arthas snapshots every minute
```

### 5. Troubleshooting Common Issues

#### 5.1 JMeter Issues
- **Out of Memory**: Increase heap size: `jmeter -Xmx4g -n -t test.jmx`
- **Connection Refused**: Check if target service is running and accessible
- **SSL Issues**: Add server cert to JMeter keystore or disable SSL verification

#### 5.2 Arthas Issues
- **Process Not Found**: Ensure target Java process is running with Arthas agent
- **Connection Timeout**: Check network connectivity and firewall settings
- **Memory Issues**: Arthas itself consumes memory; monitor Arthas JVM usage

#### 5.3 Data Synchronization Issues
- **Timestamp Mismatch**: Ensure all tools use same time source (NTP synchronization)
- **Log File Locations**: Use absolute paths for all log files
- **Data Format**: Ensure CSV data matches JMeter variable names exactly

## Summary

**Core Highlights of This Enhanced Skill**:
- **Active Inquiry Capability**: Obtain key parameters before pressure testing, avoid unreasonable pressure testing plans due to information lack
- **Tool Deployment Guidance**: Built-in JMeter/Arthas download, installation, configuration steps. Even beginners can quickly complete deployment.
- **Parameter Adaptability**: Pressure testing concurrency gradients combined with user expected QPS and machine performance design, more aligned with actual needs.
- **Complete Process Closed Loop**: From questioning→tool deployment→pressure testing execution→report generation, forming complete pressure testing analysis closed loop.

## When to Use This Skill

Invoke this skill when:
- Spring Boot APIs need performance pressure testing with bottleneck identification
- Need to understand system performance limits and optimization opportunities
- Require automated pressure testing with JMeter and Arthas tool integration
- Need comprehensive performance analysis including business indicators (oversell rate, etc.)
- Want to generate standardized pressure testing reports with actionable recommendations