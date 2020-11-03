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

The resource specified by the `src` attribute should be large enough to work with all device sizes. `srcset` allows you to specify the image size of the different sources available. The `sizes` attribute tells the browser how wide the image will be when it is displayed.

[More on image file types](https://99designs.com/blog/tips/image-file-types/)

[More on images and page speed](https://pageweight.imgix.com/)

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

## Optimizing Code

- Only load what's needed (use code splitting and [tree shaking](https://developers.google.com/web/fundamentals/performance/optimizing-javascript/tree-shaking/))
- Avoid blocking main thread
- Avoid memory leaks
- Avoid multiple rerendering

We can use Chrome DevTools -> Performance tab to analyze the parsing and compiling of our code. This tool will give us a timelime of the code that runs on our page.

- Green line is first paint.
- Blue line is DOMContentLoaded event
- Dark Green line is first contentful paint
- Red line is load event (clean up event listeners)
- Black line is largest contentful paint

The Summary tab gives us a circle chart showing the time to load scripts, run scripts (parse and compile), render content, and paint content.

The Bottom-Up tab lets us sort and filter our scripts to find the longest ones.

Limit render-blocking code by analyzing your code using the devtools. Consider adding a performance budget to your JS code.

We want fast time to first paints and fast time to interactive.

### Code Splitting

Since HTTP/2 increases network request speeds, we no longer need to ship only one JS file. The main limiter is now processing the JS after it is downloaded. Since users don't need the entire webpage/webapp code all at the same time, we can split up the code and deliver as needed. This will reduce the amount of work during execution.

We want to ship a minimally functional page, and as more resources arrive, we can lazy load the rest in the background.

With CRA, we can use `import` inside of our code, and it will know to delay loading that code until it is needed. This is useful for route-based code splitting.

```
onRouteChange = (route) => {
  if (route === 'page1') {
    this.setState({route: route});
  } else if (route === 'page2') {
    import('./components/Page2).then(Page2 => {
      this.setState({route: route, component: Page2.default});
    });
  } else if (route === 'page3') {
    import('./components/Page3).then(Page3 => {
      this.setState({route: route, component: Page3.default});
    });
  }
}

...

if (this.state.route === 'page1') {
  return <Page1 onRouteChange={this.onRouteChange} />
} else {
  return <this.state.component onRouteChange={this.onRouteChange} />
}
```

We can check the Network tab in Devtools and see bundle.js vs. chunk.js. After `npm run build`, we can see file sizes after gzip show the size we were able to take off the main.js file.

Using AsyncComponent for route-based code splitting:

```
import React, { Component } from "react";

export default function asyncComponent(importComponent) {
  class AsyncComponent extends Component {
    constructor(props) {
      super(props);
      this.state = {
        component: null
      };
    }

    async componentDidMount() {
      const { default: component } = await importComponent();

      this.setState({
        component: component
      });
    }

    render() {
      const Component = this.state.component;

      return Component ? <Component {...this.props} /> : null;
    }
  }

  return AsyncComponent;
}

...

import AsyncComponent from './AsyncComponent';

...

if (this.state.route === 'page1') {
  return <Page1 onRouteChange={this.onRouteChange} />
} else if (this.state.route === 'page2') {
  const AsyncPage2 = AsyncComponent(() => import("./Components/Page2"));
  return <AsyncPage2 onRouteChange={this.onRouteChange} />
} else {
  const AsyncPage3 = AsyncComponent(() => import("./Components/Page3"));
  return <AsyncPage3 onRouteChange={this.onRouteChange} />
}
```

Using [React.lazy()](https://reactjs.org/docs/code-splitting.html):

```
const Page2Lazy = React.lazy(() => import('./Components/Page2'));
const Page3Lazy = React.lazy(() => import('./Components/Page3'));

...
    if (this.state.route === 'page1') {
      return <Page1 onRouteChange={this.onRouteChange} />
    } else if (this.state.route === 'page2') {
      return (
        <Suspense fallback={<div>Loading...</div>}>
          <Page2Lazy onRouteChange={this.onRouteChange} />
        </Suspense>
      );
    } else {
      return (
        <Suspense fallback={<div>Loading...</div>}>
          <Page3Lazy onRouteChange={this.onRouteChange} />
        </Suspense>
      );
    }
```

### React Performance

localhost:3000/?react_perf will allow us to analyze our component behavior in the Performance tab in devtools. Redux helps make sure only the components that receive updates will rerender. Analyze the 'reverse Christmas trees' to see the render-blocking code.

[React Developer Tools](https://chrome.google.com/webstore/detail/react-developer-tools/fmkadmapgofadopljbjfkapdkoienihi?hl=en) is also helpful in monitoring performance. You can view changes to your component tree as you use your app. Use ~~"Highlight Updates"~~ Profiler -> Make sure to click "Record why each component rendered while profiling". You'll be able to see when each component rendered and make any necessary changes as a result. This is similar to [why did you render?](https://www.npmjs.com/package/@welldone-software/why-did-you-render).

use `shouldComponentUpdate()`, but use it cautiously since it will be run every time before `render()`

```
shouldComponentUpdate(nextProps, nextState) {
  if (this.props.property !== nextProps) {
    return true;
  }
  return false;
}
```

use `PureComponent` for components that are stateless and you only want to change if their props change. But do not use them for deeply nested object props.

[More info on setState()](https://medium.com/@wereHamster/beware-react-setstate-is-asynchronous-ce87ef1a9cf3)
[More more infor on setState()](https://vasanthk.gitbooks.io/react-bits/content/patterns/19.async-nature-of-setState.html)

## Progressive Web Apps

PWAs help web pages behave more like native apps on mobile devices

[Publishing PWA on 3 app stores](http://debuggerdotbreak.judahgabriel.com/2018/04/13/i-built-a-pwa-and-published-it-in-3-app-stores-heres-what-i-learned/)
[PWAs in 2020](https://firt.dev/pwa-2020/)
[Top PWAs](https://appsco.pe/)

PWA capability will constantly be changing and improving. [Here's](https://web.dev/pwa-checklist/) an updated checklist for making one.

Three most important parts:

1. HTTPS
- Use [Let's Encrypt](https://letsencrypt.org/) to get a free TLS certificate.
- [Cloudflare](https://www.cloudflare.com/) also hosts websites and gives free HTTPS.
- [Github Pages](https://pages.github.com/) works really well for simple websites.
2. App Manifest
- Make sure to have a `<meta name="viewport">` tage in your html file.
- Use a `manifest.json` file to give instructions for how the app should appear to the user where they would expect to see apps.
- Use [realfavicongenerator](https://realfavicongenerator.net/) to generate different sized favicons for mobile devices and to create a splash screen.
3. Service Worker
Service workers will work in the background off the main thread. This allows for offline experiences.
- CRA initiates your app with a serviceWorker.js file, you just need to register it.
- The Service worker acts as a network proxy once its registered. It intercepts any requests to the network and checks to see if you need to communicate with the network, because you may already have those files. The service worker will then access the Cache API.
- Use [isServiceWorkerReady?](https://jakearchibald.github.io/isserviceworkerready/) to check status of PWA availability across browsers.
- [PWAs can even give push notifications on mobile devices](https://auth0.com/blog/introduction-to-progressive-web-apps-push-notifications-part-3/)
- [Service workers are not accessible in dev mode](https://github.com/facebook/create-react-app/issues/2396)

[More on accessibility](https://www.w3.org/standards/webdesign/accessibility)
[Progressive Tooling](https://progressivetooling.com/)

## CDNs

Caches files in servers around the world for quicker delivery to the user through decreased latency. They also offer security benefits.

[Cloudflare](https://www.cloudflare.com/) is most popular. You just need to update your DNS name server to use Cloudflare.

Amazon (Cloudfront) and Microsoft (Azure) have popular CDNs as well.

## GZIP

Gzip compression of code files is one of the best ways to improve website performance. And by default, all modern browsers allow gzipping. Webpack compresses files using gzip.

In an express app, just install the `compression` middleware. You can check the content encoding method in the Network tab.

```
const compression = require("compression");

app.use(compression());
```

[Brotli](https://github.com/google/brotli) is a newer compression algorithm that is roughly 20% better than gzip, but it doesn't have 100% adoption rate yet.

## Database Scaling

1. Identify inefficient queries
  * Only request what you need
  * Index your tables to improve queries. It sorts your tables based on the fields you decide on. This does require more memory.
2. Increase memory
3. Vertical scaling (Redis, Memcached, etc.)
  * store a cache of regularly accessed data
4. Sharding
  * Separate out your databases
5. More databases
6. Database type

## Caching

Temporary storage area for our data for faster access. Caching happens everywhere from our personal computer's CPU to browsers and CDNs.

PWAs use the browser's cache storage.

Express automatically sends Response Headers (Cache Control and ETag) to help the browser set a cache.

[Caching Everywhere](https://www.freecodecamp.org/news/the-hidden-components-of-web-caching-970854fe2c49/)
[Cache Headers](https://web.dev/http-cache/)
[Caching and Performance](https://devcenter.heroku.com/articles/increasing-application-performance-with-http-cache-headers)

## Load Balancing

Load balancing is used to distribute server requests among multiple servers.

nginx can be used as an effective load balancer/reverse proxy.