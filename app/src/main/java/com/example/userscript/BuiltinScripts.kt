package com.example.userscript

import com.example.data.local.UserscriptEntity

object BuiltinScripts {
    fun getPreloadedScripts(): List<UserscriptEntity> {
        return listOf(
            UserscriptEntity(
                id = 10,
                name = "Google 网页智能翻译 (Google Translate Extension)",
                description = "谷歌官方网页即时多语言翻译扩展，自动检测外语网页，支持在网页内直接一键翻译为简体中文或恢复原文",
                version = "2.1",
                author = "Google Translate Team / LeftTab",
                matchPatterns = "*://*/*",
                runAt = "document-end",
                code = """
(function() {
    if (window.__lefttab_translate_loaded) return;
    window.__lefttab_translate_loaded = true;
    
    window.googleTranslateElementInit = function() {
        try {
            new google.translate.TranslateElement({
                pageLanguage: 'auto',
                includedLanguages: 'zh-CN,en,ja,ko,fr,de,es,ru',
                layout: google.translate.TranslateElement.InlineLayout.SIMPLE,
                autoDisplay: false
            }, 'google_translate_element_box');
        } catch(e){}
    };
    
    // Create floating translate button
    const container = document.createElement('div');
    container.id = 'google_translate_element_box';
    container.style.cssText = 'position:fixed;bottom:20px;left:20px;z-index:999999;background:rgba(255,255,255,0.95);border:1px solid #1a73e8;border-radius:24px;padding:4px 10px;box-shadow:0 4px 14px rgba(0,0,0,0.18);display:flex;align-items:center;font-size:12px;color:#1a73e8;cursor:pointer;';
    
    const label = document.createElement('span');
    label.innerText = '🌐 译为中文';
    label.style.fontWeight = 'bold';
    label.onclick = function() {
        const select = document.querySelector('.goog-te-combo');
        if (select) {
            select.value = 'zh-CN';
            select.dispatchEvent(new Event('change'));
        }
    };
    container.appendChild(label);
    
    document.body.appendChild(container);
    
    const script = document.createElement('script');
    script.src = 'https://translate.google.com/translate_a/element.js?cb=googleTranslateElementInit';
    document.head.appendChild(script);
})();
                """.trimIndent(),
                isEnabled = true,
                isBuiltIn = true
            ),
            UserscriptEntity(
                id = 1,
                name = "强制全网夜间模式 (Dark Mode)",
                description = "为所有网页注入深色背景与低对比度保护，夜间阅读更护眼，自动保护图片与视频不被反色",
                version = "1.2",
                author = "LeftTab",
                matchPatterns = "*://*/*",
                runAt = "document-end",
                code = """
(function() {
    if (window.__lefttab_dark_mode_injected) return;
    window.__lefttab_dark_mode_injected = true;
    const style = document.createElement('style');
    style.id = '__lefttab_dark_style';
    style.innerHTML = `
        html {
            background-color: #121212 !important;
            filter: invert(90%) hue-rotate(180deg) !important;
        }
        img, video, canvas, svg, picture, iframe, [style*="background-image"] {
            filter: invert(100%) hue-rotate(180deg) !important;
        }
    `;
    document.documentElement.appendChild(style);
})();
                """.trimIndent(),
                isEnabled = false,
                isBuiltIn = true
            ),
            UserscriptEntity(
                id = 2,
                name = "网页净化与去除APP引流遮罩 (Clean Web)",
                description = "自动隐藏移动端网页底部的'在APP中打开'引流栏、遮罩弹窗以及折叠展开限制",
                version = "1.1",
                author = "LeftTab",
                matchPatterns = "*://*/*",
                runAt = "document-end",
                code = """
(function() {
    const cleanPage = () => {
        // Unfold folded articles
        const folded = document.querySelectorAll('.fold-box, .read-more, .unfold-btn, [class*="fold"], [class*="readmore"]');
        folded.forEach(el => {
            if (el.innerText && (el.innerText.includes('展开') || el.innerText.includes('阅读全文'))) {
                try { el.click(); } catch(e){}
            }
        });
        
        // Remove app download floating bars
        const selectors = [
            '.callup-btn', '.app-open', '.open-app', '.app-btn',
            '[class*="openApp"]', '[class*="open-app"]', '[class*="download-app"]',
            '[id*="openApp"]', '[id*="download-app"]', '.top-banner-app', '.bottom-banner-app'
        ];
        document.querySelectorAll(selectors.join(',')).forEach(el => {
            el.style.display = 'none';
        });
        
        // Restore body scroll if locked by modal
        if (document.body.style.overflow === 'hidden') {
            document.body.style.overflow = 'auto';
        }
    };
    cleanPage();
    setTimeout(cleanPage, 1500);
    setTimeout(cleanPage, 3000);
})();
                """.trimIndent(),
                isEnabled = true,
                isBuiltIn = true
            ),
            UserscriptEntity(
                id = 3,
                name = "解除文字复制与右键限制 (Unblock Copy)",
                description = "解除网页禁止复制、禁止选择文本、禁止右键菜单等烦人限制",
                version = "1.0",
                author = "LeftTab",
                matchPatterns = "*://*/*",
                runAt = "document-end",
                code = """
(function() {
    const enableSelect = () => {
        const doc = document;
        const body = document.body;
        if (!body) return;
        ['copy', 'cut', 'selectstart', 'contextmenu', 'mousedown', 'mouseup'].forEach(event => {
            doc.addEventListener(event, (e) => { e.stopImmediatePropagation(); }, true);
        });
        const style = document.createElement('style');
        style.innerHTML = `* { -webkit-user-select: auto !important; user-select: auto !important; }`;
        document.head.appendChild(style);
    };
    enableSelect();
})();
                """.trimIndent(),
                isEnabled = true,
                isBuiltIn = true
            ),
            UserscriptEntity(
                id = 4,
                name = "HTML5 视频倍速与画中画增强 (Video Tool)",
                description = "为网页中的视频提供快捷浮窗控制：支持 1.25x、1.5x、2.0x、3.0x 倍速及一键画中画(PiP)",
                version = "1.3",
                author = "LeftTab",
                matchPatterns = "*://*/*",
                runAt = "document-end",
                code = """
(function() {
    if (window.__lefttab_video_tool_injected) return;
    window.__lefttab_video_tool_injected = true;
    
    function attachVideoControls() {
        const videos = document.querySelectorAll('video');
        if (!videos.length) return;
        
        if (document.getElementById('__lefttab_video_bar')) return;
        
        const bar = document.createElement('div');
        bar.id = '__lefttab_video_bar';
        bar.style.cssText = 'position:fixed;bottom:24px;right:24px;z-index:999999;background:rgba(15,23,42,0.85);backdrop-filter:blur(8px);border:1px solid #38BDF8;border-radius:24px;padding:6px 12px;display:flex;align-items:center;gap:8px;font-family:sans-serif;font-size:12px;color:#F8FAFC;box-shadow:0 4px 16px rgba(0,0,0,0.4);';
        
        const label = document.createElement('span');
        label.innerText = '🎬 视频倍速:';
        label.style.fontWeight = 'bold';
        label.style.color = '#38BDF8';
        bar.appendChild(label);
        
        const speeds = [1.0, 1.25, 1.5, 2.0, 3.0];
        speeds.forEach(speed => {
            const btn = document.createElement('button');
            btn.innerText = speed + 'x';
            btn.style.cssText = 'background:#1E293B;border:1px solid #334155;color:#E2E8F0;border-radius:12px;padding:4px 8px;cursor:pointer;font-size:11px;';
            btn.onclick = () => {
                document.querySelectorAll('video').forEach(v => { v.playbackRate = speed; });
                document.querySelectorAll('#__lefttab_video_bar button').forEach(b => b.style.borderColor = '#334155');
                btn.style.borderColor = '#38BDF8';
                btn.style.background = '#0284C7';
            };
            bar.appendChild(btn);
        });
        
        const pipBtn = document.createElement('button');
        pipBtn.innerText = '画中画';
        pipBtn.style.cssText = 'background:#0284C7;border:none;color:#FFF;border-radius:12px;padding:4px 8px;cursor:pointer;font-size:11px;';
        pipBtn.onclick = () => {
            const v = document.querySelector('video');
            if (v && v.requestPictureInPicture) {
                v.requestPictureInPicture().catch(() => {});
            }
        };
        bar.appendChild(pipBtn);
        
        const closeBtn = document.createElement('span');
        closeBtn.innerText = '✕';
        closeBtn.style.cssText = 'cursor:pointer;padding:2px 4px;color:#94A3B8;';
        closeBtn.onclick = () => { bar.remove(); };
        bar.appendChild(closeBtn);
        
        document.body.appendChild(bar);
    }
    
    setTimeout(attachVideoControls, 2000);
    setInterval(attachVideoControls, 5000);
})();
                """.trimIndent(),
                isEnabled = true,
                isBuiltIn = true
            ),
            UserscriptEntity(
                id = 5,
                name = "平板宽屏自适应铺满 (Tablet Layout Fit)",
                description = "消除部分移动端网页在平板上被固定为狭窄手机宽度的缺陷，自动居中并铺满内容区域",
                version = "1.0",
                author = "LeftTab",
                matchPatterns = "*://*/*",
                runAt = "document-end",
                code = """
(function() {
    const style = document.createElement('style');
    style.innerHTML = `
        body { max-width: 100% !important; margin: 0 auto !important; }
        .wrapper, .container, #wrapper, #container, .main-container {
            max-width: 100% !important;
            width: 100% !important;
        }
    `;
    document.head.appendChild(style);
})();
                """.trimIndent(),
                isEnabled = false,
                isBuiltIn = true
            )
        )
    }
}
