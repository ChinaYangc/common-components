package com.fansz.orm.dao.impl;

import com.fansz.orm.dao.annotation.NamedExec;
import com.fansz.orm.dao.annotation.NamedQuery;
import com.fansz.orm.dao.support.Page;
import com.fansz.orm.dao.support.QueryResult;
import com.fansz.pub.exception.FrameworkException;
import com.fansz.pub.utils.BeanTools;
import com.fansz.pub.utils.GenericTools;
import com.fansz.pub.utils.TypeTools;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 动态生成DAO的方法实现
 */
@Component("daoMethodInterceptor")
public class DaoMethodInterceptor extends BaseDAOImpl<Object> implements MethodInterceptor {

    // 查询件数的语句的后缀
    private static final Object COUNTER_SUFFIX = ".counter";

    private ConcurrentMap<Method, Boolean> makeMapCache = new ConcurrentHashMap<Method, Boolean>();

    /**
     * 拦截DAO中以NamedQuery和NamedExecute注解开头的方法
     *
     * @param obj CGLib动态生成的代理类实例
     * @param method 上文中实体类所调用的被代理的方法引用
     * @param args 参数值列表
     * @param proxy 代理类对方法的代理引用
     * @return 以NamedQuery和NamedExecute注解开头的方法的执行结果
     */
    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) {
        NamedQuery namedQuery = method.getAnnotation(NamedQuery.class);
        if (namedQuery != null) {
            Object parameter = makeParameter(method, args, namedQuery.parameters());
            return interceptQuery(namedQuery, method, args, parameter);
        }
        else {
            NamedExec namedExecute = method.getAnnotation(NamedExec.class);
            if (namedExecute != null) {
                Object parameter = makeParameter(method, args, namedExecute.parameters());
                return interceptOthers(namedExecute, parameter);
            }
            else {
                try {
                    return proxy.invokeSuper(obj, args);
                }
                catch (Throwable e) {
                    throw new FrameworkException("can not find NamedQuery and NamedExecute  annotation");
                }
            }
        }
    }

    /**
     * 处理查询
     *
     * @param namedQuery 查询注解对象
     * @param method 拦截的方法，及DAO中的方法
     * @param args 方法参数，数组格式
     * @param parameter 方法参数，可能为Map
     * @return 查询结果
     */
    private Object interceptQuery(NamedQuery namedQuery, Method method, Object[] args, Object parameter) {
        // QueryId
        String queryId = namedQuery.queryId();

        // 返回类型
        Class<?> returnType = method.getReturnType();
        if (returnType.equals(Void.TYPE)) {
            throw new FrameworkException("return type should not be void");
        }

        // 返回List的情况
        if (List.class.isAssignableFrom(returnType)) {
            return dealQueryReturnList(method, queryId, parameter);
        }
        else if (Map.class.isAssignableFrom(returnType)) {
            // 返回单个Map的情况
            return dealQueryReturnMap(queryId, parameter);
        }
        else if (QueryResult.class.isAssignableFrom(returnType) && args[0] instanceof Page) {
            // 返回QueryResult的情况
            // QueryResult里的类型
            return dealQueryReturnQueryResult(method, args, queryId, parameter);
        }
        else {
            // 其他情况按照返回单个映射对象来处理
            return dealQueryReturnSingleObject(queryId, returnType, parameter);
        }
    }

    /**
     * 处理增删改
     *
     * @param namedExec 增删改注解对象
     * @param parameter 传入参数
     * @return 增删改结果
     */
    private Object interceptOthers(NamedExec namedExec, Object parameter) {
        String executeId = namedExec.execId();
        if (parameter instanceof Map) {
            return executeUpdate(executeId, (Map<String, Object>)parameter);
        }
        else {
            return executeUpdate(executeId, BeanTools.getProperties(parameter));
        }
    }

    /**
     * 处理返回单个映射对象的查询
     *
     * @param queryId
     * @param returnType
     * @param parameter
     * @return 查询结果
     */
    @SuppressWarnings("unchecked")
    private Object dealQueryReturnSingleObject(String queryId, Class<?> returnType, Object parameter) {
        List<Object> results = (List<Object>)findByNamedQuery(queryId, parameter, returnType);
        if (results.isEmpty()) {
            return null;
        }
        else {
            return results.get(0);
        }
    }

    /**
     * 处理返回分页结果的查询
     *
     * @param method
     * @param args
     * @param queryId
     * @param parameter
     * @return
     */
    private Object dealQueryReturnQueryResult(Method method, Object[] args, String queryId, Object parameter) {
        Type genericType = method.getGenericReturnType();
        Class<?> elemType = GenericTools.getSuperClassGenericType(genericType);
        // 分页
        Page page = (Page)args[0];
        String counterId = queryId + COUNTER_SUFFIX;
        int pageSize = page.getPageSize();
        int firstIndex = (page.getPage() - 1) * pageSize;
        if (Map.class.isAssignableFrom(elemType)) {
            return findByNamedQuery(queryId, counterId, parameter, firstIndex, pageSize);
        }
        else {
            return findByNamedQuery(queryId, counterId, parameter, firstIndex, pageSize, elemType);
        }
    }

    /**
     * 处理返回单个Map类型的查询
     *
     * @param queryId
     * @param parameter
     * @return
     */
    private Object dealQueryReturnMap(String queryId, Object parameter) {
        List<Map<?, ?>> results = findByNamedQuery(queryId, parameter);
        if (results.isEmpty()) {
            return null;
        }
        else {
            return results.get(0);
        }
    }

    /**
     * 处理返回List的查询
     *
     * @param method
     * @param queryId
     * @param parameter
     * @return
     */
    private Object dealQueryReturnList(Method method, String queryId, Object parameter) {
        Type genericType = method.getGenericReturnType();
        Class<?> elemType = GenericTools.getSuperClassGenericType(genericType);
        if (Map.class.isAssignableFrom(elemType)) {
            return findByNamedQuery(queryId, parameter);
        }
        else {
            return findByNamedQuery(queryId, parameter, elemType);
        }
    }

    /**
     * 把方法的参数转换为findBy的参数
     *
     * @param method 方法
     * @param args 参数
     * @return findBy的参数
     */
    private Object makeParameter(Method method, Object[] args, String[] paramNames) {
        Object parameter;
        if (args.length == 0) {
            parameter = null;
        }
        else if (shouldMakeMap(method)) {
            Map<String, Object> param = new HashMap<String, Object>();
            for (int i = 0; i < paramNames.length; i++) {
                param.put(paramNames[i], args[i]);
            }
            parameter = param;
        }
        else {
            parameter = args[0];
        }
        return parameter;
    }

    /**
     * 是否应该把参数转换Map
     *
     * @param method 方法
     * @return 是否应该把参数转换Map
     */
    private boolean shouldMakeMap(Method method) {
        Boolean shouldMake = makeMapCache.get(method);
        if (shouldMake != null) {
            return shouldMake;
        }

        shouldMake = method.getParameterTypes().length > 1 || isParameterType(method.getParameterTypes()[0]);
        makeMapCache.put(method, shouldMake);

        return shouldMake;
    }

    /**
     * 是否为简单对象
     *
     * @param clz 类型
     * @return 是否为简单对象
     */
    private boolean isParameterType(Class<?> clz) {
        return clz.isPrimitive()
                || TypeTools.isSubClassOf(clz, Integer.class, Number.class, Boolean.class, Date.class, String.class,
                        List.class, Set.class);
    }
}
