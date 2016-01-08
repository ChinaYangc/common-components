package com.fansz.orm.dao.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.fansz.pub.exception.FrameworkException;
import com.fansz.pub.utils.StringTools;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * QueryBuilderImpl静态方法工具类
 */
final class QueryBuilderImplHelper {
    // LOG
    private static final Logger LOG = LoggerFactory.getLogger(QueryBuilderImplHelper.class);

    /**
     * 私有构造函数,防止误用
     */
    private QueryBuilderImplHelper() {
    }

    /**
     * 加载指定位置的配置文件
     *
     * @param location 配置文件的配置
     * @return 配置文件中的查询信息
     */
    public static List<QueryBean> loadQueryConfig(Resource location, Configuration cfg) {
        List<QueryBean> queries = new ArrayList<QueryBean>();
        InputStream in = null;
        try {
            in = location.getInputStream();
            SAXReader reader = new SAXReader();
            Document doc = reader.read(in);
            Element rootEl = doc.getRootElement();
            for (Object obj : rootEl.elements()) {
                collectQuery(cfg, queries, obj);
            }
        }
        catch (Exception e) {
            LOG.error("can not load query configuration from " + location, e);
            return queries;
        }
        finally {

            if (in != null) {
                try {
                    in.close();
                }
                catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }

        return queries;
    }

    /**
     * 把xml元素转换为query对象并加入到queries集合中去
     *
     * @param cfg
     * @param queries
     * @param obj
     * @throws IOException
     */
    private static void collectQuery(Configuration cfg, List<QueryBean> queries, Object obj) throws IOException {
        Element element = (Element)obj;
        // 名称
        String id = element.attributeValue("name");
        // 类型
        QueryType queryType;
        // 模板
        Template template;
        // 是否lock
        String lock = element.attributeValue("lock");
        boolean isLock = "true".equals(lock);

        if (StringTools.isBlank(id)) {
            throw new FrameworkException("can not find name attribute");
        }

        // 类型
        if (QueryType.HQL.name.equals(element.getName())) {
            queryType = QueryType.HQL;
        }
        else if (QueryType.SQL.name.equals(element.getName())) {
            queryType = QueryType.SQL;
        }
        else {
            queryType = QueryType.FRAGMENT;
        }
        // 模板
        String text = element.getTextTrim();
        try {
            text = "<#escape x as sql_sanitize(x)>" + text + "</#escape>";
            template = new Template(id, new StringReader(text), cfg);
            QueryBean bean = new QueryBean(id, queryType, template);
            bean.setLock(isLock);
            queries.add(bean);
            if (LOG.isDebugEnabled()) {
                LOG.debug(bean.toString());
            }
        }
        catch (ParseException ex) {
            LOG.error("can not parse query " + id + " of " + text, ex);
        }
    }
}
