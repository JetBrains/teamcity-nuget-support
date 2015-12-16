using System;
using System.Globalization;
using System.IO;
using PowerArgs;

namespace JetBrains.TeamCity.NuGet.CredentialProvider
{
  public class Program
  {
    public static int Main(string[] args)
    {
      try
      {
        using (var stringWriter = new StringWriter(CultureInfo.CurrentCulture))
        {
          Console.SetOut(stringWriter);
          var credentialProviderArgs = Args.Parse<CredentialProviderArgs>(args);
          if (credentialProviderArgs == null)
          {
            Console.Error.WriteLine(stringWriter.ToString());
            return (int)ExitCode.UnknownError;
          }

          using (var streamWriter = new StreamWriter(Console.OpenStandardOutput()))
          {
            streamWriter.AutoFlush = true;
            Console.SetOut(streamWriter);
            Console.WriteLine(new CredentialProviderResponse{Message = "message", Password = "pwd", Username = "user"}.ToString());
          }
          return (int)ExitCode.Failure;
        }

      }
      catch (Exception ex)
      {
        Console.Error.WriteLine(ex.Message);
        return (int)ExitCode.UnknownError;
      }
    }
  }
}
