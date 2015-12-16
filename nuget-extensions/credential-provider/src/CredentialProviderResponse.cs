using System.Runtime.Serialization;
using Newtonsoft.Json;

namespace JetBrains.TeamCity.NuGet.CredentialProvider
{
  [DataContract]
  public class CredentialProviderResponse
  {
    [DataMember]
    public string Username { get; set; }

    [DataMember]
    public string Password { get; set; }

    [DataMember(EmitDefaultValue = false)]
    public string Message { get; set; }

    public override string ToString()
    {
      return JsonConvert.SerializeObject(this);
    }
  }
}