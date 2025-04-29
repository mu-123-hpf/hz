<?php
header('Content-Type: application/json');

// 允许的文件类型
$allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];

if ($_FILES['photo']) {
    $file = $_FILES['photo'];
    
    // 检查文件类型
    if (!in_array($file['type'], $allowedTypes)) {
        echo json_encode([
            'success' => false,
            'message' => '只允许上传 JPG、PNG、GIF 或 WEBP 格式的图片'
        ]);
        exit;
    }
    
    // 检查文件大小（限制为 5MB）
    if ($file['size'] > 5 * 1024 * 1024) {
        echo json_encode([
            'success' => false,
            'message' => '图片大小不能超过 5MB'
        ]);
        exit;
    }
    
    // 生成安全的文件名
    $extension = pathinfo($file['name'], PATHINFO_EXTENSION);
    $fileName = time() . '_' . bin2hex(random_bytes(8)) . '.' . $extension;
    $targetDir = 'photo/';
    
    // 确保目标目录存在
    if (!file_exists($targetDir)) {
        mkdir($targetDir, 0777, true);
    }
    
    $targetPath = $targetDir . $fileName;
    
    // 检查文件是否为真实的图片
    if (!getimagesize($file['tmp_name'])) {
        echo json_encode([
            'success' => false,
            'message' => '无效的图片文件'
        ]);
        exit;
    }
    
    if (move_uploaded_file($file['tmp_name'], $targetPath)) {
        // 获取当前页面的完整URL
        $protocol = isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? 'https://' : 'http://';
        $host = $_SERVER['HTTP_HOST'];
        $baseUrl = $protocol . $host;
        
        // 获取当前脚本的目录路径
        $scriptDir = dirname($_SERVER['SCRIPT_NAME']);
        if ($scriptDir === '/') {
            $scriptDir = '';
        }
        
        // 构建完整的图片URL
        $imageUrl = $baseUrl . $scriptDir . '/' . $targetPath;
        
        echo json_encode([
            'success' => true,
            'url' => $imageUrl
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => '上传失败'
        ]);
    }
} else {
    echo json_encode([
        'success' => false,
        'message' => '没有收到文件'
    ]);
}
?> 