# Weverse Translator v0.2

Android 个人侧载 MVP。通过 OpenAI Sign in with ChatGPT 的开源客户端流程，让符合条件的 ChatGPT Plus / Pro 账户授权应用使用套餐额度完成 Responses API 翻译。

流程：Continue with ChatGPT -> 浏览器 OAuth + PKCE -> 本机 127.0.0.1 回调 -> 保存 OAuth 凭据 -> AccessibilityService 读取 Weverse 韩文 -> Responses API 流式翻译 -> Accessibility Overlay 显示中文。

不需要 API Key。Responses 请求固定 `store:false`、`stream:true`。
