package jetbrains.buildServer.nuget.tests.integration;

import com.intellij.openapi.util.io.StreamUtil;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.xpath.*;
import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion;
import jetbrains.buildServer.nuget.feed.server.json.JsonServiceIndexHandler;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.SimpleHttpServerBase;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.jetbrains.annotations.NotNull;

import static jetbrains.buildServer.util.SimpleHttpServerBase.*;

public class MockNuGetRequestHandler {
  private final MockNuGetHTTPServerApi myServerApi;

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

  @NotNull
  private SimpleHttpServerBase.Response getODataResponse(String path) throws IOException, JDOMException {
    final List<String> xml = Arrays.asList("DataServiceVersion: 1.0;", "Content-Type: application/xml;charset=utf-8");
    final List<String> atom = Arrays.asList("DataServiceVersion: 2.0;", "Content-Type: application/atom+xml;charset=utf-8");

    if (path.endsWith("$metadata")) {
      return createStringResponse(STATUS_LINE_200, xml, loadMockODataFiles("feed/mock/feed.metadata.xml"));
    }

    if (path.contains("nuget/Packages()") && path.contains("?$filter=")) {
      List<String> entities = new ArrayList<>();
      if (path.contains("finecollection")) {
        entities.add("feed/mock/feed.package.xml");
      }
      if (path.contains("nunit")) {
        entities.add("feed/mock/feed.nunit.package.xml");
        if (!path.contains("IsLatestVersion")) {
          entities.add("feed/mock/feed.nunit.2.5.9.package.xml");
        }
      }
      if (path.contains("youtracksharp")) {
        entities.add("feed/mock/feed.youtracksharp.package.xml");
      }
      if (path.contains("easyhttp")) {
        entities.add("feed/mock/feed.easyhttp.package.xml");
      }
      if (path.contains("elmah")) {
        entities.add("feed/mock/feed.elmah.package.xml");
      }
      if (path.contains("jquery")) {
        entities.add("feed/mock/feed.jquery.package.xml");
      }
      if (!entities.isEmpty()) {
        final Document baseDocument = loadDocument(entities.get(0));
        final Element baseElement = baseDocument.getRootElement();
        final XPath xpath = XPath.newInstance("*[name()='feed']/*[name()='entry']");
        for (int index = 1; index < entities.size(); index++) {
          final Document document = loadDocument(entities.get(index));
          baseElement.addContent(((Element)xpath.selectSingleNode(document)).detach());
        }
        final String resultDocumentStr = XmlUtil.toString(baseDocument);
        return createStringResponse(STATUS_LINE_200, atom, prepareOutput(resultDocumentStr));
      }
    }

    if (path.contains("nuget/Packages")) {
      return createStringResponse(STATUS_LINE_200, atom, loadMockODataFiles("feed/mock/feed.packages.xml"));
    }

    if (path.contains("nuget")) {
      return createStringResponse(STATUS_LINE_200, xml, loadMockODataFiles("feed/mock/feed.root.xml"));
    }

    if (path.contains("FineCollection.1.0.189.152.nupkg")) {
      return myServerApi.createFileResponse(Paths.getTestDataPath("feed/mock/FineCollection.1.0.189.152.nupkg"), Arrays.asList("Content-Type: application/zip"));
    }

    return createStringResponse(STATUS_LINE_404, Collections.<String>emptyList(), "Not found");
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
      return myServerApi.createFileResponse(Paths.getTestDataPath("feed/mock/FineCollection.1.0.189.152.nupkg"), Arrays.asList("Content-Type: application/zip"));
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

  @NotNull
  private Document loadDocument(@NotNull String name) throws IOException, JDOMException {
    File file = Paths.getTestDataPath(name);
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
}
