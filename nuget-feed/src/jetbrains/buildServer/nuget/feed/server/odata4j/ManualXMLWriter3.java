

package jetbrains.buildServer.nuget.feed.server.odata4j;

import org.odata4j.stax2.QName2;
import org.odata4j.stax2.XMLWriter2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

/**
 * Created 22.04.13 17:31
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 * a copy of {@link org.odata4j.stax2.domimpl.ManualXMLWriter2}
 */
public class ManualXMLWriter3 implements XMLWriter2 {

  private final Writer writer;
  private boolean isStartElementOpen;
  private final Stack<QName2> elements = new Stack<QName2>();

  public ManualXMLWriter3(Writer writer) {
    this.writer = new BufferedWriter(writer, 512 * 1024);
  }

  public void endDocument() {

    while (!elements.isEmpty())
      endElement(elements.peek().getLocalPart());

    try {
      writer.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void endElement(String localName) {
    final QName2 startElementName = elements.pop();
    if (!startElementName.getLocalPart().equals(localName))
      throw new IllegalArgumentException();

    try {
      if (isStartElementOpen) {
        write("/");
        write(">");
        isStartElementOpen = false;
        return;
      }

      write("</");
      if (startElementName.getPrefix() != null) {
        write(startElementName.getPrefix());
        write(":");
      }
      write(localName);
      write(">");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void startDocument() {
    try {
      write("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void startElement(String name) {
    startElement(new QName2(name));
  }

  public void startElement(QName2 qname) {
    startElement(qname, null);
  }

  public void startElement(QName2 qname, String xmlns) {
    try {
      ensureStartElementClosed();
      write("<");
      if (qname.getPrefix() != null) {
        write(qname.getPrefix());
        write(":");
      }
      write(qname.getLocalPart());

      if (xmlns != null) {
        write(" xmlns=\"" + xmlns + "\"");
      }
      isStartElementOpen = true;
      elements.push(qname);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void writeAttribute(String localName, String value) {
    writeAttribute(new QName2(localName), value);
  }

  public void writeAttribute(QName2 qname, String value) {
    if (!isStartElementOpen)
      throw new IllegalStateException();

    try {
      write(" ");
      if (qname.getPrefix() != null) {
        write(qname.getPrefix());
        write(":");
      }
      write(qname.getLocalPart());
      write("=\"");
      writeEncodeAttributeValue(value);
      write("\"");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void writeNamespace(String prefix, String namespaceUri) {
    try {
    if (!isStartElementOpen)
      throw new IllegalStateException();
    write(" xmlns:" + prefix + "=\"" + namespaceUri + "\"");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void writeText(String content) {
    try {
      ensureStartElementClosed();
      writeEncodeElementValue(content);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void ensureStartElementClosed() throws IOException {
    if (isStartElementOpen) {
      write(">");
      isStartElementOpen = false;
    }
  }

  private void write(String value) throws IOException {
    writer.write(value);
  }

  private void write(char value) throws IOException {
    writer.write(value);
  }

  private void writeEncodeElementValue(String value) throws IOException {
    writeEncodeAttributeValue(value); // TODO
  }

  private void writeEncodeAttributeValue(String value) throws IOException {
    if (value == null)
      return;

    final int len = value.length();
    if (len == 0)
      return;

    for(int i = 0;  i < len; i++) {
      final char c = value.charAt(i);
      if (c == '<')
        write("&lt;");
      else if (c == '\"')
        write("&quot;");
      else if (c == '>')
        write("&gt;");
      else if (c == '\'')
        write("&apos;");
      else if (c == '&')
        write("&amp;");
      else
        write(c);
    }
  }

}
