using System;
using System.Net;
using JetBrains.TeamCity.NuGet.ExtendedCommands.Data;
using Moq;
using NUnit.Framework;
using NuGet;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands
{
  [TestFixture]
  public class NuGetCredentialsProviderTest
  {
    private void AssertAuth(ICredentialProvider provider, string username = null, string password = null,
                            string url = "http://jetbrains.com")
    {
      var value =
        provider.GetCredentials(new Uri(url), new Mock<IWebProxy>().Object, CredentialType.RequestCredentials, false) as
        NetworkCredential;

      if (username == null)
      {
        Assert.Null(value);
      }
      else
      {
        Assert.NotNull(value);
        Assert.AreEqual(username, value.UserName);
        Assert.AreEqual(password, value.Password);
      }
    }

    [Test]
    public void Single_NoCredentials()
    {

      var source = new NuGetSource
                     {
                       Source = "http://foo"
                     };
      var p = new TeamCitySingleCredentialProvider(source);
      AssertAuth(p);
    }

    [Test]
    public void Single_Credentials()
    {

      var source = new NuGetSource
                     {
                       Source = "http://foo",
                       Username = "aaa",
                       Password = "qqq"
                     };
      var p = new TeamCitySingleCredentialProvider(source);
      AssertAuth(p, username: "aaa", password: "qqq");
    }

    [Test]
    public void Multiple_Empty()
    {
      var p = new TeamCityMultipleCredentialProvider(new INuGetSource[0]);

      AssertAuth(p);
    }

    [Test]
    public void Multiple_Credentials_One()
    {

      var source = new NuGetSource
                     {
                       Source = "http://foo",
                       Username = "aaa",
                       Password = "qqq"
                     };
      var p = new TeamCityMultipleCredentialProvider(new INuGetSource[] {source});
      AssertAuth(p, username: "aaa", password: "qqq", url: "http://foo");
    }

    [Test]
    public void Multiple_Credentials_Multiple()
    {

      var sources = new INuGetSource[]
                      {
                        new NuGetSource
                          {
                            Source = "http://foo",
                            Username = "aaa",
                            Password = "qqq"
                          },

                        new NuGetSource
                          {
                            Source = "https://another-feed",
                            Username = "zzz",
                            Password = "ttt"
                          },
                      };

      var p = new TeamCityMultipleCredentialProvider(sources);

      AssertAuth(p, username: "aaa", password: "qqq", url: "http://foo");
      AssertAuth(p, username: "aaa", password: "qqq", url: "http://foo/");
      AssertAuth(p, username: "zzz", password: "ttt", url: "https://another-feed");

      AssertAuth(p, username: null, url: "https://the-other");
    }
  }
}
