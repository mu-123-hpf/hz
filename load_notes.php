<?php
header('Content-Type: application/json');

$notesDir = 'notes/';
$notes = [];

// 确保notes目录存在
if (!file_exists($notesDir)) {
    mkdir($notesDir, 0777, true);
}

// 读取notes目录下的所有JSON文件
if ($handle = opendir($notesDir)) {
    while (false !== ($entry = readdir($handle))) {
        if ($entry != "." && $entry != ".." && pathinfo($entry, PATHINFO_EXTENSION) == 'json') {
            $noteFile = $notesDir . $entry;
            $noteData = json_decode(file_get_contents($noteFile), true);
            if ($noteData) {
                $notes[] = $noteData;
            }
        }
    }
    closedir($handle);
}

// 按ID降序排序
usort($notes, function($a, $b) {
    return $b['id'] - $a['id'];
});

echo json_encode([
    'success' => true,
    'notes' => $notes
]);
?> 