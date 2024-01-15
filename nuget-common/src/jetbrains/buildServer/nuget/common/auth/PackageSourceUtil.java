

package jetbrains.buildServer.nuget.common.auth;

import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Collection;

/**
 * Created 04.01.13 19:33
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com), evgeniy.koshkin (evgeniy.koshkin@jetbrains.com)
 */
public class PackageSourceUtil {
  public static void writeSources(@NotNull final File file,
                           @NotNull final Collection<PackageSource> sources) throws IOException {
    final Element root = new Element("sources");
    for (PackageSource source : sources) {
      final Element sourceElement = new Element("source");
      sourceElement.setAttribute("source", source.getSource());

      final String username = source.getUsername();
      if (username != null) sourceElement.setAttribute("username", username);

      final String password = source.getPassword();
      if (password != null) sourceElement.setAttribute("password", password);

      root.addContent((Content)sourceElement);
    }

    final OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
    try {
      XmlUtil.saveDocument(new Document(root), os);
    } finally {
      FileUtil.close(os);
    }
  }
}
