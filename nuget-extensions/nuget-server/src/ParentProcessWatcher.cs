using System;
using System.ComponentModel;
using System.Diagnostics;
using System.Runtime.InteropServices;
using System.Threading;

namespace JetBrains.TeamCity.NuGet.Server
{
  public class ParentProcessWatcher
  {
    public ParentProcessWatcher(Action onParentProcessGone)
    {
      Process parentProcess = ParentProcessUtilities.GetParentProcess();
      if (parentProcess == null)
      {
        Console.Out.WriteLine("Parent process not found.");
        return;
      }

      Console.Out.WriteLine("Parent process id={0}, process id = {1}", parentProcess.Id, Process.GetCurrentProcess().Id);

      var processWatchThead = new Thread(() =>
                                           {
                                             while (true)
                                             {
                                               bool exited;
                                               try
                                               {
                                                 exited = parentProcess.HasExited;
                                               }
                                               catch
                                               {
                                                 exited = false;
                                                 //NOP
                                               }
                                               if (exited)
                                               {
                                                 Console.Out.WriteLine("Parent process has exited. Exiting.");
                                                 onParentProcessGone();
                                                 return;
                                               }
                                               Thread.Sleep(TimeSpan.FromMilliseconds(500));
                                             }
                                           });

      processWatchThead.IsBackground = true;
      processWatchThead.Name = "Parent process watcher";
      processWatchThead.Start();
    }

    /// <summary>
    /// A utility class to determine a process parent.
    /// </summary>
    [StructLayout(LayoutKind.Sequential)]
    public struct ParentProcessUtilities
    {
      // These members must match PROCESS_BASIC_INFORMATION
      internal IntPtr Reserved1;
      internal IntPtr PebBaseAddress;
      internal IntPtr Reserved2_0;
      internal IntPtr Reserved2_1;
      internal IntPtr UniqueProcessId;
      internal IntPtr InheritedFromUniqueProcessId;

      [DllImport("ntdll.dll")]
      private static extern int NtQueryInformationProcess(IntPtr processHandle, int processInformationClass, ref ParentProcessUtilities processInformation, int processInformationLength, out int returnLength);

      /// <summary>
      /// Gets the parent process of the current process.
      /// </summary>
      /// <returns>An instance of the Process class.</returns>
      public static Process GetParentProcess()
      {
        try
        {
          return GetParentProcess(Process.GetCurrentProcess().Handle);
        } catch
        {
          return null;
        }
      }

      /// <summary>
      /// Gets the parent process of a specified process.
      /// </summary>
      /// <param name="handle">The process handle.</param>
      /// <returns>An instance of the Process class.</returns>
      public static Process GetParentProcess(IntPtr handle)
      {
        var pbi = new ParentProcessUtilities();
        int returnLength;
        int status = NtQueryInformationProcess(handle, 0, ref pbi, Marshal.SizeOf(pbi), out returnLength);
        if (status != 0)
          throw new Win32Exception(status);

        try
        {
          return Process.GetProcessById(pbi.InheritedFromUniqueProcessId.ToInt32());
        }
        catch
        {
          // not found
          return null;
        }
      }
    }
  }
}
