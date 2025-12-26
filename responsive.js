// src/main/resources/static/js/responsive.js
/**
 * 响应式调整
 */

class ResponsiveManager {
    constructor() {
        this.isMobile = window.innerWidth <= 768;
        this.isTablet = window.innerWidth > 768 && window.innerWidth <= 992;
        this.isDesktop = window.innerWidth > 992;

        this.init();
    }

    init() {
        this.adjustLayout();
        this.bindEvents();
        this.setupMobileMenu();
        this.adjustTextareas();
    }

    /**
     * 调整布局
     */
    adjustLayout() {
        // 调整卡片布局
        const cards = document.querySelectorAll('.card');
        cards.forEach(card => {
            if (this.isMobile) {
                card.classList.add('border-0');
                card.classList.remove('shadow-sm');
            } else {
                card.classList.remove('border-0');
                card.classList.add('shadow-sm');
            }
        });

        // 调整表格
        const tables = document.querySelectorAll('table');
        tables.forEach(table => {
            if (this.isMobile) {
                table.classList.add('table-responsive');
            } else {
                table.classList.remove('table-responsive');
            }
        });
    }

    /**
     * 绑定事件
     */
    bindEvents() {
        window.addEventListener('resize', this.debouncedResize.bind(this));
    }

    /**
     * 设置移动端菜单
     */
    setupMobileMenu() {
        const navbarToggler = document.querySelector('.navbar-toggler');
        const navbarCollapse = document.querySelector('.navbar-collapse');

        if (!navbarToggler || !navbarCollapse) return;

        // 点击外部关闭菜单
        document.addEventListener('click', (event) => {
            const isClickInside = navbarCollapse.contains(event.target) ||
                navbarToggler.contains(event.target);

            if (!isClickInside && navbarCollapse.classList.contains('show')) {
                const bsCollapse = bootstrap.Collapse.getInstance(navbarCollapse);
                if (bsCollapse) {
                    bsCollapse.hide();
                }
            }
        });

        // 菜单项点击后自动关闭（移动端）
        navbarCollapse.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', () => {
                if (this.isMobile) {
                    const bsCollapse = bootstrap.Collapse.getInstance(navbarCollapse);
                    if (bsCollapse) {
                        bsCollapse.hide();
                    }
                }
            });
        });
    }

    /**
     * 调整文本域大小
     */
    adjustTextareas() {
        const textareas = document.querySelectorAll('textarea');

        textareas.forEach(textarea => {
            if (this.isMobile) {
                textarea.style.minHeight = '150px';
                textarea.style.fontSize = '16px'; // 防止iOS缩放
            } else {
                textarea.style.minHeight = '200px';
                textarea.style.fontSize = '';
            }
        });
    }

    /**
     * 防抖调整大小
     */
    debouncedResize = debounce(() => {
        const wasMobile = this.isMobile;
        const wasTablet = this.isTablet;
        const wasDesktop = this.isDesktop;

        this.isMobile = window.innerWidth <= 768;
        this.isTablet = window.innerWidth > 768 && window.innerWidth <= 992;
        this.isDesktop = window.innerWidth > 992;

        // 如果设备类型发生变化，重新调整布局
        if (wasMobile !== this.isMobile ||
            wasTablet !== this.isTablet ||
            wasDesktop !== this.isDesktop) {
            this.adjustLayout();
            this.adjustTextareas();
        }
    }, 250);
}

// 初始化响应式管理器
document.addEventListener('DOMContentLoaded', function() {
    new ResponsiveManager();
});