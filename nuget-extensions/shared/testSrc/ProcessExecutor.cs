using System;
using System.Diagnostics;
using System.Text;
using NUnit.Framework;
using System.Linq;

namespace JetBrains.TeamCity.NuGet.Tests
{
  public static class ProcessExecutor
  {
    public static Result ExecuteProcess(string exe, params string[] args)
    {
      var pi = new ProcessStartInfo
               {
                 FileName = exe,
                 Arguments = string.Join(" ", args.Select(x => x.Contains(' ') ? "\"" + x + "\"" : x)),
                 RedirectStandardError = true,
                 RedirectStandardOutput = true,
                 RedirectStandardInput = true,
                 UseShellExecute = false,
                 CreateNoWindow = true,
               };

      Console.Out.WriteLine("Starting: " + pi.FileName + " " + pi.Arguments);

      var process = new Process {StartInfo = pi};

      var errorDataBuilder = new StringBuilder();
      process.ErrorDataReceived += delegate(object sender, DataReceivedEventArgs e)
                                   {
                                     lock (errorDataBuilder)
                                     {
                                       errorDataBuilder.AppendLine(e.Data);
                                     }
                                   };

      var outputDataBuilder = new StringBuilder();
      process.OutputDataReceived += delegate(object sender, DataReceivedEventArgs e)
                                    {
                                      lock (outputDataBuilder)
                                      {
                                        outputDataBuilder.AppendLine(e.Data);
                                      }
                                    };

      process.Start();
      process.BeginOutputReadLine();
      process.BeginErrorReadLine();

      var exitCode = 1;
      if (process.WaitForExit(60000))
      {
        exitCode = process.ExitCode;
      }

      return new Result(outputDataBuilder.ToString(), errorDataBuilder.ToString(), exitCode);
    }

    public class Result
    {
      public string Output { get; private set; }
      public string Error { get; private set; }
      public int ExitCode { get; private set; }

      public Result(string output, string error, int exitCode)
      {
        Output = output;
        Error = error;
        ExitCode = exitCode;
      }

      public Result AssertOutputContains(params string[] text)
      {
        foreach (var _ in text)
        {
          var s = _.Trim();
          Assert.IsTrue(Output.Contains(s), "Process Output must contain {0}. Output: {1}", s, Output);
        }
        return this;
      }

      public Result AssertErrorContains(params string[] text)
      {
        foreach (var _ in text)
        {
          var s = _.Trim();
          Assert.IsTrue(Error.Contains(s), "Process Output must contain {0}. Output: {1}", s, Output);
        }
        return this;
      }

      public Result AssertNoErrorOutput()
      {
        Assert.IsTrue(string.IsNullOrWhiteSpace(Error), String.Format("Error recieved - {0}", Error));
        return this;
      }

      public Result AssertExitedSuccessfully()
      {
        Assert.That(ExitCode, Is.EqualTo(0));
        return this;
      }

      public Result AssertExitCode(int expectedExitCode)
      {
        Assert.That(ExitCode, Is.EqualTo(expectedExitCode));
        return this;
      }

      public Result Dump()
      {
        Console.Out.WriteLine(this);
        return this;
      }

      public override string ToString()
      {
        var sb = new StringBuilder();
        sb.AppendFormat("ExitCode: {0}\r\n", ExitCode);
        if (!string.IsNullOrWhiteSpace(Output))
        {
          sb
            .Append("Output:\n")
            .Append(Output)
            .Append("\n");
        }
        if (!string.IsNullOrWhiteSpace(Error))
        {
          sb
            .Append("Error:\n")
            .Append(Error);
        }
        return sb.ToString();
      }
    }
  }
}
