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

There are new file types for images, but there isn't complete browser support yet.

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



