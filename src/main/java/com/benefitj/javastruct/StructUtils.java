package com.benefitj.javastruct;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class StructUtils {

  /**
   * 设置是否可以访问
   *
   * @param ao   可访问对象
   * @param flag 是否可以访问
   */
  public static void setAccessible(AccessibleObject ao, boolean flag) {
    if (ao != null) {
      ao.setAccessible(flag);
    }
  }

  /**
   * 迭代 field
   *
   * @param type        类
   * @param filter      过滤器
   * @param consumer    消费者
   * @param interceptor 拦截器
   * @param superclass  是否继续迭代父类
   */
  public static void foreachField(Class<?> type,
                                  Predicate<Field> filter,
                                  Consumer<Field> consumer,
                                  Predicate<Field> interceptor,
                                  boolean superclass) {
    foreach(type, Class::getDeclaredFields, filter, consumer, interceptor, superclass);
  }

  /**
   * 迭代Class
   *
   * @param type        类
   * @param call        *
   * @param filter      过滤器 -> 返回true表示符合要求，需要处理
   * @param consumer    消费者
   * @param interceptor 拦截器 -> 返回true表示停止循环
   * @param superclass  是否继续迭代父类
   */
  public static <T> void foreach(final Class<?> type,
                                 final Function<Class<?>, T[]> call,
                                 final Predicate<T> filter,
                                 final Consumer<T> consumer,
                                 final Predicate<T> interceptor,
                                 boolean superclass) {
    if (type == null || type == Object.class) {
      return;
    }
    T[] ts = call.apply(type);
    for (T field : ts) {
      if (filter != null) {
        if (filter.test(field)) {
          consumer.accept(field);
        }
      } else {
        consumer.accept(field);
      }
      if (interceptor.test(field)) {
        return;
      }
    }

    if (superclass) {
      foreach(type.getSuperclass(), call, filter, consumer, interceptor, superclass);
    }
  }

  /**
   * 获取字段的值
   *
   * @param field 字段
   * @param obj   原对象
   * @param <V>   值类型
   * @return 返回获取到的值
   */
  public static <V> V getFieldValue(Field field, Object obj) {
    try {
      setAccessible(field, true);
      return (V) field.get(obj);
    } catch (IllegalAccessException ignore) {/* ~ */}
    return null;
  }

  /**
   * 设置字段的值
   *
   * @param field 字段
   * @param obj   对象
   * @param value 值
   * @return 返回是否设置成功
   */
  public static boolean setFieldValue(Field field, Object obj, Object value) {
    if (field != null && obj != null) {
      try {
        setAccessible(field, true);
        field.set(obj, value);
        return true;
      } catch (IllegalAccessException ignore) {/* ~ */}
    }
    return false;
  }

}
