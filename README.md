# 软件工程小组作业 - TA招聘系统

**项目类型**: 大学软件工程课程小组作业  
**技术栈**: Java Servlet + JSP + 嵌入式Tomcat  
**最后更新**: 2026年4月4日

## 项目目标

实现一个校园TA（教学助理）招聘管理系统，包含用户权限管理、申请流程等核心功能。支持用户自助注册和账户管理。

## 我做了什么（Wenqi）

### 用户认证系统完善 (US01)
**需求**: 完整的用户生命周期管理，包括注册、登录、密码修改

**实现内容**:
- **用户注册功能**: 支持邮箱格式验证和用户名规范检查
- **密码修改功能**: 已登录用户可以修改账户密码，增强安全性
- **登录系统**: 安全的密码验证和会话管理
- **账户状态管理**: 支持账户启用/禁用处理
- **会话控制**: 完善的登出机制和会话超时管理

### 代码架构优化

- **创建BaseServlet基类**: 统一处理登录检查、权限验证，减少重复代码
- **合并Dashboard页面**: 从多个独立页面合并为1个动态模板
- **配置集中管理**: 新增AppConfig类，避免硬编码
- **包结构优化**: 保持清晰分层，移除不必要嵌套
- **数据隔离设计**: 基于用户角色的数据访问控制


## 如何运行项目

### 环境要求
- Java 21+
- Maven 3.6+

### 运行步骤
1. **进入项目目录**:
   ```bash
   cd ta-recruitment
   ```

2. **运行应用**:
   ```bash
   # 使用Maven 
   mvn clean compile exec:java

   # 或使用批处理脚本
   run.bat
   ```

3. **访问系统**:
   打开浏览器: http://localhost:8080

### 测试账户
| 角色 | 邮箱 | 密码 | 权限说明 |
|------|------|------|----------|
| TA | ta@example.com | password | 用户注册 + 个人资料 + 申请管理 |
| MO | mo@example.com | password | TA权限 + 申请审核 + 职位发布 |
| ADMIN | admin@example.com | password | 所有权限 + 用户管理 + 账户禁用 |

**新增功能**: 所有用户可通过 http://localhost:8080/register 页面自助注册
已登录用户可在账户设置中修改密码

## 项目结构说明

```
ta-recruitment/
├── src/main/java/com/bupt/ta/
│   ├── config/AppConfig.java          # 配置文件
│   ├── filter/AuthFilter.java         # 认证过滤器
│   ├── model/                         # 数据模型 (User, Application, etc.)
│   ├── security/PermissionChecker.java # 权限检查工具
│   ├── servlet/
│   │   ├── BaseServlet.java           # Servlet基类
│   │   ├── RegisterServlet.java       # 用户注册处理
│   │   ├── LoginServlet.java          # 用户登录
│   │   ├── ChangePasswordServlet.java # 修改密码
│   │   ├── DashboardServlet.java      # 仪表板
│   │   ├── ProfileServlet.java        # 个人资料
│   │   └── ...                        # 其他功能Servlet
│   └── storage/                       # 数据存储层
├── src/main/webapp/
│   ├── index.jsp                      # 登录页面
│   ├── register.jsp                   # 注册页面（新增）
│   ├── WEB-INF/includes/              # JSP模板
│   │   ├── header.jsp                 # 页面头部
│   │   ├── base_dashboard.jsp         # 统一仪表板 
│   │   └── footer.jsp                 # 页面底部
│   ├── secure/                        # 受保护页面
│   │   ├── account/
│   │   │   └── change_password.jsp    # 修改密码页面（新增）
│   │   ├── ta/                        # TA页面
│   │   ├── mo/                        # MO页面
│   │   └── admin/                     # 管理员页面
│   └── assets/                        # CSS/JS资源
├── pom.xml                            # Maven配置
└── run.bat                            # 运行脚本
```

## 核心功能详解

### 1. 用户注册 (RegisterServlet)
- **入口**: GET `/register` 显示注册表单
- **提交**: POST `/register` 处理注册数据
- **验证**: 
  - 邮箱格式: `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$`
  - 用户名: `^[A-Za-z0-9_]{3,20}$`（3-20字符）
- **防护**: 已登录用户直接跳转到仪表板
- **角色**: 新注册用户默认为 TA 角色

### 2. 修改密码 (ChangePasswordServlet)
- **入口**: GET `/secure/account/change-password` 显示表单
- **提交**: POST `/secure/account/change-password` 处理修改
- **验证**: 
  - 当前密码正确性验证
  - 新密码确认匹配
  - 新密码是否与当前密码相同
- **权限**: 仅已登录用户可访问
- **安全**: 支持密码重置和加密存储

### 3. 权限管理体系
- **TA 角色**: 可以注册、登录、修改个人密码、管理自己的申请
- **MO 角色**: 拥有TA的全部权限，外加应用审核、职位发布等功能
- **ADMIN 角色**: 拥有系统全部功能，包括用户管理、账户禁用等

## 后续开发指南

### 添加新功能的一般步骤

1. **确定功能需求** (如: 添加消息通知功能)

2. **创建数据模型** (如果需要)
   ```java
   // 在 model/ 包下创建新类
   public class Message {
       // 字段和方法
   }
   ```

3. **实现数据存储** (如果需要)
   ```java
   // 在 storage/ 包下创建新类
   public class MessageStorage {
       // 继承或参考现有Storage类
   }
   ```

4. **创建Servlet控制器**
   ```java
   // 继承BaseServlet
   @WebServlet("/secure/messages")
   public class MessageServlet extends BaseServlet {
       @Override
       protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
           requireLogin(req, resp);  // 检查登录
           // 你的业务逻辑
       }
   }
   ```

5. **添加权限检查** (如果需要)
   ```java
   requirePermission(req, resp, User.Role.MO);  // 检查权限
   ```

6. **创建JSP页面**
   ```jsp
   <%@ include file="/WEB-INF/includes/header.jsp" %>
   <!-- 你的页面内容 -->
   <%@ include file="/WEB-INF/includes/footer.jsp" %>
   ```

7. **更新导航** (在base_dashboard.jsp中添加链接)

### 常用开发模式

#### 添加新页面
```java
// 1. 创建Servlet (参考RegisterServlet 或 ChangePasswordServlet)
@WebServlet("/secure/new-feature")
public class NewFeatureServlet extends BaseServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        requireLogin(req, resp);
        // 业务逻辑
        forwardTo(req, resp, "/secure/new-feature.jsp");
    }
}

// 2. 创建JSP页面
// 3. 在dashboard中添加链接
```

#### 验证用户输入
```java
// 使用正则表达式，参考RegisterServlet的方式
private static final Pattern EMAIL_PATTERN = 
    Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

if (!EMAIL_PATTERN.matcher(email).matches()) {
    // 处理验证错误
}
```

#### 处理密码操作
```java
// 参考ChangePasswordServlet的实现步骤：
// 1.验证当前密码
// 2.检查确认密码匹配
// 3.检查新密码与当前密码不同
// 4.更新存储
```

#### 修改数据存储
```java
// 当前使用JSON存储，要换成数据库：
// 修改Storage类的实现
// 参考 ApplicationStorage / UserStorage / ProfileStorage
```

## 新增功能对比 (US01 vs 基础版本)

| 功能 | 基础版本 | US01 |
|------|---------|------|
| 用户登录 | ✓ | ✓ |
| 用户注册 | ✗ | ✓ |
| 修改密码 | ✗ | ✓ |
| TA管理申请 | ✓ | ✓ |
| MO审核申请 | ✓ | ✓ |
| ADMIN用户管理 | ✓ | ✓ |
| 账户状态管理 | 基础 | 完整 |
| 数据隔离 | ✓ | ✓ |

## 已知限制与改进方向

1. **当前数据存储**: 使用JSON文件，重启后数据不持久化（可改为数据库）
2. **密码安全**: 建议添加密码强度检查和加密存储
3. **验证机制**: 可增加邮箱验证和两因素认证
4. **UI/UX**: 当前使用基础JSP，可升级为前端框架
5. **测试覆盖**: 建议添加单元测试和集成测试

---

| GitHub Username | QMID |
| --- | --- |
| qin204 | 231223380 |
| Cloudiers1 | 231223405 |
| Operaer | 231223391 |
| MY123456-A11Y | 231223427 |
| Zzc-bot | 231223346 |
| Joseph1Stalin | 231223416 |
