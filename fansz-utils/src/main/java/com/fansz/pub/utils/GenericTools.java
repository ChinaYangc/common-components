package com.fansz.pub.utils;

import com.fansz.pub.exception.FrameworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基本工具类.
 */
public final class GenericTools {
    private static final Logger LOG = LoggerFactory.getLogger(GenericTools.class);

    private GenericTools() {

    }

    /**
     * 通过反射,获得指定类的父类的泛型参数的实际类型. 如BuyerServiceBean extends DaoSupport<Buyer>.
     * 
     * @param clazz clazz 需要反射的类,该类必须继承范型父类
     * @param index 泛型参数所在索引,从0开始.
     * @return 范型参数的实际类型, 如果没有实现ParameterizedType接口，即不支持泛型，所以直接返回<code>Object.class</code>
     */
    @SuppressWarnings("rawtypes")
    public static Class getSuperClassGenericType(Class clazz, int index) {
        Map<Class<?>, GenericInfo> infoMap = getGenericInfos(clazz);
        for (Class cl : infoMap.keySet()) {
            GenericInfo info = infoMap.get(cl);
            if (info.genericTypes.length >= index) {
                Type param = info.genericTypes[index];
                return getReturnClass(param);
            }
        }
        return Object.class;
    }

    private static Class getReturnClass(Type param) {
        if (!(param instanceof Class)) {
            if (param instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType)param).getRawType();
                if (rawType instanceof Class) {
                    return (Class<?>)rawType;
                }
            }
            return Object.class;
        }
        return (Class<?>)param;
    }

    /**
     * 获取父类基本类型.
     * 
     * @param genType 反射类型
     */
    public static Class<?> getSuperClassGenericType(Type genType) {
        return getSuperClassGenericType(genType, 0);
    }

    /**
     * 获取父类基本类型.
     * 
     * @param genType 反射类型
     * @param index 输入的索引
     */
    public static Class<?> getSuperClassGenericType(Type genType, int index) {
        // 如果没有实现ParameterizedType接口，即不支持泛型，直接返回Object.class
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        // 返回表示此类型实际类型参数的Type对象的数组,数组里放的都是对应类型的Class
        Type[] params = ((ParameterizedType)genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            throw new FrameworkException("你输入的索引" + (index < 0 ? "不能小于0" : "超出了参数的总数"));
        }
        Type param = params[index];
        return getReturnClass(param);
    }

    /**
     * 通过反射,获得指定类的父类的第一个泛型参数的实际类型. 如BuyerServiceBean extends DaoSupport<Buyer>.
     * 
     * @param clazz clazz 需要反射的类,该类必须继承泛型父类
     * @return 泛型参数的实际类型, 如果没有实现ParameterizedType接口，即不支持泛型，所以直接返回<code>Object.class</code>
     */
    @SuppressWarnings("rawtypes")
    public static Class getSuperClassGenericType(Class clazz) {
        return getSuperClassGenericType(clazz, 0);
    }

    /**
     * 泛型信息
     */
    public static class GenericInfo {
        public Type type;

        public Type[] genericTypes;

        public boolean parameterized = true;

        public Type ownerType;

        public boolean parsed = false;

        public TypeVariable<?>[] typeVariables;
    }

    /**
     * 获取泛型信息
     * 
     * @param type
     */
    public static Map<Class<?>, GenericInfo> getGenericInfos(Class<?> type) {
        List<GenericInfo> result = new ArrayList<GenericInfo>();

        getGenericInfos(type, result, type);

        parseGenericInfo(result, result.size() - 1);

        Map<Class<?>, GenericInfo> map = new HashMap<Class<?>, GenericInfo>();
        for (GenericInfo genericInfo : result) {
            map.put((Class<?>)genericInfo.type, genericInfo);
        }

        return map;

    }

    /**
     * 递归获取泛型信息
     * 
     * @param type
     * @param result
     * @param ownerType
     */
    private static void getGenericInfos(Class<?> type, List<GenericInfo> result, Class<?> ownerType) {
        Type[] genTypes = type.getGenericInterfaces();
        if (genTypes.length > 0) {
            for (Type genType : genTypes) {
                if (genType instanceof ParameterizedType) {
                    ParameterizedType pType = (ParameterizedType)genType;
                    LOG.debug("Parameterized type found " + ((Class<?>)pType.getRawType()).getName());
                    GenericInfo genericInfo = new GenericInfo();
                    genericInfo.type = pType.getRawType();
                    genericInfo.genericTypes = pType.getActualTypeArguments();
                    genericInfo.ownerType = ownerType;
                    for (Type actualArg : pType.getActualTypeArguments()) {
                        if (actualArg instanceof TypeVariable) {
                            genericInfo.parameterized = false;
                        }
                    }
                    if (genericInfo.parameterized) {
                        genericInfo.parsed = true;
                    }
                    genericInfo.typeVariables = ((Class<?>)pType.getRawType()).getTypeParameters();
                    result.add(genericInfo);

                    getGenericInfos((Class<?>)pType.getRawType(), result, (Class<?>)pType.getRawType());
                }
                else if (genType instanceof Class) {
                    LOG.debug("Recursive find generic type " + ((Class<?>)genType).getName());
                    getGenericInfos((Class<?>)genType, result, (Class<?>)genType);
                }
            }
        }

        Type genType = type.getGenericSuperclass();
        if (genType != null && !genType.equals(Object.class)) {
            if (genType instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType)genType;

                LOG.debug("Parameterized type found " + ((Class<?>)pType.getRawType()).getName());

                GenericInfo genericInfo = new GenericInfo();
                genericInfo.type = pType.getRawType();
                genericInfo.genericTypes = pType.getActualTypeArguments();
                genericInfo.ownerType = ownerType;
                for (Type actualArg : pType.getActualTypeArguments()) {
                    if (actualArg instanceof TypeVariable) {
                        genericInfo.parameterized = false;
                    }
                }
                if (genericInfo.parameterized) {
                    genericInfo.parsed = true;
                }
                genericInfo.typeVariables = ((Class<?>)pType.getRawType()).getTypeParameters();
                result.add(genericInfo);

                getGenericInfos((Class<?>)pType.getRawType(), result, (Class<?>)pType.getRawType());
            }
            else if (genType instanceof Class) {
                LOG.debug("Recursive find generic type " + ((Class<?>)genType).getName());
                getGenericInfos((Class<?>)genType, result, (Class<?>)genType);
            }
        }

    }

    /**
     * 解析泛型信息
     * 
     * @param result
     * @param index
     */
    private static void parseGenericInfo(List<GenericInfo> result, int index) {
        for (int i = index; i >= 0; i--) {
            GenericInfo genericInfo = result.get(i);
            if (!genericInfo.parameterized && genericInfo.ownerType != null && !genericInfo.parsed) {
                if (index > 0) {
                    parseGenericInfo(result, index - 1);
                }
                boolean parameterized = true;
                for (int j = 0; j < genericInfo.typeVariables.length; j++) {
                    if (genericInfo.genericTypes[j] instanceof TypeVariable) {
                        TypeVariable<?> typeVariable = (TypeVariable<?>)genericInfo.genericTypes[j];
                        for (int k = index - 1; k >= 0; k--) {
                            GenericInfo ownerInfo = result.get(k);
                            if (genericInfo.ownerType.equals(ownerInfo.type)) {
                                for (int l = 0; l < ownerInfo.typeVariables.length; l++) {
                                    if (typeVariable.getName().equals(ownerInfo.typeVariables[l].getName())) {
                                        if (ownerInfo.genericTypes[l] instanceof Class) {
                                            genericInfo.genericTypes[j] = ownerInfo.genericTypes[l];
                                        }
                                        else {
                                            parameterized = false;
                                        }
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                if (parameterized) {
                    genericInfo.parameterized = true;
                }
                genericInfo.parsed = true;
            }
        }
    }
}
