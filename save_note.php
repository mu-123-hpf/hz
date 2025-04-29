<?php
header('Content-Type: application/json');

// 获取POST数据
$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['id']) && isset($data['content']) && isset($data['date'])) {
    $note = [
        'id' => $data['id'],
        'content' => $data['content'],
        'date' => $data['date'],
        'url' => $data['url']
    ];
    
    // 确保notes目录存在
    $notesDir = 'notes/';
    if (!file_exists($notesDir)) {
        mkdir($notesDir, 0777, true);
    }
    
    // 保存笔记信息到JSON文件
    $noteFile = $notesDir . $data['id'] . '.json';
    if (file_put_contents($noteFile, json_encode($note, JSON_PRETTY_PRINT))) {
        echo json_encode([
            'success' => true
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => '保存笔记失败'
        ]);
    }
} else {
    echo json_encode([
        'success' => false,
        'message' => '缺少必要的数据'
    ]);
}
?> 