# Bundlers

[Webpack](https://webpack.js.org/) is the most used, but [Parcel](https://parceljs.org/) and [Rollup](https://rollupjs.org/guide/en/) are also good. Parcel doesn't require any configuration and is good if you're building your own project. Rollup is very good at tree shaking and is good for creating your own npm package.

Configuration and bundling best practices are constantly being updated. CRA has really good configuration with webpack.

## Webpack

1. Entry - usually a JS file (index.js). Answers the question "Where does webpack enter your project?" so that we know what it should output.
2. Output - webpack will usually output into a build folder the final static files.
3. Loaders - these compile and transpile your code (Babel, ESLint, etc.).
4. Plugins - play a vital role in outputting your code. Different plugins will be used depending on what needs to be outputted.

### Setting up

`npm install --save-dev webpack webpack-dev-server webpack-cli`

It will feel overwhelming setting up all the packages at first. This is why CRA is so helpful for React apps.

```
"scripts": {
    "start": "webpack-dev-server --config ./webpack.config.js --mode development"
}
```

Use webpack-dev-server with the webpack.config.js configuration file inside the development mode (webpack 4 feature).

Inside webpack.config.js - 

```
module.exports = {
    entry: [
        './src/index.js' // path to entry file 
    ],
    output: {
        path: __dirname + '/dist', // how to navigate to output location (index.html)
        publicPath: '/', // url to output directory relative to html file
        filename: 'bundle.js' // name of output file
    },
    devServer: {
        contentBase: './dist' // tells local server where to serve file from
    },
    module: {
        rules: [
        {
            test: /\.(js || jsx)$/, // regex to find all files that end with .js or .jsx
            exclude: /node_modules/,
            use: ['babel-loader']
        },
        {
            test: /\.(js || jsx)$/, // regex to find all files that end with .js or .jsx
            exclude: /node_modules/,
            use: ['eslint-loader']
        }
        ]
    },
    resolve: {
        extensions: ['.js', '.jsx'] // when importing files in the project, assume they're .js or .jsx
    }
}
```

### Adding [Babel](https://babeljs.io/)

Transpiles JS into ES2015 JS

`npm install --save-dev babel-core babel-loader babel-preset-env babel-preset-react` - this is deprecated in Babel 7
`npm install --save-dev @babel/core babel-loader @babel/preset-env @babel/preset-react` - Babel 7

`babel-loader` is for use with webpack and `babel-preset-env` will update only the necessary JS based on the browser and browser version.

`babel-preset-stage-*` lets you write experimental JS that hasn't been approved for release yet. This is deprecated in Babel 7.

Inside `package.json` or `.babelrc` - (not Babel 7)

```
"babel": {
    "presets": [
        "env",
        "react"
    ]
}
```

Babel 7 -

```
"babel": {
    "presets": [
        "@babel/preset-env",
        "@babel/preset-react"
    ],
    "plugins": [...] // how to add plugins
}
```

### Adding [ESLint](https://eslint.org/)

Helps you maintain the same JS syntax/style throughout your project. Code "spellchecker". It needs to be configured so it knows what errors you want it to check for.

`npm install --save-dev eslint eslint-loader babel-eslint`

Extras - 

`npm install --save-dev eslint-config-airbnb eslint-plugin-import eslint-plugin-jsx-a11y`

Inside `.eslintrc.json` - (now it needs to be a .js, .yaml, or .json now)

```
{
    parser: "babel-eslint",
    "rules": {
        "no-console": "warn" // example that warns if there's any console.log() statements
    },
    "extends": ["airbnb-base"]
}
```

[ESLint, Prettier, Husky](https://medium.com/swlh/react-js-adding-eslint-with-prettier-husky-git-hook-480ad39e65e9)
[Helpful webpack config tool](https://createapp.dev/)

## Parcel

Saves a lot of time by having zero-configuration.

`npm install --save-dev parcel-bundler @babel/preset-env @babel/preset-react`

```
"scripts": {
    "start": "parcel index.html"
}
```

```
"babel": {
    "presets": [
        "@babel/preset-env",
        "@babel/preset-react"
    ]
}
```