package com.fansz.pub.utils;

import com.fansz.pub.exception.FrameworkException;
import net.sf.cglib.beans.BeanCopier;
import org.apache.commons.beanutils.BeanMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean - 工具类
 */
public final class BeanTools {
    private BeanTools() {

    }

    private static final Logger LOG = LoggerFactory.getLogger(BeanTools.class);

    /*
     * 用来缓存BeanCopier的缓存
     */
    private static final ConcurrentHashMap<String, BeanCopier> BEAN_COPIER_CACHE = new ConcurrentHashMap<String, BeanCopier>();

    private static BeanCopier getBeanCopier(Class<?> sourceClass, Class<?> destClass) {
        String key = sourceClass.getCanonicalName() + ":" + destClass.getCanonicalName();
        BeanCopier beanCopier = BEAN_COPIER_CACHE.get(key);
        if (beanCopier == null) {
            beanCopier = BeanCopier.create(sourceClass, destClass, false);
            BEAN_COPIER_CACHE.putIfAbsent(key, beanCopier);
        }
        return beanCopier;
    }

    /**
     * 复制某个对象为目标对象类型的对象 当source与target对象属性名相同, 但数据类型不一致时，source的属性值不会复制到target对象
     *
     * @param <T>      目标对象类型参数
     * @param source   源对象
     * @param destType 目标对象类型
     * @return 复制后的结果对象
     */
    public static <T> T copyAs(Object source, Class<T> destType) {
        if (source == null || destType == null) {
            return null;
        }
        try {
            BeanCopier beanCopier = getBeanCopier(source.getClass(), destType);
            T dest = destType.newInstance();
            beanCopier.copy(source, dest, null);
            return dest;
        } catch (IllegalAccessException e) {
            LOG.error("new object error,{} is not a class", destType);
            throw new FrameworkException(e);
        } catch (InstantiationException e) {
            LOG.error("new object error", e);
            throw new FrameworkException(e);
        }
    }

    /**
     * 复制源对象集合到目标对象列表
     *
     * @param source   源对象
     * @param destType 目标对象
     * @param <T>      源对象类型参数
     * @param <K>      目标对象类型参数
     * @return 结果集合, 一个list
     */
    public static <T, K> List<K> copyAs(Collection<T> source, Class<K> destType) {
        if (CollectionTools.isNullOrEmpty(source) || destType == null) {
            return Collections.EMPTY_LIST;
        }

        List<K> result = new ArrayList<K>();
        if (source.isEmpty()) {
            return result;
        }
        try {
            Iterator<T> iterator = source.iterator();
            Class<?> sourceClass = iterator.next().getClass();
            BeanCopier beanCopier = getBeanCopier(sourceClass, destType);
            for (Object object : source) {
                K dest = destType.newInstance();
                beanCopier.copy(object, dest, null);
                result.add(dest);
            }
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
        return result;
    }

    /**
     * 复制属性：从源对象复制和目标对象相同的属性
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copy(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanCopier beanCopier = getBeanCopier(source.getClass(), target.getClass());
        beanCopier.copy(source, target, null);
    }


    /**
     * 将Map对象拷贝到Bean，Map中的key对应Bean的属性名，value对应属性值
     *
     * @param source 源对象，map
     * @param target 目标对象
     */
    public static void copyMapToObject(Map<String, ?> source, Object target) {
        try {
            org.apache.commons.beanutils.BeanUtils.populate(target, source);
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

    /**
     * 设置属性
     *
     * @param bean  目标对象
     * @param name  属性名
     * @param value 属性值
     */
    public static void setProperty(Object bean, String name, Object value) {
        try {
            org.apache.commons.beanutils.BeanUtils.setProperty(bean, name, value);
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

    /**
     * 获取属性的值
     *
     * @param bean 目标对象
     * @param name 属性名
     * @return 属性的值，其实是String类型
     */
    public static Object getProperty(Object bean, String name) {
        try {
            return org.apache.commons.beanutils.BeanUtils.getProperty(bean, name);
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

    /**
     * 设置Field值
     *
     * @param bean      要设置对象
     * @param fieldName 字段名
     * @param value     值
     */
    public static void setFieldValue(Object bean, String fieldName, Object value) {
        try {
            Field field = findField(bean.getClass(), fieldName);
            field.setAccessible(true);
            field.set(bean, value);
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

    /**
     * 取得指定名称的Field, 子类找不到, 去父类里找
     *
     * @param clz       类
     * @param fieldName 指定名称
     * @return 找不到返回null
     */
    public static Field findField(Class<?> clz, String fieldName) {
        Field f = null;
        try {
            f = clz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (clz.getSuperclass() != null) {
                f = findField(clz.getSuperclass(), fieldName);
            }
            LOG.warn(String.format("field (%s) not exists", fieldName), e);
        }
        return f;
    }

    /**
     * 用于model修改时的对象复制,把srcModel复制到destModel,srcModel中为null的字段不复制，同名且类型相同的属性才复制
     *
     * @param srcModel  表单提交的源对象
     * @param destModel 数据库中的目标对象
     */
    public static void copyNotNullProperties(Object srcModel, Object destModel) {
        if (srcModel == null || destModel == null) {
            return;
        }

        try {
            PropertyDescriptor[] srcDescriptors = Introspector.getBeanInfo(srcModel.getClass())
                    .getPropertyDescriptors();
            PropertyDescriptor[] destDescriptors = Introspector.getBeanInfo(destModel.getClass())
                    .getPropertyDescriptors();
            Map<String, PropertyDescriptor> destPropertyNameDescriptorMap = new HashMap<String, PropertyDescriptor>();
            for (PropertyDescriptor destPropertyDescriptor : destDescriptors) {
                destPropertyNameDescriptorMap.put(destPropertyDescriptor.getName(), destPropertyDescriptor);
            }
            for (PropertyDescriptor srcDescriptor : srcDescriptors) {
                PropertyDescriptor destDescriptor = destPropertyNameDescriptorMap.get(srcDescriptor.getName());
                if (destDescriptor != null && destDescriptor.getPropertyType() == srcDescriptor.getPropertyType()
                        && destDescriptor.getPropertyType() != Class.class) {// 类型相同的属性才复制
                    Object val = srcDescriptor.getReadMethod().invoke(srcModel);
                    if (val != null && destDescriptor.getWriteMethod() != null) {// not
                        // null
                        destDescriptor.getWriteMethod().invoke(destModel, val);
                    }
                }
            }
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }


    /**
     * 把对象当作Map用
     *
     * @param obj 对象
     * @return Map
     */
    public static Map<String, Object> getProperties(Object obj) {
        return new BeanMap(obj);
    }
}
