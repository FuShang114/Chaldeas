---
name: "test-guide"
description: "Guides large models on how to conduct testing. Invoke when user requests testing guidance, needs to create test plans, or when testing-related tasks are needed."
---

# Test Guide

This skill teaches large models how to conduct comprehensive testing by following a structured approach.

## Testing Process Overview

### Step 1: Request Requirements Documentation
- **Action**: Ask the user to provide detailed requirements documentation
- **Purpose**: To understand the project scope, functionality, and expected behavior
- **Key information to request**:
  - Functional requirements
  - Non-functional requirements (performance, security, etc.)
  - Business logic descriptions
  - System architecture diagrams
  - API specifications if applicable

### Step 2: Analyze Interfaces and Dependencies
- **Action**: Identify all interfaces mentioned in the requirements
- **Process**:
  1. Extract API endpoints from documentation
  2. Identify external service dependencies
  3. Map out data flow between components
  4. Document input/output formats for each interface

### Step 3: Decompose Methods and Functions
- **Action**: Break down each interface into individual methods
- **Analysis points**:
  - Method signatures and parameters
  - Return types and error handling
  - Edge cases and boundary conditions
  - Authentication and authorization requirements

### Step 4: Identify Ambiguous Requirements
- **Action**: Flag any unclear or ambiguous requirements
- **Examples of ambiguity**:
  - Vague success/failure criteria
  - Undefined error handling behavior
  - Missing input validation rules
  - Unspecified performance requirements
- **Process**: Document ambiguous points and request clarification from the user

### Step 5: Identify High-Risk Operations
- **Action**: Detect operations that could cause issues if not tested properly
- **Common high-risk operations**:
  - Multi-threaded operations
  - Physical data deletion
  - Database schema changes
  - Authentication bypass risks
  - External service integrations
  - Financial transaction processing
- **Mitigation**: Design specific test cases to verify these operations work correctly and safely

## Test Case Design Principles

### Test Types to Consider
- Unit tests
- Integration tests
- End-to-end tests
- Performance tests
- Security tests
- Regression tests

### Test Case Structure
1. **Test ID**: Unique identifier
2. **Test Description**: What the test verifies
3. **Prerequisites**: Conditions needed before testing
4. **Test Steps**: Detailed execution steps
5. **Expected Results**: What should happen if the test passes
6. **Actual Results**: What actually happens during testing
7. **Status**: Pass/Fail/Blocked
8. **Notes**: Additional information

## Testing Best Practices

### Before Testing
- Review requirements thoroughly
- Understand the system architecture
- Identify test environments needed
- Create a test plan and schedule

### During Testing
- Document all test cases and results
- Test both positive and negative scenarios
- Verify error handling mechanisms
- Test edge cases and boundary conditions
- Perform regression testing after changes

### After Testing
- Generate comprehensive test reports
- Document any issues found
- Provide recommendations for improvements
- Update test cases for future iterations

## Example Workflow

1. **Request requirements**: "Please provide the project requirements documentation so I can understand the scope and functionality."

2. **Analyze interfaces**: "Based on the requirements, I've identified the following API endpoints that need testing: /users, /products, /orders."

3. **Decompose methods**: "For the /users endpoint, we need to test GET /users, POST /users, PUT /users/{id}, and DELETE /users/{id} methods."

4. **Flag ambiguities**: "I noticed the requirements don't specify the maximum length for user names. Could you clarify this?"

5. **Identify risks**: "The DELETE /users/{id} endpoint performs physical deletion, which is a high-risk operation. We should test this thoroughly with proper authorization checks."

6. **Design test cases**: "I'll create test cases covering normal operations, error handling, edge cases, and security scenarios."

7. **Execute tests**: "Running the test suite and documenting results."

8. **Report findings**: "Test completed. Found 2 critical issues and 3 minor issues that need attention."

## When to Use This Skill

Invoke this skill when:
- User asks for testing guidance
- Creating test plans or strategies
- Reviewing requirements for testing purposes
- Identifying test scenarios for new features
- Analyzing code changes for testing needs
- Preparing for quality assurance activities