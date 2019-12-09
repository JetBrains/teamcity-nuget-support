package jetbrains.buildServer.nuget.tests.integration;

import com.intellij.openapi.util.io.StreamUtil;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion;
import jetbrains.buildServer.nuget.feed.server.json.JsonServiceIndexHandler;
import jetbrains.buildServer.tools.utils.SemanticVersion;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.SimpleHttpServerBase;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.apache.olingo.odata2.api.edm.EdmLiteral;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.uri.expression.*;
import org.apache.olingo.odata2.core.uri.expression.ExpressionParserInternalError;
import org.apache.olingo.odata2.core.uri.expression.FilterParserImpl;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.util.SimpleHttpServerBase.*;

public class MockNuGetRequestHandler {
  private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("((?<key>[a-zA-Z0-9\\.]+)\\s*=\\s*'(?<value>[a-zA-Z0-9\\.]*)',?)");
  private static final Pattern PACKAGE_FILE_NAME = Pattern.compile("(?<name>[a-zA-Z0-9-\\.]+.nupkg)");
  private static final String FINDPACKAGESBYID_QUERY_MARKER = "findpackagesbyid()?";
  private static final String KEY_VALUE_QUERY_MARKER = "nuget/packages(";
  private static final String FILTER_QUERY_MARKER = "?$filter=";

  private final MockNuGetHTTPServerApi myServerApi;
  private final List<Package> myPackages = new ArrayList<>();

  public MockNuGetRequestHandler(MockNuGetHTTPServerApi serverApi) {
    myServerApi = serverApi;
  }

  public SimpleHttpServerBase.Response getResponse(String request) throws IOException, JDOMException {
    final String path = myServerApi.getRequestPath(request);
    if (path == null) return createStreamResponse(STATUS_LINE_404, Collections.<String>emptyList(), "Not found".getBytes("utf-8"));
    log("NuGet request path: " + path);

    if (myServerApi.getApiVersion() != NuGetAPIVersion.V3) {
      return getODataResponse(path);
    } else {
      return getJsonResponse(path);
    }
  }

  public void withPackage(final String packageName, final String version, final String metadataFilePath, final String nupkgDataPath, final boolean isLatestVersion) {
    myPackages.add(new Package(packageName, version, metadataFilePath, nupkgDataPath, isLatestVersion));
  }

  @NotNull
  private SimpleHttpServerBase.Response getODataResponse(String path) throws IOException, JDOMException {
    path = path.toLowerCase();

    final List<String> xml = Arrays.asList("DataServiceVersion: 1.0;", "Content-Type: application/xml;charset=utf-8");
    final List<String> atom = Arrays.asList("DataServiceVersion: 2.0;", "Content-Type: application/atom+xml;charset=utf-8");

    if (path.endsWith("$metadata")) {
      return createStringResponse(STATUS_LINE_200, xml, loadMockODataFiles("feed/mock/feed.metadata.xml"));
    }

    if ("/nuget/".equals(path)) {
      return createStringResponse(STATUS_LINE_200, xml, loadMockODataFiles("feed/mock/feed.root.xml"));
    }

    String filterString = extractPackagesFilter(path);
    if (filterString != null) {
      final List<Package> packages = filterPackages(filterString);
      if (!packages.isEmpty()) {
        final Document resultDocument = concatPackagesXmlMetadata(packages);
        final String resultDocumentStr = XmlUtil.toString(resultDocument);
        return createStringResponse(STATUS_LINE_200, atom, prepareOutput(resultDocumentStr));
      }
    }

    final Matcher downloadFileNameMatcher = PACKAGE_FILE_NAME.matcher(path);
    if (downloadFileNameMatcher.find()) {
      final String nupkgName = downloadFileNameMatcher.group("name");
      final Optional<Package> pkg = myPackages.stream().filter(x -> x.getNupkgName().toLowerCase().equals(nupkgName)).findFirst();
      if (pkg.isPresent()) {
        return myServerApi.createFileResponse(Paths.getTestDataPath(pkg.get().getNupkgDataPath()), Arrays.asList("Content-Type: application/zip"));
      }
    }

    return createStringResponse(STATUS_LINE_404, Collections.<String>emptyList(), "Not found");
  }

  private String extractPackagesFilter(String path) throws UnsupportedEncodingException {
    path = URLDecoder.decode(path, "UTF-8");
    String filterString = null;
    if (path.contains("nuget/packages()") && path.contains(FILTER_QUERY_MARKER)) {
      filterString = path.substring(path.indexOf(FILTER_QUERY_MARKER) + FILTER_QUERY_MARKER.length());
      if (filterString.contains("&")) {
        filterString = filterString.substring(0, filterString.indexOf("&"));
      }
    } else if (path.contains(KEY_VALUE_QUERY_MARKER)) {
      filterString = extractPackagesFilterFromKeyValues(path);
    } else if (path.contains(FINDPACKAGESBYID_QUERY_MARKER)) {
      filterString = path.substring(path.indexOf(FINDPACKAGESBYID_QUERY_MARKER) + FINDPACKAGESBYID_QUERY_MARKER.length());
      filterString = extractPackagesFilterFromKeyValues(filterString);
    }
    return filterString;
  }

  @NotNull
  private String extractPackagesFilterFromKeyValues(final String path) {
    String filterString = "";
    final Matcher matcher = KEY_VALUE_PATTERN.matcher(path);
    while(matcher.find()) {
      final String key = matcher.group("key");
      final String value = matcher.group("value");
      if (filterString.length() > 0) {
        filterString += " and ";
      }
      filterString += "tolower(" + key + ") eq '" + value + "'";
    }
    return filterString;
  }

  private List<Package> filterPackages(final String filterString) {
    FilterParserImpl filterParser = new FilterParserImpl(null);
    PackageFilterExpressionItem packageFilterExpressionItem = null;
    try {
      final FilterExpression filterExpression = filterParser.parseFilterString(filterString);
      packageFilterExpressionItem = (PackageFilterExpressionItem)filterExpression.accept(new ExpressionEvaluator());
    } catch (ExpressionParserException | ExceptionVisitExpression | ODataApplicationException | ExpressionParserInternalError e) {
      log(e.getMessage());
      return new ArrayList<>();
    }
    return filterPackages(packageFilterExpressionItem);
  }

  private List<Package> filterPackages(final PackageFilterExpressionItem filterRequest) {
    return myPackages.stream().filter(x -> (Boolean)filterRequest.getValue(x)).collect(Collectors.toList());
  }

  private SimpleHttpServerBase.Response getJsonResponse(String path) throws IOException {
    if (path.contains("/index.json")) {
      try (InputStream is = JsonServiceIndexHandler.class.getResourceAsStream("/feed-metadata/NuGet-V3.json")) {
        String pattern = StreamUtil.readText(is);
        String sourceUrl = StringUtil.trimEnd(myServerApi.getSourceUrl(), "/");
        String text = String.format(pattern, sourceUrl, sourceUrl);
        return createStringResponse(STATUS_LINE_200, Collections.emptyList(), text);
      }
    }

    if (path.endsWith("/flatcontainer/finecollection/2.2.1/finecollection.2.2.1.nupkg")) {
      return myServerApi.createFileResponse(Paths.getTestDataPath("feed/mock/feed.finecollection.1.0.189.152.nupkg"), Arrays.asList("Content-Type: application/zip"));
    }

    return createStringResponse(STATUS_LINE_404, Collections.emptyList(), "Not found");
  }

  private String loadMockODataFiles(@NotNull String name) throws IOException {
    String source = loadFileUTF8(name);
    return prepareOutput(source);
  }

  private String prepareOutput(@NotNull String source) {
    source = source.replace("http://buildserver.labs.intellij.net/httpAuth/app/nuget/v1/FeedService.svc/", myServerApi.getSourceUrl());
    source = source.replace("http://buildserver.labs.intellij.net/httpAuth/repository/download/", myServerApi.getDownloadUrl());
    source = source.replaceAll("xml:base=\".*\"", "xml:base=\"" + myServerApi.getSourceUrl() + "\"");
    return source;
  }

  private Document concatPackagesXmlMetadata(final List<Package> packages) throws IOException, JDOMException {
    if (packages.isEmpty()) throw new IllegalArgumentException("packages is empty");

    final Document resultDocument = loadXmlMetadata(packages.get(0));
    final Element baseElement = resultDocument.getRootElement();
    final XPath xpath = XPath.newInstance("*[name()='feed']/*[name()='entry']");
    for (int index = 1; index < packages.size(); index++) {
      final Document document = loadXmlMetadata(packages.get(index));
      baseElement.addContent(((Element)xpath.selectSingleNode(document)).detach());
    }
    return resultDocument;
  }

  @NotNull
  private Document loadXmlMetadata(@NotNull Package pkg) throws IOException, JDOMException {
    File file = Paths.getTestDataPath(pkg.getMetadataFilePath());
    return FileUtil.parseDocument(file).getDocument();
  }

  @NotNull
  private String loadFileUTF8(@NotNull String name) throws IOException {
    File file = Paths.getTestDataPath(name);
    return loadFileUTF8(file);
  }

  private String loadFileUTF8(@NotNull File file) throws IOException {
    final InputStream is = new BufferedInputStream(new FileInputStream(file));
    try {
      final Reader rdr = new InputStreamReader(is, "utf-8");
      StringBuilder sb = new StringBuilder();
      int ch;
      while ((ch = rdr.read()) >= 0) {
        sb.append((char) ch);
      }
      return sb.toString();
    } finally {
      FileUtil.close(is);
    }
  }

  private void log(String message) {
    System.out.println("[mock feed] " + message);
  }

  private class PackageVersion {
    private final String myVersion;

    private PackageVersion(final String version) {
      myVersion = version;
    }

    public String getVersion() { return myVersion; }

    public SemanticVersion getSemanticVersion() { return SemanticVersion.valueOf(myVersion); }
  }

  private class Package {
    private final String myName;
    private final PackageVersion myVersion;
    private final String myMetadataFilePath;
    private final String myNupkgDataPath;
    private final boolean myIsLatestVersion;

    private Package(final String name, final String version, final String metadataFilePath, final String nupkgDataPath, final boolean isLatestVersion) {
      myName = name;
      myVersion = new PackageVersion(version);
      myMetadataFilePath = metadataFilePath;
      myNupkgDataPath = nupkgDataPath;
      myIsLatestVersion = isLatestVersion;
    }

    public String getName() {
      return myName;
    }

    public PackageVersion getVersion() {
      return myVersion;
    }

    public String getMetadataFilePath() {
      return myMetadataFilePath;
    }

    public String getNupkgDataPath() {
      return myNupkgDataPath;
    }

    public boolean isLatestVersion() {
      return myIsLatestVersion;
    }

    public String getNupkgName() {
      return myName + "." + myVersion.getVersion() + ".nupkg";
    }
  }

  private interface PackageFilterExpressionItem {
    public Class<?> getType();
    public Object getValue(Package pkg);
  }

  private class ExpressionEvaluator implements ExpressionVisitor {

    @Override
    public Object visitFilterExpression(final FilterExpression filterExpression, final String expressionString, final Object expression) {
      return expression;
    }

    @Override
    public Object visitBinary(final BinaryExpression binaryExpression,
                              final BinaryOperator operator,
                              final Object leftSide, final Object rightSide) {
      final PackageFilterExpressionItem left = (PackageFilterExpressionItem)leftSide;
      final PackageFilterExpressionItem right = (PackageFilterExpressionItem)rightSide;
      checkTypesCompatibility(operator, left, right);

      switch (operator) {
        case EQ: {
          if (left.getType() == Boolean.class) {
            return new PackageFilterExpressionItem() {
              @Override
              public Class<?> getType() { return Boolean.class; }
              @Override
              public Object getValue(final Package pkg) {
                final Boolean leftValue = (Boolean)left.getValue(pkg);
                final Boolean rightValue = (Boolean)right.getValue(pkg);
                return leftValue == rightValue;
              }
            };
          }
          return new PackageFilterExpressionItem() {
            @Override
            public Class<?> getType() { return Boolean.class; }
            @Override
            public Object getValue(final Package pkg) {
              final Object leftValue = left.getValue(pkg);
              final Object rightValue = right.getValue(pkg);
              if (leftValue == rightValue) return true;
              if (leftValue == null ^ rightValue == null) return false;
              if (left.getType() == right.getType()) {
                if (left.getType() == String.class) {
                  final String leftStr = (String)left.getValue(pkg);
                  final String rightStr = (String)right.getValue(pkg);
                  return leftStr.equals(rightStr);
                } else {
                  final PackageVersion leftVersion = (PackageVersion)left.getValue(pkg);
                  final PackageVersion rightVersion = (PackageVersion)right.getValue(pkg);
                  return leftVersion.getVersion().equals(rightVersion.getVersion()) ||
                         leftVersion.getSemanticVersion().compareTo(rightVersion.getSemanticVersion()) == 0;
                }
              }
              if (left.getType() == String.class) {
                final String leftStr = (String)left.getValue(pkg);
                final PackageVersion rightVersion = (PackageVersion)right.getValue(pkg);
                return leftStr.equals(rightVersion.getVersion()) ||
                       SemanticVersion.valueOf(leftStr).compareTo(rightVersion.getSemanticVersion()) == 0;
              } else {
                final String rightStr = (String)right.getValue(pkg);
                final PackageVersion leftVersion = (PackageVersion)left.getValue(pkg);
                return rightStr.equals(leftVersion.getVersion()) ||
                       SemanticVersion.valueOf(rightStr).compareTo(leftVersion.getSemanticVersion()) == 0;
              }
            }
          };

        }
        case OR:
        case AND:
        {
          if (left.getType() != Boolean.class) {
            throw new RuntimeException("Incorrect left and/or right types for operator: " + operator.toString());
          }
          return new PackageFilterExpressionItem() {
            @Override
            public Class<?> getType() { return Boolean.class; }
            @Override
            public Object getValue(final Package pkg) {
              final Boolean leftValue = (Boolean)left.getValue(pkg);
              final Boolean rightValue = (Boolean)right.getValue(pkg);
              if (operator == BinaryOperator.OR)
                return leftValue || rightValue;
              return leftValue && rightValue;
            }
          };
        }
      }
      throw new RuntimeException("Unknown operator: " + operator.toString());
    }

    private void checkTypesCompatibility(final BinaryOperator operator,
                                         final PackageFilterExpressionItem left,
                                         final PackageFilterExpressionItem right) {
      final Class<?> leftType = left.getType();
      if (left.getType() == right.getType()) {
        if (leftType != String.class && leftType != Boolean.class && leftType != PackageVersion.class) {
          throw new RuntimeException("Incorrect left and/or right types for operator: " + operator.toString());
        }
        return;
      }
      final Class<?> rightType = right.getType();
      if (leftType == String.class && rightType == PackageVersion.class ||
          leftType == PackageVersion.class && rightType == String.class) {
        return;
      }
      throw new RuntimeException("Incorrect left and/or right types for operator: " + operator.toString());
    }

    @Override
    public Object visitOrderByExpression(final OrderByExpression orderByExpression, final String expressionString, final List<Object> orders) {
      return null;
    }

    @Override
    public Object visitOrder(final OrderExpression orderExpression, final Object filterResult, final SortOrder sortOrder) {
      return null;
    }

    @Override
    public Object visitLiteral(final LiteralExpression literal, final EdmLiteral edmLiteral) {
      return new PackageFilterExpressionItem() {
        @Override
        public Class<?> getType() { return String.class; }
        @Override
        public Object getValue(final Package pkg) {
          return literal.getUriLiteral().substring(1, literal.getUriLiteral().length() - 1);
        }
      };
    }

    @Override
    public Object visitMethod(final MethodExpression methodExpression, final MethodOperator method, final List<Object> parameters) {
      switch(method.toString()) {
        case "tolower":
          if (parameters.size() != 1 ) {
            throw new RuntimeException("Incorrect method parameters count. Method: " + method.toString());
          }
          final PackageFilterExpressionItem param = ((PackageFilterExpressionItem)parameters.get(0));
          if (param.getType() != String.class && param.getType() != PackageVersion.class) {
            throw new RuntimeException("Incorrect method parameter types. Method: " + method.toString());
          }
          return new PackageFilterExpressionItem() {
            @Override
            public Class<?> getType() { return param.getType(); }
            @Override
            public Object getValue(final Package pkg) {
              final Object value = param.getValue(pkg);
              if (param.getType() == String.class) {
                final String strValue = (String)value;
                if (strValue == null) return null;
                return strValue.toLowerCase();
              }
              return param.getValue(pkg);
            }
          };
      }
      throw new RuntimeException("Undefined method: " + method.toString());
    }

    @Override
    public Object visitMember(final MemberExpression memberExpression, final Object path, final Object property) {
      return null;
    }

    @Override
    public Object visitProperty(final PropertyExpression propertyExpression, final String uriLiteral, final EdmTyped edmProperty) {
      switch (uriLiteral) {
        case "islatestversion":
          return new PackageFilterExpressionItem() {
            @Override
            public Class<?> getType() { return Boolean.class; }
            @Override
            public Object getValue(final Package pkg) { return pkg.isLatestVersion(); }
          };
        case "id": {
          return new PackageFilterExpressionItem() {
            @Override
            public Class<?> getType() { return String.class; }
            @Override
            public Object getValue(final Package pkg) { return pkg.getName(); }
          };
        }
        case "version": {
          return new PackageFilterExpressionItem() {
            @Override
            public Class<?> getType() { return PackageVersion.class; }
            @Override
            public Object getValue(final Package pkg) { return pkg.getVersion(); }
          };
        }
      }
      throw new RuntimeException("Undefined property: " + uriLiteral);
    }

    @Override
    public Object visitUnary(final UnaryExpression unaryExpression, final UnaryOperator operator, final Object operand) {
      return null;
    }
  }
}
