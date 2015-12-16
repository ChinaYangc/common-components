package com.fansz.orm.dao.support;

import java.util.List;

/**
 * 查询结果,分页用
 *
 * @param <T> 结果的类型
 */
public class QueryResult<T> {
    // 结果集
    private List<T> resultlist;

    // 总条数
    private long totalrecord;

    public List<T> getResultlist() {
        return resultlist;
    }

    public void setResultlist(List<T> resultlist) {
        this.resultlist = resultlist;
    }

    public long getTotalrecord() {
        return totalrecord;
    }

    public void setTotalrecord(long totalrecord) {
        this.totalrecord = totalrecord;
    }

    /**
     * constructor
     * 
     * @param resultlist List<T>
     * @param totalrecord long
     */
    public QueryResult(List<T> resultlist, long totalrecord) {
        this.resultlist = resultlist;
        this.totalrecord = totalrecord;
    }

    /**
     * constructor
     */
    public QueryResult() {
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QueryResult{");
        sb.append("resultlist=").append(resultlist);
        sb.append(", totalrecord=").append(totalrecord);
        sb.append('}');
        return sb.toString();
    }
}
