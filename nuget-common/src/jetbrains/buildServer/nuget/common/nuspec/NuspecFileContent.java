/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.common.nuspec;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Evgeniy.Koshkin
 */
public class NuspecFileContent {

  private static final String NS = "http://schemas.microsoft.com/packaging/2010/07/nuspec.xsd";
  private static final String METADATA_ELEMENT = "metadata";
  private static final String ID = "id";
  private static final String VERSION = "version";
  private static final String TITLE_ELEMENT = "title";
  private static final String FRAMEWORK_ASSEMBLIES_ELEMENT = "frameworkAssemblies";
  private static final String FRAMEWORK_ASSEMBLY_ELEMENT = "frameworkAssembly";
  private static final String TARGET_FRAMEWORK_ATTRIBUTE = "targetFramework";
  private static final String RELEASE_NOTES_ELEMENT = "releaseNotes";
  private static final String AUTHORS_ELEMENT = "authors";
  private static final String DESCRIPTION_ELEMENT = "description";
  private static final String COPYRIGHT_ELEMENT = "copyright";
  private static final String MIN_CLIENT_VERSION_ATTRIBUTE = "minClientVersion";
  private static final String REQUIRE_LICENSE_ACCEPTANCE_ELEMENT = "requireLicenseAcceptance";
  private static final String LICENSE_URL_ELEMENT = "licenseUrl";
  private static final String PROJECT_URL_ELEMENT = "projectUrl";
  private static final String TAGS_ELEMENT = "tags";
  private static final String ICON_URL_ELEMENT = "iconUrl";
  private static final String DEPENDENCIES_ELEMENT = "dependencies";
  private static final String DEPENDENCY_ELEMENT = "dependency";
  private static final String GROUP_ELEMENT = "group";

  private final Element myContent;

  public NuspecFileContent(@NotNull Element content) {
    myContent = content;
  }

  public String getId() {
    return parseMetadataProperty(myContent, ID);
  }
  public String getVersion() {
    return parseMetadataProperty(myContent, VERSION);
  }
  public String getTitle() {
    return parseMetadataProperty(myContent, TITLE_ELEMENT);
  }
  public String getReleaseNotes() {
    return parseMetadataProperty(myContent, RELEASE_NOTES_ELEMENT);
  }
  public String getAuthors() {
    return parseMetadataProperty(myContent, AUTHORS_ELEMENT);
  }
  public String getDescription() {
    return parseMetadataProperty(myContent, DESCRIPTION_ELEMENT);
  }
  public String getCopyright() {
    return parseMetadataProperty(myContent, COPYRIGHT_ELEMENT);
  }
  public String getMinClientVersion() {
    return parseMetadataAttribute(myContent, MIN_CLIENT_VERSION_ATTRIBUTE);
  }
  public String getRequireLicenseAcceptance() {
    return parseMetadataProperty(myContent, REQUIRE_LICENSE_ACCEPTANCE_ELEMENT);
  }
  public String getLicenseUrl() {
    return parseMetadataProperty(myContent, LICENSE_URL_ELEMENT);
  }
  public String getProjectUrl() {
    return parseMetadataProperty(myContent, PROJECT_URL_ELEMENT);
  }
  public String getTags() {
    return parseMetadataProperty(myContent, TAGS_ELEMENT);
  }
  public String getIconUrl() {
    return parseMetadataProperty(myContent, ICON_URL_ELEMENT);
  }

  @Nullable
  public Dependencies getDependencies(){
    final Element metadata = getChild(myContent, METADATA_ELEMENT);
    final Element dependencies = getChild(metadata, DEPENDENCIES_ELEMENT);
    if(dependencies == null) return null;

    final Collection<Dependency> topLevelDependencies = new HashSet<Dependency>();
    for (Element dep : getChildren(dependencies, DEPENDENCY_ELEMENT)) {
      final String id = dep.getAttributeValue(ID);
      final String versionConstraint = dep.getAttributeValue(VERSION);
      topLevelDependencies.add(new Dependency(id, versionConstraint));
    }

    final Collection<DependencyGroup> groups = new HashSet<DependencyGroup>();
    for (Element group : getChildren(dependencies, GROUP_ELEMENT)) {
      final Collection<Dependency> groupDependencies = new HashSet<Dependency>();
      for (Element dep : getChildren(group, DEPENDENCY_ELEMENT)) {
        final String id = dep.getAttributeValue(ID);
        final String versionConstraint = dep.getAttributeValue(VERSION);
        groupDependencies.add(new Dependency(id, versionConstraint));
      }
      groups.add(new DependencyGroup(groupDependencies, group.getAttributeValue(TARGET_FRAMEWORK_ATTRIBUTE)));
    }

    return new Dependencies(topLevelDependencies, groups);
  }

  @NotNull
  public Set<FrameworkAssembly> getFrameworkAssemblies() {
    final Set<FrameworkAssembly> frameworkAssemblies = new HashSet<FrameworkAssembly>();
    for(Element frameworkAssemblyElement : getChildren(getChild(getChild(myContent, METADATA_ELEMENT), FRAMEWORK_ASSEMBLIES_ELEMENT), FRAMEWORK_ASSEMBLY_ELEMENT)) {
      frameworkAssemblies.add(new FrameworkAssembly(frameworkAssemblyElement.getAttributeValue(TARGET_FRAMEWORK_ATTRIBUTE)));
    }
    return frameworkAssemblies;
  }

  @Nullable
  private static String parseMetadataProperty(@NotNull final Element root, final @NotNull String name) {
    final Element child = getChild(getChild(root, METADATA_ELEMENT), name);
    return child == null ? null : child.getTextNormalize();
  }

  @Nullable
  private String parseMetadataAttribute(@NotNull final Element root, @NotNull final String attribute) {
    final Element metadata = getChild(root, METADATA_ELEMENT);
    if (metadata == null) return null;
    return metadata.getAttributeValue(attribute);
  }

  @Nullable
  private static Element getChild(@Nullable final Element root, final String childName) {
    if (root == null) return null;
    Element child = root.getChild(childName);
    if (child != null) return child;
    return root.getChild(childName, root.getNamespace(NS));
  }

  @NotNull
  private static List<Element> getChildren(@Nullable final Element root, final String child) {
    if (root == null) return Collections.emptyList();
    List<Element> result = new ArrayList<Element>();
    for (List list : Arrays.asList(root.getChildren(child), root.getChildren(child, root.getNamespace(NS)))) {
      for (Object o : list) {
        result.add((Element)o);
      }
    }
    return result;
  }
}
