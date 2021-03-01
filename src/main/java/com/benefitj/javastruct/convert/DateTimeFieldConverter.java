package com.benefitj.javastruct.convert;

import com.benefitj.javastruct.JavaStructField;
import com.benefitj.javastruct.PrimitiveType;
import com.benefitj.javastruct.StructField;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Date;

/**
 * 时间戳转换器
 */
public class DateTimeFieldConverter extends AbstractFieldConverter<Object> {

  public DateTimeFieldConverter() {
  }

  public DateTimeFieldConverter(boolean local) {
    super(local);
  }

  @Override
  public boolean support(Field field, JavaStructField jsf, PrimitiveType pt) {
    Class<?> type = field.getType();
    return type.isAssignableFrom(Date.class) || type.isAssignableFrom(Timestamp.class);
  }

  @Override
  public byte[] convert(StructField field, Object value) {
    long time;
    if (value instanceof Timestamp) {
      time = ((Timestamp) value).getTime();
    } else {
      time = ((Date) value).getTime();
    }
    int size = field.getFieldSize();
    byte[] bytes;
    switch (size) {
      case 4:
        bytes = getBinary().intToBytes((int) (time / 1000), field.getByteOrder());
        break;
      case 6:
        byte[] buf = getCache(6);
        bytes = getBinary().longToBytes((int) (time / 1000), field.getByteOrder());
        copy(bytes, 0, buf, 0);
        bytes = getBinary().intToBytes((int) time % 1000, field.getByteOrder());
        return copy(bytes, 0, buf, 4, 2);
      case 8:
      default:
        bytes = getBinary().longToBytes(time, field.getByteOrder());
        break;
    }
    return copy(bytes, srcPos(bytes, size), getCache(size), destPos(bytes, size));
  }

  @Override
  public Object parse(StructField field, byte[] data, int position) {
    long time;
    int size = field.getFieldSize();
    if (size == 4) {
      byte[] buf = copy(data, position, getCache(size), 0);
      time = getBinary().bytesToLong(buf, field.getByteOrder()) * 1000;
    } else if (size == 6) {
      byte[] buf = copy(data, position, getCache(4), 0, 4);
      time = getBinary().bytesToLong(buf, field.getByteOrder());
      buf = copy(data, position + 4, getCache(2), 0, 2);
      time += getBinary().bytesToLong(buf, field.getByteOrder());
    } else {
      byte[] buf = copy(data, position, getCache(size), 0);
      time = getBinary().bytesToLong(buf);
    }

    Field f = field.getField();
    if (f.getType().isAssignableFrom(java.sql.Date.class)) {
      return new java.sql.Date(time);
    } else if (f.getType().isAssignableFrom(Date.class)) {
      return new Date(time);
    } else if (f.getType().isAssignableFrom(Timestamp.class)) {
      return new Timestamp(time);
    }
    throw new UnsupportedOperationException();
  }

}
