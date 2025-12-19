
# Java 测试修复任务

你现在扮演：**资深 Java 后端与测试基础设施专家（20+年）**，精通 Java 17+、JUnit 5、Mockito、Spring Boot Test、Testcontainers、AssertJ 与并发测试。


## 目录

1. [角色与目标](#角色与目标)
2. [快速诊断决策树](#快速诊断决策树)
3. [环境与约束](#环境与约束)
4. [详细修复流程](#详细修复流程)
5. [技术修复指南](#技术修复指南)
6. [最佳实践与模式](#最佳实践与模式)
7. [验收标准与交付物](#验收标准与交付物)
8. [附录：常见场景示例](#附录常见场景示例)


## 角色与目标

### 任务目标

1. 运行并**稳定修复**目录 `src/test/java` 的所有测试。
2. 如测试暴露真实缺陷，**以最小变更**修复生产代码（保持向后兼容）。
3. 移除或替换**过期/变更 API** 引发的错误（例如 Spring Boot、JUnit、Mockito 行为变更）。
4. 产出可重复的本地验证步骤与提交的修改说明（changelog）。

### 核心原则

- **优先修测试**：尽量在测试层面解决问题，避免修改生产代码
- **最小变更**：如需修改生产代码，保持向后兼容，变更最小化
- **充分测试**：所有变更必须有测试覆盖
- **文档记录**：详细记录修复过程和兼容性说明


## 快速诊断决策树

遇到测试失败时，按以下决策树快速定位问题类型：

```text
测试失败
│
├─ 依赖注入/Spring上下文相关错误？
│  ├─ NoSuchBeanDefinitionException
│  ├─ BeanCreationException
│  ├─ UnsatisfiedDependencyException
│  └─ → 问题类型：A. Spring Boot Test 配置问题
│
├─ Mock/Stub 相关错误？
│  ├─ NullPointerException（mock 未正确初始化）
│  ├─ UnnecessaryStubbingException
│  ├─ WrongTypeOfReturnValue
│  └─ → 问题类型：B. Mockito 配置问题
│
├─ 数据库/JPA 相关错误？
│  ├─ DataIntegrityViolationException
│  ├─ ConstraintViolationException
│  ├─ EntityNotFoundException
│  └─ → 问题类型：C. 数据库与 JPA 测试问题
│
├─ 并发/线程相关错误？
│  ├─ ConcurrentModificationException
│  ├─ 测试偶尔失败
│  └─ → 问题类型：D. 并发与不稳定测试
│
├─ API/HTTP 测试错误？
│  ├─ MockMvc 测试失败
│  ├─ RestTemplate/WebClient 测试失败
│  └─ → 问题类型：E. Spring MVC/WebFlux 测试问题
│
└─ 其他错误？
   └─ → 查看详细错误堆栈，参考「技术修复指南」
```

### 快速排查表

| 错误特征 | 可能原因 | 快速修复方向 |
|---------|---------|------------|
| `NoSuchBeanDefinitionException` | Spring Bean 未注册或配置错误 | 检查 `@SpringBootTest` 配置、组件扫描路径 |
| `NullPointerException` in mock | Mock 未初始化或注入失败 | 检查 `@Mock`、`@InjectMocks` 使用 |
| `UnnecessaryStubbingException` | Stub 设置但未使用 | 移除未使用的 stubbing 或使用 lenient() |
| JPA 测试失败 | 数据库连接、事务管理问题 | 检查 `@DataJpaTest`、事务隔离设置 |
| 测试随机失败 | 并发问题、时间依赖 | 使用 `@DirtiesContext`、Clock mock |


## 环境与约束

### 运行环境假设

- **Java**: 17+ (LTS)
- **构建工具**: Maven 3.9+ 或 Gradle 8.0+
- **测试框架**: JUnit 5 (Jupiter)
- **Mock 框架**: Mockito 5.x
- **Spring Boot**: 3.2+ (如使用)
- **数据库**: H2/Testcontainers (集成测试)
- **断言库**: AssertJ 3.x

### 测试命令

```bash
# Maven 项目
./mvnw clean test

# Gradle 项目
./gradlew clean test

# 运行单个测试类
./mvnw test -Dtest=UserServiceTest

# 运行单个测试方法
./mvnw test -Dtest=UserServiceTest#testCreateUser
```

### 约束与风格

- **优先修测试**：仅在确认生产代码确有缺陷时再改生产代码，且**变更最小**、**兼容旧行为**。
- **禁止测试专用逻辑**：不可为迎合测试而新增与业务无关的临时逻辑分支或"测试专用开关"。
- **保持语义一致**：保持类型与异常语义不变；新增或变更行为需在测试中明确覆盖到。
- **测试隔离**：每个测试方法独立运行，不依赖其他测试的执行顺序或状态。


## 详细修复流程

### 阶段 1：首次运行并收集失败日志

#### 执行步骤

1. **运行测试命令**：
   ```bash
   # Maven
   ./mvnw clean test

   # Gradle
   ./gradlew clean test
   ```

2. **收集完整输出**：
   - 复制全部失败堆栈
   - 记录所有警告（Deprecated、Removed）
   - 保存测试执行日志（Surefire/Gradle Test Report）

3. **归纳失败点列表**：
   - [ ] 断言失败/预期值不匹配
   - [ ] Spring Bean 注入失败（`NoSuchBeanDefinitionException`）
   - [ ] Mock 配置错误（`NullPointerException`、`UnnecessaryStubbingException`）
   - [ ] 数据库/JPA 错误（事务、约束违反）
   - [ ] API 版本变更导致的失配
   - [ ] 并发/线程安全问题
   - [ ] 其他错误类型

#### 检查清单

- [ ] 已收集所有失败测试的完整堆栈
- [ ] 已记录所有 Deprecated 和 Removed 警告
- [ ] 已使用「快速诊断决策树」分类问题类型
- [ ] 已创建失败点列表，标注优先级


### 阶段 2：定位成因，按优先级修复

根据「快速诊断决策树」的结果，按以下优先级修复：

#### A. Spring Boot Test 配置问题

**错误特征**：
- `NoSuchBeanDefinitionException`
- `BeanCreationException`
- `UnsatisfiedDependencyException`
- `ApplicationContextException`

**诊断步骤**：
1. 检查测试类上的注解配置（`@SpringBootTest`、`@WebMvcTest`、`@DataJpaTest`）
2. 确认组件扫描路径是否正确
3. 检查测试配置类（`@TestConfiguration`）是否正确
4. 验证 Profile 配置（`@ActiveProfiles`）

**修复方案**：

**方案 1：正确使用 Spring Boot Test 注解**

```java
// 完整的应用上下文测试
@SpringBootTest
class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Test
    void testCreateUser() {
        // 测试代码
    }
}

// Web 层测试（只加载 MVC 层）
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void testGetUser() throws Exception {
        mockMvc.perform(get("/users/1"))
               .andExpect(status().isOk());
    }
}

// JPA 层测试
@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testFindByUsername() {
        User user = new User("test", "test@example.com");
        entityManager.persist(user);

        Optional<User> found = userRepository.findByUsername("test");
        assertThat(found).isPresent();
    }
}
```

**方案 2：使用 @TestConfiguration 补充 Bean**

```java
@SpringBootTest
class UserServiceTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public Clock testClock() {
            return Clock.fixed(
                Instant.parse("2024-01-01T00:00:00Z"),
                ZoneId.of("UTC")
            );
        }
    }

    @Autowired
    private UserService userService;

    @Test
    void testTimeDependent() {
        // 使用固定时钟测试
    }
}
```

**方案 3：正确使用 @MockBean**

```java
@SpringBootTest
class UserServiceTest {
    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void testGetUser() {
        when(userRepository.findById(1L))
            .thenReturn(Optional.of(new User("test")));

        User user = userService.getUser(1L);
        assertThat(user.getUsername()).isEqualTo("test");
    }
}
```

**验证方法**：
```bash
./mvnw test -Dtest=UserServiceTest
```

**注意事项**：
- `@SpringBootTest` 会加载完整的应用上下文，较慢
- `@WebMvcTest`、`@DataJpaTest` 等切片测试只加载相关层，更快
- 避免在单元测试中使用 `@SpringBootTest`


#### B. Mockito 配置问题

**错误特征**：
- `NullPointerException`（Mock 未初始化）
- `UnnecessaryStubbingException`
- `WrongTypeOfReturnValue`
- `TooManyActualInvocations`

**诊断步骤**：
1. 检查 `@Mock`、`@InjectMocks` 是否正确使用
2. 确认 Mock 初始化（`@ExtendWith(MockitoExtension.class)`）
3. 检查 Stubbing 是否被实际使用
4. 验证 Verify 调用是否匹配实际情况

**修复方案**：

**方案 1：正确初始化 Mock**

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateUser() {
        User user = new User("test", "test@example.com");
        when(userRepository.save(any(User.class)))
            .thenReturn(user);

        User created = userService.createUser("test", "test@example.com");

        assertThat(created.getUsername()).isEqualTo("test");
        verify(userRepository, times(1)).save(any(User.class));
    }
}
```

**方案 2：处理 UnnecessaryStubbingException**

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testUpdateUser() {
        // 使用 lenient() 允许未使用的 stubbing
        lenient().when(userRepository.findById(999L))
            .thenReturn(Optional.empty());

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(new User("test")));

        User updated = userService.updateUser(1L, "newName");
        assertThat(updated.getUsername()).isEqualTo("newName");
    }
}
```

**方案 3：ArgumentCaptor 使用**

```java
@Test
void testSaveUserWithCaptor() {
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    userService.createUser("test", "test@example.com");

    verify(userRepository).save(userCaptor.capture());
    User capturedUser = userCaptor.getValue();
    assertThat(capturedUser.getUsername()).isEqualTo("test");
    assertThat(capturedUser.getEmail()).isEqualTo("test@example.com");
}
```

**验证方法**：
```bash
./mvnw test -Dtest=UserServiceTest
```


#### C. 数据库与 JPA 测试问题

**错误特征**：
- `DataIntegrityViolationException`
- `ConstraintViolationException`
- `EntityNotFoundException`
- `TransactionRequiredException`

**诊断步骤**：
1. 检查测试数据库配置（H2/Testcontainers）
2. 确认事务管理配置
3. 检查实体关系和约束
4. 验证测试数据准备和清理

**修复方案**：

**方案 1：使用 @DataJpaTest**

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testFindByUsername() {
        // 准备测试数据
        User user = new User("test", "test@example.com");
        entityManager.persist(user);
        entityManager.flush();

        // 测试
        Optional<User> found = userRepository.findByUsername("test");

        // 验证
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @Transactional
    void testUpdateUser() {
        User user = new User("test", "test@example.com");
        entityManager.persist(user);
        entityManager.flush();

        user.setEmail("new@example.com");
        userRepository.save(user);
        entityManager.flush();

        User updated = entityManager.find(User.class, user.getId());
        assertThat(updated.getEmail()).isEqualTo("new@example.com");
    }
}
```

**方案 2：使用 Testcontainers 进行真实数据库测试**

```java
@SpringBootTest
@Testcontainers
class UserServiceIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserService userService;

    @Test
    void testCreateUser() {
        User user = userService.createUser("test", "test@example.com");
        assertThat(user.getId()).isNotNull();
    }
}
```

**方案 3：使用 @Sql 准备测试数据**

```java
@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @Sql("/test-data/users.sql")
    void testFindAllUsers() {
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(3);
    }

    @Test
    @Sql(scripts = "/test-data/users.sql",
         executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/test-data/cleanup.sql",
         executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testWithCleanup() {
        // 测试代码
    }
}
```

**验证方法**：
```bash
./mvnw test -Dtest=UserRepositoryTest
```


#### D. 并发与不稳定测试

**错误特征**：
- 测试偶尔失败
- `ConcurrentModificationException`
- 时间相关的断言失败
- 竞态条件

**诊断步骤**：
1. 识别测试中的时间依赖（`LocalDateTime.now()`、`System.currentTimeMillis()`）
2. 检查是否有竞态条件
3. 确认是否有共享状态
4. 检查测试执行顺序依赖

**修复方案**：

**方案 1：使用 Clock 注入固定时间**

```java
@SpringBootTest
class OrderServiceTest {
    @MockBean
    private Clock clock;

    @Autowired
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
            Instant.parse("2024-01-01T10:00:00Z"),
            ZoneId.of("UTC")
        );
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    void testCreateOrder() {
        Order order = orderService.createOrder();
        assertThat(order.getCreatedAt())
            .isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
    }
}
```

**方案 2：使用 @DirtiesContext 隔离测试**

```java
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CacheServiceTest {
    @Autowired
    private CacheService cacheService;

    @Test
    void testCachePut() {
        cacheService.put("key", "value");
        assertThat(cacheService.get("key")).isEqualTo("value");
    }

    @Test
    void testCacheEvict() {
        cacheService.put("key", "value");
        cacheService.evict("key");
        assertThat(cacheService.get("key")).isNull();
    }
}
```

**方案 3：使用 Awaitility 处理异步测试**

```java
@Test
void testAsyncOperation() {
    service.triggerAsyncTask();

    await().atMost(Duration.ofSeconds(5))
           .pollInterval(Duration.ofMillis(100))
           .until(() -> service.isTaskCompleted());

    assertThat(service.getResult()).isEqualTo("expected");
}
```

**验证方法**：
- 多次运行测试，确认稳定性
- 使用并行测试：`./mvnw test -Djunit.jupiter.execution.parallel.enabled=true`


#### E. Spring MVC/WebFlux 测试问题

**错误特征**：
- MockMvc 测试失败
- 状态码/响应内容不匹配
- JSON 序列化/反序列化问题
- 请求参数/Header 缺失

**诊断步骤**：
1. 检查 Controller 层测试配置
2. 确认 MockMvc 请求构造
3. 验证响应断言
4. 检查 JSON 映射配置

**修复方案**：

**方案 1：完整的 MockMvc 测试**

```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void testGetUser() throws Exception {
        User user = new User(1L, "test", "test@example.com");
        when(userService.getUser(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1")
                   .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.username").value("test"))
               .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).getUser(1L);
    }

    @Test
    void testCreateUser() throws Exception {
        User user = new User(1L, "test", "test@example.com");
        when(userService.createUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/users")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"username\":\"test\",\"email\":\"test@example.com\"}"))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testGetUserNotFound() throws Exception {
        when(userService.getUser(999L))
            .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/999"))
               .andExpect(status().isNotFound());
    }
}
```

**方案 2：使用 TestRestTemplate 进行完整的集成测试**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testGetUser() {
        User user = userRepository.save(new User("test", "test@example.com"));

        ResponseEntity<User> response = restTemplate.getForEntity(
            "/api/users/" + user.getId(),
            User.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("test");
    }

    @Test
    void testCreateUser() {
        User user = new User("test", "test@example.com");

        ResponseEntity<User> response = restTemplate.postForEntity(
            "/api/users",
            user,
            User.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
    }
}
```

**验证方法**：
```bash
./mvnw test -Dtest=UserControllerTest
```


### 阶段 3：实现修复与补测

#### 检查清单

- [ ] 已修改最小必要代码
- [ ] 已就地为涉及行为的关键路径补充/细化测试
- [ ] 已使用 Mock/Fake 替代外部依赖
- [ ] 已禁止真实网络调用
- [ ] 使用 Testcontainers 或 H2 进行数据库测试

#### Mock 外部依赖示例

**Mock HTTP 请求（使用 MockRestServiceServer）**：

```java
@SpringBootTest
class ExternalApiClientTest {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ExternalApiClient apiClient;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void testGetData() {
        mockServer.expect(requestTo("https://api.example.com/data"))
                  .andExpect(method(HttpMethod.GET))
                  .andRespond(withSuccess(
                      "{\"result\":\"ok\"}",
                      MediaType.APPLICATION_JSON
                  ));

        String result = apiClient.getData();

        assertThat(result).isEqualTo("ok");
        mockServer.verify();
    }
}
```

**Mock 数据库（使用 @DataJpaTest）**：

```java
@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testFindByUsername() {
        User user = new User("test", "test@example.com");
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> found = userRepository.findByUsername("test");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }
}
```


### 阶段 4：二次运行与基准校验

#### 执行步骤

1. **重新运行测试命令**：
   ```bash
   ./mvnw clean test
   ```

2. **验证结果**：
   - [ ] 失败数 = 0
   - [ ] 无新的警告或弃用信息
   - [ ] 测试执行时间在合理范围内

3. **检查覆盖率**：
   ```bash
   # Maven with JaCoCo
   ./mvnw clean test jacoco:report

   # Gradle with JaCoCo
   ./gradlew clean test jacocoTestReport
   ```

#### 检查清单

- [ ] 所有测试通过
- [ ] 无性能回归
- [ ] 无新的弃用警告
- [ ] 测试执行时间在合理范围内


### 阶段 5：输出交付物

#### 生成调试文档

在 `src/test/resources/README_debug.md` 中包含：

```markdown
# 测试修复调试文档

## 失败点与根因摘要

### 问题 1：Spring Bean 注入失败
- **错误信息**：`NoSuchBeanDefinitionException: No qualifying bean of type 'UserRepository'`
- **根因**：测试类缺少 `@SpringBootTest` 注解
- **修复方案**：添加 `@SpringBootTest` 或使用 `@MockBean`

### 问题 2：Mock 未初始化
- **错误信息**：`NullPointerException`
- **根因**：缺少 `@ExtendWith(MockitoExtension.class)` 注解
- **修复方案**：添加 Mockito 扩展

## 具体修复点

| 文件 | 行号 | 变更说明 |
|------|------|---------|
| `UserServiceTest.java` | 15 | 添加 `@ExtendWith(MockitoExtension.class)` |
| `UserControllerTest.java` | 20 | 使用 `@WebMvcTest` 替代 `@SpringBootTest` |

## 兼容性说明

- **Spring Boot**: >= 3.2 需要使用新的测试注解
- **JUnit**: >= 5.9 使用 `@ExtendWith` 替代 `@RunWith`
- **Mockito**: >= 5.0 改进了 stubbing 验证

## 本地复现与验证命令

```bash
# 复现问题（修复前）
git checkout <before-fix-commit>
./mvnw clean test

# 验证修复（修复后）
./mvnw clean test
```

## 风险与后续建议

- **风险**：无
- **后续建议**：
  - 定期更新依赖版本
  - 关注 Deprecated 警告
  - 考虑添加依赖版本锁定
```

#### 提交信息格式

使用 Conventional Commits 格式：

```
test(user): fix Spring context configuration in UserServiceTest

- Add @SpringBootTest annotation
- Fix mock initialization with @ExtendWith(MockitoExtension.class)
- Update MockMvc tests with proper request builders

Fixes: #123
```


## 技术修复指南

### 常见问题快速修复

#### 1. Spring Bean 注入失败

**问题**：测试中无法注入 Spring Bean

**解决方案**：

```java
// 使用正确的测试注解
@SpringBootTest
class ServiceTest {
    @Autowired
    private MyService service;
}

// 或使用 Mock
@ExtendWith(MockitoExtension.class)
class ServiceTest {
    @Mock
    private MyRepository repository;

    @InjectMocks
    private MyService service;
}
```

#### 2. 事务测试回滚

**问题**：测试数据未回滚，影响其他测试

**解决方案**：

```java
@DataJpaTest
@Transactional
class RepositoryTest {
    // 测试方法会自动回滚

    @Test
    void testSave() {
        // 数据会在测试后自动回滚
    }
}
```

#### 3. 并发测试

**问题**：测试涉及多线程，结果不稳定

**解决方案**：

```java
@Test
void testConcurrentAccess() throws Exception {
    int threadCount = 10;
    CountDownLatch latch = new CountDownLatch(threadCount);
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
            try {
                service.doSomething();
            } finally {
                latch.countDown();
            }
        });
    }

    latch.await(5, TimeUnit.SECONDS);
    executor.shutdown();

    // 验证结果
}
```


## 最佳实践与模式

### 1. 测试类组织

```java
@SpringBootTest
@DisplayName("用户服务测试")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Nested
    @DisplayName("创建用户测试")
    class CreateUserTests {
        @Test
        @DisplayName("成功创建用户")
        void shouldCreateUserSuccessfully() {
            // 测试代码
        }

        @Test
        @DisplayName("用户名重复时抛出异常")
        void shouldThrowExceptionWhenUsernameExists() {
            // 测试代码
        }
    }

    @Nested
    @DisplayName("查询用户测试")
    class FindUserTests {
        // 查询相关测试
    }
}
```

### 2. 参数化测试

```java
@ParameterizedTest
@CsvSource({
    "john, john@example.com, true",
    "jane, jane@example.com, true",
    "invalid, invalid-email, false"
})
void testUserValidation(String username, String email, boolean expected) {
    User user = new User(username, email);
    assertThat(userValidator.isValid(user)).isEqualTo(expected);
}

@ParameterizedTest
@MethodSource("provideUsers")
void testUserCreation(User user) {
    User created = userService.createUser(user);
    assertThat(created.getId()).isNotNull();
}

static Stream<User> provideUsers() {
    return Stream.of(
        new User("user1", "user1@example.com"),
        new User("user2", "user2@example.com")
    );
}
```

### 3. AssertJ 断言

```java
@Test
void testAssertJ() {
    User user = userService.getUser(1L);

    // 基础断言
    assertThat(user).isNotNull();
    assertThat(user.getId()).isEqualTo(1L);
    assertThat(user.getUsername()).isEqualTo("test");

    // 链式断言
    assertThat(user)
        .hasFieldOrPropertyWithValue("id", 1L)
        .hasFieldOrPropertyWithValue("username", "test")
        .extracting("email")
        .isEqualTo("test@example.com");

    // 集合断言
    List<User> users = userService.getAllUsers();
    assertThat(users)
        .hasSize(3)
        .extracting("username")
        .containsExactly("user1", "user2", "user3");
}
```


## 验收标准与交付物

### 验收标准（必须全部满足）

- [ ] `./mvnw clean test` **全部通过**
- [ ] **无**不必要的生产代码入侵性修改；所有变更有充分测试覆盖
- [ ] 对 Spring Boot/JUnit/Mockito 的**兼容性问题已消除**，无关键弃用警告
- [ ] 新增或变更的行为均有**明确断言**与**文档说明**
- [ ] 测试覆盖率达标（建议 ≥ 80%）

### 交付物清单

- [ ] `src/test/resources/README_debug.md` - 调试文档
- [ ] 所有修复的测试文件
- [ ] 新增的边界条件测试文件（如需要）
- [ ] 更新的配置文件（`pom.xml`、`build.gradle`、测试配置等）
- [ ] Git 提交信息（Conventional Commits 格式）


## 附录：常见场景示例

### 场景 1：修复 Spring Bean 注入问题

**问题描述**：
```text
NoSuchBeanDefinitionException: No qualifying bean of type 'UserRepository'
```

**修复步骤**：

1. **添加 @SpringBootTest 注解**：
   ```java
   @SpringBootTest
   class UserServiceTest {
       @Autowired
       private UserService userService;
   }
   ```

2. **或使用 @MockBean**：
   ```java
   @SpringBootTest
   class UserServiceTest {
       @MockBean
       private UserRepository userRepository;

       @Autowired
       private UserService userService;
   }
   ```

3. **验证修复**：
   ```bash
   ./mvnw test -Dtest=UserServiceTest
   ```


### 场景 2：修复 Mockito 配置问题

**问题描述**：
```text
NullPointerException at line 25
```

**修复步骤**：

1. **添加 Mockito 扩展**：
   ```java
   @ExtendWith(MockitoExtension.class)
   class UserServiceTest {
       @Mock
       private UserRepository userRepository;

       @InjectMocks
       private UserService userService;
   }
   ```

2. **验证修复**：
   ```bash
   ./mvnw test -Dtest=UserServiceTest
   ```


### 场景 3：修复数据库测试问题

**问题描述**：
数据库约束违反或事务问题

**修复步骤**：

1. **使用 @DataJpaTest**：
   ```java
   @DataJpaTest
   class UserRepositoryTest {
       @Autowired
       private UserRepository userRepository;

       @Autowired
       private TestEntityManager entityManager;

       @Test
       void testSave() {
           User user = new User("test", "test@example.com");
           User saved = userRepository.save(user);
           entityManager.flush();

           assertThat(saved.getId()).isNotNull();
       }
   }
   ```

2. **验证修复**：
   ```bash
   ./mvnw test -Dtest=UserRepositoryTest
   ```


## 使用方式

在 Cursor 中通过命令面板（Cmd+Shift+P）或直接引用此命令，AI 将帮助你：

1. **运行测试**：执行指定的测试命令并收集失败信息
2. **诊断问题**：使用快速诊断决策树分析失败原因，识别兼容性和实现问题
3. **修复代码**：按优先级修复测试和生产代码，遵循最小变更原则
4. **生成报告**：创建调试文档和变更说明

## 相关命令

- `test-governance`: 执行 Java 测试治理任务
- `code-review`: 代码审查
- `git-commit`: 提交修复变更


**最后更新**：2025-12-01
**维护者**：Test Infrastructure Team
**版本**：**1.0.0（Java 测试修复专版）**
