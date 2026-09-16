# 小兔鲜商城后端系统

对标**黑马程序员「苍穹外卖」**架构，基于 `Spring Boot + MyBatis-Plus + MySQL + Redis + JWT` 实现的「小兔鲜商城」后端，为已有的 `vue-rabbit`（Vue3 + Pinia + ElementPlus）前端提供接口支撑。

---

## 一、技术栈与选型理由

| 技术 | 版本 | 说明 |
| --- | --- | --- |
| Spring Boot | 2.7.3 | 约定大于配置，生态成熟，适合快速搭建企业级项目（苍穹外卖同版本） |
| MyBatis-Plus | 3.5.3.1 | 单表 CRUD 免写 SQL，配合 `LambdaQueryWrapper` 提升开发效率 |
| MySQL | 8.x | 关系型数据（用户/商品/订单）落地存储，`utf8mb4` 支持 emoji |
| Redis | 7.x | 缓存热点数据 + 购物车 + Token 黑名单，扛高并发读 |
| JWT (jjwt 0.9.1) | 0.9.1 | 无状态认证，配合拦截器 + ThreadLocal 解析登录用户 |
| Knife4j | 3.0.3 | 在线接口文档，方便前后端联调 |

**密码加密为什么选 BCrypt 而不是 MD5？**
MD5 是哈希摘要，无盐且可被彩虹表 / 快速碰撞破解，不适合存密码；BCrypt 自带随机盐、不可逆、计算耗时可控（可调 cost 因子），是业界通行做法。项目引入 `spring-security-crypto` 仅用于 `BCryptPasswordEncoder`，不引入整套 Spring Security 过滤器链，保持轻量。

---

## 二、项目结构（Maven 多模块）

```
springboot-rabbit
├── pom.xml                        # 父工程 tuxian-mall，统一依赖版本 + 聚合子模块
├── sql/tuxian_mall.sql            # 数据库初始化脚本（含种子数据）
├── tuxian-common                  # 公共模块
│   └── constant / context / exception / json / properties / result / utils
├── tuxian-pojo                    # 数据对象模块
│   └── entity / dto / vo
└── tuxian-server                  # 服务模块（启动 + 业务）
    └── config / controller / interceptor / mapper / service / handler
```

- `tuxian-common`：`Result`、`PageResult`、`BaseContext`（ThreadLocal）、`BaseException`、`JwtUtil`、`JacksonObjectMapper`、`JwtProperties`、常量类。
- `tuxian-pojo`：与数据库表对应的实体、接口入参 DTO、返回给前端的 VO。
- `tuxian-server`：配置类、JWT 拦截器、Mapper、Service、Controller、全局异常处理、启动类。

---

## 三、快速开始

按顺序启动：**MySQL → Redis(Docker) → 后端 → 前端**。

**TL;DR —— 日常启动（数据库已建好，无需重建）：**

```powershell
# 1. 先打开 Docker Desktop，再启动 Redis
docker start tuxian-redis

# 2. 终端1：启动后端（在仓库根目录，即 springboot-rabbit 的上一级）
java -jar springboot-rabbit\tuxian-server\target\tuxian-server-1.0.0.jar

# 3. 终端2：启动前端
cd vue-rabbit
npm run dev
```

浏览器打开 http://localhost:5173，用 `18610848230 / 123456` 登录。

### 1. 环境准备
- JDK 17
- Maven 3.6+
- MySQL 8
- Redis 7（用 Docker 运行，容器名 `tuxian-redis`，端口 `6379`）
- Node.js + npm（前端依赖）

### 2. 初始化数据库（首次）
在 `springboot-rabbit` 目录下，用 **utf8mb4** 导入（否则中文会报 `Data too long`）：

```powershell
# -p 后面不跟密码，回车后会提示输入
cmd /c "mysql --default-character-set=utf8mb4 -uroot -p < sql\tuxian_mall.sql"
```

脚本会自动建库 `tuxian_mall` 并插入种子数据，可重复执行（先 DROP 再 CREATE）。

### 3. 配置本地密码与密钥（首次必做）
仓库里的 `application.yml` 是**不含敏感信息**的模板：数据库密码、JWT 签名密钥被抽到了 `application-local.yml`，该文件已被 `.gitignore` 排除，由每位开发者本地自己维护。

```powershell
cd springboot-rabbit\tuxian-server\src\main\resources
copy application-local.yml.example application-local.yml
```

然后编辑 `application-local.yml`，把 `CHANGE_ME` 换成自己的值：

| 配置项 | 填什么 |
| --- | --- |
| `spring.datasource.password` | 你的 MySQL 密码（用户名在 `application.yml` 里，默认 `root`） |
| `tuxian.jwt.user-secret-key` | 用户端 JWT 签名密钥，建议 32 位以上随机串 |
| `tuxian.jwt.admin-secret-key` | 管理端 JWT 签名密钥，必须与用户端不同 |

`application.yml` 里的 `spring.profiles.active: local` 会自动把这份文件合并进来，不需要额外加启动参数。Redis 本机默认无密码，若你的有密码，在 `application-local.yml` 里补 `spring.redis.password`。

### 4. 启动 Redis（Docker）
先打开 **Docker Desktop**，等它就绪后启动容器：

```powershell
docker start tuxian-redis
```

> 首次没有容器时创建：`docker run -d --name tuxian-redis -p 6379:6379 redis:7-alpine`

### 5. 启动后端
方式一（命令行，推荐）：

```powershell

# 需先确保 JAVA_HOME 指向 JDK 17，且 mvn 可用（没加进 PATH 就用你的 Maven 全路径）
mvn -f springboot-rabbit\pom.xml -pl tuxian-server -am clean package -DskipTests
java -jar springboot-rabbit\tuxian-server\target\tuxian-server-1.0.0.jar
```

方式二（IDEA）：打开 `springboot-rabbit`，运行 `TuxianApplication`。

- 接口文档：<http://localhost:8080/doc.html>
- 服务端口：`8080`

### 6. 启动前端（vue-rabbit）
```powershell
cd vue-rabbit
npm install   # 首次
npm run dev
```
前端跑在 `http://localhost:5173`，浏览器打开即可联调。

### 7. 前端适配（已配好）
前端两处 `baseURL` 已改为本地后端 `http://localhost:8080`：
1. `vue-rabbit/src/utils/http.js` → `baseURL: 'http://localhost:8080'`
2. `vue-rabbit/src/views/Pay/index.vue` → `const baseURL = 'http://localhost:8080/'`

### 8. 测试账号
| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 用户 | `18610848230` | `123456` |
| 管理员 | `admin` | `admin123` |

### 9. 常见坑
- **SQL 导入中文报 `Data too long`**：文件是 UTF-8，客户端却按系统 GBK 读导致的，加 `--default-character-set=utf8mb4` 即可。
- **商品接口报 SQL 语法错误**：曾把 `description` 列别名成 MySQL 保留字 `desc`，已在 `Goods` 实体把字段改为 `description`（前端返回字段仍是 `desc`）。

---

## 四、接口清单（均已适配前端）

| 模块 | 方法 | 路径 | 是否需要登录 |
| --- | --- | --- | --- |
| 首页 | GET | `/home/banner` | 否 |
| 首页 | GET | `/home/new` | 否 |
| 首页 | GET | `/home/hot` | 否 |
| 首页 | GET | `/home/goods` | 否 |
| 首页 | GET | `/home/category/head` | 否 |
| 登录 | POST | `/login` | 否 |
| 登录 | POST | `/logout` | 否 |
| 商品 | GET | `/goods?id=` | 否 |
| 商品 | GET | `/goods/hot` | 否 |
| 商品 | GET | `/goods/relevant` | 否 |
| 分类 | GET | `/category?id=` | 否 |
| 分类 | GET | `/category/sub/filter?id=` | 否 |
| 分类 | POST | `/category/goods/temporary` | 否 |
| 用户 | GET | `/member/user` | 是 |
| 购物车 | GET/POST/PUT/DELETE | `/member/cart` | 是 |
| 购物车 | POST | `/member/cart/merge` | 是 |
| 地址 | GET/POST/PUT/DELETE | `/member/address` | 是 |
| 订单 | GET | `/member/order/pre`、`/member/order/pre/now` | 是 |
| 订单 | POST | `/member/order` | 是 |
| 订单 | GET | `/member/order`、`/member/order/{id}` | 是 |
| 订单 | PUT/DELETE | `/member/order/{id}/cancel`、`/member/order/{id}` | 是 |
| 支付 | GET | `/pay/aliPay`（模拟支付跳转） | 否 |
| 后台 | POST/GET | `/admin/login`、`/admin/dashboard` | 管理端 JWT |

> 统一返回：成功 `{ "code": "1", "msg": "操作成功", "result": ... }`；失败 `{ "code": "0", "msg": "...", "result": null }`，且失败会返回非 2xx 状态码，前端 axios 拦截器读取 `data.message` 弹出提示（因此失败时同时携带了 `message` 字段）。

---

## 五、关键业务实现说明

### 1. JWT 登录认证流程
1. `POST /login` 校验账号密码（BCrypt），生成 JWT（载荷存 `userId`）返回给前端；
2. 前端每次请求在 `Authorization: Bearer <token>` 头携带令牌；
3. `JwtTokenUserInterceptor` 拦截 `/member/**`：取 token → 查 Redis 黑名单 → 解析出 `userId` → 存入 `BaseContext`（ThreadLocal）→ 放行；
4. 拦截器 `afterCompletion` 调用 `BaseContext.removeCurrentId()` 防止内存泄漏；
5. 退出登录时把 token 写入 Redis 黑名单，有效期与 token 剩余时长一致。

### 2. ThreadLocal 上下文
`BaseContext` 用 `ThreadLocal<Long>` 存当前登录用户 ID，Service 层直接 `BaseContext.getCurrentId()` 获取，避免从 Controller 层层传参。

### 3. 购物车（Redis Hash）
购物车存 Redis Hash：`key = cart:{userId}`，`field = skuId`，`value = 购物车项 JSON`。不落 MySQL，读写快；登录后用 `/member/cart/merge` 把本地购物车合并进服务端。

### 4. 订单事务 + 库存扣减（防超卖）
下单、扣库存、生成订单项在同一个 `@Transactional` 中；扣库存用**条件更新**：
```sql
UPDATE goods_sku SET inventory = inventory - ? WHERE id = ? AND inventory >= ?
```
只有当库存充足时才扣减，从数据库层面防止超卖（高并发可进一步叠加 Redis 分布式锁）。

### 5. 缓存一致性
分类、商品详情、首页推荐等用 `@Cacheable` 缓存到 Redis；采用「先更新数据库、再删缓存」策略，管理端变更分类/商品时用 `@CacheEvict` 清缓存。

### 6. 统一返回与异常处理
`Result<T>` / `PageResult<T>` 统一返回；`GlobalExceptionHandler` 捕获业务异常、参数校验异常、未知异常并转成统一结构。

---

## 六、面试亮点

1. **多模块工程拆分**（common/pojo/server），职责清晰、可复用；
2. **JWT 无状态认证** + 自定义拦截器 + ThreadLocal 上下文，管理端与用户端两套密钥隔离；
3. **Redis 三种典型用法**：热点数据缓存（Spring Cache）、购物车 Hash、Token 黑名单；
4. **订单扣库存防超卖**：数据库条件更新 + 事务保证一致性；
5. **密码安全**：BCrypt 加盐哈希，不存储明文；
6. **统一异常处理 + 统一返回结构 + 参数校验 + 跨域**，工程规范。

---

## 七、说明

- 前期需求文档里的部分「接口文档」路径（如 `/hot/new`）与真实前端代码不一致，本后端**以真实前端代码为准**进行适配（`/home/new` 等）。
- 后台管理模块前端暂未提供，本后端实现了登录 + 看板的基础能力，其余管理接口可按需扩展（标注「待前端确认」）。
- 商品/分类/轮播图已从 `picsum.photos` 占位图换成与文案相干的本地真实图片，存放在
  `vue-rabbit/public/images/`（69 张，前端经 Vite 以 `/images/xxx.jpg` 访问）。
  - 重新抓取：`powershell -File scripts/fetch-images.ps1`（已存在且有效的图会跳过，`-Force` 全部重抓，`-Only a,b` 只补某几张）。
  - 图源是百度图片（Bing 对这类中文词返回的结果基本不相干，脚本注释里写了排查过程）；
    百度 CDN 上限 500px，所以非轮播图成品最大 500px，源图不够大就不放大。
  - 种子文件 `sql/tuxian_mall.sql` 里的图片路径已经是 `/images/...` 的最终状态，建库时直接生效，不存在额外的改图脚本。
  - 改完记得清缓存，否则首页仍是旧的：缓存不在默认的 db0，而在 **db10**
    （`docker exec tuxian-redis redis-cli -n 10 DEL homeGoods::homeGoods categoryHead::head homeHotGoods::hot homeNewGoods::new`）。
