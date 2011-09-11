package org.odata4j.edm;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.core4j.Enumerable;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.core.Guid;

/**
 * Primitive types in the EDM type system.
 * Simple types are exposed as constants and associated with one or more java-types.
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb399213.aspx">[msdn] Simple Types (EDM)</a>
 */
public class EdmSimpleType extends EdmType {

  private static Set<EdmSimpleType> all = new HashSet<EdmSimpleType>();
  
  // http://msdn.microsoft.com/en-us/library/bb399213.aspx
  public static final EdmSimpleType BINARY = newSimple("Edm.Binary", byte[].class, Byte[].class);
  public static final EdmSimpleType BOOLEAN = newSimple("Edm.Boolean", boolean.class, Boolean.class);
  public static final EdmSimpleType BYTE = newSimple("Edm.Byte", byte.class, Byte.class);
  public static final EdmSimpleType DATETIME = newSimple("Edm.DateTime", LocalDateTime.class);
  public static final EdmSimpleType DATETIMEOFFSET = newSimple("Edm.DateTimeOffset", DateTime.class);
  public static final EdmSimpleType DECIMAL = newSimple("Edm.Decimal", BigDecimal.class);
  public static final EdmSimpleType DOUBLE = newSimple("Edm.Double", double.class, Double.class);
  public static final EdmSimpleType GUID = newSimple("Edm.Guid", Guid.class);
  public static final EdmSimpleType INT16 = newSimple("Edm.Int16", short.class, Short.class);
  public static final EdmSimpleType INT32 = newSimple("Edm.Int32", int.class, Integer.class);
  public static final EdmSimpleType INT64 = newSimple("Edm.Int64", long.class, Long.class);
  public static final EdmSimpleType SINGLE = newSimple("Edm.Single", float.class, Float.class);
  public static final EdmSimpleType STRING = newSimple("Edm.String", char.class, Character.class, String.class);
  public static final EdmSimpleType TIME = newSimple("Edm.Time", LocalTime.class);

  private static EdmSimpleType newSimple(String typeString, Class<?>... javaTypes) {
    EdmSimpleType rt = new EdmSimpleType(typeString, Collections.unmodifiableSet(Enumerable.create(javaTypes).toSet()));
    all.add(rt);
    return rt;
  }
  
  /**
   * Immutable set of all edm simple types.
   */
  public static Set<EdmSimpleType> ALL = Collections.unmodifiableSet(all);

  private final Set<Class<?>> javaTypes;

  private EdmSimpleType(String fullyQualifiedTypeName, Set<Class<?>> javaTypes) {
    super(fullyQualifiedTypeName);
    this.javaTypes = javaTypes;
  }
  
  @Override
  public boolean isSimple() {
    return true;
  }

  /**
   * Gets the java-types associated with this edm-type.
   *
   * @return the associated java-types.
   */
  public Set<Class<?>> getJavaTypes() {
    return javaTypes;
  }

  /**
   * Finds the edm simple type for a given java-type.
   * 
   * @param javaType  the java-type
   * @return the associated edm simple type, else null
   */
  public static EdmSimpleType forJavaType(Class<?> javaType) {
    for (EdmSimpleType simple : ALL)
      if (simple.getJavaTypes().contains(javaType))
        return simple;
    return null;
  }

}
