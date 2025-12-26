// src/main/resources/static/js/marked.js
// 简化版marked.js，用于Markdown解析

const marked = {
    parse: function(markdown) {
        // 转换标题
        let html = markdown
            .replace(/^# (.*$)/gim, '<h1>$1</h1>')
            .replace(/^## (.*$)/gim, '<h2>$1</h2>')
            .replace(/^### (.*$)/gim, '<h3>$1</h3>')
            .replace(/^#### (.*$)/gim, '<h4>$1</h4>')
            .replace(/^##### (.*$)/gim, '<h5>$1</h5>')
            .replace(/^###### (.*$)/gim, '<h6>$1</h6>');

        // 转换粗体
        html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
        html = html.replace(/__(.*?)__/g, '<strong>$1</strong>');

        // 转换斜体
        html = html.replace(/\*(.*?)\*/g, '<em>$1</em>');
        html = html.replace(/_(.*?)_/g, '<em>$1</em>');

        // 转换删除线
        html = html.replace(/~~(.*?)~~/g, '<del>$1</del>');

        // 转换代码
        html = html.replace(/`(.*?)`/g, '<code>$1</code>');

        // 转换代码块
        html = html.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>');

        // 转换行内代码块
        html = html.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>');

        // 转换无序列表
        html = html.replace(/^\s*-\s(.*$)/gim, '<li>$1</li>');
        html = html.replace(/(<li>.*<\/li>)/gs, '<ul>$1</ul>');

        // 转换有序列表
        html = html.replace(/^\s*\d+\.\s(.*$)/gim, '<li>$1</li>');
        html = html.replace(/(<li>.*<\/li>)/gs, '<ol>$1</ol>');

        // 转换引用
        html = html.replace(/^>\s(.*$)/gim, '<blockquote><p>$1</p></blockquote>');

        // 转换水平线
        html = html.replace(/^\*\*\*$/gim, '<hr>');

        // 转换链接
        html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>');

        // 转换图片
        html = html.replace(/!\[([^\]]+)\]\(([^)]+)\)/g, '<img src="$2" alt="$1">');

        // 转换换行
        html = html.replace(/\n/g, '<br>');

        return html;
    }
};

// 添加到全局
window.marked = marked;