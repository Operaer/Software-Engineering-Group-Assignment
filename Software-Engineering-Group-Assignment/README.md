# 软件工程小组作业 - TA招聘系统

**项目类型**: 大学软件工程课程小组作业  
**技术栈**: Java Servlet + JSP + 嵌入式Tomcat  
**最后更新**: 2026年4月1日

## 项目目标

实现一个校园TA（教学助理）招聘管理系统，包含用户权限管理、申请流程等核心功能。

## 我做了什么（zzc）

### 代码重构优化

- **创建BaseServlet基类**: 统一处理登录检查、权限验证，减少重复代码
- **合并Dashboard页面**: 从3个独立页面合并为1个动态模板
- **配置集中管理**: 新增AppConfig类，避免硬编码
- **包结构优化**: 保持清晰分层，移除不必要嵌套

### 权限管理系统实现 (US02)
**需求**: 三级权限架构，安全管控，防误操作

**实现内容**:
- **三级角色**: TA(学生) < MO(模块组织者) < ADMIN(管理员)
- **安全认证**: 密码登录 + 账户状态管理
- **权限隔离**: 严格检查，防止越界访问
- **数据隔离**: 用户只能访问自己的数据
- **管理员限制**: 不能随意修改MO内容


## 如何运行项目

### 环境要求
- Java 21+
- Maven 3.6+ (或使用run.bat脚本)

### 运行步骤
1. **进入项目目录**:
   ```bash
   cd ta-recruitment
   ```

2. **运行应用**:
   ```bash
   # 方法1: 使用Maven 
   mvn exec:java

   # 方法2: 使用批处理脚本
   run.bat
   ```

3. **访问系统**:
   打开浏览器: http://localhost:8080

### 测试账户
| 角色 | 邮箱 | 密码 | 权限说明 |
|------|------|------|----------|
| TA | ta@example.com | password | 个人资料 + 申请管理 |
| MO | mo@example.com | password | TA权限 + 申请审核 + 职位发布 |
| ADMIN | admin@example.com | password | 所有权限 + 用户管理 |

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
│   │   ├── DashboardServlet.java      # 仪表板
│   │   ├── LoginServlet.java          # 登录
│   │   └── ...                        # 其他功能Servlet
│   └── storage/                       # 数据存储层
├── src/main/webapp/
│   ├── index.jsp                      # 登录页面
│   ├── WEB-INF/includes/              # JSP模板
│   │   ├── header.jsp                 # 页面头部
│   │   ├── base_dashboard.jsp         # 统一仪表板 
│   │   └── footer.jsp                 # 页面底部
│   ├── secure/                        # 受保护页面
│   │   ├── ta/                        # TA页面
│   │   ├── mo/                        # MO页面
│   │   └── admin/                     # 管理员页面
│   └── assets/                        # CSS/JS资源
├── pom.xml                            # Maven配置
└── run.bat                            # 运行脚本
```

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
// 1. 创建Servlet
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

#### 添加新权限
```java
// 在PermissionChecker.java中添加方法
public static boolean canDoSomething(User user) {
    return hasPermission(user, User.Role.MO);
}
```

#### 修改数据存储
```java
// 如果要换数据库，修改Storage类
// 当前用JSON，改成JDBC即可
```



---

| GitHub Username | QMID |
| --- | --- |
| qin204 | 231223380 |
| Cloudiers1 | 231223405 |
| Operaer | 231223391 |
| MY123456-A11Y | 231223427 |
| Zzc-bot | 231223346 |
| Joseph1Stalin | 231223416 |
