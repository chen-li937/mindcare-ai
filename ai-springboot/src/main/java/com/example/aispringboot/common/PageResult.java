package com.example.aispringboot.common;

import lombok.Data;

import java.util.List;

// 统一分页返回结构 { list, total }
@Data
public class PageResult<T> {
    private List<T> list;
    private Long total;

    public static <T> PageResult<T> of(List<T> list, long total) {
        PageResult<T> page = new PageResult<>();
        page.setList(list);
        page.setTotal(total);
        return page;
    }
}
