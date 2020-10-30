# Sessions and JWT

## Cookies vs. Tokens

### Cookies

Considered stateful authentication - both browser and server store cookie

1. Browser sends authentication POST request to server
2. Server responds with cookie string
3. Browser uses that string in header for future API calls
4. Server confirms that the browser is using the correct cookie

### Modern Tokens

Considered stateless authentication - server does not have to store token to validate it

Tokens can also be used for browser and mobile

1. Browser sends authentication POST request to server
2. Server responds with token
3. Browser uses that token in header for future API calls
4. Server confirms that the browser is using correct token

[Cookies vs. Tokens Guide](https://dzone.com/articles/cookies-vs-tokens-the-definitive-guide)
[More on cookies vs. tokens](https://stackoverflow.com/questions/17000835/token-authentication-vs-cookies)
[Why JWT sucks](https://scotch.io/bar-talk/why-jwts-suck-as-session-tokens) 