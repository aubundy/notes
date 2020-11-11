# Security

Security is like defense in soccer. You don't get any of the glory, and all of the blame. You know you are doing your job when nothing happens

## Injections

1. Sanitize input
    * use `createTextNode` over `innerHTML`
    * don't use `eval()`
    * don't use `document.write()`
2. Parameterize your queries/ Use prepared statements
3. Use ORMs like knex.js

## Third Party Libraries

Trust large libraries.

`npm audit fix` - find outdated packages and update them

[NPM 6 update](https://medium.com/npm-inc/announcing-npm-6-5d0b1799a905)

## Logging

Use logs to get info from your system, user interaction, etc. Time to detect breaches takes about 200 days. Don't be too descriptive, though.

Morgan - use as a logging middleware after bodyParser
Winston - advanced console.log

## HTTPS Everywhere

Use SSL/TLS Certificates on your website to use HTTPS.

[Let's Encrypt](https://letsencrypt.org/)
[Cloudflare](https://www.cloudflare.com/)

## XSS + CSRF

Cross-site scripting occurs when a website uses untrusted data by not using proper validation or escaping. This allows hackers to run JS in a victim's browser. They could do things like steal saved cookie information and use it to log into accounts (session hijacking). Sanitizing inputs/validating inputs prevents these attacks.

Cross-site request forgery occurs when a server and client trust each other, and a hacker forges the request being made to the server. Content Security Policy headers prevent these attacks.

Additional:
* Don't use `eval()`
* Don't use `document.write()`
* Secure and HTTPOnly for cookies

[CSP](https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP)
[HTTPOnly and Secure Cookies](https://developer.mozilla.org/en-US/docs/Web/HTTP/Cookies)
[More on XSS](https://hackernoon.com/cross-site-scripting-for-dummies-be30f76fad09)

## Code Secrets

### Environmental Variables

Use `.env` files and the dotenv package to use API keys and passwords securely

### Commit History

Never commit secrets to GitHub

Check commit history to see if any passwords, API keys, etc. were committed by mistake in the past. If so, update them

## Secure Headers

In express apps, `npm install helmet`. Helmet.js takes care of everything for us.

[HTTP](https://code.tutsplus.com/tutorials/http-the-protocol-every-web-developer-must-know-part-1--net-31177)
[HTTP Headers](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers)
[HTTP Header Fields](https://www.tutorialspoint.com/http/http_header_fields.htm)
[Helmet Docs](https://github.com/helmetjs/helmet)