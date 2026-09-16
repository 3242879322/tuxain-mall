# vue-rabbit —— 小兔鲜商城前端

Vue 3 + Vite 4 实现的电商前台，配合 [`springboot-rabbit`](../springboot-rabbit) 后端使用。

## 技术栈

| 用途 | 选型 |
| --- | --- |
| 框架 | Vue 3（`<script setup>` 组合式 API） |
| 构建 | Vite 4 |
| 状态管理 | Pinia + `pinia-plugin-persistedstate`（用户态持久化） |
| 路由 | Vue Router 4（含登录前置守卫） |
| UI | Element Plus（按需自动引入） |
| 请求 | Axios（统一封装 + 拦截器） |
| 样式 | Sass |
| 工具 | VueUse、Day.js |

`unplugin-auto-import` / `unplugin-vue-components` 已配置，`ref`、`computed`、`ElMessage` 等无需手写 import。

## 目录结构

```
src/
├── apis/            # 按模块拆分的接口函数：home / category / detail / cart / order / pay / user / checkout
├── components/      # 通用组件：ImageView（图片懒加载）、XtxSku（SKU 规格选择）
├── directives/      # 自定义指令（图片懒加载）
├── router/          # 路由表 + 登录守卫
├── stores/          # Pinia：userStore（登录态）、cartStore（购物车）、categoryStore（分类缓存）
├── styles/          # 全局样式 + Element Plus 变量覆盖
├── utils/http.js    # Axios 实例：baseURL、token 注入、错误统一提示
└── views/           # 页面
    ├── Layout/      # 顶部导航 / 底部 / 头部搜索
    ├── Home/        # 首页：轮播、分类、新鲜好物、人气推荐、猜你喜欢
    ├── Category/    # 分类页
    ├── SubCategory/ # 二级分类商品列表
    ├── Detail/      # 商品详情 + SKU 选择
    ├── CartList/    # 购物车
    ├── Checkout/    # 结算页
    ├── Pay/         # 支付（模拟支付宝跳转与回调）
    ├── Login/       # 登录
    └── Member/      # 个人中心：订单列表 / 订单详情 / 地址管理
```

## 启动

**先确保后端已在 `http://localhost:8080` 运行**（见 [后端 README](../springboot-rabbit/README.md)）。

```sh
npm install
npm run dev
```

打开 <http://localhost:5173>，用 `18610848230 / 123456` 登录。

其他命令：

```sh
npm run build     # 生产构建到 dist/
npm run preview   # 预览构建产物
npm run lint      # ESLint 检查并自动修复
```

## 与后端联调的两处地址

接口地址写在代码里，换后端地址时改这两个地方：

1. `src/utils/http.js` → `baseURL: 'http://localhost:8080'`
2. `src/views/Pay/index.vue` → `baseURL` 与 `backURL`（支付跳转与回调地址）

## 静态资源

`public/images/` 下是 69 张商品 / 分类 / 轮播图，后端种子数据以根相对路径 `/images/xxx.jpg` 引用它们，Vite 会直接以该路径托管 `public/`。所以图片是前后端共享的，挪目录需要同步改数据库里的路径。