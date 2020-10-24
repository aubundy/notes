# Server Side Rendering

## CSR

1. Longer initial pageload
2. Low SEO potential
4. Rich interactions
5. Faster response after pageload
6. Web Applications

## SSR

1. Best for static sites
2. High SEO potential
3. Great initial page load
4. Needs full page reloads
5. Slower page rendering - rendering on server is synchronous. All code pauses until the rendering is complete.
6. Lots of server requests


## React SSR

You need React on both client and server side in order to build Virtual DOM.

Use react-dom/server and use `ReactDOMServer.renderToString()`* to send to client and use `ReactDOM.hydrate()` to preserve markup and then attach event handlers to component (because you need the `window` object to use event handlers).

*or `ReactDOMServer.renderToNodeStream()`

React was originally built for client-side use, so there can be a lot of bugs or difficulties when switching to SSR. Next.js and Gatsby can help with this.

### Libraries

[Gatsby](https://www.gatsbyjs.com/) is great for documentation sites/static sites. React uses this for their documentation.

[Next.js](https://nextjs.org/) is great for web applications. It's like CRA for SSR websites.
