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

## User Authentication

When the user signs in, we want to generate a token, save it to Redis, and send it back to be stored for their session.

We check if the user already has the token saved in their browser (we should see it as a Request Header), if so, we confirm it is correct using Redis. If not, we create a new session token.

```
const signinAuthentication = (db, bcrypt) => (req, res) => {
  const { authorization } = req.headers;
  return authorization ? getAuthTokenId(req, res) :
    handleSignin(db, bcrypt, req, res)
    .then(data => {
      return data.id && data.email ? createSession(data) : Promise.reject(data)
    })
    .then(session => res.json(session))
    .catch(err => res.status(400).json(err));
}

const getAuthTokenId = (req, res) => {
  const { authorization } = req.headers;
  return redisClient.get(authorization, (err, reply) => {
    if (err || !reply) {
      return res.status(401).json('Unauthorized');
    }
    return res.json({id: reply});
  });
}

const handleSignin = (db, bcrypt, req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return Promise.reject('incorrect form submission');
  }
  return db.select('email', 'hash').from('login')
    .where('email', '=', email)
    .then(data => {
      const isValid = bcrypt.compareSync(password, data[0].hash);
      if (isValid) {
        return db.select('*').from('users')
          .where('email', '=', email)
          .then(user => user[0])
          .catch(err => Promise.reject('unable to get user'))
      } else {
        return Promise.reject('wrong credentials')
      }
    })
    .catch(err => Promise.reject('wrong credentials'));
}

const createSession = user => {
  const { email, id } = user;
  const token = signToken(email);
  return setToken(token, id)
    .then(() => {
      return { success: 'true', userId: id, token }
    })
    .catch(console.log)
}

const signToken = email => {
  const jwtPayload = { email };
  return jwt.sign(jwtPayload, 'This should be env variable');
}

const setToken = (token, id) => {
  return Promise.resolve(redisClient.set(token, id));
}
```

## Store jwt token for user session

When the user successfully signs in, we save the generated token in either `window.sessionStorage` or `window.localStorage`. We then use the saved token in our future Request Headers to let the backend know we are in the same session, so give us the requested data.

```
fetch('http://localhost:3000/signin', {
    method: 'post',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({
        email: this.state.signInEmail,
        password: this.state.signInPassword
    })
})
.then(response => response.json())
.then(data => {
    if (data && data.success === "true") {
        this.saveAuthTokenInSessions(data.token)
        fetch(`http://localhost:3000/profile/${data.userId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': data.token
            }
        })
        .then(response => response.json())
        .then(user => {
            if (user && user.email) {
                this.props.loadUser(user)
                this.props.onRouteChange('home');
            }
        })
        .catch(err => console.log(err))
    }
})
.catch(console.log)

saveAuthTokenInSessions = (token) => {
    window.sessionStorage.setItem('token', token);
}
```

## Authorization middleware

We then add a middleware for routes on the backend we only want signed-in users to access.

We confirm that the Authorization header is sent in the request, then we confirm Redis has their token saved.

```
const requireAuth = (req, res, next) => {
  const { authorization } = req.headers;
  if (!authorization) {
    return res.status(401).send('Unauthorized');
  }
  return redisClient.get(authorization, (err, reply) => {
    if (err || !reply) {
      return res.status(401).send('Unauthorized');
    }
    return next();
  });
};
```

[HTML Entities](https://www.w3schools.com/charsets/ref_html_entities_4.asp)
[Refresh Tokens](https://auth0.com/blog/refresh-tokens-what-are-they-and-when-to-use-them/)
['Bearer' inside Authorization Header](https://security.stackexchange.com/questions/108662/why-is-bearer-required-before-the-token-in-authorization-header-in-a-http-re)