-- =====================================================================
-- 小兔鲜商城 数据库初始化脚本
-- 用法：在 MySQL 8 中执行（Navicat / DataGrip / 命令行均可）
--   mysql -uroot -p < tuxian_mall.sql
-- 说明：字符集统一 utf8mb4；所有业务表含 id / create_time / update_time / is_deleted
-- =====================================================================

CREATE DATABASE IF NOT EXISTS tuxian_mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE tuxian_mall;

-- ---------------------------------------------------------------------
-- 管理员表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS admin;
CREATE TABLE admin (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名',
    password    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt）',
    name        VARCHAR(50)  DEFAULT NULL COMMENT '姓名',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    is_deleted  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '管理员表';

-- ---------------------------------------------------------------------
-- 用户表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS user;
CREATE TABLE user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    account     VARCHAR(50)  NOT NULL COMMENT '登录账号（手机号）',
    password    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt）',
    nickname    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    avatar      VARCHAR(500) DEFAULT NULL COMMENT '头像',
    mobile      VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    gender      TINYINT      DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    is_deleted  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_account (account)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ---------------------------------------------------------------------
-- 收货地址表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS user_address;
CREATE TABLE user_address (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id       BIGINT       NOT NULL COMMENT '用户 ID',
    receiver      VARCHAR(50)  DEFAULT NULL COMMENT '收货人',
    contact       VARCHAR(50)  DEFAULT NULL COMMENT '联系方式',
    province_code VARCHAR(20)  DEFAULT NULL COMMENT '省份编码',
    city_code     VARCHAR(20)  DEFAULT NULL COMMENT '城市编码',
    county_code   VARCHAR(20)  DEFAULT NULL COMMENT '区县编码',
    full_location VARCHAR(200) DEFAULT NULL COMMENT '省市区完整名称',
    address       VARCHAR(200) DEFAULT NULL COMMENT '详细地址',
    is_default    TINYINT      DEFAULT 1 COMMENT '是否默认 0默认 1非默认',
    create_time   DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time   DATETIME     DEFAULT NULL COMMENT '更新时间',
    is_deleted    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '收货地址表';

-- ---------------------------------------------------------------------
-- 商品分类表（两级：level 1 一级，level 2 二级）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS category;
CREATE TABLE category (
    id          VARCHAR(32)  NOT NULL COMMENT '分类 ID',
    name        VARCHAR(50)  NOT NULL COMMENT '分类名称',
    parent_id   VARCHAR(32)  DEFAULT NULL COMMENT '父分类 ID（一级为空）',
    level       TINYINT      DEFAULT NULL COMMENT '层级 1一级 2二级',
    picture     VARCHAR(500) DEFAULT NULL COMMENT '分类图片',
    sale_info   VARCHAR(100) DEFAULT NULL COMMENT '卖点文案',
    sort        INT          DEFAULT 0 COMMENT '排序',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    is_deleted  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品分类表';

-- ---------------------------------------------------------------------
-- 商品表（id 为字符串，与前端约定一致）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS goods;
CREATE TABLE goods (
    id            VARCHAR(32)   NOT NULL COMMENT '商品 ID',
    name          VARCHAR(255)  NOT NULL COMMENT '商品名称',
    description   VARCHAR(1000) DEFAULT NULL COMMENT '商品描述',
    price         DECIMAL(10, 2) DEFAULT NULL COMMENT '售价',
    old_price     DECIMAL(10, 2) DEFAULT NULL COMMENT '原价',
    picture       VARCHAR(500)  DEFAULT NULL COMMENT '封面图',
    main_pictures TEXT          COMMENT '主图集合 JSON 数组',
    category_id   VARCHAR(32)   DEFAULT NULL COMMENT '所属二级分类 ID',
    brand         VARCHAR(100)  DEFAULT NULL COMMENT '品牌',
    sales_count   INT           DEFAULT 0 COMMENT '销量',
    comment_count INT           DEFAULT 0 COMMENT '评论数',
    collect_count INT           DEFAULT 0 COMMENT '收藏数',
    order_num     INT           DEFAULT 0 COMMENT '销量/排序号',
    is_new        TINYINT       DEFAULT 0 COMMENT '是否新品 1是 0否',
    status        TINYINT       DEFAULT 1 COMMENT '上架状态 1上架 0下架',
    details       TEXT          COMMENT '详情 JSON',
    create_time   DATETIME      DEFAULT NULL COMMENT '创建时间',
    update_time   DATETIME      DEFAULT NULL COMMENT '更新时间',
    is_deleted    TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品表';

-- ---------------------------------------------------------------------
-- 商品 SKU 表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS goods_sku;
CREATE TABLE goods_sku (
    id          VARCHAR(32)   NOT NULL COMMENT 'SKU ID',
    goods_id    VARCHAR(32)   NOT NULL COMMENT '商品 ID',
    price       DECIMAL(10, 2) DEFAULT NULL COMMENT 'SKU 售价',
    old_price   DECIMAL(10, 2) DEFAULT NULL COMMENT 'SKU 原价',
    inventory   INT           DEFAULT 0 COMMENT '库存',
    specs       TEXT          COMMENT '规格组合 JSON：[{name,valueName}]',
    create_time DATETIME      DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME      DEFAULT NULL COMMENT '更新时间',
    is_deleted  TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    PRIMARY KEY (id),
    KEY idx_goods_id (goods_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品 SKU 表';

-- ---------------------------------------------------------------------
-- 轮播图表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS banner;
CREATE TABLE banner (
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    img_url           VARCHAR(500) DEFAULT NULL COMMENT '图片 URL',
    distribution_site TINYINT      DEFAULT 1 COMMENT '投放位置 1首页 2分类页',
    sort              INT          DEFAULT 0 COMMENT '排序',
    create_time       DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time       DATETIME     DEFAULT NULL COMMENT '更新时间',
    is_deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '轮播图表';

-- ---------------------------------------------------------------------
-- 订单表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS orders;
CREATE TABLE orders (
    id                 VARCHAR(32)   NOT NULL COMMENT '订单号',
    user_id            BIGINT        NOT NULL COMMENT '用户 ID',
    order_state        TINYINT       DEFAULT 1 COMMENT '订单状态 1待付款 2待发货 3待收货 4待评价 5已完成 6已取消',
    total_money        DECIMAL(10, 2) DEFAULT NULL COMMENT '商品总价',
    post_fee           DECIMAL(10, 2) DEFAULT NULL COMMENT '邮费',
    pay_money          DECIMAL(10, 2) DEFAULT NULL COMMENT '应付总额',
    pay_type           TINYINT       DEFAULT NULL COMMENT '支付方式',
    pay_channel        TINYINT       DEFAULT NULL COMMENT '支付渠道',
    delivery_time_type TINYINT       DEFAULT NULL COMMENT '配送时间类型',
    buyer_message      VARCHAR(500)  DEFAULT NULL COMMENT '买家留言',
    address_snapshot   TEXT          COMMENT '收货地址快照 JSON',
    pay_time           DATETIME      DEFAULT NULL COMMENT '支付时间',
    create_time        DATETIME      DEFAULT NULL COMMENT '创建时间',
    update_time        DATETIME      DEFAULT NULL COMMENT '更新时间',
    is_deleted         TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单表';

-- ---------------------------------------------------------------------
-- 订单项表（下单时商品快照）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS order_item;
CREATE TABLE order_item (
    id               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_id         VARCHAR(32)   NOT NULL COMMENT '订单号',
    goods_id         VARCHAR(32)   DEFAULT NULL COMMENT '商品 ID',
    sku_id           VARCHAR(32)   DEFAULT NULL COMMENT 'SKU ID',
    name             VARCHAR(255)  DEFAULT NULL COMMENT '商品名称快照',
    picture          VARCHAR(500)  DEFAULT NULL COMMENT '图片快照',
    attrs_text       VARCHAR(500)  DEFAULT NULL COMMENT '规格文本快照',
    price            DECIMAL(10, 2) DEFAULT NULL COMMENT '下单时单价',
    pay_price        DECIMAL(10, 2) DEFAULT NULL COMMENT '下单时实付单价',
    count            INT           DEFAULT 0 COMMENT '数量',
    total_price      DECIMAL(10, 2) DEFAULT NULL COMMENT '小计',
    total_pay_price  DECIMAL(10, 2) DEFAULT NULL COMMENT '实付小计',
    create_time      DATETIME      DEFAULT NULL COMMENT '创建时间',
    update_time      DATETIME      DEFAULT NULL COMMENT '更新时间',
    is_deleted       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单项表';

-- =====================================================================
-- 种子数据
-- =====================================================================

-- 管理员（账号 admin / 密码 admin123）
INSERT INTO admin (username, password, name, create_time, update_time)
VALUES ('admin', '$2b$10$DRKo28joSFMmVfqgJfOvPe26WyIUR2fe/jWEso88e6l08RwAxJawO', '管理员', NOW(), NOW());

-- 用户（账号 18610848230 / 密码 123456）
INSERT INTO user (account, password, nickname, avatar, mobile, gender, create_time, update_time)
VALUES ('18610848230', '$2b$10$tNxX8Gx.WZnwczIILSSGWuSUm2EhvpB4lrvDahzLM2DBd1X53aIMu', '小兔鲜用户',
        'https://picsum.photos/seed/avatar/100/100', '18610848230', 1, NOW(), NOW());

-- 收货地址
INSERT INTO user_address (user_id, receiver, contact, province_code, city_code, county_code, full_location, address, is_default, create_time, update_time)
VALUES (1, '张三', '13800138000', '440000', '440100', '440106', '广东省 广州市 天河区', '天河路 100 号 3 栋 501', 0, NOW(), NOW()),
       (1, '张三', '13800138000', '110000', '110100', '110105', '北京市 北京市 朝阳区', '朝阳北路 20 号', 1, NOW(), NOW());

-- 一级分类
INSERT INTO category (id, name, parent_id, level, picture, sale_info, sort, create_time, update_time) VALUES
('1005000', '居家', NULL, 1, '/images/cat-1005000.jpg', '好物精选', 1, NOW(), NOW()),
('1005001', '美食', NULL, 1, '/images/cat-1005001.jpg', '¥199', 2, NOW(), NOW()),
('1005002', '服饰', NULL, 1, '/images/cat-1005002.jpg', '换季上新', 3, NOW(), NOW()),
('1005003', '母婴', NULL, 1, '/images/cat-1005003.jpg', '宝妈精选', 4, NOW(), NOW()),
('1005004', '数码家电', NULL, 1, '/images/cat-1005004.jpg', '黑科技', 5, NOW(), NOW()),
('1005005', '美妆', NULL, 1, '/images/cat-1005005.jpg', '正品保障', 6, NOW(), NOW()),
('1005006', '运动户外', NULL, 1, '/images/cat-1005006.jpg', '活力焕新', 7, NOW(), NOW());

-- 二级分类
INSERT INTO category (id, name, parent_id, level, picture, sale_info, sort, create_time, update_time) VALUES
('1010001', '收纳整理', '1005000', 2, '/images/subcat-1010001.jpg', NULL, 1, NOW(), NOW()),
('1010002', '家纺布艺', '1005000', 2, '/images/subcat-1010002.jpg', NULL, 2, NOW(), NOW()),
('1011001', '休闲零食', '1005001', 2, '/images/subcat-1011001.jpg', NULL, 1, NOW(), NOW()),
('1011002', '生鲜水果', '1005001', 2, '/images/subcat-1011002.jpg', NULL, 2, NOW(), NOW()),
('1012001', '男装', '1005002', 2, '/images/subcat-1012001.jpg', NULL, 1, NOW(), NOW()),
('1012002', '女装', '1005002', 2, '/images/subcat-1012002.jpg', NULL, 2, NOW(), NOW()),
('1013001', '尿裤湿巾', '1005003', 2, '/images/subcat-1013001.jpg', NULL, 1, NOW(), NOW()),
('1013002', '儿童玩具', '1005003', 2, '/images/subcat-1013002.jpg', NULL, 2, NOW(), NOW()),
('1014001', '手机', '1005004', 2, '/images/subcat-1014001.jpg', NULL, 1, NOW(), NOW()),
('1014002', '智能家居', '1005004', 2, '/images/subcat-1014002.jpg', NULL, 2, NOW(), NOW()),
('1015001', '护肤', '1005005', 2, '/images/subcat-1015001.jpg', NULL, 1, NOW(), NOW()),
('1015002', '彩妆', '1005005', 2, '/images/subcat-1015002.jpg', NULL, 2, NOW(), NOW()),
('1016001', '运动鞋服', '1005006', 2, '/images/subcat-1016001.jpg', NULL, 1, NOW(), NOW()),
('1016002', '健身器材', '1005006', 2, '/images/subcat-1016002.jpg', NULL, 2, NOW(), NOW());

-- 商品
INSERT INTO goods (id, name, description, price, old_price, picture, main_pictures, category_id, brand, sales_count, comment_count, collect_count, order_num, is_new, status, details, create_time, update_time) VALUES
('4008501', '平步青云财神家居摆件', '艺术家手绘上色，礼盒包装，招财纳福', 646.00, 699.00, '/images/goods-4008501-1.jpg', '["/images/goods-4008501-1.jpg","/images/goods-4008501-2.jpg"]', '1010001', '瞿广慈', 1200, 328, 560, 60, 1, 1, '{"properties":[{"name":"品牌","value":"瞿广慈"},{"name":"材质","value":"树脂"}],"pictures":["/images/goods-4008501-1.jpg","/images/goods-4008501-2.jpg"]}', NOW(), NOW()),
('4008502', '北欧简约收纳箱三件套', '大中小三件组合，可折叠省空间', 89.00, 129.00, '/images/goods-4008502-1.jpg', '["/images/goods-4008502-1.jpg","/images/goods-4008502-2.jpg"]', '1010001', '小兔鲜', 2300, 510, 300, 45, 0, 1, '{"properties":[{"name":"材质","value":"无纺布"}],"pictures":["/images/goods-4008502-1.jpg","/images/goods-4008502-2.jpg"]}', NOW(), NOW()),
('4008503', '全棉四件套 1.8m床', '100% 精梳棉，亲肤透气不褪色', 299.00, 399.00, '/images/goods-4008503-1.jpg', '["/images/goods-4008503-1.jpg","/images/goods-4008503-2.jpg"]', '1010002', '小兔鲜', 890, 156, 210, 38, 1, 1, '{"properties":[{"name":"面料","value":"纯棉"}],"pictures":["/images/goods-4008503-1.jpg","/images/goods-4008503-2.jpg"]}', NOW(), NOW()),
('4008504', '智能感应垃圾桶', '挥手即开，自动打包，静音缓降', 159.00, 199.00, '/images/goods-4008504-1.jpg', '["/images/goods-4008504-1.jpg","/images/goods-4008504-2.jpg"]', '1010001', '小兔鲜', 1560, 268, 180, 33, 0, 1, '{"properties":[{"name":"容量","value":"12L"}],"pictures":["/images/goods-4008504-1.jpg","/images/goods-4008504-2.jpg"]}', NOW(), NOW()),
('4008505', '每日坚果礼盒30包', '六种坚果科学配比，独立小包装', 109.00, 139.00, '/images/goods-4008505-1.jpg', '["/images/goods-4008505-1.jpg","/images/goods-4008505-2.jpg"]', '1011001', '小兔鲜', 3200, 760, 420, 72, 1, 1, '{"properties":[{"name":"净含量","value":"750g"}],"pictures":["/images/goods-4008505-1.jpg","/images/goods-4008505-2.jpg"]}', NOW(), NOW()),
('4008506', '儿童运动鞋（抓绒保暖）', '毛毛虫卡通设计，抓绒内里保暖防滑', 299.00, 399.00, '/images/goods-4008506-1.jpg', '["/images/goods-4008506-1.jpg","/images/goods-4008506-2.jpg"]', '1016001', '小兔鲜', 1800, 420, 350, 55, 1, 1, '{"properties":[{"name":"适用","value":"儿童"},{"name":"功能","value":"保暖"}],"pictures":["/images/goods-4008506-1.jpg","/images/goods-4008506-2.jpg"]}', NOW(), NOW()),
('4008507', '有机赣南脐橙5斤', '产地直采，果肉细嫩化渣，甜度高', 39.90, 49.90, '/images/goods-4008507-1.jpg', '["/images/goods-4008507-1.jpg","/images/goods-4008507-2.jpg"]', '1011002', '小兔鲜', 5100, 980, 300, 88, 0, 1, '{"properties":[{"name":"产地","value":"赣南"}],"pictures":["/images/goods-4008507-1.jpg","/images/goods-4008507-2.jpg"]}', NOW(), NOW()),
('4008508', '男士休闲夹克', '立体剪裁，防风面料，商务休闲两相宜', 359.00, 499.00, '/images/goods-4008508-1.jpg', '["/images/goods-4008508-1.jpg","/images/goods-4008508-2.jpg"]', '1012001', '小兔鲜', 680, 130, 95, 25, 0, 1, '{"properties":[{"name":"版型","value":"修身"}],"pictures":["/images/goods-4008508-1.jpg","/images/goods-4008508-2.jpg"]}', NOW(), NOW()),
('4008509', '女士针织连衣裙', '柔软亲肤，收腰显瘦，气质百搭', 269.00, 359.00, '/images/goods-4008509-1.jpg', '["/images/goods-4008509-1.jpg","/images/goods-4008509-2.jpg"]', '1012002', '小兔鲜', 820, 190, 160, 29, 0, 1, '{"properties":[{"name":"面料","value":"针织"}],"pictures":["/images/goods-4008509-1.jpg","/images/goods-4008509-2.jpg"]}', NOW(), NOW()),
('4008510', '婴儿纸尿裤L码54片', '超薄透气，海量吸收，整夜干爽', 89.00, 129.00, '/images/goods-4008510-1.jpg', '["/images/goods-4008510-1.jpg","/images/goods-4008510-2.jpg"]', '1013001', '小兔鲜', 4100, 890, 500, 80, 1, 1, '{"properties":[{"name":"码数","value":"L码"}],"pictures":["/images/goods-4008510-1.jpg","/images/goods-4008510-2.jpg"]}', NOW(), NOW()),
('4008511', '积木拼装玩具1000粒', '大颗粒安全材质，激发创造力', 199.00, 259.00, '/images/goods-4008511-1.jpg', '["/images/goods-4008511-1.jpg","/images/goods-4008511-2.jpg"]', '1013002', '小兔鲜', 1300, 260, 200, 40, 0, 1, '{"properties":[{"name":"颗粒","value":"1000粒"}],"pictures":["/images/goods-4008511-1.jpg","/images/goods-4008511-2.jpg"]}', NOW(), NOW()),
('4008512', '5G智能手机 8+256G', '旗舰芯片，2亿像素，超长续航', 3999.00, 4299.00, '/images/goods-4008512-1.jpg', '["/images/goods-4008512-1.jpg","/images/goods-4008512-2.jpg"]', '1014001', '小兔鲜', 2100, 680, 900, 95, 1, 1, '{"properties":[{"name":"内存","value":"8+256G"}],"pictures":["/images/goods-4008512-1.jpg","/images/goods-4008512-2.jpg"]}', NOW(), NOW()),
('4008513', '智能扫地机器人', '激光导航，扫拖一体，自动回充', 1599.00, 1999.00, '/images/goods-4008513-1.jpg', '["/images/goods-4008513-1.jpg","/images/goods-4008513-2.jpg"]', '1014002', '小兔鲜', 980, 210, 300, 35, 0, 1, '{"properties":[{"name":"功能","value":"扫拖一体"}],"pictures":["/images/goods-4008513-1.jpg","/images/goods-4008513-2.jpg"]}', NOW(), NOW()),
('4008514', '玻尿酸补水面膜20片', '深层补水，紧致肌肤，敏感肌可用', 79.00, 99.00, '/images/goods-4008514-1.jpg', '["/images/goods-4008514-1.jpg","/images/goods-4008514-2.jpg"]', '1015001', '小兔鲜', 2600, 540, 380, 66, 1, 1, '{"properties":[{"name":"规格","value":"20片"}],"pictures":["/images/goods-4008514-1.jpg","/images/goods-4008514-2.jpg"]}', NOW(), NOW()),
('4008515', '丝绒雾面口红', '显白不拔干，持久不脱妆', 129.00, 169.00, '/images/goods-4008515-1.jpg', '["/images/goods-4008515-1.jpg","/images/goods-4008515-2.jpg"]', '1015002', '小兔鲜', 1900, 330, 260, 48, 0, 1, '{"properties":[{"name":"色号","value":"豆沙色"}],"pictures":["/images/goods-4008515-1.jpg","/images/goods-4008515-2.jpg"]}', NOW(), NOW()),
('4008516', '男士跑步鞋', '缓震回弹，透气网面，轻量设计', 329.00, 429.00, '/images/goods-4008516-1.jpg', '["/images/goods-4008516-1.jpg","/images/goods-4008516-2.jpg"]', '1016001', '小兔鲜', 1500, 360, 290, 50, 0, 1, '{"properties":[{"name":"功能","value":"缓震"}],"pictures":["/images/goods-4008516-1.jpg","/images/goods-4008516-2.jpg"]}', NOW(), NOW()),
('4008517', '瑜伽垫加厚防滑', 'TPE 材质，加厚防滑，附收纳袋', 59.00, 89.00, '/images/goods-4008517-1.jpg', '["/images/goods-4008517-1.jpg","/images/goods-4008517-2.jpg"]', '1016002', '小兔鲜', 2400, 470, 310, 58, 0, 1, '{"properties":[{"name":"厚度","value":"10mm"}],"pictures":["/images/goods-4008517-1.jpg","/images/goods-4008517-2.jpg"]}', NOW(), NOW()),
('4008518', '全麦面包整箱', '0 蔗糖，高膳食纤维，早餐代餐', 29.90, 39.90, '/images/goods-4008518-1.jpg', '["/images/goods-4008518-1.jpg","/images/goods-4008518-2.jpg"]', '1011001', '小兔鲜', 3600, 720, 280, 76, 1, 1, '{"properties":[{"name":"净含量","value":"1kg"}],"pictures":["/images/goods-4008518-1.jpg","/images/goods-4008518-2.jpg"]}', NOW(), NOW()),
('4008519', '便携榨汁杯', 'USB 充电，一键榨汁，随身携带', 99.00, 149.00, '/images/goods-4008519-1.jpg', '["/images/goods-4008519-1.jpg","/images/goods-4008519-2.jpg"]', '1014002', '小兔鲜', 1100, 230, 190, 31, 0, 1, '{"properties":[{"name":"容量","value":"300ml"}],"pictures":["/images/goods-4008519-1.jpg","/images/goods-4008519-2.jpg"]}', NOW(), NOW()),
('4008520', '情侣款纯棉T恤', '纯棉面料，简约百搭，多色可选', 79.00, 109.00, '/images/goods-4008520-1.jpg', '["/images/goods-4008520-1.jpg","/images/goods-4008520-2.jpg"]', '1012002', '小兔鲜', 1700, 350, 240, 44, 1, 1, '{"properties":[{"name":"面料","value":"纯棉"}],"pictures":["/images/goods-4008520-1.jpg","/images/goods-4008520-2.jpg"]}', NOW(), NOW()),
('4008521', '智能手环运动版', '心率血氧监测，50 米防水，超长续航', 199.00, 259.00, '/images/goods-4008521-1.jpg', '["/images/goods-4008521-1.jpg","/images/goods-4008521-2.jpg"]', '1014001', '小兔鲜', 2900, 610, 450, 62, 0, 1, '{"properties":[{"name":"功能","value":"心率监测"}],"pictures":["/images/goods-4008521-1.jpg","/images/goods-4008521-2.jpg"]}', NOW(), NOW());

-- 商品 SKU（普通商品单个 SKU，规格为"规格:默认"）
INSERT INTO goods_sku (id, goods_id, price, old_price, inventory, specs, create_time, update_time) VALUES
('4008501-01', '4008501', 646.00, 699.00, 200, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW()),
('4008502-01', '4008502', 89.00, 129.00, 500, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW()),
('4008503-01', '4008503', 299.00, 399.00, 300, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW()),
('4008504-01', '4008504', 159.00, 199.00, 180, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW()),
('4008505-01', '4008505', 109.00, 139.00, 600, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW()),
('4008507-01', '4008507', 39.90, 49.90, 1000, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW()),
('4008508-01', '4008508', 359.00, 499.00, 120, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW()),
('4008509-01', '4008509', 269.00, 359.00, 150, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW()),
('4008510-01', '4008510', 89.00, 129.00, 800, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW()),
('4008511-01', '4008511', 199.00, 259.00, 260, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW()),
('4008512-01', '4008512', 3999.00, 4299.00, 100, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW()),
('4008513-01', '4008513', 1599.00, 1999.00, 90, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW()),
('4008514-01', '4008514', 79.00, 99.00, 700, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW()),
('4008515-01', '4008515', 129.00, 169.00, 350, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW()),
('4008517-01', '4008517', 59.00, 89.00, 400, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW()),
('4008518-01', '4008518', 29.90, 39.90, 900, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW()),
('4008519-01', '4008519', 99.00, 149.00, 220, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW()),
('4008520-01', '4008520', 79.00, 109.00, 320, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW()),
('4008521-01', '4008521', 199.00, 259.00, 150, '[{"name":"规格","valueName":"默认"}]', NOW(), NOW());

-- 儿童运动鞋（多规格：颜色 × 尺寸）
INSERT INTO goods_sku (id, goods_id, price, old_price, inventory, specs, create_time, update_time) VALUES
('4008506-01', '4008506', 299.00, 399.00, 100, '[{"name":"颜色","valueName":"瓷白色"},{"name":"尺寸","valueName":"8寸"}]', NOW(), NOW()),
('4008506-02', '4008506', 299.00, 399.00, 80, '[{"name":"颜色","valueName":"瓷白色"},{"name":"尺寸","valueName":"9寸"}]', NOW(), NOW()),
('4008506-03', '4008506', 289.00, 389.00, 60, '[{"name":"颜色","valueName":"黑色"},{"name":"尺寸","valueName":"8寸"}]', NOW(), NOW()),
('4008506-04', '4008506', 289.00, 389.00, 0, '[{"name":"颜色","valueName":"黑色"},{"name":"尺寸","valueName":"9寸"}]', NOW(), NOW());

-- 男士跑步鞋（多规格：颜色 × 尺码）
INSERT INTO goods_sku (id, goods_id, price, old_price, inventory, specs, create_time, update_time) VALUES
('4008516-01', '4008516', 329.00, 429.00, 90, '[{"name":"颜色","valueName":"黑色"},{"name":"尺码","valueName":"42"}]', NOW(), NOW()),
('4008516-02', '4008516', 329.00, 429.00, 75, '[{"name":"颜色","valueName":"黑色"},{"name":"尺码","valueName":"43"}]', NOW(), NOW()),
('4008516-03', '4008516', 319.00, 419.00, 50, '[{"name":"颜色","valueName":"灰色"},{"name":"尺码","valueName":"42"}]', NOW(), NOW()),
('4008516-04', '4008516', 319.00, 419.00, 0, '[{"name":"颜色","valueName":"灰色"},{"name":"尺码","valueName":"43"}]', NOW(), NOW());

-- 轮播图（1 首页，2 分类页）
INSERT INTO banner (img_url, distribution_site, sort, create_time, update_time) VALUES
('/images/banner-1.jpg', 1, 1, NOW(), NOW()),
('/images/banner-2.jpg', 1, 2, NOW(), NOW()),
('/images/banner-3.jpg', 1, 3, NOW(), NOW()),
('/images/banner-4.jpg', 1, 4, NOW(), NOW()),
('/images/banner-5.jpg', 2, 1, NOW(), NOW()),
('/images/banner-6.jpg', 2, 2, NOW(), NOW());
