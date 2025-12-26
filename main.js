// src/main/resources/static/js/main.js
/**
 * 问答平台主JavaScript文件
 */

// 等待DOM加载完成
document.addEventListener('DOMContentLoaded', function() {
    // 初始化所有功能
    initCaptchaRefresh();
    initAutoDismissAlerts();
    initFormValidation();
    initMarkdownPreview();
    initConfirmationDialogs();
    initCharacterCounters();
});

/**
 * 初始化验证码刷新功能
 */
function initCaptchaRefresh() {
    const captchaImages = document.querySelectorAll('.captcha-img');
    if (captchaImages.length > 0) {
        captchaImages.forEach(img => {
            img.addEventListener('click', refreshCaptcha);
        });
    }
}

/**
 * 刷新验证码
 */
async function refreshCaptcha() {
    try {
        const response = await fetch('/captcha/refresh');
        if (!response.ok) {
            throw new Error('刷新失败');
        }
        const data = await response.json();

        // 更新验证码ID
        const captchaIdInput = document.querySelector('input[name="captchaId"]');
        if (captchaIdInput) {
            captchaIdInput.value = data.captchaId;
        }

        // 更新验证码图片
        const captchaImg = document.querySelector('.captcha-img');
        if (captchaImg) {
            captchaImg.src = data.captchaImage;
        }
    } catch (error) {
        console.error('验证码刷新失败:', error);
        showToast('验证码刷新失败，请刷新页面重试', 'error');
    }
}

/**
 * 初始化自动关闭警告框
 */
function initAutoDismissAlerts() {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        // 5秒后自动关闭
        setTimeout(() => {
            if (alert.parentNode) {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }
        }, 5000);
    });
}

/**
 * 初始化表单验证
 */
function initFormValidation() {
    const forms = document.querySelectorAll('.needs-validation');

    forms.forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);

        // 实时验证
        form.querySelectorAll('input, textarea, select').forEach(input => {
            input.addEventListener('input', function() {
                validateField(this);
            });

            input.addEventListener('blur', function() {
                validateField(this);
            });
        });
    });

    // 密码确认验证
    const passwordInputs = document.querySelectorAll('input[type="password"]');
    passwordInputs.forEach(input => {
        input.addEventListener('input', validatePasswordConfirmation);
    });
}

/**
 * 验证单个字段
 */
function validateField(field) {
    const isValid = field.checkValidity();
    const feedback = field.parentElement.querySelector('.invalid-feedback');

    if (isValid) {
        field.classList.remove('is-invalid');
        field.classList.add('is-valid');
    } else {
        field.classList.remove('is-valid');
        field.classList.add('is-invalid');
    }
}

/**
 * 验证密码确认
 */
function validatePasswordConfirmation() {
    const password = document.getElementById('password');
    const confirmPassword = document.getElementById('confirmPassword');

    if (!password || !confirmPassword) return;

    if (confirmPassword.value && password.value !== confirmPassword.value) {
        confirmPassword.setCustomValidity('两次输入的密码不一致');
        confirmPassword.classList.add('is-invalid');
    } else {
        confirmPassword.setCustomValidity('');
        if (confirmPassword.checkValidity()) {
            confirmPassword.classList.remove('is-invalid');
            confirmPassword.classList.add('is-valid');
        }
    }
}

/**
 * 初始化Markdown预览功能
 */
function initMarkdownPreview() {
    const markdownTextareas = document.querySelectorAll('.markdown-editor');

    markdownTextareas.forEach(textarea => {
        // 添加预览容器
        const container = textarea.parentElement;
        const previewDiv = document.createElement('div');
        previewDiv.className = 'markdown-preview card mt-2 d-none';
        previewDiv.innerHTML = `
            <div class="card-header">
                <ul class="nav nav-tabs card-header-tabs">
                    <li class="nav-item">
                        <button class="nav-link active" type="button" data-bs-toggle="tab" data-bs-target="#edit-${textarea.id}">编辑</button>
                    </li>
                    <li class="nav-item">
                        <button class="nav-link" type="button" data-bs-toggle="tab" data-bs-target="#preview-${textarea.id}">预览</button>
                    </li>
                </ul>
            </div>
            <div class="card-body tab-content">
                <div class="tab-pane fade show active" id="edit-${textarea.id}">
                    ${textarea.outerHTML}
                </div>
                <div class="tab-pane fade" id="preview-${textarea.id}">
                    <div class="preview-content"></div>
                </div>
            </div>
        `;

        // 插入到容器中
        textarea.remove();
        container.appendChild(previewDiv);

        // 获取新的textarea
        const newTextarea = previewDiv.querySelector('textarea');
        const previewContent = previewDiv.querySelector('.preview-content');

        // 添加Tab切换事件
        previewDiv.querySelectorAll('button[data-bs-toggle="tab"]').forEach(button => {
            button.addEventListener('click', function() {
                if (this.textContent === '预览') {
                    // 将Markdown转换为HTML
                    const markdown = newTextarea.value;
                    const html = marked.parse(markdown);
                    previewContent.innerHTML = html;
                }
            });
        });
    });
}

/**
 * 初始化确认对话框
 */
function initConfirmationDialogs() {
    const deleteButtons = document.querySelectorAll('button[onclick*="confirm"]');

    deleteButtons.forEach(button => {
        const originalOnClick = button.getAttribute('onclick');
        button.removeAttribute('onclick');

        button.addEventListener('click', function(event) {
            event.preventDefault();

            const message = extractConfirmMessage(originalOnClick) || '确定要执行此操作吗？';

            showConfirmationDialog(message)
                .then(() => {
                    // 用户点击确认，执行原操作
                    const form = button.closest('form');
                    if (form) {
                        form.submit();
                    } else {
                        eval(originalOnClick.replace('return confirm', ''));
                    }
                })
                .catch(() => {
                    // 用户点击取消，什么都不做
                    console.log('操作已取消');
                });
        });
    });
}

/**
 * 从onclick属性中提取确认消息
 */
function extractConfirmMessage(onclickString) {
    const match = onclickString.match(/confirm\('([^']+)'\)/);
    return match ? match[1] : null;
}

/**
 * 显示自定义确认对话框
 */
function showConfirmationDialog(message) {
    return new Promise((resolve, reject) => {
        // 创建对话框
        const dialogId = 'confirmation-dialog-' + Date.now();
        const dialogHtml = `
            <div class="modal fade" id="${dialogId}" tabindex="-1">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">确认</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <i class="bi bi-exclamation-triangle text-warning me-2"></i>
                            ${message}
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                            <button type="button" class="btn btn-danger" id="confirm-btn">确认</button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // 添加到页面
        document.body.insertAdjacentHTML('beforeend', dialogHtml);
        const modalElement = document.getElementById(dialogId);
        const modal = new bootstrap.Modal(modalElement);

        // 显示对话框
        modal.show();

        // 确认按钮事件
        document.getElementById('confirm-btn').addEventListener('click', function() {
            modal.hide();
            resolve();
        });

        // 对话框隐藏时清理
        modalElement.addEventListener('hidden.bs.modal', function() {
            modalElement.remove();
            reject();
        });
    });
}

/**
 * 初始化字符计数器
 */
function initCharacterCounters() {
    const textareas = document.querySelectorAll('textarea[maxlength]');

    textareas.forEach(textarea => {
        const maxLength = parseInt(textarea.getAttribute('maxlength'));
        const counterId = 'counter-' + textarea.id;

        // 创建计数器
        const counter = document.createElement('div');
        counter.id = counterId;
        counter.className = 'form-text text-end';
        counter.style.fontSize = '0.875rem';
        counter.style.marginTop = '0.25rem';

        // 插入计数器
        textarea.parentElement.appendChild(counter);

        // 更新计数器
        function updateCounter() {
            const currentLength = textarea.value.length;
            counter.textContent = `${currentLength}/${maxLength}`;

            if (currentLength > maxLength * 0.9) {
                counter.classList.add('text-warning');
                counter.classList.remove('text-success');
            } else if (currentLength > maxLength * 0.75) {
                counter.classList.add('text-info');
                counter.classList.remove('text-warning', 'text-success');
            } else {
                counter.classList.add('text-success');
                counter.classList.remove('text-warning', 'text-info');
            }

            if (currentLength > maxLength) {
                counter.classList.add('text-danger');
                textarea.classList.add('is-invalid');
            } else {
                counter.classList.remove('text-danger');
                textarea.classList.remove('is-invalid');
            }
        }

        // 初始化计数器
        updateCounter();

        // 监听输入事件
        textarea.addEventListener('input', updateCounter);
        textarea.addEventListener('keyup', updateCounter);
    });
}

/**
 * 显示Toast通知
 */
function showToast(message, type = 'info') {
    const toastId = 'toast-' + Date.now();
    const typeConfig = {
        success: { icon: 'bi-check-circle', color: 'text-success' },
        error: { icon: 'bi-exclamation-circle', color: 'text-danger' },
        warning: { icon: 'bi-exclamation-triangle', color: 'text-warning' },
        info: { icon: 'bi-info-circle', color: 'text-info' }
    };

    const config = typeConfig[type] || typeConfig.info;

    const toastHtml = `
        <div id="${toastId}" class="toast align-items-center" role="alert">
            <div class="d-flex">
                <div class="toast-body">
                    <i class="bi ${config.icon} ${config.color} me-2"></i>
                    ${message}
                </div>
                <button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `;

    // 获取或创建Toast容器
    let toastContainer = document.getElementById('toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toast-container';
        toastContainer.className = 'toast-container position-fixed top-0 end-0 p-3';
        toastContainer.style.zIndex = '1060';
        document.body.appendChild(toastContainer);
    }

    // 添加Toast
    toastContainer.insertAdjacentHTML('beforeend', toastHtml);
    const toastElement = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastElement, {
        autohide: true,
        delay: 3000
    });

    toast.show();

    // Toast隐藏后移除
    toastElement.addEventListener('hidden.bs.toast', function() {
        toastElement.remove();
    });
}

/**
 * 防抖函数
 */
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

/**
 * 节流函数
 */
function throttle(func, limit) {
    let inThrottle;
    return function() {
        const args = arguments;
        const context = this;
        if (!inThrottle) {
            func.apply(context, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

/**
 * 检测用户是否在线
 */
function checkOnlineStatus() {
    if (!navigator.onLine) {
        showToast('网络连接已断开', 'warning');
    }

    window.addEventListener('online', () => {
        showToast('网络连接已恢复', 'success');
    });

    window.addEventListener('offline', () => {
        showToast('网络连接已断开', 'warning');
    });
}

/**
 * 复制文本到剪贴板
 */
function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
        showToast('已复制到剪贴板', 'success');
    }).catch(err => {
        console.error('复制失败:', err);
        showToast('复制失败', 'error');
    });
}

/**
 * 格式化时间
 */
function formatTime(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHour = Math.floor(diffMin / 60);
    const diffDay = Math.floor(diffHour / 24);

    if (diffSec < 60) {
        return '刚刚';
    } else if (diffMin < 60) {
        return `${diffMin}分钟前`;
    } else if (diffHour < 24) {
        return `${diffHour}小时前`;
    } else if (diffDay < 30) {
        return `${diffDay}天前`;
    } else {
        return date.toLocaleDateString();
    }
}

/**
 * 初始化相对时间
 */
function initRelativeTime() {
    const timeElements = document.querySelectorAll('.relative-time');

    timeElements.forEach(element => {
        const dateString = element.getAttribute('data-time');
        if (dateString) {
            element.textContent = formatTime(dateString);
        }
    });
}

// 导出函数供全局使用
window.QAPlatform = {
    showToast,
    refreshCaptcha,
    copyToClipboard,
    formatTime,
    showConfirmationDialog
};