package com.fansz.orm.template;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * 通过类型值获取编码文本.
 */
public class CodeTextByTypeValue implements TemplateMethodModelEx {
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        if (arguments.size() != 2) {
            throw new TemplateModelException("get_text(...) expects one argument.");
        }
        Object obj1 = arguments.get(0);
        Object obj2 = arguments.get(1);
        if (obj1 == null || obj2 == null) {
            return "";
        }
        String type = ((TemplateScalarModel)obj1).getAsString();
        String value = ((TemplateScalarModel)obj2).getAsString();
        return value;
    }
}
