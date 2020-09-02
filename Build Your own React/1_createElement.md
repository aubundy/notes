# 1. createElement function

```
const element = (
  <div id="foo">
    <a>bar</a>
    <b />
  </div>
);

const container = document.getElementById("root");
ReactDOM.render(element, container);
```

Now we’ll replace React code with our own version. 

```
const element = React.createElement(
  "div",
  { id: "foo" },
  React.createElement("a", null, "bar"),
  React.createElement("b")
);
```

Replace JSX with JS

Our function only needs to be able to return an object with type and props

```
function createElement(type, props, ...children) {
  return {
    type,
    props: {
      ...props,
      children,
    },
  }
}
```

rest parameter on children makes sure children is always an array

```
children: children.map(child =>
        typeof child === "object"
          ? child
          : createTextElement(child)
      ),

function createTextElement(text) {
  return {
    type: "TEXT_ELEMENT",
    props: {
      nodeValue: text,
      children: [],
    },
  }
}
```

children could also contain primitive values, so we will wrap what isn’t an object in its own element and create a special type for them.
React doesn’t do this, but we want simple code, not performant.

```
const Didact = {
  createElement,
}
​
const element = Didact.createElement(
  "div",
  { id: "foo" },
  Didact.createElement("a", null, "bar"),
  Didact.createElement("b")
);
```

change name to Didact

```
/** @jsx Didact.createElement */
const element = (
  <div id="foo">
    <a>bar</a>
    <b />
  </div>
)
```

this tells babel to continue to use JSX, but use our function to do it