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
        
        // 创建图片元素
        const imgElement = document.createElement('img');
        imgElement.src = note.url;
        imgElement.alt = "上传的照片";
        
        // 添加图片加载错误处理
        imgElement.onerror = function() {
            console.error('图片加载失败:', note.url);
            // 尝试使用相对路径
            if (note.url.startsWith('http')) {
                const urlParts = note.url.split('/');
                const fileName = urlParts[urlParts.length - 1];
                this.src = 'photo/' + fileName;
            }
        };
        
        noteElement.innerHTML = `
            <button class="delete-btn" onclick="deleteNote(${note.id})">删除</button>
            <div class="note-content">
                <p class="note-text" contenteditable="true" 
                   data-placeholder="点击此处添加描述..." 
                   onfocus="clearPlaceholder(this)" 
                   onblur="saveEdit(${note.id}, this, 'content')">${note.content === '点击此处添加描述...' ? '' : note.content}</p>
                <small class="note-date" contenteditable="true" 
                       onblur="saveEdit(${note.id}, this, 'date')">${note.date}</small>
            </div>
        `;
        
        // 将图片元素插入到正确的位置
        noteElement.insertBefore(imgElement, noteElement.firstChild.nextSibling);
        
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
    saveNotes();
}

// 删除笔记或照片
function deleteNote(id) {
    const note = notes.find(n => n.id === id);
    if (note && note.type === 'photo') {
        // 发送请求删除服务器上的图片
        fetch('delete.php', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                filePath: note.url
            })
        })
        .then(response => response.json())
        .then(data => {
            if (!data.success) {
                console.error('删除图片失败:', data.message);
            }
        })
        .catch(error => {
            console.error('删除图片错误:', error);
        });
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
    // 直接加载笔记，不需要密码验证
    const notes = JSON.parse(localStorage.getItem('notes')) || [];
    displayNotes(notes);
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

// 修改其他相关函数，移除密码验证检查
function addNote() {
    const noteInput = document.getElementById('noteInput');
    const noteText = noteInput.value.trim();
    if (noteText) {
        const notes = JSON.parse(localStorage.getItem('notes')) || [];
        notes.push({
            text: noteText,
            timestamp: new Date().toISOString()
        });
        localStorage.setItem('notes', JSON.stringify(notes));
        noteInput.value = '';
        displayNotes(notes);
    }
} 