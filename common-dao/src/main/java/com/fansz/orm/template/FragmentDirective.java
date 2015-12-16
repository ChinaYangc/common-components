package com.fansz.orm.template;

import java.io.IOException;
import java.util.Map;

import com.fansz.orm.dao.support.IQueryBuilder;
import com.fansz.pub.utils.StringTools;
import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.Template;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * DAO查询模板的fragment支持, 可以在一个查询语句中包含其他的查询语句
 */
public class FragmentDirective implements TemplateDirectiveModel {

    private IQueryBuilder queryBuilder;

    /**
     * 默认构造函数
     */
    public FragmentDirective() {
    }

    /**
     * 有参构造函数
     *
     * @param queryBuilder 查询用
     */
    public FragmentDirective(IQueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
            throws TemplateException, IOException {
        SimpleScalar argName = (SimpleScalar)params.get("name");
        String name = argName == null ? null : argName.toString();
        if (StringTools.isEmpty(name)) {
            throw new TemplateModelException("name parameter is empty");
        }

        Template template = queryBuilder.getTemplate(name);
        if (template == null) {
            throw new TemplateModelException("can not find query for id : " + name);
        }

        // 把包含进来的模板执行一下, 输出到父模板中
        template.process(env.getDataModel(), env.getOut());
    }

    public void setQueryBuilder(IQueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }
}
