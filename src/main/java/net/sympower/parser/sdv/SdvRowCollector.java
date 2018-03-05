package net.sympower.parser.sdv;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class SdvRowCollector<T> {

  private static final String SETTER_PREFIX = "set";
  private static final String ADD_PREFIX = "add";

  private final T document;

  private final HashMap<Class<?>, Method> handlerMethods = new HashMap<>();
  private final HashMap<Class<?>, Field> handlerSimpleFields = new HashMap<>();
  private final HashMap<Class<?>, Collection> handlerCollections = new HashMap<>();

  SdvRowCollector(Class<T> documentType) {
    this(makeDoc(documentType));
  }

  SdvRowCollector(T document) {
    this.document = document;
    Class<T> documentType = (Class<T>) document.getClass();
    for (Method method : documentType.getMethods()) {
      registerMethod(method);
    }
    for (Method method : documentType.getDeclaredMethods()) {
      method.setAccessible(true);
      registerMethod(method);
    }
    for (Field field : documentType.getFields()) {
      registerField(field);
    }
    for (Field field : documentType.getDeclaredFields()) {
      field.setAccessible(true);
      registerField(field);
    }
  }

  private static <T> T makeDoc(Class<T> documentType) {
    try {
      return documentType.newInstance();
    }
    catch (InstantiationException | IllegalAccessException e) {
      throw new SdvParsingReflectionException(
        String.format("Error while invoking constructor on class %s", documentType), e);
    }
  }

  private void registerMethod(Method method) {
    if (method.getDeclaredAnnotation(SdvIgnore.class) != null) {
      return;
    }
    if (method.getName().startsWith(SETTER_PREFIX) || method.getName().startsWith(ADD_PREFIX)) {
      Class<?>[] parameterTypes = method.getParameterTypes();
      if (parameterTypes.length == 1) {
        handlerMethods.put(parameterTypes[0], method);
      }
    }
  }

  private void registerField(Field field) {
    if (field.getDeclaredAnnotation(SdvIgnore.class) != null) {
      return;
    }
    if (Collection.class.isAssignableFrom(field.getType())) {
      try {
        Collection collection = (Collection) field.get(document);
        if (collection == null) {
          collection = new ArrayList();
          field.set(document, collection);
        }
        handlerCollections.put((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0], collection);
      }
      catch (IllegalAccessException e) {
        throw new SdvParsingReflectionException(
          String.format("Error while getting field %s on class %s", field.getName(), document.getClass()), e);
      }
    }
    else {
      handlerSimpleFields.put(field.getType(), field);
    }
  }

  void registerRowBeanTypes(SdvReader reader) {
    for (Class<?> rowType : handlerMethods.keySet()) {
      reader.registerRowType(rowType);
    }
    for (Class<?> rowType : handlerSimpleFields.keySet()) {
      reader.registerRowType(rowType);
    }
    for (Class<?> rowType : handlerCollections.keySet()) {
      reader.registerRowType(rowType);
    }
  }

  void newRow(Object o) {
    Method method = handlerMethods.get(o.getClass());
    if (method != null) {
      try {
        method.invoke(document, o);
        return;
      }
      catch (InvocationTargetException | IllegalAccessException e) {
        throw new SdvParsingReflectionException(
          String.format("Error while invoking method %s on class %s", method.getName(), document.getClass()), e);
      }
    }
    Collection collection = handlerCollections.get(o.getClass());
    if (collection != null) {
      collection.add(o);
      return;
    }
    Field field = handlerSimpleFields.get(o.getClass());
    if (field != null) {
      try {
        field.set(document, o);
        return;
      }
      catch (IllegalAccessException e) {
        throw new SdvParsingReflectionException(
          String.format("Error while setting field %s on class %s", field.getName(), document.getClass()), e);
      }
    }
  }

  public T getDocument() {
    return document;
  }

}
