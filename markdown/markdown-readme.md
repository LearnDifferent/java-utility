# 使用场景

Markdown 可以使用 `<span>` 标签，所以只要在 `<span>` 标签中加上 `id` 属性，就能用 `[文本](#id)` 的格式去链接到该 `<span>` 标签。

这个 [MarkdownTool](./MarkdownTool.java) 工具，可以根据文本，生成符合格式的链接内容。

在程序结束后，会自动在当前路径下的 *markdown_temp* 目录（如果没有该目录，会自动创建）中，生成 Log 文件以保存输出的记录。
