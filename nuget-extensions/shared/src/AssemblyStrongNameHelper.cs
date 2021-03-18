using System;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using System.Security;

namespace JetBrains.TeamCity.NuGetRunner
{
  public static class AssemblyStrongNameHelper
  {
    public static void VerifyStrongName(string path)
    {
      if (Environment.OSVersion.Platform != PlatformID.Win32NT) return;

      var strongName = GetStrongNameService();
      var wasVerified = strongName.StrongNameSignatureVerificationEx(path, true);
      if (!wasVerified)
      {
        throw new VerificationException($"Incorrect strong name. Failed to verify assembly: {path}.");
      }
    }

    private static ICLRStrongName GetStrongNameService()
    {
      return RuntimeEnvironment.GetRuntimeInterfaceAsObject(
        new Guid("B79B0ACD-F5CD-409b-B5A5-A16244610B92"),
        new Guid("9FD93CCF-3280-4391-B3A9-96E1CDE77C8D")) as ICLRStrongName;
    }

    [InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
    [ComConversionLoss]
    [Guid("9FD93CCF-3280-4391-B3A9-96E1CDE77C8D")]
    [ComImport]
    internal interface ICLRStrongName
    {
      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void GetHashFromAssemblyFile(
        [MarshalAs(UnmanagedType.LPStr), In] string pszFilePath,
        [In, Out] ref uint piHashAlg,
        out byte pbHash,
        [In] uint cchHash,
        out uint pchHash);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void GetHashFromAssemblyFileW(
        [MarshalAs(UnmanagedType.LPWStr), In] string pwzFilePath,
        [In, Out] ref uint piHashAlg,
        out byte pbHash,
        [In] uint cchHash,
        out uint pchHash);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void GetHashFromBlob(
        [In] ref byte pbBlob,
        [In] uint cchBlob,
        [In, Out] ref uint piHashAlg,
        out byte pbHash,
        [In] uint cchHash,
        out uint pchHash);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void GetHashFromFile(
        [MarshalAs(UnmanagedType.LPStr), In] string pszFilePath,
        [In, Out] ref uint piHashAlg,
        out byte pbHash,
        [In] uint cchHash,
        out uint pchHash);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void GetHashFromFileW(
        [MarshalAs(UnmanagedType.LPWStr), In] string pwzFilePath,
        [In, Out] ref uint piHashAlg,
        out byte pbHash,
        [In] uint cchHash,
        out uint pchHash);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void GetHashFromHandle(
        [In] IntPtr hFile,
        [In, Out] ref uint piHashAlg,
        out byte pbHash,
        [In] uint cchHash,
        out uint pchHash);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      uint StrongNameCompareAssemblies([MarshalAs(UnmanagedType.LPWStr), In] string pwzAssembly1, [MarshalAs(UnmanagedType.LPWStr), In] string pwzAssembly2);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void StrongNameFreeBuffer([In] ref byte pbMemory);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void StrongNameGetBlob([MarshalAs(UnmanagedType.LPWStr), In] string pwzFilePath, [In, Out] ref byte pbBlob, [In, Out] ref uint pcbBlob);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void StrongNameGetBlobFromImage(
        [In] ref byte pbBase,
        [In] uint dwLength,
        out byte pbBlob,
        [In, Out] ref uint pcbBlob);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void StrongNameGetPublicKey(
        [MarshalAs(UnmanagedType.LPWStr), In] string pwzKeyContainer,
        [In] ref byte pbKeyBlob,
        [In] uint cbKeyBlob,
        [Out] IntPtr ppbPublicKeyBlob,
        out uint pcbPublicKeyBlob);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      uint StrongNameHashSize([In] uint ulHashAlg);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void StrongNameKeyDelete([MarshalAs(UnmanagedType.LPWStr), In] string pwzKeyContainer);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void StrongNameKeyGen(
        [MarshalAs(UnmanagedType.LPWStr), In] string pwzKeyContainer,
        [In] uint dwFlags,
        [Out] IntPtr ppbKeyBlob,
        out uint pcbKeyBlob);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void StrongNameKeyGenEx(
        [MarshalAs(UnmanagedType.LPWStr), In] string pwzKeyContainer,
        [In] uint dwFlags,
        [In] uint dwKeySize,
        [Out] IntPtr ppbKeyBlob,
        out uint pcbKeyBlob);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void StrongNameKeyInstall([MarshalAs(UnmanagedType.LPWStr), In] string pwzKeyContainer, [In] ref byte pbKeyBlob, [In] uint cbKeyBlob);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void StrongNameSignatureGeneration(
        [MarshalAs(UnmanagedType.LPWStr), In] string pwzFilePath,
        [MarshalAs(UnmanagedType.LPWStr), In] string pwzKeyContainer,
        [In] ref byte pbKeyBlob,
        [In] uint cbKeyBlob,
        [Out] IntPtr ppbSignatureBlob,
        out uint pcbSignatureBlob);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void StrongNameSignatureGenerationEx(
        [MarshalAs(UnmanagedType.LPWStr), In] string wszFilePath,
        [MarshalAs(UnmanagedType.LPWStr), In] string wszKeyContainer,
        [In] ref byte pbKeyBlob,
        [In] uint cbKeyBlob,
        [Out] IntPtr ppbSignatureBlob,
        out uint pcbSignatureBlob,
        [In] uint dwFlags);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void StrongNameSignatureSize([In] ref byte pbPublicKeyBlob, [In] uint cbPublicKeyBlob, [In] ref uint pcbSize);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      uint StrongNameSignatureVerification([MarshalAs(UnmanagedType.LPWStr), In] string pwzFilePath, [In] uint dwInFlags);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      [return: MarshalAs(UnmanagedType.I1)]
      bool StrongNameSignatureVerificationEx([MarshalAs(UnmanagedType.LPWStr), In] string pwzFilePath, [MarshalAs(UnmanagedType.I1), In] bool fForceVerification);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      uint StrongNameSignatureVerificationFromImage([In] ref byte pbBase, [In] uint dwLength, [In] uint dwInFlags);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void StrongNameTokenFromAssembly(
        [MarshalAs(UnmanagedType.LPWStr), In] string pwzFilePath,
        [Out] IntPtr ppbStrongNameToken,
        out uint pcbStrongNameToken);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void StrongNameTokenFromAssemblyEx(
        [MarshalAs(UnmanagedType.LPWStr), In] string pwzFilePath,
        [Out] IntPtr ppbStrongNameToken,
        out uint pcbStrongNameToken,
        [Out] IntPtr ppbPublicKeyBlob,
        out uint pcbPublicKeyBlob);

      [MethodImpl(MethodImplOptions.InternalCall, MethodCodeType = MethodCodeType.Runtime)]
      void StrongNameTokenFromPublicKey(
        [In] ref byte pbPublicKeyBlob,
        [In] uint cbPublicKeyBlob,
        [Out] IntPtr ppbStrongNameToken,
        out uint pcbStrongNameToken);
    }
  }

}
