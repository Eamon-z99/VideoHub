# 数据库说明

## 数据库设计

### 核心表结构

1. **users** - 用户表
   - 存储用户基本信息、登录凭证

2. **videos** - 视频表
   - 存储视频元数据（标题、时长、存储路径等）
   - 关联用户ID（上传者）

3. **transcode_tasks** - 转码任务表
   - 跟踪视频转码进度和状态

4. **play_history** - 播放历史表
   - 记录用户播放记录

5. **favorites** - 收藏表
   - 用户收藏的视频

6. **comments** - 评论表
   - 视频评论（支持回复）

7. **video_likes** - 视频点赞表
   - 用户对视频的点赞

8. **comment_likes** - 评论点赞表
   - 用户对评论的点赞

9. **follows** - 关注表
   - 用户之间的关注关系

## 使用方法

### 1. 创建数据库

```bash
# 方式1：使用MySQL命令行
mysql -u root -p < src/main/resources/db/schema.sql

# 方式2：在MySQL客户端中执行
source src/main/resources/db/schema.sql
```

### 2. 配置数据库连接

在 `application.properties` 中添加：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/videohub?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA配置（如果使用）
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### 3. 添加MySQL依赖

在 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

## 索引说明

- **主键索引**：所有表都有自增主键
- **唯一索引**：防止重复数据（如用户账号、视频ID）
- **外键索引**：关联查询优化
- **时间索引**：按时间排序查询优化

## 注意事项

1. **字符集**：使用 `utf8mb4` 支持emoji和特殊字符
2. **外键约束**：设置了级联删除，删除用户时会删除相关数据
3. **时间字段**：使用 `DATETIME` 类型，自动更新 `update_time`
4. **状态字段**：使用字符串类型，便于扩展

## 后续扩展

如果需要添加新功能，可以考虑：

- **标签表（tags）**：视频标签分类
- **分类表（categories）**：视频分类
- **弹幕表（danmaku）**：弹幕数据
- **消息表（messages）**：用户私信
- **通知表（notifications）**：系统通知









