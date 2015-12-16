package com.fansz.orm.dao.impl;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;

import com.fansz.orm.dao.IBaseDAO;
import com.fansz.orm.dao.support.IQueryBuilder;
import com.fansz.orm.dao.support.QueryResult;
import com.fansz.pub.utils.DomainTools;
import com.fansz.pub.utils.GenericTools;
import com.fansz.pub.utils.StringTools;
import com.fansz.pub.utils.TypeTools;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.fansz.pub.utils.BeanTools;
import com.fansz.pub.utils.CollectionTools;

/**
 * 数据访问层的基础类, 具体的Domain相关DAO类可以继承与这个类并制定泛型参数
 *
 * @param <T> 具体的Domain类
 */
@SuppressWarnings("unchecked")
public class BaseDAOImpl<T> implements IBaseDAO<T> {
    /**
     * 要操作的Domain类的类型
     */
    protected Class<T> entityClass = GenericTools.getSuperClassGenericType(this.getClass());

    // 日志
    private static final Logger LOG = LoggerFactory.getLogger(BaseDAOImpl.class.getName());

    /**
     * HibernateTemplate实例, 一般由Spring注入
     */
    @Resource(name = "hibernateTemplate")
    protected HibernateTemplate hibernateTemplate;

    /**
     * 自动创建Query的工具
     */
    @Resource(name = "queryBuilder")
    protected IQueryBuilder queryBuilder;

    /**
     * constructor
     */
    public BaseDAOImpl() {
    }

    /**
     * constructor
     * 
     * @param entityClass 实体
     */
    public BaseDAOImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }

    public HibernateTemplate getHibernateTemplate() {
        return hibernateTemplate;
    }

    public IQueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    public void setQueryBuilder(IQueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    /**
     * 清楚
     */
    public void clear() {
        hibernateTemplate.clear();
    }

    /**
     * flush
     */
    public void flush() {
        hibernateTemplate.flush();
    }

    /**
     * 删除方法.
     * 
     * @param entityids 实体id
     */
    public void delete(Serializable... entityids) {
        if (entityids != null) {
            for (Object id : entityids) {
                // 如果找不到实体的话就不用删除了
                try {
                    if (id.getClass().getAnnotation(Entity.class) != null) {
                        hibernateTemplate.delete(id);
                    }
                }
                catch (EntityNotFoundException ex) {
                    LOG.warn(ex.getMessage(), ex);
                }

            }
        }
    }

    /**
     * 根据指定条件删除数据
     *
     * @param wherejpql jpql 条件
     * @param queryParams jpql 条件
     */
    protected void deleteByCriteria(String wherejpql, Object[] queryParams) {
        String entityname = getEntityName(this.entityClass);
        String hql = "delete from " + entityname + " o " + (StringTools.isBlank(wherejpql) ? "" : "where " + wherejpql);
        this.hibernateTemplate.bulkUpdate(hql, queryParams);
    }

    /**
     * 根据实体类id查找对应的对象.
     * 
     * @param entityId 实体id
     * @return 实体
     */
    public T load(Serializable entityId) {
        if (entityId == null) {
            throw new IllegalArgumentException(this.entityClass.getName() + ":传入的实体id不能为空");
        }
        return this.hibernateTemplate.load(this.entityClass, entityId);
    }

    /**
     * 保存对象.
     * 
     * @param entity 实体
     */
    public void save(T entity) {
        hibernateTemplate.saveOrUpdate(entity);
    }

    public long getCount() {
        return (Long)hibernateTemplate.find(
                "select count(" + getCountField(this.entityClass) + ") from " + getEntityName(this.entityClass) + " o")
                .get(0);
    }

    /**
     * 更新实体类.
     * 
     * @param entity 实体
     */
    public void update(T entity) {
        hibernateTemplate.update(entity);
    }

    /**
     * 通过SQL或者HQL批量更新或删除
     * 
     * @param queryId 语句的ID
     * @param parameters 参数
     * @return 更新的记录数
     */
    protected int executeUpdate(final String queryId, final Map<String, Object> parameters) {
        return this.hibernateTemplate.execute(new HibernateCallback<Integer>()
        {

            @Override
            public Integer doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = queryBuilder.getQuery(session, queryId, parameters);
                if (query != null) {
                    return query.executeUpdate();
                }
                return 0;
            }

        });

    }

    /**
     * 查找所有实体类.
     * 
     * @return 实体
     */
    public List<T> findAll() {
        return findByCriteria(new HashMap<String, Object>());
    }

    /**
     * 根据属性=值的方式去查询
     *
     * @param parameters 属性名=值的查询条件表
     * @return 查询结果
     */
    protected List<T> findByCriteria(Map<String, Object> parameters) {
        DetachedCriteria criteria = DetachedCriteria.forClass(entityClass);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            criteria.add(Property.forName(entry.getKey()).eq(entry.getValue()));
        }
        return hibernateTemplate.findByCriteria(criteria);
    }

    @Override
    public List<T> findBy(Map<String, Object> parameters) {
        return findByCriteria(parameters);
    }

    /**
     * 根据属性=值的方式去查询
     *
     * @param propertyName 属性名
     * @param propertyValue 属性值
     * @return 查询结果
     */
    protected List<T> findByCriteria(String propertyName, Object propertyValue) {
        Map<String, Object> params = CollectionTools.makeMap(propertyName, propertyValue);
        return findByCriteria(params);
    }

    /**
     * 获取实体的名称
     *
     * @param <E> 实体类行
     * @param clazz 实体类
     * @return 实体的名称
     */
    private static <E> String getEntityName(Class<E> clazz) {
        String entityname = clazz.getSimpleName();
        Entity entity = clazz.getAnnotation(Entity.class);
        if (entity.name() != null && !"".equals(entity.name())) {
            entityname = entity.name();
        }
        return entityname;
    }

    /**
     * 获取统计属性,该方法是为了解决hibernate解析联合主键select count(o) from Xxx o语句BUG而增加,hibernate对此jpql解析后的sql为select
     * count(field1,field2,...),显示使用count()统计多个字段是错误的
     * 
     * @param <E> 泛型
     * @param clazz class
     * @return String
     */
    private static <E> String getCountField(Class<E> clazz) {
        String out = "o";
        try {
            PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
            for (PropertyDescriptor propertydesc : propertyDescriptors) {
                Method method = propertydesc.getReadMethod();
                if (method != null && method.isAnnotationPresent(EmbeddedId.class)) {
                    PropertyDescriptor[] ps = Introspector.getBeanInfo(propertydesc.getPropertyType())
                            .getPropertyDescriptors();
                    out = "o." + propertydesc.getName() + "."
                            + (!"class".equals(ps[1].getName()) ? ps[1].getName() : ps[0].getName());
                    break;
                }
            }
        }
        catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return out;
    }

    /**
     * 通过指定的查询的ID的QL进行查询
     * 
     * @param <RowType> RowType
     * @param queryId 查询id
     * @param parameters 参数
     * @return 查到对的数据, 对象数组的List
     */
    protected <RowType> List<RowType> findByNamedQuery(String queryId, Object parameters) {
        if (parameters instanceof Map) {
            return findByNamedQuery(queryId, (Map<String, Object>)parameters);
        }
        else {
            Map<String, Object> paramMap = BeanTools.getProperties(parameters);
            return findByNamedQuery(queryId, paramMap);
        }

    }

    /**
     * 通过指定的查询的ID的QL进行查询
     * 
     * @param <RowType> RowType
     * @param queryId 查询的ID
     * @param parameters 参数
     * @return 查到对的数据, 对象数组的List
     */
    protected <RowType> List<RowType> findByNamedQuery(final String queryId, final Map<String, Object> parameters) {
        return this.hibernateTemplate.execute(new HibernateCallback<List<RowType>>()
        {

            @Override
            public List<RowType> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = queryBuilder.getQuery(session, queryId, parameters);
                if (query != null) {
                    return query.list();
                }
                return Collections.EMPTY_LIST;
            }

        });
    }

    /**
     * 通过指定的查询的ID的QL进行查询
     *
     * @param queryId 查询的ID
     * @param parameters 参数
     * @param targetClass 行数据映射
     * @param <RowType> 行数据类型
     * @return 行数据
     */
    protected <RowType> List<RowType> findByNamedQuery(String queryId, Object parameters, Class<RowType> targetClass) {
        if (parameters instanceof Map) {
            return findByNamedQuery(queryId, (Map<String, Object>)parameters, targetClass);
        }
        else {
            Map<String, Object> paramMap = BeanTools.getProperties(parameters);
            return findByNamedQuery(queryId, paramMap, targetClass);
        }
    }

    /**
     * 通过指定的查询的ID的QL进行查询
     *
     * @param queryId 查询的ID
     * @param parameters 参数
     * @param targetClass 行数据映射
     * @param <RowType> 行数据类型
     * @return 行数据
     */
    protected <RowType> List<RowType> findByNamedQuery(String queryId, Map<String, Object> parameters,
            Class<RowType> targetClass) {

        List<Object> rows = findByNamedQuery(queryId, parameters);
        List<RowType> results = new ArrayList<RowType>();
        if (!CollectionTools.isNullOrEmpty(rows)) {
            for (Object row : rows) {
                if (DomainTools.isEntity(targetClass)) {
                    results.add((RowType)row);
                }
                else {
                    if (isPrimitiveType(targetClass)) {
                        results.add((RowType)getPrimitiveObject(row));
                    }
                    else {
                        results.add(convertMapToBean((Map<?, ?>)row, targetClass));
                    }
                }
            }
        }
        return results;
    }

    /**
     * @param row 行
     * @param <RowType> 行
     * @return rowtype
     */
    private <RowType> RowType getPrimitiveObject(Object row) {
        RowType rowObj;
        if (row instanceof Map) {
            // Map
            Map<Object, Object> countResult = (Map<Object, Object>)row;
            Object key = countResult.keySet().iterator().next();
            rowObj = (RowType)countResult.get(key);
        }
        else {
            // Object
            rowObj = (RowType)row;
        }
        return rowObj;
    }

    /**
     * 处理通用的跨Domain等复杂查询,分页
     *
     * @param <RowType> RowType
     * @param queryId 查询的ID
     * @param counterId 计算分页总件数的查询
     * @param parameters 参数对象
     * @param firstIndex 翻页的第一条数据
     * @param maxResult 本页取多少条
     * @return 查询结果, 分页数据
     */
    protected <RowType> QueryResult<RowType> findByNamedQuery(String queryId, String counterId, Object parameters,
                                                              int firstIndex, int maxResult) {
        if (parameters instanceof Map) {
            return findByNamedQuery(queryId, counterId, (Map<String, Object>)parameters, firstIndex, maxResult);
        }
        else {
            Map<String, Object> paramMap = BeanTools.getProperties(parameters);
            return findByNamedQuery(queryId, counterId, paramMap, firstIndex, maxResult);
        }
    }

    /**
     * 处理通用的跨Domain等复杂查询,分页
     *
     * @param <RowType> RowType
     * @param queryId 查询的ID
     * @param counterId 计算分页总件数的查询
     * @param parameters 参数
     * @param firstIndex 翻页的第一条数据
     * @param maxResult 本页取多少条
     * @return 查询结果, 分页数据
     */
    protected <RowType> QueryResult<RowType> findByNamedQuery(final String queryId, final String counterId,
            final Map<String, Object> parameters, final int firstIndex, final int maxResult) {
        return hibernateTemplate.execute(new HibernateCallback<QueryResult<RowType>>()
        {

            @Override
            public QueryResult<RowType> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = queryBuilder.getQuery(session, queryId, parameters);
                Query counter = queryBuilder.getQuery(session, counterId, parameters);
                if (query != null && counter != null) {
                    query.setFirstResult(firstIndex);
                    query.setMaxResults(maxResult);
                    List<RowType> resultList = query.list();

                    // 取得全件数
                    long totalRecord;
                    Object obj = counter.uniqueResult();
                    if (obj instanceof Map) {
                        // Map
                        Map<Object, Object> countResult = (Map<Object, Object>)obj;
                        Object key = countResult.keySet().iterator().next();
                        totalRecord = ((Number)countResult.get(key)).longValue();
                    }
                    else {
                        // Object
                        totalRecord = ((Number)obj).longValue();
                    }
                    return new QueryResult<RowType>(resultList, totalRecord);
                }
                return null;
            }

        });

    }

    /**
     * 处理通用的跨Domain等复杂查询,分页
     *
     * @param <RowType> RowType
     * @param queryId 查询的ID
     * @param counterId 计算分页总件数的查询
     * @param parameters 参数对象
     * @param firstIndex 翻页的第一条数据
     * @param maxResult 本页取多少条
     * @param targetClass 类目标
     * @return 查询结果, 分页数据
     */
    protected <RowType> QueryResult<RowType> findByNamedQuery(String queryId, String counterId, Object parameters,
            int firstIndex, int maxResult, Class<RowType> targetClass) {
        if (parameters instanceof Map) {
            return findByNamedQuery(queryId, counterId, (Map<String, Object>)parameters, firstIndex, maxResult,
                    targetClass);
        }
        else {
            Map<String, Object> paramMap = BeanTools.getProperties(parameters);
            return findByNamedQuery(queryId, counterId, paramMap, firstIndex, maxResult, targetClass);
        }
    }

    /**
     * 处理通用的跨Domain等复杂查询,分页
     *
     * @param <RowType> RowType
     * @param queryId 查询的ID
     * @param counterId 计算分页总件数的查询
     * @param parameters 参数
     * @param firstIndex 翻页的第一条数据
     * @param maxResult 本页取多少条
     * @param targetClass 目标class
     * @return 查询结果, 分页数据
     */
    protected <RowType> QueryResult<RowType> findByNamedQuery(String queryId, String counterId,
            Map<String, Object> parameters, int firstIndex, int maxResult, Class<RowType> targetClass) {
        QueryResult<Object> rows = findByNamedQuery(queryId, counterId, parameters, firstIndex, maxResult);
        if (rows == null) {
            return null;
        }

        List<RowType> resultList = new ArrayList<RowType>();
        for (Object row : rows.getResultlist()) {
            if (DomainTools.isEntity(targetClass)) {
                resultList.add((RowType)row);
            }
            else {
                if (isPrimitiveType(targetClass)) {
                    resultList.add((RowType)getPrimitiveObject(row));
                }
                else {
                    resultList.add(convertMapToBean((Map<?, ?>)row, targetClass));
                }
            }
        }

        return new QueryResult<RowType>(resultList, rows.getTotalrecord());
    }

    /**
     * 把Map映射到targetClass的对象
     *
     * @param map 数据
     * @param targetClass 对象
     * @param <RowType> 类型
     * @return 指定类型的对象
     */
    public static <RowType> RowType convertMapToBean(Map<?, ?> map, Class<RowType> targetClass) {
        try {
            RowType rowObj = targetClass.newInstance();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String propertyName = (String)entry.getKey();
                if (propertyName.contains("_") || Character.isUpperCase(propertyName.charAt(0))) {
                    propertyName = StringTools.camelCase(propertyName);
                }

                Object propertyValue = entry.getValue();
                if (propertyValue != null) {
                    BeanTools.setProperty(rowObj, propertyName, propertyValue);
                }
            }
            return rowObj;
        }
        catch (InstantiationException e) {
            LOG.error(e.getMessage(), e);
        }
        catch (IllegalAccessException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 是否为简单对象
     *
     * @param clz 类型
     * @return 是否为简单对象
     */
    private boolean isPrimitiveType(Class<?> clz) {
        return clz.isPrimitive() || TypeTools.isSubClassOf(clz, Number.class, Boolean.class, Date.class, String.class);
    }
}
