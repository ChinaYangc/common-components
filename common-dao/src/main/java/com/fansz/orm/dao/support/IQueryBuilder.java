package com.fansz.orm.dao.support;

import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;

import freemarker.template.Template;

/**
 * 自动创建Query
 */
public interface IQueryBuilder {
    /**
     * 根据指定的查询ID和参数来创建Query对象
     *
     * @param entityManager EntityManager
     * @param queryId 查询ID
     * @param parameters 参数
     * @return Query对象
     */
    Query getQuery(Session session, String queryId, Map<String, Object> parameters);

    /**
     * 根据指定的查询ID和参数来生成查询语句
     *
     * @param queryId 查询ID
     * @param parameters 参数
     * @return 查询语句
     */
    String getQueryText(String queryId, Map<String, Object> parameters);

    /**
     * 根据指定的查询ID取得其Template
     *
     * @param queryId 查询ID
     * @return Template
     */
    Template getTemplate(String queryId);

}
