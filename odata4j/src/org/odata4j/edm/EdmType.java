package org.odata4j.edm;

import java.util.Map;

import org.core4j.Enumerable;
import org.odata4j.core.OFuncs;

/**
 * A type in the EDM type system.
 * 
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb399591.aspx">[msdn] Types (Metadata)</a>
 */
public abstract class EdmType {

  private static class LazyInit {
    private static final Map<String, EdmType> POOL = Enumerable.create(EdmSimpleType.ALL)
        .cast(EdmType.class)
        .toMap(OFuncs.edmTypeFullyQualifiedTypeName());
  }
  
  private final String fullyQualifiedTypeName;
  
  public EdmType(String fullyQualifiedTypeName) {
    this.fullyQualifiedTypeName = fullyQualifiedTypeName;
  }

  /**
   * Gets an edm-type for a given type name.
   *
   * @param fullyQualifiedTypeName  the fully-qualified type name
   * @return the edm-type
   */
  public static EdmType get(String fullyQualifiedTypeName) {
    if (fullyQualifiedTypeName == null)
      return null;
    if (!LazyInit.POOL.containsKey(fullyQualifiedTypeName))
      LazyInit.POOL.put(fullyQualifiedTypeName, new EdmNonSimpleType(fullyQualifiedTypeName));
    return LazyInit.POOL.get(fullyQualifiedTypeName);
  }
  
  /**
   * Gets the fully-qualified type name for this edm-type.
   */
  public String getFullyQualifiedTypeName() {
    return this.fullyQualifiedTypeName;
  }

  @Override
  public String toString() {
    return String.format("%s[%s]", getClass().getSimpleName(), getFullyQualifiedTypeName());
  }

  @Override
  public int hashCode() {
    return this.fullyQualifiedTypeName.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof EdmType && ((EdmType) other).fullyQualifiedTypeName.equals(this.fullyQualifiedTypeName);
  }
  
  public abstract boolean isSimple();
}
