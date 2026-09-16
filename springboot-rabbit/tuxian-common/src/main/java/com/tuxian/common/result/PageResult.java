package com.tuxian.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 统一分页结果。
 * <p>
 * 前端约定字段名为 items（当前页数据）和 counts（总条数），
 * page / pageSize 附带返回便于调试。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    /** 当前页数据 */
    private List<T> items;
    /** 总条数 */
    private long counts;
    /** 当前页码 */
    private long page;
    /** 每页条数 */
    private long pageSize;
}