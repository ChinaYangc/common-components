package com.fansz.common.provider;

import com.fansz.common.provider.model.CommonPagedResult;
import com.fansz.common.provider.model.CommonResult;
import com.fansz.common.provider.model.NullResult;
import com.fansz.common.provider.model.PageParam;
import com.fansz.pub.model.QueryResult;
import com.github.miemiedev.mybatis.paginator.domain.PageList;

/**
 * Provider抽象类
 */
public abstract class AbstractProvider {
    protected final static NullResult PRESENCE = new NullResult();

    protected final static String SUCCESS = "0";

    protected CommonResult<NullResult> renderSuccess() {
        return this.renderSuccess(PRESENCE);
    }

    protected <T> CommonResult<T> renderSuccess(T data) {
        return renderSuccess(data, "Success");
    }

    protected <T> CommonResult<T> renderSuccess(T data, String message) {
        CommonResult<T> result = new CommonResult<>();
        result.setResult(data);
        result.setStatus(SUCCESS);
        result.setMessage(message);
        return result;
    }

    protected <T> CommonPagedResult<T> renderPagedSuccess(PageList<T> data, String message) {
        PageParam pager = new PageParam();
        if (data.getPaginator() != null) {
            pager.setPageSize(data.getPaginator().getLimit());
            pager.setPageNum(data.getPaginator().getPage());
            pager.setTotalNum(Long.valueOf(data.getPaginator().getTotalCount()));
        }
        CommonPagedResult<T> result = new CommonPagedResult<>(SUCCESS, message, pager, data);
        return result;
    }

    protected <T> CommonPagedResult<T> renderPagedSuccess(PageList<T> data) {
        return renderPagedSuccess(data, "Success");
    }

    protected <T> CommonPagedResult<T> renderPagedSuccess(QueryResult<T> data, String message) {
        PageParam pager = new PageParam();
        pager.setTotalNum(data.getTotalrecord());
        CommonPagedResult<T> result = new CommonPagedResult<>(SUCCESS, message, pager, data.getResultlist());
        return result;
    }

    protected <T> CommonPagedResult<T> renderPagedSuccess(QueryResult<T> data) {
        return renderPagedSuccess(data, "Success");
    }

    protected CommonResult<NullResult> renderFail(String errorCode, String errorMessage) {
        return renderFail(PRESENCE, errorCode, errorMessage);
    }

    protected <T> CommonResult<T> renderFail(String errorCode) {
        return renderFail(null, errorCode, "");
    }


    protected <T> CommonResult<T> renderFail(T data, String errorCode) {
        return renderFail(data, errorCode, "");
    }

    protected <T> CommonResult<T> renderFail(T data, String errorCode, String errorMessage) {
        CommonResult<T> result = new CommonResult<>();
        result.setResult(data);
        result.setStatus(errorCode);
        result.setMessage(errorMessage);
        return result;
    }
}
