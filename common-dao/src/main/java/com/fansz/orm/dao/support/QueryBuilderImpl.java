package com.fansz.orm.dao.support;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * 自动创建Query
 */
public class QueryBuilderImpl implements IQueryBuilder {
    // LOG
    private static final Logger LOG = LoggerFactory.getLogger(QueryBuilderImpl.class);

    /** */
    public static final int MONITOR_DELAY = 1000;

    // 模板缓存
    private ConcurrentMap<String, QueryBean> templateCache = new ConcurrentHashMap<String, QueryBean>();

    // 配置文件
    private List<Resource> configLocations;

    // Freemarker的配置
    private Configuration freemarkerConfiguration;

    // 执行原生SQL时返回的结果集里是Map
    private boolean nativeSqlReturnMap = true;

    @Override
    public Query getQuery(Session session, String queryId, Map<String, Object> parameters) {
        QueryBean queryBean = templateCache.get(queryId);
        if (queryBean == null) {
            LOG.warn("queryId{} not found,please make sure the QueryBuilder configLocations is correct",queryId);
            return null;
        }
        else {
            StringWriter writer = new StringWriter();
            try {
                queryBean.getTemplate().process(parameters, writer);
                String ql = writer.toString();
                return createQuery(session, queryId, parameters, queryBean, ql);
            }
            catch (TemplateException e) {
                LOG.error(e.getMessage(), e);
            }
            catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
            catch (RuntimeException e) {
                LOG.error("can not get query for " + queryId);
                throw e;
            }

        }
        return null;
    }

    /**
     * 创建Query对象
     *
     * @param session session
     * @param queryId 查询标识
     * @param parameters 参数
     * @param queryBean QueryBean
     * @param ql sql
     * @return Query
     */
    private Query createQuery(Session session, String queryId, Map<String, Object> parameters, QueryBean queryBean,
            String ql) {
        if (queryBean.getType() == QueryType.HQL) {
            Query query = session.createQuery(ql);
            return getQuery(queryId, parameters, queryBean, ql, query);
        }
        else {
            SQLQuery query = session.createSQLQuery(ql);
            // 如果需要NativeSQL执行结果转换为Map
            if (nativeSqlReturnMap) {
                query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
            }
            return getQuery(queryId, parameters, queryBean, ql, query);
        }
    }

    /**
     * @param queryId 查询标识
     * @param parameters 参数
     * @param queryBean QueryBean
     * @param ql sql
     * @param query Query
     * @return Query
     */
    private Query getQuery(String queryId, Map<String, Object> parameters, QueryBean queryBean, String ql, Query query) {
        fillParameters(queryId, ql, query, parameters, queryBean);
        if (queryBean.isLock()) {
            query.setLockMode("", LockMode.UPGRADE);
        }
        return query;
    }

    @Override
    public String getQueryText(String queryId, Map<String, Object> parameters) {
        QueryBean queryBean = templateCache.get(queryId);
        if (queryBean == null) {
            return null;
        }
        StringWriter writer = new StringWriter();
        try {
            queryBean.getTemplate().process(parameters, writer);
            return writer.toString();
        }
        catch (TemplateException e) {
            LOG.error(e.getMessage(), e);
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Template getTemplate(String queryId) {
        QueryBean queryBean = templateCache.get(queryId);
        if (queryBean == null) {
            return null;
        }
        return queryBean.getTemplate();
    }

    /**
     * 添加参数
     *
     * @param queryId 查询标识
     * @param ql 查询语句
     * @param query 要添加参数的Query实例
     * @param parameters 要添加的参数
     * @param queryBean QueryBean
     */
    private static void fillParameters(String queryId, String ql, Query query, Map<String, Object> parameters,
            QueryBean queryBean) {
        String[] parameterNames = query.getNamedParameters();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (ArrayUtils.contains(parameterNames, entry.getKey())) {
                try {
                    if (QueryType.SQL.equals(queryBean.getType()) && entry.getValue() != null
                            && entry.getValue().getClass().isEnum()) {
                        query.setParameter(entry.getKey(), entry.getValue().toString());
                    }
                    else {
                        query.setParameter(entry.getKey(), entry.getValue());
                    }
                }
                catch (IllegalArgumentException e) {
                    LOG.error("Map里存放了不合法的参数:" + queryId + "(" + ql + ")" + " : " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 初始化--加载SQL
     */
    @PostConstruct
    public void initialize() {
        loadConfigurations();
    }

    /**
     * 加载配置文件
     */
    private void loadConfigurations() {
        for (Resource location : configLocations) {
            if (location.exists()) {
                List<QueryBean> queryBeans = QueryBuilderImplHelper.loadQueryConfig(location, freemarkerConfiguration);
                for (QueryBean queryBean : queryBeans) {
                    templateCache.put(queryBean.getId(), queryBean);
                }
            }
        }
    }

    @PreDestroy
    public void destory() {
    }

    public List<Resource> getConfigLocations() {
        return configLocations;
    }

    public void setConfigLocations(List<Resource> configLocations) {
        this.configLocations = configLocations;
    }

    public Configuration getFreemarkerConfiguration() {
        return freemarkerConfiguration;
    }

    public void setFreemarkerConfiguration(Configuration freemarkerConfiguration) {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    public boolean isNativeSqlReturnMap() {
        return nativeSqlReturnMap;
    }

    public void setNativeSqlReturnMap(boolean nativeSqlReturnMap) {
        this.nativeSqlReturnMap = nativeSqlReturnMap;
    }
}

/**
 * 查询QL的类型
 */
enum QueryType {
    HQL("hql-query"),

    SQL("native-query"),

    FRAGMENT("fragment");

    final String name;

    /**
     * constructor
     * 
     * @param name 名称
     */
    QueryType(String name) {
        this.name = name;
    }
}

/**
 * 查询QL的VO
 */
class QueryBean {
    private String id;

    private QueryType type;

    private Template template;

    private boolean lock;

    String getId() {
        return id;
    }

    QueryType getType() {
        return type;
    }

    Template getTemplate() {
        return template;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public boolean isLock() {
        return lock;
    }

    /**
     * constructor
     * 
     * @param id 主键
     * @param type 查询类型
     * @param template 模板
     */
    QueryBean(String id, QueryType type, Template template) {
        this.id = id;
        this.type = type;
        this.template = template;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QueryBean{");
        sb.append("id='").append(id).append('\'');
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QueryBean that = (QueryBean)o;

        return !(id != null ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
