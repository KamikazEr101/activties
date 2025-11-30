# 配置请求头和Body
$headers = @{
    "Authorization" = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhZG1pbklkIjoxLCJyb2xlVHlwZSI6MiwidXNlcm5hbWUiOiJhZG1pbiIsInN1YiI6ImFkbWluIiwiaWF0IjoxNzYzMjg1ODYyLCJleHAiOjQ5MTY4ODU4NjJ9.dvl0zYF5NGIRP0Xqe-lnqeM3F5yJU1d8Wio_-whhIic"
    "Content-Type"  = "application/json"
}

$body = @{
    activityName        = "Test Activity"
    activityDescription = "Test Description"
    location            = "Test Location"
    activityTypeName    = "Test Type"
    startTime           = "2024-12-01 14:00"
    stylePrompt         = "modern style"
} | ConvertTo-Json

Write-Host "正在请求AI生成海报..." -ForegroundColor Cyan

try {
    # 发送请求
    $response = Invoke-RestMethod -Uri "http://localhost:8080/file/ai/generate-poster" -Method POST -Headers $headers -Body $body
    
    if ($response.code -eq 200) {
        # 获取Base64数据
        $base64Data = $response.data.imageBase64
        
        # 移除可能存在的前缀 (data:image/png;base64,)
        if ($base64Data -match "data:image/(?<type>.+?);base64,(?<data>.+)") {
            $base64Data = $Matches['data']
        }
        
        # 解码并保存
        $bytes = [Convert]::FromBase64String($base64Data)
        $fileName = "generated_poster.png"
        [IO.File]::WriteAllBytes($fileName, $bytes)
        
        Write-Host "✅ 图片已成功保存到: $PWD\$fileName" -ForegroundColor Green
    }
    else {
        Write-Host "❌ 请求失败: $($response.message)" -ForegroundColor Red
    }
}
catch {
    Write-Host "❌ 发生错误: $($_.Exception.Message)" -ForegroundColor Red
}
