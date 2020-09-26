# Performance

3 ways to improve performance:
- improve client side loading speed
  - critical render path
  - optimized code
  - PWA
- improve transfer of files over the internet
  - minimize files
  - minimize delivery
- improve backend processing
  - CDN
  - caching
  - load balancing
  - DB scaling
  - gzip

Since we download all of the files of a webpage from a server, the more KBs the files have, the longer it takes to download.

## Minimize files

minimize html, css, and js files - remove whitespace, etc. so the machine reads the code faster. Webpack helps minimize files as part of its build process.

## Minimize images

4 main image file types
- jpeg - usually used for complex photos. Provides a lot of **colors**. Cannot have transparent background. Reduce with [JPEG optimizer](http://www.jpeg-optimizer.com/). Lower jpeg quality by 30-60% (use Macbook preview settings).
- gif - reduces color options to save on file size. Useful for **animations**
- png - also reduces color options. Useful for smaller logos and images with **transparent** backgrounds. Can be reduced with [TinyPNG](https://tinypng.com/)
- svg - vector graphics. Useful for **icons** and **logos**.

- Try to use simple illustrations over highly detailed photos.
- Resize image based on how it will be displayed (don't need 1,500px image if it is only 500px wide on the screen)
- Different sized images for different backgrounds with media queries.
- Use image CDNs like [imigx](https://www.imgix.com/).
- Remove image metadata.

### New Image Formats

There are [new file types for images](https://web.dev/uses-webp-images/?utm_source=lighthouse&utm_medium=unknown), but there isn't complete browser support yet. JPEG 2000, JPEG XR, and WebP have superior compression and quality compared to older image formats.[WebP works on Chrome and Opera](https://caniuse.com/webp).

### [Lazy-loading Images](https://web.dev/browser-level-image-lazy-loading/)

Lazy-loading can delay the loading of images until they are near the viewport. It is supported by many browsers, and the ones that do not support it, simplay ignore it.

### [Image CDNs](https://web.dev/image-cdns/)

Image CDNs can yield a 40-80% savings in file size. They create new versions of images as they are needed. They are usually better suited for creating images that are heavily customized for individual clients. Images are usually customized via url provided by the CDN. It also provides transformations and security keys.

### Serve Responsive Images

Serve different image size depending on device type. It is common to serve 3-5 different versions of an image. The `<img>` tag's `src`, `srcset`, and `sizes` attributes all interact to achieve this end result. 

The resource specified by the `src` attribute should be large enough to work with all device sizes.

[More on image file types](https://99designs.com/blog/tips/image-file-types/)
[More on images and page speec](https://pageweight.imgix.com/)
[More on selecting image file type](https://www.sitepoint.com/gif-png-jpg-which-one-to-use/)

## Optimize file transfer

- reconsider each framework that is used and the size it adds to your files
- bundle files together

[More on max connections in a browers](https://stackoverflow.com/questions/985431/max-parallel-http-connections-in-a-browser)

## Critical Path

Client browser fetches files from a server. It then begins putting together the DOM based on the HTML file. When it comes across a .css file, it puts together the CSSOM. Whenever it comes across a .js file, it makes the necessary changes to the DOM and CSSOM. Using all three types of files, it puts together the render tree. It uses this render tree to put together the layout and then paint to the screen.

1. HTML file
  - We want .css files to arrive as early as possible and .js files to arrive as late as possible.
  - Put `style` tags in `head` and `script` tags at the bottom of `body`.
2. CSS file
  - Only load whatever is needed. Use inline CSS or internal css in a `style` tag for above the fold needs. Fewer files needed to be loaded.
  - Use 'above the fold' loading (check Network tab in Dev Tools. Below the fold files should load after Waterfall red line).
```
  <script>
    const loadStyleSheet = (src) =>{
        if (document.createStyleSheet) {
          document.createStyleSheet(src);
        }
        else {
          const stylesheet = document.createElement('link');
          stylesheet.href = src;
          stylesheet.rel = 'stylesheet';
          stylesheet.type = 'text/css';
          document.getElementsByTagName('head')[0].appendChild(stylesheet);
        }
    }
      // All of the objects are in the DOM, and all the images, scripts, links have finished loading.
      window.onload = function () {
        console.log('window done!')
        loadStyleSheet('./style3.css');
      };
  </script>
```
  - Use Media queries to download in background if not correct screen type.
```
<link rel="stylesheet" href="./style2.css" media="only screen and (min-width:501px)">
```
  - Less specificity in css
```
/* bad */
.header .nav .item .link a.important {
  color: pink;
}

/* good */
a.important {
  color: pink;
}
```
3. JS - Once a script file is detected, DOM construction is paused. JS is parser blocking.
  - Load scripts asynchronously. We can use `async` or `defer` inside `script` tags to avoid pausing parsing while the script is being downloaded. Add `async` to anything that doesn't affect the DOM or CSSOM (like tracking or analytical scripts). May change on the browser.
  - Defer loading of scripts. We can use `defer` inside `script` tags to avoid pausing parsing while the script is being downloaded and delay the execution. This is good if the core functionality does not require JS. May change on the browser.
  - Minimize DOM manipulation. Use `DOMContentLoaded` and `load` event listeners.
  - Avoid long running JavaScript. Will be covered more in depth in part 2.

[More info on async and defer](https://stackoverflow.com/questions/10808109/script-tag-async-defer)

4. `DOMContentLoaded` -> Render -> Layout -> Paint -> `Load`
5. JS Events cause a rerender followed by new layout and repainting. This is why we limit DOM manipulation.

## Testing Performance

[PageSpeed Insights](https://developers.google.com/speed/pagespeed/insights/)
[WebPageTest](https://www.webpagetest.org/)

## [Prefetching, Preloading, Prebrowsing](https://css-tricks.com/prefetching-preloading-prebrowsing/)

## More performance tools

- [View main thread activities in a table](https://developers.google.com/web/tools/chrome-devtools/evaluate-performance/reference#activities)
- [View main thread activities in realtime](https://developers.google.com/web/tools/chrome-devtools/evaluate-performance/reference#main)
- [Analyze animation FPS](https://developers.google.com/web/tools/chrome-devtools/evaluate-performance/reference#fps)
- [Learn to use the Performance Monitor](https://developers.google.com/web/updates/2017/11/devtools-release-notes#perf-monitor)
- [Capture screenshots of performance tests](https://developers.google.com/web/tools/chrome-devtools/evaluate-performance/reference#screenshots)
- [View interactions](https://developers.google.com/web/tools/chrome-devtools/evaluate-performance/reference#interactions)
- [Find scroll performance issues](https://developers.google.com/web/tools/chrome-devtools/evaluate-performance/reference#scrolling-performance-issues)
- [View paint events in realtime](https://developers.google.com/web/tools/chrome-devtools/evaluate-performance/reference#paint-flashing)

## HTTP/2 (more in Part 2)

Protocol update that is still compatible with HTTP, but seeks to improve network latency. This will change some of our optimization techniques. Bundling files may not be faster since server requests will be faster.

[Learn more](https://developers.google.com/web/fundamentals/performance/http2/)
[Learn about HTTP/3](https://blog.cloudflare.com/http3-the-past-present-and-future/)