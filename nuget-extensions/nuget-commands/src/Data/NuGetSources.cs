using System;
using System.Collections.Generic;
using System.Xml.Serialization;

namespace JetBrains.TeamCity.NuGet.ExtendedCommands.Data
{
  [Serializable]
  [XmlRoot("sources")]
  public class NuGetSources : INuGetSources
  {
    [XmlElement("source")]
    public NuGetSource[] Sources { get; set; }

    public INuGetSource FindSource(Uri uri)
    {
      if (Sources == null) return null;
      
      var requestUri = GetNormalizedUri(uri);
      foreach (var source in Sources)
      {
        if (!source.HasCredentials) continue;
        
        Uri result;
        if (!Uri.TryCreate(source.Source, UriKind.Absolute, out result)) continue;
        
        var sourceUri = GetNormalizedUri(result);
        if (!requestUri.StartsWith(sourceUri, StringComparison.OrdinalIgnoreCase)) continue;
        
        return source;
      }

      return null;
    }

    private string GetNormalizedUri(Uri uri)
    {
      var uriBuilder = new UriBuilder(uri);
      if (!uriBuilder.Path.EndsWith("/"))
      {
        uriBuilder.Path += "/";
      }

      return uriBuilder.Uri.AbsoluteUri;
    }

    [XmlIgnore]
    IEnumerable<INuGetSource> INuGetSources.Sources {
      get { return Sources ?? new INuGetSource[0]; }
    }
  }
}
