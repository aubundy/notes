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