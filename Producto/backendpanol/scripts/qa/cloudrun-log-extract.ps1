param(
  [Parameter(Mandatory = $true)]
  [string]$ProjectId,

  [Parameter(Mandatory = $true)]
  [string]$ServiceName,

  [Parameter(Mandatory = $true)]
  [string]$Region,

  [Parameter(Mandatory = $true)]
  [string]$StartTimeIso,

  [Parameter(Mandatory = $true)]
  [string]$EndTimeIso,

  [string]$ContainsText,

  [string]$OutputFile = ""
)

$filter = @(
  'resource.type="cloud_run_revision"',
  "resource.labels.service_name=\"$ServiceName\"",
  "resource.labels.location=\"$Region\"",
  "timestamp>=\"$StartTimeIso\"",
  "timestamp<=\"$EndTimeIso\""
) -join ' AND '

if ($ContainsText -and $ContainsText.Trim().Length -gt 0) {
  $filter = "$filter AND textPayload:\"$ContainsText\""
}

$cmd = @(
  "logging", "read",
  $filter,
  "--project", $ProjectId,
  "--limit", "2000",
  "--format", "json"
)

if ($OutputFile -and $OutputFile.Trim().Length -gt 0) {
  gcloud @cmd | Set-Content -Path $OutputFile
  Write-Host "Logs exportados a $OutputFile"
} else {
  gcloud @cmd
}
