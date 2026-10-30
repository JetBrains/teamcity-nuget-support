// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

using Newtonsoft.Json;
using System;

namespace JetBrains.TeamCity.NuGet.Compatibility.Versioning
{
  public sealed class SemanticVersion : IComparable<SemanticVersion>, IComparable
  {
    public SemanticVersion(int major, int minor, int patch)
      : this(major, minor, patch, null, null)
    {
    }

    [JsonConstructor]
    public SemanticVersion(int major, int minor, int patch, string release, string metadata)
    {
      Major = major;
      Minor = minor;
      Patch = patch;
      Release = release;
      Metadata = metadata;
    }

    public int Major { get; }
    public int Minor { get; }
    public int Patch { get; }
    public string Release { get; }
    public string Metadata { get; }

    public static SemanticVersion Parse(string value)
    {
      SemanticVersion version;
      if (!TryParse(value, out version))
      {
        throw new ArgumentException("Invalid semantic version.", nameof(value));
      }

      return version;
    }

    public static bool TryParse(string value, out SemanticVersion version)
    {
      version = null;
      if (string.IsNullOrEmpty(value))
      {
        return false;
      }

      var metadataSplit = value.Split(new[] {'+'}, 2);
      var releaseSplit = metadataSplit[0].Split(new[] {'-'}, 2);
      var parts = releaseSplit[0].Split('.');

      int major;
      int minor;
      int patch;
      if (parts.Length < 3 ||
          !int.TryParse(parts[0], out major) ||
          !int.TryParse(parts[1], out minor) ||
          !int.TryParse(parts[2], out patch))
      {
        return false;
      }

      version = new SemanticVersion(
        major,
        minor,
        patch,
        releaseSplit.Length > 1 ? releaseSplit[1] : null,
        metadataSplit.Length > 1 ? metadataSplit[1] : null);
      return true;
    }

    public int CompareTo(object obj)
    {
      return CompareTo(obj as SemanticVersion);
    }

    public int CompareTo(SemanticVersion other)
    {
      if (ReferenceEquals(other, null))
      {
        return 1;
      }

      var result = Major.CompareTo(other.Major);
      if (result != 0) return result;
      result = Minor.CompareTo(other.Minor);
      if (result != 0) return result;
      result = Patch.CompareTo(other.Patch);
      if (result != 0) return result;

      var hasRelease = !string.IsNullOrEmpty(Release);
      var otherHasRelease = !string.IsNullOrEmpty(other.Release);
      if (hasRelease == otherHasRelease)
      {
        return string.Compare(Release, other.Release, StringComparison.OrdinalIgnoreCase);
      }

      return hasRelease ? -1 : 1;
    }

    public override string ToString()
    {
      return Major + "." + Minor + "." + Patch + (string.IsNullOrEmpty(Release) ? string.Empty : "-" + Release);
    }

    public static bool operator <(SemanticVersion left, SemanticVersion right)
    {
      return Compare(left, right) < 0;
    }

    public static bool operator >(SemanticVersion left, SemanticVersion right)
    {
      return Compare(left, right) > 0;
    }

    public static bool operator <=(SemanticVersion left, SemanticVersion right)
    {
      return Compare(left, right) <= 0;
    }

    public static bool operator >=(SemanticVersion left, SemanticVersion right)
    {
      return Compare(left, right) >= 0;
    }

    private static int Compare(SemanticVersion left, SemanticVersion right)
    {
      return ReferenceEquals(left, null)
        ? ReferenceEquals(right, null) ? 0 : -1
        : left.CompareTo(right);
    }
  }
}
