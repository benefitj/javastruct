package com.benefitj.javastruct;


import com.benefitj.javastruct.convert.DateTimeFieldConverter;
import com.benefitj.javastruct.convert.DefaultPrimitiveFieldConverter;
import com.benefitj.javastruct.convert.FieldConverter;
import com.benefitj.javastruct.convert.HexStringConverter;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 结构体管理
 */
public class JavaStructManager {

  public static final JavaStructManager INSTANCE = new JavaStructManager();

  /**
   * 字段解析器
   */
  private final Map<Class<?>, FieldConverter<?>> fieldConverters = Collections.synchronizedMap(new LinkedHashMap<>());
  /**
   * 缓存的类
   */
  private final Map<Class, StructClass> structClasses = new WeakHashMap<>();
  /**
   * 字符串编码
   */
  private String charset = Charset.defaultCharset().name();
  /**
   * 二进制转换工具
   */
  protected final BinaryHelper binary = BinaryHelper.INSTANCE;
  /**
   * 类结构解析对象
   */
  private StructResolver structResolver;

  public JavaStructManager() {
    this(true);
  }

  public JavaStructManager(boolean initSetup) {
    if (initSetup) {
      setup();
    }
  }

  /**
   * 初始化
   */
  public void setup() {
    // 初始化解析器
    this.put(new DefaultPrimitiveFieldConverter());
    this.put(new DateTimeFieldConverter());
    this.put(new HexStringConverter());

    if (this.structResolver == null) {
      this.structResolver = new DefaultStructResolver();
    }
  }

  public String getCharset() {
    return charset;
  }

  public void setCharset(String charset) {
    this.charset = charset;
  }

  public StructResolver getStructResolver() {
    return structResolver;
  }

  public void setStructResolver(StructResolver structResolver) {
    this.structResolver = structResolver;
  }

  public Map<Class, StructClass> getStructClasses() {
    return structClasses;
  }

  /**
   * 获取结构体信息
   *
   * @param type   对象类型
   * @param create 如果不存在是否创建
   * @return 返回结构体信息
   */
  public StructClass getStructClass(Class<?> type, boolean create) {
    if (create) {
      return this.getStructClasses().computeIfAbsent(type, this::parseStructClass);
    }
    return this.getStructClasses().get(type);
  }

  /**
   * 字段转换器
   */
  public Map<Class<?>, FieldConverter<?>> getFieldConverters() {
    return fieldConverters;
  }

  public FieldConverter<?> put(FieldConverter converter) {
    return put(converter.getClass(), converter);
  }

  public FieldConverter<?> put(Class<? extends FieldConverter> type, FieldConverter converter) {
    return getFieldConverters().put(type, converter);
  }

  /**
   * 获取字段解析器
   *
   * @param resolverType 解析器类型
   * @return 返回对应的解析器
   */
  public FieldConverter getFieldResolver(Class<?> resolverType) {
    return getFieldConverters().get(resolverType);
  }

  public BinaryHelper getBinary() {
    return binary;
  }

  /**
   * 转换字节数组
   *
   * @param o 对象
   * @return 返回转换的字节数组
   */
  public byte[] toBytes(Object o) {
    return getStructClass(o.getClass(), true).toBytes(o);
  }

  /**
   * 解析结构体数据
   *
   * @param type 类型
   * @param data 数据
   * @param <T>  对象类型
   * @return 返回解析的对象
   */
  public <T> T parseObject(Class<T> type, byte[] data) {
    return parseObject(type, data, 0);
  }

  /**
   * 解析结构体数据
   *
   * @param type  类型
   * @param data  数据
   * @param start 开始的位置
   * @param <T>   对象类型
   * @return 返回解析的对象
   */
  public <T> T parseObject(Class<T> type, byte[] data, int start) {
    return getStructClass(type, true).parseObject(data, start);
  }

  /**
   * 解析结构体
   *
   * @param type 类型
   * @return 返回解析的结构体信息
   */
  protected StructClass parseStructClass(Class<?> type) {
    return getStructResolver().resolve(this, type);
  }

}
