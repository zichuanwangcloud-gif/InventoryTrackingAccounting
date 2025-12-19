---
name: sqli-detection
description: SQL injection detection and prevention skill. Use when analyzing code for SQL injection vulnerabilities, writing database queries, reviewing security issues, working with user input in SQL contexts, or implementing parameterized queries. Covers SQLi attack vectors, detection patterns, secure coding practices, and remediation strategies.
---

# SQL Injection Detection Skill

## Purpose

Comprehensive guide for detecting, preventing, and remediating SQL injection vulnerabilities in application code. This skill helps identify dangerous patterns and enforce secure database query practices.

## When to Use

Automatically activates when:
- Analyzing code for SQL injection vulnerabilities
- Writing or reviewing database queries
- Handling user input that interacts with databases
- Conducting security code reviews
- Implementing parameterized queries or ORM patterns
- Discussing SQLi attack vectors or prevention

---

## SQL Injection Overview

### What is SQL Injection?

SQL injection occurs when untrusted data is sent to an interpreter as part of a command or query. Attackers can:
- Bypass authentication
- Access/modify/delete data
- Execute administrative operations
- In some cases, execute OS commands

### OWASP Classification

- **OWASP Top 10**: A03:2021 - Injection
- **CWE**: CWE-89 (SQL Injection)
- **Severity**: Critical (CVSS 9.8 typical)

---

## Detection Patterns

### High-Risk Code Patterns

#### 1. String Concatenation in Queries

```javascript
// VULNERABLE - String concatenation
const query = "SELECT * FROM users WHERE id = " + userId;
const query = `SELECT * FROM users WHERE name = '${username}'`;
const query = "SELECT * FROM users WHERE id = '" + req.params.id + "'";
```

#### 2. Template Literals with User Input

```javascript
// VULNERABLE - Template literal injection
db.query(`SELECT * FROM products WHERE category = '${category}'`);
```

#### 3. Dynamic Query Building

```javascript
// VULNERABLE - Dynamic column/table names
const query = `SELECT ${columns} FROM ${tableName} WHERE id = ${id}`;
```

#### 4. Raw Query Methods

```python
# VULNERABLE - Raw SQL in Python
cursor.execute("SELECT * FROM users WHERE username = '%s'" % username)
cursor.execute(f"SELECT * FROM users WHERE id = {user_id}")
```

```java
// VULNERABLE - Raw SQL in Java
String query = "SELECT * FROM users WHERE id = " + userId;
Statement stmt = connection.createStatement();
ResultSet rs = stmt.executeQuery(query);
```

### Detection Regex Patterns

```regex
# String concatenation in SQL
(SELECT|INSERT|UPDATE|DELETE|FROM|WHERE).*[\+\$\{].*(?:req\.|params\.|body\.|query\.|input|user)

# f-string/template literal SQL
(execute|query|raw)\s*\(\s*[f`].*(?:SELECT|INSERT|UPDATE|DELETE)

# Dangerous string formatting
(execute|query).*%.*(?:req|params|input|user)

# Dynamic identifiers
(?:SELECT|FROM|WHERE|ORDER BY)\s+.*\$\{?\w+\}?(?:\s|$)
```

---

## Secure Coding Patterns

### 1. Parameterized Queries (Prepared Statements)

**Node.js (mysql2)**
```javascript
// SECURE - Parameterized query
const [rows] = await connection.execute(
  'SELECT * FROM users WHERE id = ? AND status = ?',
  [userId, status]
);
```

**Node.js (pg - PostgreSQL)**
```javascript
// SECURE - Parameterized query
const result = await client.query(
  'SELECT * FROM users WHERE id = $1 AND email = $2',
  [userId, email]
);
```

**Python (psycopg2)**
```python
# SECURE - Parameterized query
cursor.execute(
    "SELECT * FROM users WHERE id = %s AND email = %s",
    (user_id, email)
)
```

**Java (JDBC)**
```java
// SECURE - PreparedStatement
String sql = "SELECT * FROM users WHERE id = ? AND email = ?";
PreparedStatement pstmt = connection.prepareStatement(sql);
pstmt.setInt(1, userId);
pstmt.setString(2, email);
ResultSet rs = pstmt.executeQuery();
```

### 2. ORM Best Practices

**Prisma (Node.js)**
```typescript
// SECURE - Prisma ORM
const user = await prisma.user.findUnique({
  where: { id: userId }
});

// SECURE - Prisma with parameterized raw query
const users = await prisma.$queryRaw`
  SELECT * FROM users WHERE id = ${userId}
`;
```

**SQLAlchemy (Python)**
```python
# SECURE - SQLAlchemy ORM
user = session.query(User).filter(User.id == user_id).first()

# SECURE - SQLAlchemy with text() and bound parameters
from sqlalchemy import text
result = session.execute(
    text("SELECT * FROM users WHERE id = :id"),
    {"id": user_id}
)
```

**TypeORM**
```typescript
// SECURE - TypeORM
const user = await userRepository.findOne({
  where: { id: userId }
});

// SECURE - Query builder with parameters
const users = await userRepository
  .createQueryBuilder("user")
  .where("user.id = :id", { id: userId })
  .getMany();
```

### 3. Input Validation Layer

```typescript
// Additional defense - Input validation with Zod
import { z } from 'zod';

const UserIdSchema = z.string().uuid();
const SearchSchema = z.object({
  query: z.string().max(100).regex(/^[a-zA-Z0-9\s]+$/),
  limit: z.number().int().min(1).max(100).default(20)
});

// Validate before any database operation
const validatedId = UserIdSchema.parse(req.params.id);
const validatedSearch = SearchSchema.parse(req.query);
```

### 4. Allowlisting for Dynamic Identifiers

```typescript
// SECURE - Allowlist for column names
const ALLOWED_COLUMNS = ['id', 'name', 'email', 'created_at'] as const;
const ALLOWED_SORT_DIRS = ['ASC', 'DESC'] as const;

function buildQuery(sortBy: string, sortDir: string) {
  // Validate against allowlist
  if (!ALLOWED_COLUMNS.includes(sortBy as any)) {
    throw new Error('Invalid sort column');
  }
  if (!ALLOWED_SORT_DIRS.includes(sortDir as any)) {
    throw new Error('Invalid sort direction');
  }

  // Safe to use - values are from allowlist
  return `SELECT * FROM users ORDER BY ${sortBy} ${sortDir}`;
}
```

---

## SQLi Attack Vectors

### 1. Classic Union-Based

```sql
-- Attack payload
' UNION SELECT username, password FROM users--

-- Resulting query
SELECT * FROM products WHERE id = '' UNION SELECT username, password FROM users--'
```

### 2. Boolean-Based Blind

```sql
-- Attack payloads
' AND 1=1--  (returns normal)
' AND 1=2--  (returns different/empty)

-- Used to extract data bit by bit
' AND SUBSTRING(username,1,1)='a'--
```

### 3. Time-Based Blind

```sql
-- Attack payloads
'; WAITFOR DELAY '0:0:5'--  (SQL Server)
' AND SLEEP(5)--            (MySQL)
'; SELECT pg_sleep(5)--     (PostgreSQL)
```

### 4. Error-Based

```sql
-- Attack payload (SQL Server)
' AND 1=CONVERT(int, (SELECT TOP 1 username FROM users))--

-- Extracts data through error messages
```

### 5. Second-Order SQLi

```javascript
// First request - stores malicious payload
POST /register
{ "username": "admin'--", "email": "attacker@evil.com" }

// Second request - payload executes when retrieved
GET /profile  // Uses stored username in query
```

---

## Code Review Checklist

### Must Check

- [ ] All SQL queries use parameterized statements
- [ ] No string concatenation with user input in queries
- [ ] ORM methods used correctly (no raw queries with user data)
- [ ] Dynamic column/table names use strict allowlists
- [ ] Input validation applied before database operations
- [ ] Error messages don't expose SQL details

### Red Flags

- `execute()`, `query()`, `raw()` with string interpolation
- Template literals containing SQL keywords
- String concatenation with `+` near SQL statements
- `%s` or `.format()` in Python SQL strings
- Any use of `eval()` near database code

### Framework-Specific

**Express/Node.js**
- Check all `req.params`, `req.query`, `req.body` usage in queries
- Verify middleware validates input before routes

**Django/Python**
- Prefer ORM over `raw()` and `extra()`
- Check `RawSQL()` and `cursor.execute()` usage

**Spring/Java**
- Verify `@Query` annotations use named parameters
- Check for `Statement` vs `PreparedStatement`

---

## Remediation Examples

### Before/After: Node.js

```javascript
// BEFORE - Vulnerable
app.get('/user/:id', async (req, res) => {
  const query = `SELECT * FROM users WHERE id = '${req.params.id}'`;
  const result = await db.query(query);
  res.json(result);
});

// AFTER - Secure
app.get('/user/:id', async (req, res) => {
  const [result] = await db.execute(
    'SELECT * FROM users WHERE id = ?',
    [req.params.id]
  );
  res.json(result);
});
```

### Before/After: Python

```python
# BEFORE - Vulnerable
@app.route('/user/<user_id>')
def get_user(user_id):
    cursor.execute(f"SELECT * FROM users WHERE id = {user_id}")
    return cursor.fetchone()

# AFTER - Secure
@app.route('/user/<user_id>')
def get_user(user_id):
    cursor.execute("SELECT * FROM users WHERE id = %s", (user_id,))
    return cursor.fetchone()
```

### Before/After: Search Function

```typescript
// BEFORE - Vulnerable to SQLi
async function searchProducts(query: string, sortBy: string) {
  return db.query(`
    SELECT * FROM products
    WHERE name LIKE '%${query}%'
    ORDER BY ${sortBy}
  `);
}

// AFTER - Secure
const ALLOWED_SORT = ['name', 'price', 'created_at'] as const;

async function searchProducts(query: string, sortBy: string) {
  // Validate sort column
  if (!ALLOWED_SORT.includes(sortBy as any)) {
    sortBy = 'name';
  }

  // Use parameterized query for search term
  return db.execute(
    `SELECT * FROM products WHERE name LIKE ? ORDER BY ${sortBy}`,
    [`%${query}%`]
  );
}
```

---

## Testing for SQLi

### Manual Testing Payloads

```
' OR '1'='1
" OR "1"="1
' OR '1'='1'--
' OR '1'='1'/*
1' AND '1'='1
1 AND 1=1
1' AND '1'='2
'; DROP TABLE users;--
' UNION SELECT NULL--
' UNION SELECT NULL,NULL--
```

### Automated Tools

- **SQLMap**: `sqlmap -u "http://target/page?id=1" --dbs`
- **Burp Suite**: Active scanner with SQLi detection
- **OWASP ZAP**: Automated SQLi scanning

### SAST Integration

```yaml
# Example: Semgrep rule for SQLi detection
rules:
  - id: sql-injection-string-concat
    patterns:
      - pattern-either:
          - pattern: $QUERY = "..." + $VAR + "..."
          - pattern: $QUERY = f"...{$VAR}..."
          - pattern: $QUERY = `...${$VAR}...`
      - metavariable-regex:
          metavariable: $QUERY
          regex: .*(SELECT|INSERT|UPDATE|DELETE|FROM|WHERE).*
    message: "Potential SQL injection via string concatenation"
    severity: ERROR
```

---

## Quick Reference

### Secure Query Methods by Language

| Language | Secure Method | Example |
|----------|--------------|---------|
| Node.js (mysql2) | `execute(sql, params)` | `execute('SELECT * FROM users WHERE id = ?', [id])` |
| Node.js (pg) | `query(sql, params)` | `query('SELECT * FROM users WHERE id = $1', [id])` |
| Python (psycopg2) | `execute(sql, params)` | `execute('SELECT * FROM users WHERE id = %s', (id,))` |
| Java (JDBC) | `PreparedStatement` | `pstmt.setInt(1, id)` |
| C# (.NET) | `SqlParameter` | `cmd.Parameters.AddWithValue("@id", id)` |

### Defense in Depth

1. **Primary**: Parameterized queries / Prepared statements
2. **Secondary**: Input validation and sanitization
3. **Tertiary**: Least privilege database accounts
4. **Monitoring**: SQL error logging and alerting
5. **WAF**: Web Application Firewall rules

---

## Related Resources

- [OWASP SQL Injection Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html)
- [CWE-89: SQL Injection](https://cwe.mitre.org/data/definitions/89.html)
- [PortSwigger SQL Injection](https://portswigger.net/web-security/sql-injection)

---

**Skill Status**: COMPLETE
**Line Count**: ~450 (within 500-line limit)
