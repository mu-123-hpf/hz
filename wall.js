let notes = [];
let isDragging = false;
let currentWindow = null;
let offsetX, offsetY;

// 从本地存储加载笔记
function loadNotes() {
    const savedNotes = localStorage.getItem('wallNotes');
    if (savedNotes) {
        notes = JSON.parse(savedNotes);
        displayNotes();
    }
}

// 保存笔记到本地存储
function saveNotes() {
    localStorage.setItem('wallNotes', JSON.stringify(notes));
}

// 创建可拖动的笔记窗口
function createNoteWindow() {
    const noteWindow = document.createElement('div');
    noteWindow.className = 'note-window';
    noteWindow.draggable = true;
    
    // 生成唯一ID
    const noteId = Date.now();
    noteWindow.dataset.noteId = noteId;
    
    // 设置初始位置
    noteWindow.style.left = '50%';
    noteWindow.style.top = '50%';
    noteWindow.style.transform = 'translate(-50%, -50%)';
    
    noteWindow.innerHTML = `
        <div class="note-window-header">
            <input type="text" class="note-title" value="新吐槽" oninput="autoSaveNote(this)">
            <div class="note-window-actions">
                <button class="delete-btn" onclick="deleteNoteWindow(this)">
                    <i class="material-icons">delete</i>
                </button>
            </div>
        </div>
        <div class="note-window-content">
            <textarea placeholder="写下你的吐槽..." oninput="autoSaveNote(this)"></textarea>
        </div>
    `;
    
    document.body.appendChild(noteWindow);
    
    // 添加拖动功能
    const header = noteWindow.querySelector('.note-window-header');
    header.addEventListener('mousedown', startDragging);
    
    // 自动聚焦到文本框
    noteWindow.querySelector('textarea').focus();
    
    // 创建新笔记并保存
    const note = {
        id: noteId,
        title: '新吐槽',
        content: '',
        date: new Date().toLocaleString(),
        position: {
            left: noteWindow.style.left,
            top: noteWindow.style.top
        }
    };
    notes.push(note);
    saveNotes();
}

// 开始拖动
function startDragging(e) {
    if (e.target.tagName === 'BUTTON' || e.target.tagName === 'INPUT') return;
    
    isDragging = true;
    currentWindow = this.parentElement;
    offsetX = e.clientX - currentWindow.offsetLeft;
    offsetY = e.clientY - currentWindow.offsetTop;
    
    // 添加拖动时的样式
    currentWindow.style.cursor = 'grabbing';
    currentWindow.style.opacity = '0.9';
    
    document.addEventListener('mousemove', dragWindow);
    document.addEventListener('mouseup', stopDragging);
}

// 拖动窗口
function dragWindow(e) {
    if (!isDragging) return;
    
    currentWindow.style.left = (e.clientX - offsetX) + 'px';
    currentWindow.style.top = (e.clientY - offsetY) + 'px';
    currentWindow.style.transform = 'none';
    
    // 拖动时自动保存位置
    const noteId = parseInt(currentWindow.dataset.noteId);
    const position = {
        left: currentWindow.style.left,
        top: currentWindow.style.top
    };
    
    // 更新笔记位置
    const existingNote = notes.find(note => note.id === noteId);
    if (existingNote) {
        existingNote.position = position;
        saveNotes();
    }
}

// 停止拖动
function stopDragging() {
    isDragging = false;
    if (currentWindow) {
        currentWindow.style.cursor = '';
        currentWindow.style.opacity = '';
        
        // 确保在停止拖动时保存最终位置
        const noteId = parseInt(currentWindow.dataset.noteId);
        const position = {
            left: currentWindow.style.left,
            top: currentWindow.style.top
        };
        
        const existingNote = notes.find(note => note.id === noteId);
        if (existingNote) {
            existingNote.position = position;
            saveNotes();
        }
    }
    document.removeEventListener('mousemove', dragWindow);
    document.removeEventListener('mouseup', stopDragging);
}

// 自动保存笔记
function autoSaveNote(element) {
    const noteWindow = element.closest('.note-window');
    const noteId = parseInt(noteWindow.dataset.noteId);
    const title = noteWindow.querySelector('.note-title').value.trim();
    const content = noteWindow.querySelector('textarea').value.trim();
    const position = {
        left: noteWindow.style.left,
        top: noteWindow.style.top
    };
    
    // 检查是否已存在该笔记
    const existingNote = notes.find(note => note.id === noteId);
    
    if (existingNote) {
        // 更新现有笔记
        existingNote.title = title;
        existingNote.content = content;
        existingNote.date = new Date().toLocaleString();
        existingNote.position = position;
    } else if (content || title !== '新吐槽') {
        // 创建新笔记
        const note = {
            id: noteId,
            title: title,
            content: content,
            date: new Date().toLocaleString(),
            position: position
        };
        notes.push(note);
    }
    
    saveNotes();
}

// 删除笔记窗口
function deleteNoteWindow(button) {
    const noteWindow = button.closest('.note-window');
    const noteId = parseInt(noteWindow.dataset.noteId);
    
    // 从数组中删除对应的笔记
    notes = notes.filter(note => note.id !== noteId);
    
    saveNotes();
    noteWindow.remove();
}

// 显示所有笔记
function displayNotes() {
    const wallNotes = document.getElementById('wall-notes');
    wallNotes.innerHTML = '';
    
    notes.forEach(note => {
        const noteElement = document.createElement('div');
        noteElement.className = 'note-window';
        noteElement.dataset.noteId = note.id;
        noteElement.style.left = note.position.left;
        noteElement.style.top = note.position.top;
        noteElement.style.transform = 'none';
        
        noteElement.innerHTML = `
            <div class="note-window-header">
                <input type="text" class="note-title" value="${note.title || '吐槽'}" oninput="autoSaveNote(this)">
                <div class="note-window-actions">
                    <button class="delete-btn" onclick="deleteNoteWindow(this)">
                        <i class="material-icons">delete</i>
                    </button>
                </div>
            </div>
            <div class="note-window-content">
                <textarea oninput="autoSaveNote(this)">${note.content}</textarea>
                <small>${note.date}</small>
            </div>
        `;
        
        // 添加拖动功能
        const header = noteElement.querySelector('.note-window-header');
        header.addEventListener('mousedown', startDragging);
        
        wallNotes.appendChild(noteElement);
    });
}

// 页面加载时初始化
document.addEventListener('DOMContentLoaded', () => {
    loadNotes();
    
    // 设置当前页面对应的导航按钮为激活状态
    const currentPage = window.location.pathname.split('/').pop();
    const navButtons = document.querySelectorAll('.nav-button');
    
    navButtons.forEach(button => {
        const buttonPage = button.getAttribute('onclick').match(/location\.href='([^']+)'/)[1];
        if (buttonPage === currentPage) {
            button.style.backgroundColor = '#ff69b4';
            button.style.color = 'white';
            button.classList.add('active');
        } else {
            button.style.backgroundColor = '#f0f0f0';
            button.style.color = '#333';
            button.classList.remove('active');
        }
    });
}); 