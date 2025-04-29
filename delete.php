<?php
header('Content-Type: application/json');

// 获取POST数据
$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['filePath'])) {
    $filePath = $data['filePath'];
    
    // 安全检查：确保只能删除photo目录下的文件
    if (strpos($filePath, 'photo/') !== 0) {
        echo json_encode([
            'success' => false,
            'message' => '无效的文件路径'
        ]);
        exit;
    }
    
    if (file_exists($filePath)) {
        if (unlink($filePath)) {
            echo json_encode([
                'success' => true
            ]);
        } else {
            echo json_encode([
                'success' => false,
                'message' => '删除文件失败'
            ]);
        }
    } else {
        echo json_encode([
            'success' => false,
            'message' => '文件不存在'
        ]);
    }
} else {
    echo json_encode([
        'success' => false,
        'message' => '未提供文件路径'
    ]);
}
?> 