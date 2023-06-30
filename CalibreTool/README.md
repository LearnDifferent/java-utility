# CalibreTool

## 使用背景

由于 Kindle 退出中国市场，以前购买和上传的电子书就需要重新下载下来。我使用的是 [Kindle_download_helper](https://github.com/LearnDifferent/Kindle_download_helper) 、[DeDRM_tools](https://github.com/LearnDifferent/DeDRM_tools) 和 [Calibre](https://www.calibre-ebook.com/zh_CN) 来下载并转换存放在 Amazon 的电子书。

> 下载和转换电子书可以参考 [yihong0618/Kindle_download_helper 的 Issues](https://github.com/yihong0618/Kindle_download_helper/issues) 和 [使用 Calibre 移除 Kindle 电子书的 DRM](https://divineengine.net/article/how-to-remove-drm-from-kindle-books-with-calibre/)

在下载并转换后，Calibre 的路径对中文不友好（具体表现为将中文变为了拼音），且路径层级多，想提取文件比较繁琐。

CalibreTool 可以将后缀为 .mobi 和 .epub 的文件转为正确的名称，并将它们提取到一个文件夹中。

## 使用方法

先使用 `javac` 命令将 [CalibreTool.java](./src/CalibreTool.java) 编译为 `CalibreTool.class` ，然后将 `CalibreTool.class` 移动到 Calibre 存放书籍的路径下，最后使用 `java CalibreTool` 执行程序。

新生成的 `books_output` 路径存放最终的电子书，新生成的 `calibre_msg_*.txt` 则会记录执行信息。