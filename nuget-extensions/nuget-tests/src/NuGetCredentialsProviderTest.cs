using System;
using System.IO;
using JetBrains.TeamCity.NuGet.CredentialProvider;
using Newtonsoft.Json;
using NUnit.Framework;

namespace JetBrains.TeamCity.NuGet.Tests
{
  [TestFixture]
  public class NuGetCredentialsProviderTest
  {
    private static readonly string TEAMCITY_NUGET_FEEDS = "TEAMCITY_NUGET_FEEDS";

    [Test]
    public void TestEmptySourcesList()
    {
      TempFilesHolder.WithTempDirectory(
        home =>
        {
          var sourcesFile = Path.Combine(home, "sources.xml");
          File.WriteAllText(sourcesFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                         "<sources>\n" +
                                         "</sources>\n");

          Environment.SetEnvironmentVariable(TEAMCITY_NUGET_FEEDS, sourcesFile);
          var output = new StringWriter();
          var defaultOutput = Console.Out;
          Console.SetOut(output);

          try
          {
            Assert.AreEqual(1, Program.Main(new[] {"-uri", "http://jb.com"}));
          }
          finally
          {
            Environment.SetEnvironmentVariable(TEAMCITY_NUGET_FEEDS, "");
            Console.SetOut(defaultOutput);
          }

          var json = output.GetStringBuilder().ToString();
          var response = JsonConvert.DeserializeObject<CredentialProviderResponse>(json);

          Assert.AreEqual("NuGet feed credentials file contains no credentials for URL http://jb.com/", response.Message);
        });
    }

    [Test]
    public void TestSourceWithTrailingSlash()
    {
      TempFilesHolder.WithTempDirectory(
        home =>
        {
          var sourcesFile = Path.Combine(home, "sources.xml");
          File.WriteAllText(sourcesFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                         "<sources>\n" +
                                         "<source source=\"http://jb.com/\" username=\"name\" password=\"pass\" />\n" +
                                         "</sources>\n");

          Environment.SetEnvironmentVariable(TEAMCITY_NUGET_FEEDS, sourcesFile);
          var output = new StringWriter();
          var defaultOutput = Console.Out;
          Console.SetOut(output);

          try
          {
            Assert.AreEqual(0, Program.Main(new[] {"-uri", "http://jb.com"}));
          }
          finally
          {
            Environment.SetEnvironmentVariable(TEAMCITY_NUGET_FEEDS, "");
            Console.SetOut(defaultOutput);
          }

          var json = output.GetStringBuilder().ToString();
          var response = JsonConvert.DeserializeObject<CredentialProviderResponse>(json);

          Assert.AreEqual(response.Username, "name");
          Assert.AreEqual(response.Password, "pass");
        });
    }
    
    [Test]
    public void TestSourceWithDifferentCase()
    {
      TempFilesHolder.WithTempDirectory(
        home =>
        {
          var sourcesFile = Path.Combine(home, "sources.xml");
          File.WriteAllText(sourcesFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                         "<sources>\n" +
                                         "<source source=\"http://JB.COM/\" username=\"name\" password=\"pass\" />\n" +
                                         "</sources>\n");

          Environment.SetEnvironmentVariable(TEAMCITY_NUGET_FEEDS, sourcesFile);
          var output = new StringWriter();
          var defaultOutput = Console.Out;
          Console.SetOut(output);

          try
          {
            Assert.AreEqual(0, Program.Main(new[] {"-uri", "http://jb.com"}));
          }
          finally
          {
            Environment.SetEnvironmentVariable(TEAMCITY_NUGET_FEEDS, "");
            Console.SetOut(defaultOutput);
          }

          var json = output.GetStringBuilder().ToString();
          var response = JsonConvert.DeserializeObject<CredentialProviderResponse>(json);

          Assert.AreEqual(response.Username, "name");
          Assert.AreEqual(response.Password, "pass");
        });
    }

    [Test]
    public void TestSourceWhichNotMatch()
    {
      TempFilesHolder.WithTempDirectory(
        home =>
        {
          var sourcesFile = Path.Combine(home, "sources.xml");
          File.WriteAllText(sourcesFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                         "<sources>\n" +
                                         "<source source=\"http://jb2.com/\" username=\"name\" password=\"pass\" />\n" +
                                         "</sources>\n");

          Environment.SetEnvironmentVariable(TEAMCITY_NUGET_FEEDS, sourcesFile);
          var output = new StringWriter();
          var defaultOutput = Console.Out;
          Console.SetOut(output);

          try
          {
            Assert.AreEqual(1, Program.Main(new[] {"-uri", "http://jb.com"}));
          }
          finally
          {
            Environment.SetEnvironmentVariable(TEAMCITY_NUGET_FEEDS, "");
            Console.SetOut(defaultOutput);
          }

          var json = output.GetStringBuilder().ToString();
          var response = JsonConvert.DeserializeObject<CredentialProviderResponse>(json);

          Assert.IsTrue(response.Message.StartsWith("NuGet feed credentials file contains no credentials for URL"));
        });
    }

    [Test]
    public void TestWithInvalidFile()
    {
      TempFilesHolder.WithTempDirectory(
        home =>
        {
          var sourcesFile = Path.Combine(home, "sources.xml");
          File.WriteAllText(sourcesFile, "Haha!\n");

          Environment.SetEnvironmentVariable(TEAMCITY_NUGET_FEEDS, sourcesFile);
          var error = new StringWriter();
          var defaultError = Console.Error;
          Console.SetError(error);

          try
          {
            Assert.AreEqual(-1, Program.Main(new[] {"-uri", "http://jb.com"}));
          }
          finally
          {
            Environment.SetEnvironmentVariable(TEAMCITY_NUGET_FEEDS, "");
            Console.SetError(defaultError);
          }

          var message = error.GetStringBuilder().ToString();

          Assert.IsTrue(message.StartsWith("There is an error in XML document"));
        });
    }

    [Test]
    public void TestWithoutFile()
    {
      var sourcesFile = Guid.NewGuid().ToString("N") + ".xml";

      Environment.SetEnvironmentVariable(TEAMCITY_NUGET_FEEDS, sourcesFile);
      var output = new StringWriter();
      var defaultOutput = Console.Out;
      Console.SetOut(output);

      try
      {
        Assert.AreEqual(2, Program.Main(new[] {"-uri", "http://jb.com"}));
      }
      finally
      {
        Environment.SetEnvironmentVariable(TEAMCITY_NUGET_FEEDS, "");
        Console.SetOut(defaultOutput);
      }

      var json = output.GetStringBuilder().ToString();
      var response = JsonConvert.DeserializeObject<CredentialProviderResponse>(json);

      Assert.IsTrue(response.Message.StartsWith("Failed to load NuGet feed credentials file on path"));
    }

    [Test]
    public void TestWithoutEnvVariable()
    {
      var output = new StringWriter();
      var defaultOutput = Console.Out;
      Console.SetOut(output);

      try
      {
        Assert.AreEqual(1, Program.Main(new[] {"-uri", "http://jb.com"}));
      }
      finally
      {
        Console.SetOut(defaultOutput);
      }

      var json = output.GetStringBuilder().ToString();
      var response = JsonConvert.DeserializeObject<CredentialProviderResponse>(json);

      Assert.IsTrue(response.Message.StartsWith("Failed to load NuGet feed credentials file on path"));
    }

    [Test]
    public void TestSourceUrlNormalization()
    {
      TempFilesHolder.WithTempDirectory(
        home =>
        {
          var sourcesFile = Path.Combine(home, "sources.xml");
          File.WriteAllText(sourcesFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                         "<sources>\n" +
                                         "<source source=\"http://jb.com:80\" username=\"name\" password=\"pass\" />\n" +
                                         "</sources>\n");

          Environment.SetEnvironmentVariable(TEAMCITY_NUGET_FEEDS, sourcesFile);
          var output = new StringWriter();
          var defaultOutput = Console.Out;
          Console.SetOut(output);

          try
          {
            Assert.AreEqual(0, Program.Main(new[] { "-uri", "http://jb.com" }));
          }
          finally
          {
            Environment.SetEnvironmentVariable(TEAMCITY_NUGET_FEEDS, "");
            Console.SetOut(defaultOutput);
          }

          var json = output.GetStringBuilder().ToString();
          var response = JsonConvert.DeserializeObject<CredentialProviderResponse>(json);

          Assert.AreEqual(response.Username, "name");
          Assert.AreEqual(response.Password, "pass");
        });
    }

    [Test]
    public void TestRequestUrlNormalization()
    {
      TempFilesHolder.WithTempDirectory(
        home =>
        {
          var sourcesFile = Path.Combine(home, "sources.xml");
          File.WriteAllText(sourcesFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                         "<sources>\n" +
                                         "<source source=\"http://jb.com\" username=\"name\" password=\"pass\" />\n" +
                                         "</sources>\n");

          Environment.SetEnvironmentVariable(TEAMCITY_NUGET_FEEDS, sourcesFile);
          var output = new StringWriter();
          var defaultOutput = Console.Out;
          Console.SetOut(output);

          try
          {
            Assert.AreEqual(0, Program.Main(new[] { "-uri", "http://jb.com:80" }));
          }
          finally
          {
            Environment.SetEnvironmentVariable(TEAMCITY_NUGET_FEEDS, "");
            Console.SetOut(defaultOutput);
          }

          var json = output.GetStringBuilder().ToString();
          var response = JsonConvert.DeserializeObject<CredentialProviderResponse>(json);

          Assert.AreEqual(response.Username, "name");
          Assert.AreEqual(response.Password, "pass");
        });
    }
  }
}
