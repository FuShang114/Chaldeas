# 优化缩写版 Spring Boot API 测试技能（含HTML报告模板）
---
name: "spring-boot-api-test"
description: "Spring Boot 带库交互RESTful API专属测试技能，支持**自主获取/构造测试数据**、自动化排障、流程化测试，生成含方法链、可展开详情的标准化HTML测试报告，适配数据依赖接口与简单CRUD接口。"
---a

# Spring Boot API 测试核心技能
## 一、核心定位&适用场景
### 适配环境
Java Spring Boot 项目（支持@Lazy）、带数据库交互的RESTful API、**有数据依赖的复杂接口**/简单CRUD接口测试

### 核心能力
? 自主获取/构造测试数据（优先接口拉取真实数据，无法获取则现造）
? 测试数据双维度验证（流程跑通+边界场景）
? 大模型自验数据有效性（失败先排自身数据问题）
? 轻量启动优化+按序测试+自动化排障
? 生成**可直接浏览器打开**的HTML测试报告（含方法链、可展开测试详情）

### 测试目标
实现「轻量启动→自主造数→按序测试→排障优化→HTML报告」全流程自动化，输出可追溯、可复用的测试结果。

## 二、核心执行规则（必按顺序）
### 核心重点：测试数据自主获取&构造（优先级+双维度）
1. **数据获取优先级**：
   - 有数据依赖接口（如按用户ID查关系表）：**优先调用测试环境查询接口**拉取真实可用数据，基于真实数据构造测试用例；
   - 无法拉取真实数据：主动构造合法数据跑通完整流程；
   - 简单CRUD接口：直接构造测试数据，无需调用查询接口。
2. **测试数据双维度考察**：
   - 基础维度：确保数据能跑通接口完整调用流程（入参合法、依赖数据有效）；
   - 边界维度：覆盖参数极值/空值、分页边界、重复数据、非空校验等边界场景。
3. **数据自验责任**：测试失败时，大模型**必须先校验自身构造/获取的测试数据**（无格式错、范围越界、依赖缺失），确认自身数据无问题后，再判定接口/项目本身问题。

### Step 1：项目轻量启动配置
- 启动类添加`@Lazy`注解实现Bean懒加载，减少非测试组件加载，避免启动耗时/冲突；非核心配置/服务类可追加`@Lazy`；
- 核心代码示例：
  ```java
  @Lazy
  @SpringBootApplication
  public class XxxApplication {
      public static void main(String[] args) {
          SpringApplication.run(XxxApplication.class, args);
      }
  }
  ```
- 目标：快速、无干扰启动测试环境，为后续测试铺路。

### Step 2：基于自主造数设计测试用例
基于「自主获取/构造的测试数据」，按**单例单场景**设计用例，覆盖4类核心场景，且每个用例生成**标准化入参JSON**：
1. 正常场景：参数合规、依赖数据有效，接口正常返回业务数据；
2. 边界场景：参数极值/空值、分页边界（page=0/size=0）、单/多关联数据；
3. 异常场景：非法参数、缺失必传项、权限不足、请求方法错误；
4. 依赖场景：基于真实/构造的关联数据，验证数据链路有效性。

### Step 3：按序执行接口测试（不可违反）
1. 严格按用例编号升序执行，禁止跨号、逆序，禁止跳步执行；
2. 每用例执行后立即记录**完整请求日志**（地址、方法、入参JSON、请求时间、返回值、状态码、方法链）；
3. 执行成功→继续下一个；执行失败→立即进入**排障流程**（Step4），排障后重执行；
4. 全程留存日志，为排障和HTML报告生成提供依据。

### Step 4：自动化排障&数据修正
#### 核心逻辑：先验自身数据→定问题归属→靶向处理→重执行
1. **大模型自验数据**：失败后首先校验自身测试数据，确认无格式、范围、依赖缺失等问题（必做步骤）；
2. **问题归属判定**
   - **自身数据问题（必修正）**：入参格式错、依赖数据无效、参数越界、与库中数据冲突；
   - **接口/项目问题（仅记录）**：空指针、库查询错误、业务逻辑错、权限配置错（非数据导致的结果不匹配）；
3. **靶向处理**
   - 自身数据问题：重新拉取/构造有效数据（如替换无效用户ID为真实ID），修正入参JSON；
   - 接口问题：精准记录**报错信息、报错位置**，分析并标注**潜在告警**（数据丢失、漏存、链路异常等）；
4. **重执行**：修正后重执行该用例，成功则继续；连续3次失败则跳过并记录，进入下一个用例。

### Step 5：测试结果归集
全量用例执行完成后，按执行顺序归集以下核心信息，为HTML报告做准备：
1. 接口基础信息：地址、请求方法、功能描述、测试环境（JDK/数据库/启动方式）、测试时间、通过率；
2. 接口调用**方法链**：核心调用的Controller→Service→DAO→依赖接口/第三方服务；
3. 测试用例全量信息：编号、场景类型、入参JSON、执行状态（无问题/报错/告警）、报错信息/位置、告警信息、接口输出JSON、数据构造/获取方式、修正记录（如有）；
4. 统计信息：总用例数、成功/失败/跳过数、通过率（通过率=成功用例数/总用例数×100%）。

### Step 6：生成标准化HTML测试报告
基于归集结果，**直接复用下方HTML模板**，替换所有`{xxx}`占位符生成报告，报告可直接在浏览器打开，无需额外修改。
#### HTML报告核心要求
- 固定页面结构：接口概览→调用方法链→测试数据列表；
- 测试数据为**可展开卡片**，展开后显示入参JSON、执行状态、报错/告警、接口输出；
- JSON格式自动格式化，报错标红、告警标黄、无问题标绿，视觉层级清晰。

## 三、HTML测试报告模板（AI直接复用，替换{xxx}占位符）
```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{接口地址} - Spring Boot API测试报告</title>
    <style>
        * {margin: 0; padding: 0; box-sizing: border-box; font-family: "Microsoft YaHei", Consolas, monospace;}
        body {padding: 2rem; background: #f5f7fa; color: #333; line-height: 1.6;}
        .container {max-width: 1400px; margin: 0 auto; background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); padding: 2rem;}
        /* 通用标题样式 */
        .page-title {text-align: center; font-size: 1.8rem; color: #2c3e50; margin-bottom: 2rem; padding-bottom: 1rem; border-bottom: 2px solid #eef2f7;}
        .mod-title {font-size: 1.2rem; color: #2c3e50; margin: 1.5rem 0 1rem; display: flex; align-items: center; gap: 0.5rem;}
        .mod-title::before {content: ""; width: 4px; height: 18px; background: #3498db; border-radius: 2px;}
        /* 接口概览样式 */
        .api-info {display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 1rem; padding: 1rem; background: #f8f9fa; border-radius: 6px;}
        .info-item {display: flex; flex-direction: column;}
        .info-label {font-size: 0.9rem; color: #666; font-weight: 500; margin-bottom: 0.3rem;}
        .info-value {font-size: 1rem; color: #2c3e50; font-weight: 600;}
        /* 方法链样式 */
        .method-chain {padding: 1rem; background: #f8f9fa; border-radius: 6px; font-size: 1rem;}
        .chain-item {color: #3498db; font-weight: 600;}
        /* 测试数据列表样式 */
        .case-list {margin-top: 1rem;}
        .case-card {border: 1px solid #eef2f7; border-radius: 6px; margin-bottom: 1rem; overflow: hidden;}
        .case-header {padding: 1rem 1.5rem; cursor: pointer; display: flex; justify-content: space-between; align-items: center; background: #f8f9fa;}
        .case-base {display: flex; align-items: center; gap: 1rem;}
        .case-num {width: 36px; height: 36px; border-radius: 50%; background: #3498db; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 600;}
        .case-scene {font-size: 1rem; font-weight: 500; color: #2c3e50;}
        .case-status {padding: 0.3rem 0.8rem; border-radius: 20px; font-size: 0.85rem; font-weight: 600;}
        .status-ok {background: #e8f5e9; color: #2e7d32;}
        .status-error {background: #ffebee; color: #c62828;}
        .status-warn {background: #fff3e0; color: #f57c00;}
        /* 用例内容样式 */
        .case-body {padding: 0 1.5rem; max-height: 0; overflow: hidden; transition: max-height 0.3s ease;}
        .case-body.open {max-height: 3000px; padding: 1rem 1.5rem 1.5rem; border-top: 1px solid #eef2f7;}
        .case-mod {margin-bottom: 1rem;}
        .case-mod:last-child {margin-bottom: 0;}
        .case-subtitle {font-size: 0.95rem; color: #424242; font-weight: 600; margin-bottom: 0.5rem;}
        .json-box {padding: 1rem; background: #fafafa; border-radius: 4px; border: 1px solid #e0e0e0; font-size: 0.9rem; white-space: pre-wrap; word-wrap: break-word;}
        .error-box {padding: 1rem; background: #ffebee; border-left: 4px solid #c62828; border-radius: 0 4px 4px 0; font-size: 0.9rem;}
        .warn-box {padding: 1rem; background: #fff3e0; border-left: 4px solid #f57c00; border-radius: 0 4px 4px 0; font-size: 0.9rem;}
        .ok-box {padding: 1rem; background: #e8f5e9; border-left: 4px solid #2e7d32; border-radius: 0 4px 4px 0; font-size: 0.9rem; color: #2e7d32;}
        .arrow {transition: transform 0.3s ease; font-size: 1.2rem; color: #666;}
        .arrow.open {transform: rotate(180deg);}
        /* 统计样式 */
        .stat-info {position: fixed; top: 2rem; right: 2rem; background: #fff; padding: 1rem; border-radius: 6px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); min-width: 200px;}
        .stat-title {font-size: 1rem; font-weight: 600; color: #2c3e50; margin-bottom: 0.8rem; padding-bottom: 0.5rem; border-bottom: 1px solid #eef2f7;}
        .stat-item {display: flex; justify-content: space-between; margin-bottom: 0.5rem; font-size: 0.95rem;}
        .stat-key {color: #666;}
        .stat-value {font-weight: 600; color: #2c3e50;}
        .pass-rate {color: #2e7d32; font-size: 1.1rem;}
    </style>
</head>
<body>
    <!-- 悬浮统计面板 -->
    <div class="stat-info">
        <div class="stat-title">测试统计</div>
        <div class="stat-item">
            <span class="stat-key">总用例数：</span>
            <span class="stat-value">{总用例数}</span>
        </div>
        <div class="stat-item">
            <span class="stat-key">成功用例数：</span>
            <span class="stat-value">{成功用例数}</span>
        </div>
        <div class="stat-item">
            <span class="stat-key">失败用例数：</span>
            <span class="stat-value">{失败用例数}</span>
        </div>
        <div class="stat-item">
            <span class="stat-key">跳过用例数：</span>
            <span class="stat-value">{跳过用例数}</span>
        </div>
        <div class="stat-item" style="margin-top: 0.8rem; padding-top: 0.5rem; border-top: 1px dashed #eef2f7;">
            <span class="stat-key">测试通过率：</span>
            <span class="stat-value pass-rate">{通过率}%</span>
        </div>
    </div>

    <div class="container">
        <h1 class="page-title">Spring Boot API 自动化测试报告</h1>

        <!-- 模块1：接口基础信息 -->
        <div>
            <h2 class="mod-title">接口基础信息</h2>
            <div class="api-info">
                <div class="info-item">
                    <span class="info-label">接口地址</span>
                    <span class="info-value">{接口地址}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">请求方法</span>
                    <span class="info-value">{GET/POST/PUT/DELETE}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">接口功能</span>
                    <span class="info-value">{接口功能描述}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">测试环境</span>
                    <span class="info-value">JDK{版本}/{数据库类型} {版本}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">测试时间</span>
                    <span class="info-value">{测试时间}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">启动方式</span>
                    <span class="info-value">Spring Boot @Lazy 轻量启动</span>
                </div>
            </div>
        </div>

        <!-- 模块2：接口调用方法链 -->
        <div>
            <h2 class="mod-title">接口核心调用方法链</h2>
            <div class="method-chain">
                <span class="chain-item">{Controller类}#{方法名}</span> → 
                <span class="chain-item">{Service类}#{方法名}</span> → 
                <span class="chain-item">{DAO/Mapper类}#{方法名}</span> → 
                <span class="chain-item">{依赖接口/第三方服务}</span>
                <!-- 按实际调用链删减/追加，保持格式统一 -->
            </div>
        </div>

        <!-- 模块3：测试数据列表（可展开） -->
        <div>
            <h2 class="mod-title">测试数据用例详情</h2>
            <div class="case-list">
                <!-- 测试用例卡片：AI循环生成，替换{xxx}，复制即可新增 -->
                <div class="case-card">
                    <div class="case-header" onclick="toggleCase(this)">
                        <div class="case-base">
                            <div class="case-num">{用例编号}</div>
                            <div class="case-scene">{测试场景：正常/边界/异常/依赖}</div>
                        </div>
                        <div>
                            <span class="case-status status-{ok/error/warn}">{执行状态：无问题/报错/告警}</span>
                            <span class="arrow">▼</span>
                        </div>
                    </div>
                    <div class="case-body">
                        <!-- 入参JSON -->
                        <div class="case-mod">
                            <h3 class="case-subtitle">?? 测试入参JSON</h3>
                            <div class="json-box">{格式化的入参JSON，保留换行和缩进}</div>
                        </div>
                        <!-- 执行状态详情 -->
                        <div class="case-mod">
                            <h3 class="case-subtitle">?? 执行状态详情</h3>
                            <!-- 状态1：无问题 -->
                            {无问题时展示}
                            <div class="ok-box">? 接口执行成功，无报错、无潜在告警，返回结果符合预期。</div>
                            
                            <!-- 状态2：报错（大模型已自验数据无问题） -->
                            {报错时展示，替换下方内容}
                            <div class="error-box">
                                <strong>? 报错信息：</strong>{具体报错内容}<br>
                                <strong>?? 报错位置：</strong>{接口方法链/代码行/数据库层}<br>
                                <strong>?? 备注：</strong>大模型已校验测试数据，数据格式、依赖均合法，判定为接口/项目本身问题。
                            </div>
                            
                            <!-- 状态3：告警 -->
                            {告警时展示，替换下方内容}
                            <div class="warn-box">
                                <strong>?? 告警信息：</strong>{潜在问题，如数据丢失、漏存、返回字段缺失、链路数据不一致等}<br>
                                <strong>?? 分析：</strong>{大模型对告警的具体分析，如“创建接口未返回主键ID，可能导致后续数据查询失败”}
                            </div>
                        </div>
                        <!-- 接口输出JSON -->
                        <div class="case-mod">
                            <h3 class="case-subtitle">?? 接口输出JSON</h3>
                            <div class="json-box">{格式化的输出JSON/报错信息，保留换行和缩进}</div>
                        </div>
                        <!-- 附加记录 -->
                        <div class="case-mod">
                            <h3 class="case-subtitle">?? 附加记录</h3>
                            <div style="font-size: 0.95rem; color: #666;">
                                数据构造方式：{接口拉取真实数据/人工构造数据} | 修正记录：{无/修正入参XXX，替换为XXX}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        // 用例卡片折叠/展开逻辑
        function toggleCase(header) {
            const body = header.nextElementSibling;
            const arrow = header.querySelector('.arrow');
            body.classList.toggle('open');
            arrow.classList.toggle('open');
        }
    </script>
</body>
</html>
```
#### HTML模板使用说明
1. AI直接复制模板，**替换所有`{xxx}`占位符**即可，无需修改样式和脚本；
2. 测试用例卡片可**循环复制生成**，按用例编号顺序排列；
3. 执行状态仅需替换`status-{ok/error/warn}`，样式会自动匹配（绿/红/黄）；
4. JSON内容需**格式化**（保留换行、缩进），提升可读性；
5. 方法链按实际调用关系删减/追加，保持`→`连接的统一格式。

## 四、核心约束（严格遵循）
1. **顺序执行**：步骤1-6、测试用例均按编号升序执行，禁止跳步、跨号、逆序；
2. **数据优先级**：有依赖接口**优先拉取测试环境真实数据**，无法拉取再构造，简单CRUD直接构造；
3. **数据自验**：测试失败时，大模型必须先校验自身数据有效性，排除自身问题后再判定接口BUG；
4. **双维测试**：所有测试数据必须覆盖「流程跑通」和「边界场景」两个维度，缺一不可；
5. **模板复用**：HTML报告必须直接复用上述模板，替换占位符即可，保证格式标准化；
6. **轻量启动**：所有测试必须基于加`@Lazy`的轻量启动项目，禁止未配置直接测试；
7. **状态标注**：报错状态必须明确“大模型已自验数据无问题”，告警状态必须是大模型分析的潜在数据问题。

## 五、快速示例流程
1. **轻量启动**：UserService启动类添加`@Lazy`，启动测试环境；
2. **自主造数**：测试`/api/v1/order/query`（按用户ID查订单），先调用`/api/v1/user/queryAll`拉取真实用户ID=1008，构造正常入参`{userId:1008, page:1, size:10}`和边界入参`{userId:1008, page:0, size:0}`；
3. **按序执行**：先执行正常场景用例→成功，再执行边界场景用例→成功；
4. **结果归集**：记录接口信息、方法链（`OrderController→OrderService→OrderMapper`）、两个用例的入参/输出；
5. **生成报告**：复用HTML模板，替换占位符，生成可直接打开的测试报告。

## 六、技能触发场景
当用户有以下需求时，直接调用本技能：
- 测试**带数据库交互**的Spring Boot RESTful API；
- 接口存在**数据依赖**，需要从测试环境自主获取真实数据构造用例；
- 需大模型**自验测试数据有效性**，避免因数据问题导致测试失败；
- 要求生成**含方法链+可展开详情**的标准化HTML测试报告；
- 测试有数据依赖的复杂接口或简单CRUD接口；
- 因脏数据、数据依赖导致测试频繁失败，需要自动化排障。

### 总结
1. 核心强化**自主获取/构造测试数据**规则，明确优先级和双维度测试要求，绑定大模型数据自验责任；
2. 新增**可直接复用的HTML测试报告模板**，含悬浮统计、接口概览、方法链、可展开测试用例，JSON格式化、状态色标，直接替换占位符即可生成；
3. 大幅缩写原有冗余步骤，聚焦「造数→测试→排障→报告」核心流程，保留关键约束和执行顺序；
4. HTML报告固定结构，测试用例展开后包含**入参JSON、执行状态（报错/告警/无问题）、接口输出、附加记录**，满足测试追溯需求；
5. 明确报错状态必须标注“大模型已自验数据无问题”，告警状态聚焦数据丢失/漏存等潜在问题，保证测试结果的准确性。