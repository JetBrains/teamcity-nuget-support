using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Linq;
using System.Threading;

namespace JetBrains.TeamCity.NuGetRunner
{
  public class NuGetRunner
  {
    private readonly string myNuGetExe;
    private readonly Assembly myNuGetAssembly;
    private readonly List<EventHandler> myStartEvents = new List<EventHandler>();
    private readonly List<EventHandler> myFinishEvents = new List<EventHandler>();
    private readonly Dictionary<string, string> myEnv = new Dictionary<string, string>();

    public NuGetRunner(string NuGetExe)
    {
      myNuGetExe = NuGetExe;
      if (!File.Exists(NuGetExe))
        throw new NuGetLoadException("Failed to find NuGet.exe at " + myNuGetExe);

      try
      {
        myNuGetAssembly = Assembly.LoadFrom(myNuGetExe);
      }
      catch (Exception e)
      {
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

    public event EventHandler BeforeNuGetStarted { add { myStartEvents.Add(value); } remove { myStartEvents.Remove(value);  } }
    public event EventHandler AfterNuGetFinished { add { myFinishEvents.Add(value); } remove { myFinishEvents.Remove(value); } }

    private void CallEvents(IEnumerable<EventHandler> handler)
    {
      foreach (var h in handler)
      {
        try
        {
          h(this, EventArgs.Empty);
        } catch (Exception e)
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
        var pi = new ProcessStartInfo
                                 {
                                   FileName = myNuGetExe,
                                   //TODO use escapring safe escaping here.
                                   Arguments = CommandLineHelper.Join(argz),
                                   UseShellExecute = false,
                                   RedirectStandardInput = true, 
                                   RedirectStandardError = true, 
                                   RedirectStandardOutput = true, 
                                   CreateNoWindow = true,                                        
                                 };
        foreach (var e in myEnv)
        {          
          pi.EnvironmentVariables.Add(e.Key, e.Value);
        }
        var process = Process.Start(pi);
        

        process.StandardInput.Close();
        Func<StreamReader, TextWriter, Thread> readOutput = (si, so) =>
        {
          var th = new Thread(delegate()
          {
            int i;
            while ((i = si.Read()) >= 0) so.Write((char)i);            
          }) { Name = "Process output reader " + process.Id };
          th.Start();
          return th;
        };

        var t1 = readOutput(process.StandardOutput, Console.Out);
        var t2 = readOutput(process.StandardError, Console.Error);
        
        process.WaitForExit();

        t1.Join(TimeSpan.FromMinutes(5));
        t2.Join(TimeSpan.FromMinutes(5));

        return process.ExitCode;
        /*AppDomain dom = AppDomain.CreateDomain("NuGet Launcher Domain");
        var result = dom.ExecuteAssembly(myNuGetExe, argz);
        return result is int ? (int) result : 0;*/
      }
      finally
      {
        CallEvents(Enumerable.Reverse(myFinishEvents));
      }
    }
  }
}