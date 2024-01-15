

package jetbrains.buildServer.nuget.server.trigger;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.nuget.server.util.SystemInfo;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 09.11.11 21:22
 */
public class NuGetTriggerController extends BaseController {
  @NotNull
  private final PluginDescriptor myDescriptor;
  private final String myPath;
  @NotNull
  private final SystemInfo mySystemInfo;

  public NuGetTriggerController(@NotNull final PluginDescriptor descriptor,
                                @NotNull final WebControllerManager web,
                                @NotNull final SystemInfo info) {
    mySystemInfo = info;
    myPath = descriptor.getPluginResourcesPath("trigger/editSimpleTrigger.html");
    myDescriptor = descriptor;
    web.registerController(myPath, this);
  }

  @NotNull
  public String getPath() {
    return myPath;
  }

  @Override
  protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
    ModelAndView mv = new ModelAndView(myDescriptor.getPluginResourcesPath("trigger/editSimpleTrigger.jsp"));
    mv.getModel().put("canStartNuGetProcesses", mySystemInfo.canStartNuGetProcesses());
    mv.getModel().put("canStartNuGetProcessesMessage", mySystemInfo.getNotAvailableMessage());
    return mv;
  }
}
