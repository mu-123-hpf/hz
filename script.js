// 密码验证状态
let isPasswordVerified = false;

// 检查是否是从首页进入
function checkEntryPoint() {
    const referrer = document.referrer;
    const isFromIndex = referrer.includes('index.html');
    return isFromIndex;
}

// 密码验证
function checkPassword() {
    const password = document.getElementById('passwordInput').value;
    if (password === 'hpflovezjl') {
        isPasswordVerified = true;
        document.getElementById('passwordOverlay').style.display = 'none';
        document.getElementById('mainContent').style.display = 'flex';
        document.querySelector('.main-content').style.display = 'block';
        // 确保内容可见
        document.getElementById('notesList').style.display = 'grid';
    } else {
        alert('密码错误，请重试！');
        document.getElementById('passwordInput').value = '';
    }
}

// 按回车键也可以确认密码
document.getElementById('passwordInput').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        checkPassword();
    }
});

// 存储笔记和照片的数组
let notes = [];

// 创建爱心元素
function createHeart() {
    const heart = document.createElement('div');
    heart.classList.add('heart');
    heart.innerHTML = '❤️';
    
    // 随机位置
    heart.style.left = Math.random() * 100 + 'vw';
    heart.style.top = Math.random() * 100 + 'vh';
    
    // 随机大小
    const size = Math.random() * 20 + 10;
    heart.style.fontSize = size + 'px';
    
    // 随机动画持续时间
    const duration = Math.random() * 3 + 2;
    heart.style.animationDuration = duration + 's';
    
    document.querySelector('.hearts-container').appendChild(heart);
    
    // 动画结束后移除爱心
    setTimeout(() => {
        heart.remove();
    }, duration * 1000);
}

// 定期创建新的爱心
setInterval(createHeart, 300);

// 鼠标移动时创建爱心
document.addEventListener('mousemove', (e) => {
    const heart = document.createElement('div');
    heart.classList.add('heart');
    heart.innerHTML = '❤️';
    
    heart.style.left = e.clientX + 'px';
    heart.style.top = e.clientY + 'px';
    
    const size = Math.random() * 20 + 10;
    heart.style.fontSize = size + 'px';
    
    const duration = Math.random() * 3 + 2;
    heart.style.animationDuration = duration + 's';
    
    document.querySelector('.hearts-container').appendChild(heart);
    
    setTimeout(() => {
        heart.remove();
    }, duration * 1000);
});

// 处理照片上传
function handleFileUpload(file) {
    const statusDiv = document.getElementById('uploadStatus');
    
    if (!file.type.startsWith('image/')) {
        statusDiv.textContent = '请上传图片文件！';
        statusDiv.style.color = 'red';
        return;
    }

    const formData = new FormData();
    formData.append('photo', file);

    fetch('upload.php', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            const note = {
                id: Date.now(),
                url: data.url,
                content: '点击此处添加描述...',
                date: new Date().toLocaleString(),
                type: 'photo'
            };
            
            notes.push(note);
            saveNotes();
            displayNotes();
            
            // 显示上传成功提示
            statusDiv.textContent = '上传成功！';
            statusDiv.style.color = 'green';
            statusDiv.style.opacity = '1';
            
            // 3秒后淡出
            setTimeout(() => {
                statusDiv.style.transition = 'opacity 1s ease';
                statusDiv.style.opacity = '0';
            }, 3000);
        } else {
            statusDiv.textContent = data.message || '上传失败，请重试！';
            statusDiv.style.color = 'red';
        }
    })
    .catch(error => {
        statusDiv.textContent = '上传失败，请重试！';
        statusDiv.style.color = 'red';
        console.error('上传错误:', error);
    });
}

// 设置拖拽上传
const photoInput = document.getElementById('photoInput');
const body = document.body;

// 处理文件选择
photoInput.addEventListener('change', (e) => {
    if (e.target.files.length > 0) {
        handleFileUpload(e.target.files[0]);
    }
});

// 处理全局拖拽
body.addEventListener('dragover', (e) => {
    e.preventDefault();
    body.classList.add('dragover');
});

body.addEventListener('dragleave', () => {
    body.classList.remove('dragover');
});

body.addEventListener('drop', (e) => {
    e.preventDefault();
    body.classList.remove('dragover');
    
    if (e.dataTransfer.files.length > 0) {
        handleFileUpload(e.dataTransfer.files[0]);
    }
});

// 显示所有笔记和照片
function displayNotes() {
    const notesList = document.getElementById('notesList');
    if (!notesList) return;

    notesList.innerHTML = '';
    notes.sort((a, b) => b.id - a.id).forEach(note => {
        const noteElement = document.createElement('div');
        noteElement.className = 'note-item';
        
        noteElement.innerHTML = `
            <button class="delete-btn" onclick="deleteNote(${note.id})">删除</button>
            <img src="${note.url}" alt="上传的照片">
            <div class="note-content">
                <p class="note-text" contenteditable="true" 
                   data-placeholder="点击此处添加描述..." 
                   onfocus="clearPlaceholder(this)" 
                   onblur="saveEdit(${note.id}, this, 'content')">${note.content === '点击此处添加描述...' ? '' : note.content}</p>
                <small class="note-date" contenteditable="true" 
                       onblur="saveEdit(${note.id}, this, 'date')">${note.date}</small>
            </div>
        `;

        notesList.appendChild(noteElement);
    });
}

// 清除占位文字
function clearPlaceholder(element) {
    if (element.textContent === '点击此处添加描述...') {
        element.textContent = '';
    }
}

// 保存编辑
function saveEdit(id, element, field) {
    const note = notes.find(n => n.id === id);
    if (!note) return;

    const newContent = element.textContent.trim();
    if (field === 'content') {
        note.content = newContent || '点击此处添加描述...';
    } else if (field === 'date') {
        note.date = newContent;
    }
    
    // 保存到服务器
    fetch('save_note.php', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(note)
    })
    .then(response => response.json())
    .then(data => {
        if (!data.success) {
            console.error('保存笔记失败:', data.message);
        }
    })
    .catch(error => {
        console.error('保存笔记错误:', error);
    });
    
    saveNotes();
}

// 删除笔记或照片
function deleteNote(id) {
    const note = notes.find(n => n.id === id);
    if (note) {
        // 删除服务器上的笔记文件
        fetch('delete_note.php', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ id: id })
        });
        
        if (note.type === 'photo') {
            // 发送请求删除服务器上的图片
            fetch('delete.php', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    filePath: note.url
                })
            });
        }
    }
    
    notes = notes.filter(note => note.id !== id);
    saveNotes();
    displayNotes();
}

// 保存笔记到本地存储
function saveNotes() {
    localStorage.setItem('notes', JSON.stringify(notes));
}

// 从本地存储加载笔记
function loadNotes() {
    const savedNotes = localStorage.getItem('notes');
    if (savedNotes) {
        notes = JSON.parse(savedNotes);
        
        // 检查并转换 base64 图片
        notes.forEach(note => {
            if (note.type === 'photo' && note.url.startsWith('data:image')) {
                // 从 base64 中提取图片数据
                const base64Data = note.url.split(',')[1];
                const imageData = atob(base64Data);
                const arrayBuffer = new ArrayBuffer(imageData.length);
                const uint8Array = new Uint8Array(arrayBuffer);
                
                for (let i = 0; i < imageData.length; i++) {
                    uint8Array[i] = imageData.charCodeAt(i);
                }
                
                // 创建 Blob 对象
                const blob = new Blob([arrayBuffer], { type: 'image/jpeg' });
                const file = new File([blob], `converted_${note.id}.jpg`, { type: 'image/jpeg' });
                
                // 上传转换后的图片
                const formData = new FormData();
                formData.append('photo', file);
                
                fetch('upload.php', {
                    method: 'POST',
                    body: formData
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        // 更新笔记中的图片URL
                        note.url = data.url;
                        saveNotes();
                        displayNotes();
                    } else {
                        console.error('转换图片失败:', data.message);
                    }
                })
                .catch(error => {
                    console.error('转换图片错误:', error);
                });
            }
        });
        
        displayNotes();
    }
}

// 页面加载时初始化
document.addEventListener('DOMContentLoaded', function() {
    // 检查是否已经验证过密码
    if (isPasswordVerified) {
        document.getElementById('passwordOverlay').style.display = 'none';
        document.getElementById('mainContent').style.display = 'flex';
        document.querySelector('.main-content').style.display = 'block';
    } else {
        // 如果没有验证过密码，显示密码输入界面
        document.getElementById('passwordOverlay').style.display = 'flex';
        document.getElementById('mainContent').style.display = 'none';
    }
    
    // 加载已保存的笔记
    loadNotes();
    displayNotes();
}); 