package com.fansz.orm.template;

import java.math.BigDecimal;
import java.util.List;

import com.fansz.pub.utils.StringTools;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * string转化类.
 */
public class String2Number implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        if (arguments.size() != 1) {
            throw new TemplateModelException("str2number(...) expects one argument.");
        }
        Object obj = arguments.get(0);
        if (obj == null) {
            return "";
        }
        String sub = ((TemplateScalarModel)obj).getAsString();
        if (StringTools.isNumber(sub)) {
            return new BigDecimal(sub);
        }
        else {
            return sub;
        }
    }
}
