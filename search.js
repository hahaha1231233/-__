// src/main/resources/static/js/search.js
/**
 * 搜索功能
 */

class SearchService {
    constructor() {
        this.searchInput = document.getElementById('search-input');
        this.searchResults = document.getElementById('search-results');
        this.searchModal = null;
        this.questionsCache = null;
        this.lastSearchTime = 0;

        this.init();
    }

    init() {
        if (!this.searchInput) return;

        // 监听搜索输入
        this.searchInput.addEventListener('input', this.debouncedSearch.bind(this));

        // 监听键盘事件
        this.searchInput.addEventListener('keydown', this.handleKeydown.bind(this));

        // 预加载问题数据
        this.preloadQuestions();
    }

    /**
     * 预加载问题数据
     */
    async preloadQuestions() {
        try {
            const response = await fetch('/api/questions');
            if (response.ok) {
                this.questionsCache = await response.json();
            }
        } catch (error) {
            console.error('预加载问题数据失败:', error);
        }
    }

    /**
     * 执行搜索
     */
    async performSearch(query) {
        const now = Date.now();
        if (now - this.lastSearchTime < 300) {
            return; // 避免过于频繁的搜索
        }
        this.lastSearchTime = now;

        if (!query.trim()) {
            this.clearResults();
            return;
        }

        try {
            let results;

            if (this.questionsCache) {
                // 使用缓存数据
                results = this.searchInCache(query);
            } else {
                // 从服务器搜索
                results = await this.searchFromServer(query);
            }

            this.displayResults(results);
        } catch (error) {
            console.error('搜索失败:', error);
            this.showError('搜索失败，请重试');
        }
    }

    /**
     * 在缓存中搜索
     */
    searchInCache(query) {
        const lowerQuery = query.toLowerCase();
        return this.questionsCache.filter(question => {
            return question.title.toLowerCase().includes(lowerQuery) ||
                question.content.toLowerCase().includes(lowerQuery) ||
                question.user.username.toLowerCase().includes(lowerQuery);
        });
    }

    /**
     * 从服务器搜索
     */
    async searchFromServer(query) {
        const response = await fetch(`/api/search?q=${encodeURIComponent(query)}`);
        if (!response.ok) {
            throw new Error('搜索请求失败');
        }
        return await response.json();
    }

    /**
     * 显示搜索结果
     */
    displayResults(results) {
        if (!this.searchResults) return;

        if (results.length === 0) {
            this.searchResults.innerHTML = `
                <div class="text-center py-4">
                    <i class="bi bi-search display-4 text-muted"></i>
                    <p class="mt-3">没有找到相关结果</p>
                </div>
            `;
            return;
        }

        let html = '<div class="list-group">';

        results.slice(0, 10).forEach(question => {
            const excerpt = question.content.substring(0, 100) + '...';
            const date = new Date(question.createdAt).toLocaleDateString();

            html += `
                <a href="/questions/${question.id}" class="list-group-item list-group-item-action">
                    <div class="d-flex w-100 justify-content-between">
                        <h6 class="mb-1">${this.highlightText(question.title, this.searchInput.value)}</h6>
                        <small class="text-muted">${date}</small>
                    </div>
                    <p class="mb-1 small text-muted">${excerpt}</p>
                    <small class="text-muted">
                        <i class="bi bi-person"></i> ${question.user.username}
                        <span class="ms-2">
                            <i class="bi bi-chat"></i> ${question.replies.length}
                        </span>
                    </small>
                </a>
            `;
        });

        html += '</div>';

        if (results.length > 10) {
            html += `
                <div class="text-center mt-3">
                    <a href="/search?q=${encodeURIComponent(this.searchInput.value)}" 
                       class="btn btn-sm btn-outline-primary">
                        查看全部 ${results.length} 个结果
                    </a>
                </div>
            `;
        }

        this.searchResults.innerHTML = html;
    }

    /**
     * 高亮显示搜索关键词
     */
    highlightText(text, query) {
        if (!query) return text;

        const regex = new RegExp(`(${this.escapeRegExp(query)})`, 'gi');
        return text.replace(regex, '<mark>$1</mark>');
    }

    /**
     * 转义正则表达式特殊字符
     */
    escapeRegExp(string) {
        return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }

    /**
     * 清空搜索结果
     */
    clearResults() {
        if (this.searchResults) {
            this.searchResults.innerHTML = '';
        }
    }

    /**
     * 显示错误信息
     */
    showError(message) {
        if (this.searchResults) {
            this.searchResults.innerHTML = `
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle"></i>
                    ${message}
                </div>
            `;
        }
    }

    /**
     * 处理键盘事件
     */
    handleKeydown(event) {
        if (event.key === 'Escape') {
            this.clearResults();
        }

        if (event.key === 'Enter' && this.searchInput.value.trim()) {
            window.location.href = `/search?q=${encodeURIComponent(this.searchInput.value)}`;
        }
    }

    /**
     * 防抖搜索
     */
    debouncedSearch = debounce((event) => {
        this.performSearch(event.target.value);
    }, 300);
}

// 初始化搜索功能
document.addEventListener('DOMContentLoaded', function() {
    new SearchService();
});