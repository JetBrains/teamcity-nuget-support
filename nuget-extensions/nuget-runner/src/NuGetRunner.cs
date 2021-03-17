using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Linq;
using System.Security;

namespace JetBrains.TeamCity.NuGetRunner
{
  public class NuGetRunner : IDisposable
  {
    private const string NuGetName = "NuGet";

    private static readonly byte[] ExpectedPublicKeyToken = { 0x31, 0xbf, 0x38, 0x56, 0xad, 0x36, 0x4e, 0x35 };
    
    private readonly string myNuGetExe;
    private readonly bool myForceVerification;
    private readonly Assembly myNuGetAssembly;
    private readonly List<EventHandler> myStartEvents = new List<EventHandler>();
    private readonly List<EventHandler> myFinishEvents = new List<EventHandler>();
    private readonly Dictionary<string, string> myEnv = new Dictionary<string, string>();
    private readonly List<string> myCombinableVariables = new List<string>
                                                          {
                                                            "NUGET_EXTENSIONS_PATH",
                                                            "NUGET_CREDENTIALPROVIDERS_PATH"
                                                          };

    private readonly IDisposable myAssemblyLocker;

    public NuGetRunner(string NuGetExe, bool forceVerification)
    {
      myNuGetExe = NuGetExe;
      myForceVerification = forceVerification;
      if (!File.Exists(NuGetExe))
        throw new NuGetLoadException("Failed to find NuGet.exe at " + myNuGetExe);

      myAssemblyLocker = AssemblyHelper.LockAssembly(myNuGetExe);

      try
      {
        myNuGetAssembly = Assembly.LoadFrom(myNuGetExe);
      }
      catch (Exception e)
      {
        myAssemblyLocker.Dispose();
        throw new NuGetLoadException("Failed to load NuGet assembly into AppDomain. " + e.Message, e);
      }
    }

    public Version NuGetVersion
    {
      get { return myNuGetAssembly.GetName().Version; }
    }

    public Assembly NuGetAssembly
    {
      get { return myNuGetAssembly; }
    }

    public void AddEnvironmentVariable(string key, string value)
    {
      myEnv.Add(key, value);
    }

    public event EventHandler BeforeNuGetStarted
    {
      add { myStartEvents.Add(value); }
      remove { myStartEvents.Remove(value); }
    }

    public event EventHandler AfterNuGetFinished
    {
      add { myFinishEvents.Add(value); }
      remove { myFinishEvents.Remove(value); }
    }

    private void CallEvents(IEnumerable<EventHandler> handler)
    {
      foreach (var h in handler)
      {
        try
        {
          h(this, EventArgs.Empty);
        }
        catch (Exception e)
        {
          Console.Error.WriteLine("Failed to execute event: " + e);
        }
      }
    }

    public int Run(IEnumerable<string> argz)
    {
      CallEvents(myStartEvents);

      try
      {
        if (myForceVerification)
        {
          VerifyNuGetAssembly();
          AssemblyStrongNameHelper.VerifyStrongName(myNuGetExe);
        }

        var process = new Process();
        var pi = process.StartInfo;
        pi.FileName = myNuGetExe;
        pi.Arguments = CommandLineHelper.Join(argz);
        pi.UseShellExecute = false;
        pi.RedirectStandardInput = true;
        pi.RedirectStandardError = true;
        pi.RedirectStandardOutput = true;
        pi.CreateNoWindow = true;

        foreach (var e in myEnv)
        {
          if (!pi.EnvironmentVariables.ContainsKey(e.Key))
          {
            pi.EnvironmentVariables.Add(e.Key, e.Value);
          }
          else if (myCombinableVariables.Contains(e.Key))
          {
            pi.EnvironmentVariables[e.Key] += string.Format(";{0}", e.Value);
          }
        }

        process.OutputDataReceived += (sender, args) => Console.Out.WriteLine(args.Data);
        process.ErrorDataReceived += (sender, args) => Console.Error.WriteLine(args.Data);

        process.Start();

        process.BeginOutputReadLine();
        process.BeginErrorReadLine();
        process.StandardInput.Close();

        process.WaitForExit();

        return process.ExitCode;
      }
      finally
      {
        CallEvents(Enumerable.Reverse(myFinishEvents));
      }
    }

    private void VerifyNuGetAssembly()
    {
      if (NuGetName != myNuGetAssembly.GetName().Name)
      {
        throw new VerificationException($"Unexpected NuGet assembly: {myNuGetAssembly.GetName().FullName}");
      }
      var publicKeyToken = myNuGetAssembly.GetName().GetPublicKeyToken();
      if (!ExpectedPublicKeyToken.SequenceEqual(publicKeyToken))
      {
        throw new VerificationException($"Unexpected NuGet assembly: {myNuGetAssembly.GetName().FullName}");
      }
    }

    public void Dispose()
    {
      myAssemblyLocker.Dispose();
    }
  }
}
