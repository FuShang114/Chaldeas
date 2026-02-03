---
name: "spring-boot-api-test"
description: "Conducts comprehensive Spring Boot RESTful API testing with automated debugging, data self-correction, and standardized reporting. Invoke when testing Spring Boot APIs with database interactions."
---

# Spring Boot API Testing Skill

This skill provides comprehensive guidance for testing Java Spring Boot RESTful APIs with automated troubleshooting, test data self-correction capabilities, and standardized reporting.

## Core Positioning & Applicability

**Target Environment:**
- Java Spring Boot projects (Spring ecosystem, supports @Lazy annotation)
- RESTful API interface testing
- Database-interactive interface testing scenarios

**Core Problem Solving:**
- Interface test failures caused by dirty data
- Improper test case design issues
- Project startup loading overhead causing test failures
- Automated troubleshooting during testing process
- Self-correcting test cases

**Skill Goal:**
Enable large models to complete "lightweight startup → executable testing → dynamic optimization → result archiving" without manual intervention, outputting directly usable test cases and traceable test reports.

## 6 Core Execution Steps (Must Follow Order)

### Step 1: Project Lightweight Startup Configuration (Test Prerequisites)

**Action**: Guide the large model to add @Lazy annotation to Spring Boot project startup class first.

**Core Annotation**: @Lazy (achieves Bean lazy loading, minimizes startup, reduces non-test-related component loading time and conflicts)

**Usage Specification**: Add @Lazy directly to startup class, use with @SpringBootApplication:

```java
@Lazy
@SpringBootApplication
public class XxxApplication {
    public static void main(String[] args) {
        SpringApplication.run(XxxApplication.class, args);
    }
}
```

**Additional Optimization**: If project has custom configuration classes or core service classes, guide the large model to add @Lazy annotation to non-essential Beans for testing, further streamlining startup resources.

**Objective**: Ensure lightweight, fast project startup during testing with no irrelevant component interference.

### Step 2: Multi-Dimensional Test Data Design + ToDoList Standard Writing

**Test Data Design Requirements**: Design test data for target interfaces from 4 core dimensions:

1. **Normal Scenarios**: Parameters comply with interface specification, database has corresponding related data, interface returns success results + correct business data
2. **Boundary Scenarios**: Parameters take maximum/minimum values, empty values (non-required), pagination parameter boundaries (page=1/size=100, page=0/size=0), related data with only 1/multiple records
3. **Exception Scenarios**: Illegal parameters (type errors/out of range), missing required parameters, dirty data in database, insufficient interface permissions, wrong request methods
4. **Special Business Scenarios**: Design exclusive test data based on actual interface business (payment interface timeout scenarios, query interface fuzzy/precise matching, duplicate data submission for create interfaces)

**ToDoList Writing Specification**: Write all designed test data into standardized ToDoList by "dimension classification + execution order". Each todo item must include unique numbering, test scenario, input data, expected results, related interfaces. **ToDoList Format Example**:

```
【Interface Test ToDoList - Target Interface: /api/v1/user/query (User Query Interface)】
Execution Rules: Execute in ascending order by number, record actual results after each case execution, enter troubleshooting flow if failed

1. Normal Scenario - Precise Query: Input={userId:1001, userName:null}, Expected=Return complete info for user ID 1001, status 200, Related Interface=/api/v1/user/query
2. Normal Scenario - Fuzzy Query: Input={userId:null, userName:"张"}, Expected=Return all users with name containing "张", status 200, Related Interface=/api/v1/user/query
3. Boundary Scenario - Pagination Boundary: Input={userId:null, userName:null, page:1, size:100}, Expected=Return page 1 with 100 user records, status 200, Related Interface=/api/v1/user/query
4. Exception Scenario - Missing Required Parameters: Input={userId:null, userName:null}, Expected=Return parameter error message, status 400, Related Interface=/api/v1/user/query
5. Exception Scenario - Illegal Input: Input={userId:-100, userName:"张"}, Expected=Return user ID illegal error, status 400, Related Interface=/api/v1/user/query
6. Special Business Scenario - No Data Match: Input={userId:9999, userName:null}, Expected=Return no matching data, status 200, Related Interface=/api/v1/user/query
```

**Key Requirements**: 
- Must specify execution order (ascending numbers)
- Each case corresponds to only one test scenario
- Avoid mixing multiple scenarios causing troubleshooting difficulties

### Step 3: Sequential Interface Test Execution (Core Rules, Cannot Violate)

**Execution Rules**:
- Strictly execute interface tests in ascending order by ToDoList unique numbering
- **Prohibited**: Cross-number, reverse order execution
- After each case execution, immediately record "actual execution results, return status code, return data, execution success"
- Success → continue to next case
- Failure → immediately pause current execution, enter Step 4: Dynamic troubleshooting flow
- After troubleshooting and case correction, re-execute that failed case
- Success → continue to next case

**Log Retention**: Preserve complete calling logs during execution including:
- Interface call address
- Request method (GET/POST/PUT/DELETE)
- Input parameters
- Request time
- Return data
- Status code

**Purpose**: Provide basis for troubleshooting and report generation.

### Step 4: Dynamic Troubleshooting + Test Data Self-Correction (Skill Core Capability)

**Troubleshooting Flow**: After test case execution failure, immediately start troubleshooting flow, locate problem root cause, then targeted correction.

**Core Logic**: "Determine Problem Attribution → Targeted Solution → Correct Test Data → Re-execute"

#### Step 4.1: Problem Root Cause Determination

**First Step**: Based on interface error messages, return status codes, database data, determine if failure cause is "Large Model Test Data Design Problem" or "Project/Interface Itself Problem".

**Large Model Own Problem Scenarios (Must Handle)**:
- Test data input parameter format errors
- Input parameter values exceed interface definition range
- Missing required parameters
- Test data conflicts with existing database data (dirty data causes)
- Related interface call sequence errors

**Project/Interface Itself Problem Scenarios (Record Only)**:
- Interface null pointer exceptions
- Database query errors
- Business logic errors
- Permission configuration errors
- Interface return data doesn't match expectations (non-data problems)

#### Step 4.2: Database Data Query Auxiliary Troubleshooting

**When**: Suspect dirty data/data conflict causes failure.

**Action**: Guide large model to call project-related query interfaces (user query, order query, data dictionary query, etc.) to check actual status of corresponding related data in database.

**Examples**:
- Test case userId=1001 query fails, error "User does not exist" → call /api/v1/user/queryAll interface to query all user IDs in database, confirm if 1001 exists. If doesn't exist → determine as test data design problem.
- Create user interface test case userName="张三" fails, error "Username already exists" → call /api/v1/user/query?userName=张三 interface to confirm if database already has this username. If exists → determine as dirty data caused test data problem.

#### Step 4.3: Test Data Self-Correction

**Target**: Cases determined as "Large Model Own Problem".

**Direct Action**: Correct input data in test cases directly.

**Correction Principles**:
- **Input Format/Range Errors**: Correct according to interface definition specifications (change negative ID to positive, string input to numeric input)
- **Missing Required Parameters**: Supplement required parameters according to interface requirements, reference database actual valid data
- **Dirty Data/Data Conflicts**: Replace with actually existing valid data in database (change non-existent userId=1001 to existing userId=1008), or modify input to avoid duplication (change duplicate userName="张三" to "张三_test")
- **Call Sequence Errors**: Adjust execution order of related cases in ToDoList (execute create interface cases first, then query interface cases)

#### Step 4.4: Re-execution

**After**: Test data correction completed.

**Action**: Re-execute the failed case.
- Success → continue to next case
- Still fails → re-enter troubleshooting flow
- If continuously fails after 3 corrections → determine as "Non-Large Model Own Problem", record problem and skip this case, continue to next case

### Step 5: Result Organization After Full Case Execution

**After**: All ToDoList cases completed (including success, skip, failure).

**Organize Full Execution Results**:

**Core Organization Content**:
- By ToDoList numbering order, summarize each case's "test scenario, input data, expected results, actual results, execution status (success/failure/skip), problem description (required for failure/skip cases), correction records (required for corrected cases)"
- **Classification Statistics**: Success case count, failure case count, skip case count, calculate test pass rate (pass rate = success cases / total cases * 100%)
- **Problem Classification**: Organize failure/skip problems by "Large Model Own Data Problem (Corrected), Large Model Own Data Problem (Uncorrected), Project/Interface Itself Problem", clarify specific manifestations and error messages for each problem

### Step 6: Generate Standardized Test Report (Document/HTML Choose One, Direct Usable)

**Based**: Organized execution results.

**Generate**: A directly viewable, traceable, standardized format test report supporting Markdown document or HTML page format.

**Report Must Include Following Core Modules** (Module order cannot be adjusted, content must be complete without omission):

#### Report General Information
- Test project name, target interface (including interface address, request method, interface function description)
- Test time, test environment (project startup method, JDK version, database type)
- Tester (marked as "AI Large Model"), test pass rate, case statistics (total/success/failure/skip)
- **Pre-condition Description**: Project startup class has added @Lazy annotation for lightweight startup (with startup class annotation code example)

#### Test Case & Execution Result Details
- By ToDoList numbering order, display all cases' detailed information in table format
- **Fixed Table Column Names**: Case Number, Test Dimension, Test Scenario, Input Data, Expected Results, Actual Results, Execution Status, Correction Records, Problem Description

**Example Table**:
| Case Number | Test Dimension | Test Scenario | Input Data | Expected Results | Actual Results | Execution Status | Correction Records | Problem Description |
|-------------|----------------|----------------|------------|------------------|----------------|------------------|-------------------|-------------------|
| 1 | Normal Scenario | Precise Query | {userId:1001, userName:null} | Return user 1001 info, 200 | User not found, 404 | Failure→Success | Changed userId=1001 to database existing 1008 | Original test data userId=1001 was dirty data, no such user in database |

#### Problem Summary & Analysis
- **Problem Classification Statistics**: By "Corrected Own Data Problems, Uncorrected Own Data Problems, Project/Interface Itself Problem" separately list problem counts and specific problem descriptions
- **Core Problem Analysis**: For key problems affecting interface testing (high-frequency dirty data, interface core business logic errors), briefly analyze problem causes and impact scope
- **Solution Recommendations**: For "Project/Interface Itself Problems", provide specific investigation and repair suggestions (such as "Null pointer exception requires checking if user object in interface is null", "Duplicate submission problem requires adding unique index")

#### Test Summary & Recommendations
- **Test Overall Conclusion**: Clearly state overall operation status of target interface (such as "Interface operates normally overall, core functions usable normally, exists 2 boundary scenario problems need fixing")
- **Follow-up Test Recommendations**: Such as supplementing more business scenario tests, optimizing test data coverage, re-executing failed cases after fixing project bugs
- **Precautions**: Such as dirty data problems found during testing need timely cleanup, project startup should retain @Lazy annotation to improve test efficiency

#### Appendix (Optional)
- Complete interface call logs (including all request addresses, input data, return data, times)
- Database query interface call records (query interfaces and return results used during troubleshooting)
- Complete project startup class code (with @Lazy annotation)

## Core Rules & Constraints (Must Strictly Follow)

1. **Sequential Execution Rule**: All test steps must execute in order "Step 1→Step 2→Step 3→Step 4→Step 5→Step 6", test cases must execute in ToDoList numbering order, prohibited to skip steps or reverse order

2. **Troubleshooting Priority Rule**: After test case execution failure, must troubleshoot first before continuing, prohibited to skip directly, can skip only after 3 consecutive correction failures

3. **Data Authenticity Rule**: During troubleshooting must obtain actual database data through project-related query interfaces, prohibited to subjectively guess data status, ensure accuracy of test data correction

4. **Problem Attribution Rule**: Only correct "Large Model Own Test Data Design Problems", record but do not correct project/interface BUGs themselves, clearly distinguish problem attribution

5. **Output Standardization Rule**: ToDoList and test reports must be written according to format specified in this prompt, core fields cannot be missing, format cannot be arbitrarily modified, ensure output results are directly usable and traceable

6. **Lightweight Startup Rule**: All testing must be based on "minimalist startup with @Lazy annotation added", prohibited to start project testing without @Lazy annotation

## Usage Instructions (Must Be Included in Skill, Guide Large Models for Quick Start)

**Suitable For**: Spring Boot project RESTful API interface testing. Before use, confirm project supports @Lazy annotation and has corresponding database query interfaces.

**Usage Requirements**: Must clearly specify target interface address, request method, input parameter specification, business rules, expected results for test data design basis.

**Execution Process**: Strictly follow sequential execution and troubleshooting priority rules to ensure testing rigor and result accuracy.

**Report Generation**: After testing completion, can choose to generate Markdown document or HTML page format test report according to needs. HTML reports need to ensure simple style, strong readability, can be opened directly in browser.

## Example Workflow

### Step 1: Project Startup Optimization
```java
@Lazy
@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

### Step 2: ToDoList Creation
```
【Interface Test ToDoList - Target Interface: /api/v1/user/create (User Create Interface)】
Execution Rules: Execute in ascending order by number, enter troubleshooting flow if failed

1. Normal Scenario - Valid User Creation: Input={userName:"test_user", email:"test@example.com"}, Expected=Return created user info, status 201, Related Interface=/api/v1/user/create
2. Boundary Scenario - Duplicate Username: Input={userName:"test_user", email:"test2@example.com"}, Expected=Return duplicate username error, status 400, Related Interface=/api/v1/user/create
3. Exception Scenario - Missing Required Parameters: Input={userName:"test_user"}, Expected=Return missing email error, status 400, Related Interface=/api/v1/user/create
```

### Step 3: Sequential Execution
- Execute Case 1: Success → continue to Case 2
- Execute Case 2: Failure (duplicate username) → enter troubleshooting

### Step 4: Dynamic Troubleshooting
- Problem Attribution: Large Model Own Problem (test data conflict)
- Database Query: Call /api/v1/user/query?userName=test_user to check if user exists
- Data Correction: Change userName from "test_user" to "test_user_001"
- Re-execution: Success → continue to Case 3

### Step 5 & 6: Result Organization & Report Generation
- Organize all execution results
- Generate standardized test report with problem analysis and recommendations

## When to Use This Skill

Invoke this skill when:
- Testing Spring Boot RESTful APIs with database interactions
- Need automated troubleshooting capabilities during testing
- Require self-correcting test data capabilities
- Need standardized test report generation
- Testing interfaces with complex business logic or data dependencies
- Encounter frequent test failures due to dirty data or improper test design