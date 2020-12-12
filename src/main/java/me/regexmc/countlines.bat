powershell -command "Get-ChildItem -Filter '*.java' -Recurse | Get-Content | Measure-Object -line -character -word"
pause