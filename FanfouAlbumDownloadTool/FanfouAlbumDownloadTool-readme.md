# 使用场景

下载饭否用户的相册。

# 使用方法

假设要下载饭否用户“王兴”的相册

一、在 Chrome 浏览器中，打开该用户的相册的页面，比如进入 https://fanfou.com/album/wangxing 的页面。

二、点击 `F12` 打开 DevTools，选择 **Network** 选项卡，然后刷新刚刚的打开的相册页面。

三、点击 Name 为 **wangxing** 的 request，在 Headers 的 Request Headers 中，找到 **cookie** 

四、在 **cookie** 那一行，使用右键，选择 **Copy value**

五、记录下刚刚 copy 的 value，以及相册网页链接 https://fanfou.com/album/wangxing

六、运行 `FanfouAlbumDownloadTool` ，根据提示，输入相册的网页链接、需要下载的相册页数和刚刚复制的 cookie，即可自动下载

