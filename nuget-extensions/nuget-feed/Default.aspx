<%@ Page Language="C#" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>NuGet Private Repository</title>
    <style type="text/css">
        body { font-family: Calibri; }
    </style>
</head>
<body>
    <div>
        <h2>You are running TeamCity NuGet.Server v<%= typeof(JetBrains.TeamCity.NuGet.Feed.DataServices.NuGetRoutes).Assembly.GetName().Version%></h2>
        <p>
            Click <a href="<%= VirtualPathUtility.ToAbsolute("~/nuget/Packages") %>">here</a> to view your packages.
        </p>
    </div>
</body>
</html>
