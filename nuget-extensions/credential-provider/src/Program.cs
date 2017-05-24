using System;
using System.Globalization;
using System.IO;
using System.Linq;
using JetBrains.TeamCity.NuGet.ExtendedCommands;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
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

          var response = new CredentialProviderResponse();
          ExitCode exitCode;

          var path = Environment.GetEnvironmentVariable("TEAMCITY_NUGET_FEEDS");
          if (string.IsNullOrWhiteSpace(path))
          {
            response.Message = "Failed to load NuGet feed credentials file on path " + path;
            Console.Error.WriteLine("Failed to find TEAMCITY_NUGET_FEEDS var in environment");
            exitCode = ExitCode.ProviderNotApplicable;
          }
          else if (!File.Exists(path))
          {
            response.Message = "Failed to load NuGet feed credentials file on path " + path;
            exitCode = ExitCode.Failure;
          }
          else
          {
            var sources = XmlSerializerHelper.Load<NuGetSources>(path).Sources.Where(x => x.HasCredentials).ToArray();
            if (sources.Length == 0)
            {
              response.Message = "NuGet feed credentials file contains no sources with credentials specified";
              exitCode = ExitCode.ProviderNotApplicable;
            }
            else
            {
              var targetUri = credentialProviderArgs.Uri;
              var targetSource = sources.FirstOrDefault(source => targetUri.AbsoluteUri.Equals(source.Source));
              if (targetSource == null)
              {
                response.Message = "NuGet feed credentials file contains no credentials for URL " + targetUri;
                exitCode = ExitCode.ProviderNotApplicable;
              }
              else
              {
                response.Username = targetSource.Username;
                response.Password = targetSource.Password;
                response.Message = "Success";
                exitCode = ExitCode.Success;
              }
            }
          }

          using (var streamWriter = new StreamWriter(Console.OpenStandardOutput()))
          {
            streamWriter.AutoFlush = true;
            Console.SetOut(streamWriter);
            Console.WriteLine(response.ToString());
          }
          return (int)exitCode;
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
