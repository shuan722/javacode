# 初学Java案例

## 1. Git初始化

### 1.1 初始化本地仓库

```
git init
```

### 1.2 关联远程仓库

```
git remote add origin https://github.com/yourusername/javacode.git
```

### 1.3 添加并提交本地代码

```
git add .
git commit -m "Initial commit"
```

### 1.4 推送代码到远程仓库

```
git push -u origin master
```

### 仓库级配置代理
```shell
git config http.proxy 'http://proxy.server:port'
git config https.proxy 'https://proxy.server:port'
```

## 2. Java处理excel表格

## 3. Java多线程处理文本文件

## 4. 高效拷贝文件

![img.png](images/img.png)

```mermaid
flowchart TD
    A[Java IO] --> B[基础操作]
    A --> C[高级特性]
    A --> D[企业应用]
    
    B --> B1[文件复制]
    B --> B2[配置管理]
    B --> B3[控制台交互]
    
    C --> C1[对象序列化]
    C --> C2[加密传输]
    C --> C3[内存映射]
    
    D --> D1[日志系统]
    D --> D2[数据迁移]
    D --> D3[实时监控]
    
    C1 -->|设计模式| D[装饰器模式]
    C2 -->|安全规范| E[密钥管理]
    D3 -->|性能优化| F[异步IO]
```