document.addEventListener('DOMContentLoaded', () => {
    const polyhedron = document.getElementById('polyhedron');
    let isRotating = true;

    // 获取照片文件夹中的照片
    async function loadPhotos() {
        try {
            const response = await fetch('/api/photos');
            const photos = await response.json();
            createPolyhedron(photos);
        } catch (error) {
            console.error('加载照片失败:', error);
        }
    }

    // 创建多面体
    function createPolyhedron(photos) {
        const numFaces = Math.max(6, photos.length); // 最少6个面（立方体）
        const angleStep = 360 / numFaces;
        
        // 清除现有的面
        polyhedron.innerHTML = '';

        // 创建面
        for (let i = 0; i < numFaces; i++) {
            const face = document.createElement('div');
            face.className = 'face';
            
            if (i < photos.length) {
                face.style.backgroundImage = `url(${photos[i]})`;
            } else {
                face.style.backgroundColor = 'transparent';
            }

            // 计算面的位置
            const angle = angleStep * i;
            const radius = 150; // 多面体的半径
            const x = radius * Math.cos(angle * Math.PI / 180);
            const z = radius * Math.sin(angle * Math.PI / 180);
            
            face.style.transform = `translateX(${x}px) translateZ(${z}px) rotateY(${angle}deg)`;

            // 添加点击事件
            face.addEventListener('click', () => {
                if (isRotating) {
                    polyhedron.style.animation = 'none';
                    isRotating = false;
                } else {
                    polyhedron.style.animation = 'rotate 20s infinite linear';
                    isRotating = true;
                }
            });

            polyhedron.appendChild(face);
        }
    }

    // 初始加载照片
    loadPhotos();
}); 