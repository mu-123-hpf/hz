<?php
header('Content-Type: application/json');

// 获取POST数据
$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['id'])) {
    $noteFile = 'notes/' . $data['id'] . '.json';
    
    if (file_exists($noteFile)) {
        if (unlink($noteFile)) {
            echo json_encode([
                'success' => true
            ]);
        } else {
            echo json_encode([
                'success' => false,
                'message' => '删除笔记失败'
            ]);
        }
    } else {
        echo json_encode([
            'success' => false,
            'message' => '笔记文件不存在'
        ]);
    }
} else {
    echo json_encode([
        'success' => false,
        'message' => '未提供笔记ID'
    ]);
}
?> 