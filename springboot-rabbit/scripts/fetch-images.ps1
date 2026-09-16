
<#
  抓取与关键词相干的真实照片，裁成前端需要的精确尺寸，下载到 vue-rabbit/public/images/

  图源说明（都是踩过的坑，别再回头改回去）：
    - Bing 主搜索页会把「相关推荐」模块的图混进 murl，搜「儿童鞋」能抓到宗教图案、
      搜「短袖T恤」能抓到景区导览图；换 async 接口 + filterui:photo-photo 后依旧如此，
      这个客户端拿到的就是无关结果。所以除轮播图外不再用 Bing。
    - 搜狗图片接口直接返回 {"status":1,"info":"forbid"}，不可用。
    - 百度图片相关性最好，但 CDN 只给 500px 的 middleURL：w/h 参数被忽略，
      fm=30（原图）返回的是反盗链 HTML，拿不到更大尺寸。
      所以非轮播图的成品尺寸以 500px 为上限，源图不够大就不放大（前端是 CSS 拉伸，尺寸不必严格等于目标）。

  用法：
    powershell -File fetch-images.ps1                      # 全量（已存在且有效的图会跳过）
    powershell -File fetch-images.ps1 -Only banner-1       # 只补某几张
    powershell -File fetch-images.ps1 -Force               # 忽略已存在，全部重抓

  产出：图片文件 + images/image-map.json（供生成 SQL 用）
#>
param(
    [string[]]$Only = @(),
    [switch]  $Force,
    [string]  $OutDir
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

# 默认输出到仓库里的 vue-rabbit/public/images。
# 本脚本位于 <repo>\springboot-rabbit\scripts\，所以往上两级就是仓库根目录。
if (-not $OutDir) {
    $OutDir = Join-Path (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent) "vue-rabbit\public\images"
}

$UA      = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36"
$MapFile = Join-Path $OutDir "image-map.json"

if (-not (Test-Path $OutDir)) { New-Item -ItemType Directory -Path $OutDir -Force | Out-Null }

$script:Session   = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$script:Session.UserAgent = $UA
$script:BaiduReady = $false
$script:BingReady  = $false

# 收费图库站：原图普遍带「摄图网/千图网」水印
# 文档/百科站：搜出来是手抄报、流程图、PPT 截图这类完全不相干的图
# 两类都降级到兜底顺序，优先用干净来源
$script:BadHosts = @(
    "699pic.com", "58pic.com", "nipic.com", "ibaotu.com", "92to.com",
    "tupian114.com", "16pic.com", "qiantucdn.com", "zcool.com.cn",
    "51yuansu.com", "ooopic.com", "duitang.com",
    "baike.baidu.com", "wenku.baidu.com", "zhidao.baidu.com",
    "doc88.com", "docin.com", "book118.com", "renrendoc.com",
    "xueshu.baidu.com", "cnki.net"
)

# ---------------------------------------------------------------------
# 工具：判定文件是有效图片
# ---------------------------------------------------------------------
function Test-ImageFile {
    param([string]$Path)
    if (-not (Test-Path $Path)) { return $false }
    $b = [System.IO.File]::ReadAllBytes($Path)
    if ($b.Length -lt 2048) { return $false }
    $isJpg = ($b[0] -eq 0xFF -and $b[1] -eq 0xD8)
    $isPng = ($b[0] -eq 0x89 -and $b[1] -eq 0x50)
    return ($isJpg -or $isPng)
}

# ---------------------------------------------------------------------
# 工具：居中裁剪到目标宽高比 + 缩放到目标尺寸（源图不够大就不放大）
# ---------------------------------------------------------------------
function Resize-ToTarget {
    param([System.Drawing.Bitmap]$Src, [int]$TargetW, [int]$TargetH, [string]$DstPath)

    $srcRatio = $Src.Width / $Src.Height
    $dstRatio = $TargetW / $TargetH

    if ($srcRatio -gt $dstRatio) {
        # 源图更宽 -> 裁左右
        $cropH = $Src.Height
        $cropW = [int]($cropH * $dstRatio)
    } else {
        # 源图更高 -> 裁上下
        $cropW = $Src.Width
        $cropH = [int]($cropW / $dstRatio)
    }
    $cropX = [int](($Src.Width  - $cropW) / 2)
    $cropY = [int](($Src.Height - $cropH) / 2)

    # 百度只给得到 500px，放大只会糊，所以源图小于目标就按实际尺寸出图
    $outW = $TargetW
    $outH = $TargetH
    if ($cropW -lt $TargetW) {
        $outW = $cropW
        $outH = [int]($cropW / $dstRatio)
    }

    $dst = New-Object System.Drawing.Bitmap($outW, $outH)
    $g   = [System.Drawing.Graphics]::FromImage($dst)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.PixelOffsetMode   = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.SmoothingMode     = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.DrawImage($Src,
        (New-Object System.Drawing.Rectangle(0, 0, $outW, $outH)),
        (New-Object System.Drawing.Rectangle($cropX, $cropY, $cropW, $cropH)),
        [System.Drawing.GraphicsUnit]::Pixel)
    $g.Dispose()

    $codec = [System.Drawing.Imaging.ImageCodecInfo]::GetImageEncoders() |
             Where-Object { $_.MimeType -eq "image/jpeg" }
    $ep = New-Object System.Drawing.Imaging.EncoderParameters(1)
    $ep.Param[0] = New-Object System.Drawing.Imaging.EncoderParameter(
        [System.Drawing.Imaging.Encoder]::Quality, [long]88)
    $dst.Save($DstPath, $codec, $ep)
    $ep.Dispose(); $dst.Dispose()

    return "${outW}x${outH}"
}

# ---------------------------------------------------------------------
# 图源：百度图片（主力）
# ---------------------------------------------------------------------
function Get-BaiduImageIds {
    param([string]$Keyword, [int]$Retry = 3)

    if (-not $script:BaiduReady) {
        try {
            $null = Invoke-WebRequest -Uri "https://image.baidu.com/" -TimeoutSec 25 `
                -UseBasicParsing -WebSession $script:Session
            $script:BaiduReady = $true
        } catch { }
    }

    for ($attempt = 1; $attempt -le $Retry; $attempt++) {
        try {
            $q   = [uri]::EscapeDataString($Keyword)
            $url = "https://image.baidu.com/search/acjson?tn=resultjson_com&ipn=rj&ct=201326592&fp=result" +
                   "&word=$q&queryWord=$q&pn=0&rn=60&gsm=1e&ie=utf-8&oe=utf-8"
            $resp = Invoke-WebRequest -Uri $url -TimeoutSec 25 -UseBasicParsing -WebSession $script:Session `
                -Headers @{ "Referer" = "https://image.baidu.com/search/index?tn=baiduimage&word=$q"
                            "X-Requested-With" = "XMLHttpRequest" }

            # 返回的 JSON 里有 ObjURL/ObjUrl 这类重复键，ConvertFrom-Json 会直接抛
            # DuplicateKeysInJsonString，只能正则抽字段
            $raw = $resp.Content
            $mid = @([regex]::Matches($raw, '"middleURL":"(.*?)"') | ForEach-Object { $_.Groups[1].Value })
            $thu = @([regex]::Matches($raw, '"thumbURL":"(.*?)"')  | ForEach-Object { $_.Groups[1].Value })

            $bs  = [string][char]92
            $esc = $bs + "/"

            $ids  = New-Object System.Collections.ArrayList   # 干净站点，优先
            $bad  = New-Object System.Collections.ArrayList   # 水印/文档站点，仅兜底
            $seen = @{}
            for ($i = 0; $i -lt $mid.Count; $i++) {
                $u = $mid[$i]
                if (-not $u) { $u = $thu[$i] }                # 少数条目 middleURL 为空
                if (-not $u) { continue }
                $u = $u.Replace($esc, "/")
                if ($seen.ContainsKey($u)) { continue }
                $seen[$u] = $true

                $hostName = ""
                try { $hostName = ([uri]$u).Host.ToLower() } catch { continue }
                $isBad = $false
                foreach ($h in $script:BadHosts) {
                    if ($hostName.EndsWith($h)) { $isBad = $true; break }
                }
                if ($isBad) { [void]$bad.Add($u) } else { [void]$ids.Add($u) }
            }
            if ($ids.Count -gt 0) { return , $ids }
            if ($bad.Count -gt 0) { return , $bad }
        } catch { }
        Start-Sleep -Milliseconds (1500 * $attempt)
    }
    return , (New-Object System.Collections.ArrayList)
}

# ---------------------------------------------------------------------
# 图源：Bing（只给轮播图用，这几个词它返回的是真原图，尺寸够 1240 宽）
# ---------------------------------------------------------------------
function Get-BingImageIds {
    param([string]$Keyword, [int]$Retry = 3)

    if (-not $script:BingReady) {
        try {
            $null = Invoke-WebRequest -Uri "https://cn.bing.com/" -TimeoutSec 25 `
                -UseBasicParsing -WebSession $script:Session
            $script:BingReady = $true
        } catch { }
    }

    for ($attempt = 1; $attempt -le $Retry; $attempt++) {
        try {
            $q   = [uri]::EscapeDataString($Keyword)
            $url = "https://cn.bing.com/images/async?q=$q&first=0&count=35&mmasync=1&qft=+filterui:photo-photo"
            $r   = Invoke-WebRequest -Uri $url -TimeoutSec 25 -UseBasicParsing -WebSession $script:Session `
                     -Headers @{ "Referer" = "https://cn.bing.com/images/search?q=$q" }

            $m    = [regex]::Matches($r.Content, '&quot;murl&quot;:&quot;(.*?)&quot;')
            $ids  = New-Object System.Collections.ArrayList
            $bad  = New-Object System.Collections.ArrayList
            $seen = @{}
            foreach ($x in $m) {
                $u = $x.Groups[1].Value
                if (-not $u -or $seen.ContainsKey($u)) { continue }
                $seen[$u] = $true

                $hostName = ""
                try { $hostName = ([uri]$u).Host.ToLower() } catch { continue }
                $isBad = $false
                foreach ($h in $script:BadHosts) {
                    if ($hostName.EndsWith($h)) { $isBad = $true; break }
                }
                if ($isBad) { [void]$bad.Add($u) } else { [void]$ids.Add($u) }
            }
            if ($ids.Count -gt 0) { return , $ids }
            if ($bad.Count -gt 0) { return , $bad }
        } catch { }
        Start-Sleep -Milliseconds (1500 * $attempt)
    }
    return , (New-Object System.Collections.ArrayList)
}

# ---------------------------------------------------------------------
# 下载一张并处理成目标尺寸；不可用就抛异常，交给调用方换下一张候选
# ---------------------------------------------------------------------
function Save-ImageUrl {
    param([string]$Url, [int]$W, [int]$H, [string]$DstPath)

    if (-not $Url) { throw "没有可用的图源" }

    $tmp = [System.IO.Path]::GetTempFileName() + ".jpg"
    try {
        Invoke-WebRequest -Uri $Url -OutFile $tmp -TimeoutSec 30 -UseBasicParsing `
            -WebSession $script:Session -Headers @{ "Referer" = "https://image.baidu.com/" }
        if (-not (Test-ImageFile -Path $tmp)) { throw "不是有效图片" }

        $bmp = [System.Drawing.Bitmap]::FromFile($tmp)
        try {
            if ($bmp.Width -lt 240) { throw "分辨率太低 ($($bmp.Width)px)" }
            $size = Resize-ToTarget -Src $bmp -TargetW $W -TargetH $H -DstPath $DstPath
            return "$size (源图 $($bmp.Width)x$($bmp.Height))"
        } finally {
            $bmp.Dispose()
        }
    } finally {
        [System.IO.File]::Delete($tmp)
    }
}

# ---------------------------------------------------------------------
# 关键词表（用具体名词，「收纳箱」这类比「家居好物」命中率高得多）
#   Q  ：主关键词
#   Q2 ：第二张图用的关键词（不填则复用 Q，靠 Pick 错开）
#   P  ：同一关键词被多处复用时，从第几个结果开始取，避免撞图
#   Src：图源，默认 baidu；轮播图用 bing
# ---------------------------------------------------------------------
# 首页 4 张（distribution_site=1，sort 1..4）+ 分类页 2 张（distribution_site=2，sort 1..2）
$Banners = @(
    @{ K = "banner-1"; Q = "客厅";     W = 1240; H = 500; P = 0; Src = "bing" },
    @{ K = "banner-2"; Q = "数码产品"; W = 1240; H = 500; P = 0; Src = "bing" },
    @{ K = "banner-3"; Q = "化妆品";   W = 1240; H = 500; P = 0; Src = "bing" },
    @{ K = "banner-4"; Q = "美食";     W = 1240; H = 500; P = 0; Src = "bing" },
    @{ K = "banner-5"; Q = "服装店";   W = 1240; H = 500; P = 0; Src = "bing" },
    @{ K = "banner-6"; Q = "运动";     W = 1240; H = 500; P = 0; Src = "bing" }
)

$TopCats = @(
    @{ K = "cat-1005000"; Q = "客厅";       P = 1 },
    @{ K = "cat-1005001"; Q = "美食";       P = 0 },
    @{ K = "cat-1005002"; Q = "服装店";     P = 0 },
    @{ K = "cat-1005003"; Q = "婴儿用品";   P = 0 },
    @{ K = "cat-1005004"; Q = "笔记本电脑"; P = 0 },
    @{ K = "cat-1005005"; Q = "化妆品";     P = 1 },
    @{ K = "cat-1005006"; Q = "健身";       P = 0 }
)

$SubCats = @(
    @{ K = "subcat-1010001"; Q = "收纳箱";   P = 0 },
    @{ K = "subcat-1010002"; Q = "四件套";   P = 0 },
    @{ K = "subcat-1011001"; Q = "零食";     P = 0 },
    @{ K = "subcat-1011002"; Q = "水果";     P = 0 },
    @{ K = "subcat-1012001"; Q = "男装";     P = 0 },
    @{ K = "subcat-1012002"; Q = "女装";     P = 0 },
    @{ K = "subcat-1013001"; Q = "尿不湿";   P = 0 },
    @{ K = "subcat-1013002"; Q = "积木玩具"; P = 0 },
    @{ K = "subcat-1014001"; Q = "手机";     P = 1 },
    @{ K = "subcat-1014002"; Q = "扫地机器人"; P = 0 },
    @{ K = "subcat-1015001"; Q = "面膜";     P = 0 },
    @{ K = "subcat-1015002"; Q = "口红";     P = 0 },
    @{ K = "subcat-1016001"; Q = "运动鞋";   P = 0 },
    @{ K = "subcat-1016002"; Q = "瑜伽垫";   P = 0 }
)

$Goods = @(
    @{ Id = "4008501"; Q = "陶瓷摆件";   P = 0 },
    @{ Id = "4008502"; Q = "收纳箱";     P = 1 },
    @{ Id = "4008503"; Q = "床上四件套"; P = 1 },
    @{ Id = "4008504"; Q = "垃圾桶";     P = 0 },
    @{ Id = "4008505"; Q = "坚果干果";   Q2 = "坚果零食"; P = 0; P2 = 3 },
    @{ Id = "4008506"; Q = "儿童鞋";     Q2 = "童鞋"; P = 0 },
    @{ Id = "4008507"; Q = "脐橙";       P = 0 },
    @{ Id = "4008508"; Q = "夹克";       P = 0 },
    @{ Id = "4008509"; Q = "连衣裙";     Q2 = "长裙";       P = 0; P2 = 2 },
    @{ Id = "4008510"; Q = "尿不湿";     P = 0 },
    @{ Id = "4008511"; Q = "积木玩具";   P = 1 },
    @{ Id = "4008512"; Q = "智能手机";   P = 0 },
    @{ Id = "4008513"; Q = "扫地机器人"; Q2 = "智能扫地机器人"; P = 1; P2 = 0 },
    @{ Id = "4008514"; Q = "面膜";       Q2 = "面膜敷脸";   P = 1; P2 = 4 },
    @{ Id = "4008515"; Q = "口红";       P = 1 },
    @{ Id = "4008516"; Q = "跑步鞋";     P = 0 },
    @{ Id = "4008517"; Q = "瑜伽垫";     P = 1 },
    @{ Id = "4008518"; Q = "面包";       Q2 = "吐司面包";   P = 0; P2 = 2 },
    @{ Id = "4008519"; Q = "榨汁机";     P = 0 },
    @{ Id = "4008520"; Q = "短袖T恤";    P = 0 },
    @{ Id = "4008521"; Q = "手环";       Q2 = "智能手环";   P = 0; P2 = 4 }
)

# ---------------------------------------------------------------------
# 组装待办
# ---------------------------------------------------------------------
$Jobs = New-Object System.Collections.ArrayList
foreach ($b in $Banners) {
    [void]$Jobs.Add([pscustomobject]@{ Key = $b.K; Keyword = $b.Q; W = $b.W; H = $b.H; Pick = $b.P; Src = $b.Src })
}
foreach ($c in $TopCats) {
    [void]$Jobs.Add([pscustomobject]@{ Key = $c.K; Keyword = $c.Q; W = 240; H = 610; Pick = $c.P; Src = "baidu" })
}
foreach ($c in $SubCats) {
    [void]$Jobs.Add([pscustomobject]@{ Key = $c.K; Keyword = $c.Q; W = 240; H = 240; Pick = $c.P; Src = "baidu" })
}
foreach ($g in $Goods) {
    # -1 是封面（列表里只显示 160x160），-2 用于详情主图/详情图
    # 第二张抓到线稿/文字海报这类废图时，用 Q2 换词、P2 往后多跳几张
    $q2 = $g.Q2;  if (-not $q2) { $q2 = $g.Q }
    $p2 = $g.P + 1; if ($g.Q2) { $p2 = 0 }
    if ($g.P2) { $p2 = $g.P2 }
    [void]$Jobs.Add([pscustomobject]@{ Key = "goods-$($g.Id)-1"; Keyword = $g.Q; W = 600; H = 600; Pick = $g.P; Src = "baidu" })
    [void]$Jobs.Add([pscustomobject]@{ Key = "goods-$($g.Id)-2"; Keyword = $q2;  W = 800; H = 800; Pick = $p2;  Src = "baidu" })
}

if ($Only.Count -gt 0) {
    # 同时支持 -Only a,b,c（逗号串）和 -Only @("a","b")（数组）
    $want = @()
    foreach ($o in $Only) {
        $want += ($o -split ',') | ForEach-Object { $_.Trim() } | Where-Object { $_ }
    }
    $Jobs = @($Jobs | Where-Object { $want -contains $_.Key })
}

# ---------------------------------------------------------------------
# 执行
# ---------------------------------------------------------------------
Write-Host "待处理 $($Jobs.Count) 张 -> $OutDir`n" -ForegroundColor Cyan

$Results = New-Object System.Collections.ArrayList
$Fail    = New-Object System.Collections.ArrayList
$Cache   = @{}
$n = 0

foreach ($job in $Jobs) {
    $n++
    $path = Join-Path $OutDir "$($job.Key).jpg"
    $tag  = "[$n/$($Jobs.Count)] $($job.Key)"
    $pad  = " " * [Math]::Max(0, 28 - $tag.Length)
    Write-Host "$tag$pad" -NoNewline

    if ((-not $Force) -and (Test-ImageFile -Path $path)) {
        Write-Host "SKIP" -ForegroundColor DarkGray
        [void]$Results.Add([pscustomobject]@{ Key = $job.Key; File = "/images/$($job.Key).jpg" })
        continue
    }

    Write-Host " <- '$($job.Keyword)' [$($job.Src)]" -NoNewline
    try {
        $ck = "$($job.Src)|$($job.Keyword)"
        if (-not $Cache.ContainsKey($ck)) {
            if ($job.Src -eq "bing") { $Cache[$ck] = Get-BingImageIds  -Keyword $job.Keyword }
            else                     { $Cache[$ck] = Get-BaiduImageIds -Keyword $job.Keyword }
            Start-Sleep -Milliseconds 1200
        }
        $ids = $Cache[$ck]
        if ($ids.Count -eq 0) { throw "图源没返回结果" }

        $done = $false
        $lastErr = ""
        for ($i = $job.Pick; $i -lt $ids.Count -and -not $done; $i++) {
            try {
                $info = Save-ImageUrl -Url $ids[$i] -W $job.W -H $job.H -DstPath $path
                Write-Host "  OK  $info" -ForegroundColor Green
                [void]$Results.Add([pscustomobject]@{ Key = $job.Key; File = "/images/$($job.Key).jpg" })
                $done = $true
            } catch {
                $lastErr = $_.Exception.Message
                if (Test-Path $path) { Remove-Item $path -Force -ErrorAction SilentlyContinue }
                if ($i -ge $job.Pick + 9) { break }   # 最多试 10 张候选
            }
        }
        if (-not $done) { throw "候选都不可用（$lastErr）" }
        Start-Sleep -Milliseconds 300
    } catch {
        Write-Host "  FAIL  $($_.Exception.Message)" -ForegroundColor Red
        [void]$Fail.Add([pscustomobject]@{ Key = $job.Key; Keyword = $job.Keyword; Reason = $_.Exception.Message })
    }
}

# ---------------------------------------------------------------------
# 汇总
# ---------------------------------------------------------------------
$Results | ConvertTo-Json -Depth 3 | Out-File -FilePath $MapFile -Encoding utf8

Write-Host "`n===== 完成 =====" -ForegroundColor Cyan
Write-Host "成功 $($Results.Count) 张，失败 $($Fail.Count) 张"
if ($Fail.Count -gt 0) {
    Write-Host "`n失败的（补跑：-Only 逗号分隔 Key）：" -ForegroundColor Yellow
    $Fail | ForEach-Object { Write-Host "  $($_.Key)  <- '$($_.Keyword)'  $($_.Reason)" -ForegroundColor Yellow }
}
Write-Host "`n映射已写入：$MapFile"