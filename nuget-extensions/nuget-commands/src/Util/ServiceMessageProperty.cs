namespace JetBrains.TeamCity.NuGet.ExtendedCommands.Util
{
  public struct ServiceMessageProperty
  {
    public readonly string Key;
    public readonly string Value;

    public ServiceMessageProperty(string key, string value)
    {
      Key = key;
      Value = value;
    }
  }
}