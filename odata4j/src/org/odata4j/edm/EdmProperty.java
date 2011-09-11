package org.odata4j.edm;

public class EdmProperty extends EdmPropertyBase {

  public final EdmType type;
  public final boolean nullable;
  public final Integer maxLength;
  public final Boolean unicode;
  public final Boolean fixedLength;
  public final String storeGeneratedPattern;

  public final String fcTargetPath;
  public final String fcContentKind;
  public final String fcKeepInContent;
  public final String fcEpmContentKind;
  public final String fcEpmKeepInContent;

  public EdmProperty(String name, EdmType type, boolean nullable) {
    this(name, type, nullable, null, null, null, null, null, null, null, null, null);
  }

  public EdmProperty(String name, EdmType type, boolean nullable, Integer maxLength, Boolean unicode, Boolean fixedLength,
      String storeGeneratedPattern,
      String fcTargetPath, String fcContentKind, String fcKeepInContent, String fcEpmContentKind, String fcEpmKeepInContent) {
    super(name);
    this.type = type;
    this.nullable = nullable;
    this.maxLength = maxLength;
    this.unicode = unicode;
    this.fixedLength = fixedLength;
    this.storeGeneratedPattern = storeGeneratedPattern;

    this.fcTargetPath = fcTargetPath;
    this.fcContentKind = fcContentKind;
    this.fcKeepInContent = fcKeepInContent;
    this.fcEpmContentKind = fcEpmContentKind;
    this.fcEpmKeepInContent = fcEpmKeepInContent;
  }

  @Override
  public String toString() {
    return String.format("EdmProperty[%s,%s]", name, type);
  }

}
