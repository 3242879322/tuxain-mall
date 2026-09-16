# 小兔鲜商城（Tuxian Mall）

之前b站学习了黑马程序员的前端vue3小兔鲜项目,随后便自己用claude+deepseek完成了后端,本人在校大学生,技术实在难拿手,勉强做成这个模样,也懒得改随便看看得了

前后端分离的全栈电商练手项目：**Vue 3 前端 + Spring Boot 后端**，覆盖首页推荐、分类筛选、商品详情与 SKU 选择、购物车、下单、支付（模拟）的完整链路。

采用后端分层架构，前端基于 Vue 3 + Pinia + Element Plus。

---

## 技术栈

| 端 | 技术 |
| --- | --- |
| 后端 | Spring Boot 2.7.3 · MyBatis-Plus 3.5.3.1 · MySQL 8 · Redis 7 · JWT (jjwt) · BCrypt · Knife4j |
| 前端 | Vue 3 · Vite 4 · Pinia · Vue Router · Element Plus · Axios · Sass |

## 目录结构

```
rabbit/
├── springboot-rabbit/           # 后端（Maven 多模块）
│   ├── tuxian-common/           # Result / PageResult / JwtUtil / 常量 / 异常 / ThreadLocal 上下文
│   ├── tuxian-pojo/             # entity / dto / vo
│   ├── tuxian-server/           # controller / service / mapper / 拦截器 / 配置 / 全局异常处理
│   ├── sql/tuxian_mall.sql      # 建库 + 种子数据（图片路径已指向本地图片，可直接导入）
│   └── scripts/fetch-images.ps1 # 抓取商品图的辅助脚本（图片已随仓库提供，一般用不到）
└── vue-rabbit/                  # 前端
    ├── public/images/           # 69 张商品/分类/轮播图，种子里以 /images/xxx.jpg 引用
    └── src/                     # apis / components / router / stores / views / utils
```

两个子项目各有自己的 README，细节看那里：

- [后端 README](springboot-rabbit/README.md) —— 接口清单、JWT 认证流程、缓存策略、防超卖实现
- [前端 README](vue-rabbit/README.md) —— 页面结构、状态管理、请求封装

---

## 快速开始

需要 **JDK 17、Maven、MySQL 8、Docker、Node.js**。

### 1. 建库

```powershell
cd springboot-rabbit
# -p 后面不跟密码，回车后会提示输入
cmd /c "mysql --default-character-set=utf8mb4 -uroot -p < sql\tuxian_mall.sql"
```

必须带 `--default-character-set=utf8mb4`，否则中文会报 `Data too long`。脚本会建库 `tuxian_mall` 并插入种子数据，可重复执行。

### 2. 起 Redis

```powershell
docker run -d --name tuxian-redis -p 6379:6379 redis:7-alpine
```

### 3. 填本地配置（必做）

`application.yml` 是**不含敏感信息**的模板，数据库密码和 JWT 密钥被抽到了 `application-local.yml`（已被 `.gitignore` 排除）：

```powershell
cd springboot-rabbit\tuxian-server\src\main\resources
copy application-local.yml.example application-local.yml
```

然后编辑 `application-local.yml`，把 `CHANGE_ME` 换成自己的 MySQL 密码和两个随机 JWT 密钥。

### 4. 起后端

```powershell
# 需先确保 JAVA_HOME 指向 JDK 17，且 mvn 可用
mvn -f springboot-rabbit\pom.xml -pl tuxian-server -am clean package -DskipTests
java -jar springboot-rabbit\tuxian-server\target\tuxian-server-1.0.0.jar
```

后端跑在 `http://localhost:8080`，接口文档 <http://localhost:8080/doc.html>。

### 5. 起前端

```powershell
cd vue-rabbit
npm install
npm run dev
```

浏览器打开 <http://localhost:5173>。

### 6. 测试账号

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 用户 | `18610848230` | `123456` |
| 管理员 | `admin` | `admin123` |

---

## 已实现的功能

- **首页**：轮播图、一级分类、新鲜好物、人气推荐、猜你喜欢
- **分类**：一级分类 → 二级分类 → 按品牌/尺码等 SKU 维度筛选
- **商品详情**：主图切换、SKU 规格选择、热销推荐、相关推荐
- **购物车**：未登录存本地、登录后合并进 Redis Hash，支持增删改选
- **订单**：结算页、下单（事务内扣库存防超卖）、订单列表 / 详情 / 取消 / 删除
- **登录注册**：JWT 无状态认证 + Redis Token 黑名单
- **支付**：模拟支付宝跳转与回调
- **后台**：管理员登录、数据看板（其余管理接口预留）

## 关于图片

商品图、分类图、轮播图用的是仓库内的本地真实图片（`vue-rabbit/public/images/`，共 69 张），不是外链占位图。种子里存的是根相对路径 `/images/xxx.jpg`，由 Vite 开发服务器直接托管，因此 `npm run dev` 起来即可访问。

需要重新抓图时用 `springboot-rabbit/scripts/fetch-images.ps1`（脚本是 Windows PowerShell，依赖外网图源，非必需）。

## License

[MIT](LICENSE)
