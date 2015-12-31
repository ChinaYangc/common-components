package com.fansz.pub.model;

/**
 * 分页对象
 */
public class Page {
    // 页码
    private Integer page;

    // 每页显示数
    private Integer pageSize;

    // order
    private String dir;

    // 字段
    private String field;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Page{");
        sb.append("page=").append(page);
        sb.append(", pageSize=").append(pageSize);
        sb.append(", dir='").append(dir).append('\'');
        sb.append(", field='").append(field).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
