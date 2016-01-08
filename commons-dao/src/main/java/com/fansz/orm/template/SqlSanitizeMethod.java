package com.fansz.orm.template;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

import java.util.List;

/**
 * SQL注入净化用的Freemarker方法Model 用法: ${sql_sanitize(x)}
 */
@SuppressWarnings("rawtypes")
public class SqlSanitizeMethod implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        // 参数检查
        int len = arguments.size();
        if (len != 1) {
            throw new TemplateModelException("sql_sanitize(...) expects one argument.");
        }

        // null处理
        Object obj = arguments.get(0);
        if (obj == null) {
            return null;
        }

        // 参数转换
        if (!(obj instanceof TemplateScalarModel)) {
            throw new TemplateModelException("?sql_sanitize(...) expects a string as " + "its first argument.");
        }
        String sub = ((TemplateScalarModel)obj).getAsString();
        return Sanitizer.sanitizeSQL(sub);
    }
}
