package com.boyinet.demo.pipelineleakage.common;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author lengchunyun
 */
public class R<T> {

    private static final int SUCCESS = 0;

    private static final int FAIL = -1;

    private Integer code;

    private String msg;

    private T data;

    private long total;

    private int pageSize;

    private int current;

    public R(Integer code) {
        this.code = code;
    }

    public R(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public R(Integer code, String msg, T list) {
        this.code = code;
        this.msg = msg;
        this.data = list;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T list) {
        this.data = list;
    }

    public static <T> R<T> ok(T object) {
        return new R<>(SUCCESS, "", object);
    }

    public static <T> R<T> ok() {
        return new R<>(SUCCESS);
    }

    public static <T> R<T> nok(String msg) {
        return new R<>(FAIL, msg);
    }

    public static <T> R<T> nok() {
        return new R<>(FAIL);
    }

    public static <T> R<T> nok(Integer code, String msg) {
        return new R<>(code, msg);
    }

    public static <T> R<T> nok(Integer code, String msg, T object) {
        return new R<>(code, msg, object);
    }

    public boolean isSuccess() {
        return R.SUCCESS == getCode();
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public void setPage(Pageable page) {
        this.current = page.getPageNumber() + 1;
        this.pageSize = page.getPageSize();
    }
}
