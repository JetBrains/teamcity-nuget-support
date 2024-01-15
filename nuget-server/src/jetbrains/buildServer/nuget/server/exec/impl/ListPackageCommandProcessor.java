

package jetbrains.buildServer.nuget.server.exec.impl;

import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageParserCallback;
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes;
import jetbrains.buildServer.nuget.server.exec.NuGetOutputProcessorAdapter;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 14.07.11 13:23
*/
public class ListPackageCommandProcessor extends NuGetOutputProcessorAdapter<Collection<SourcePackageInfo>> {
  private final String mySource;
  private final List<SourcePackageInfo> myPackages = new ArrayList<SourcePackageInfo>();

  public ListPackageCommandProcessor(@Nullable final String source, @NotNull final String commandName) {
    super(commandName);
    mySource = source;
  }

  public void onStdOutput(@NotNull String text) {
    super.onStdOutput(text);
    ServiceMessage.parse(text, new ServiceMessageParserCallback() {
      public void regularText(@NotNull String s) {
      }

      public void serviceMessage(@NotNull ServiceMessage serviceMessage) {
        if (!"nuget-package".equals(serviceMessage.getMessageName())) return;
        final String id = serviceMessage.getAttributes().get(NuGetPackageAttributes.ID);
        final String version = serviceMessage.getAttributes().get(NuGetPackageAttributes.VERSION);

        if (StringUtil.isEmptyOrSpaces(id)) return;
        if (StringUtil.isEmptyOrSpaces(version)) return;

        myPackages.add(new SourcePackageInfo(mySource, id, version));
      }

      public void parseException(@NotNull ParseException e, @NotNull String s) {
      }
    });
  }

  @NotNull
  public Collection<SourcePackageInfo> getResult() {
    return Collections.unmodifiableList(myPackages);
  }
}
