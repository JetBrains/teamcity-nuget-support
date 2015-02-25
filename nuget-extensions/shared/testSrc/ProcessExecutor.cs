using System;
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Threading;
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
                   Arguments = string.Join(" ", args.Select(x=>x.Contains(' ') ? "\"" + x + "\"" : x)),
                   RedirectStandardError = true,
                   RedirectStandardOutput = true,
                   RedirectStandardInput = true,
                   UseShellExecute = false,
                   CreateNoWindow = true,
                 };

      Console.Out.WriteLine("Starting: " + pi.FileName + " " + pi.Arguments);

      var process = Process.Start(pi);
      process.StandardInput.Close();
      Func<StreamReader, string> readOutput = stream =>
                                                {
                                                  var result = "";
                                                  var th = new Thread(delegate()
                                                                        {
                                                                          var sb = new StringBuilder();
                                                                          int i;
                                                                          while ((i = stream.Read()) >= 0)
                                                                            sb.Append((char) i);
                                                                          result = sb.ToString();
                                                                        })
                                                             {Name = "Process output reader " + process.Id};
                                                  th.Start();
                                                  th.Join(TimeSpan.FromMinutes(10));
                                                  return result;
                                                };

      string output = readOutput(process.StandardOutput);
      string error = readOutput(process.StandardError);
      process.WaitForExit();

      return new Result(output, error, process.ExitCode);
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