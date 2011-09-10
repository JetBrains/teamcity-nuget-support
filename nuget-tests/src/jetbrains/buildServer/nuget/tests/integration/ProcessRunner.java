/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.integration;

import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.09.11 20:37
 */
public class ProcessRunner {

  public static ExecResult runProces(@NotNull final GeneralCommandLine cmd) {
    System.out.println("Run: " + cmd.getCommandLineString());
    ExecResult result = SimpleCommandLineProcessRunner.runCommand(cmd, new byte[0]);
    System.out.println("Exit code: " + result.getExitCode());
    System.out.println(result.getStdout());
    System.out.println(result.getStderr());

    return result;
  }
}
