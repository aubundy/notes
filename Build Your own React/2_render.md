# 2. render function

now to make our own ReactDOM.render function

```
function render(element, container) {
  // TODO create dom nodes
}
​
const Didact = {
  createElement,
  render,
}
​
/** @jsx Didact.createElement */
const element = (
  <div id="foo">
    <a>bar</a>
    <b />
  </div>
)
const container = document.getElementById("root")
Didact.render(element, container)
```

right now we don’t care about updating or deleting, only adding to the DOM

```
function render(element, container) {
  const dom =
    element.type == "TEXT_ELEMENT"
      ? document.createTextNode("")
      : document.createElement(element.type);

const isProperty = key => key !== "children";
  Object.keys(element.props)
    .filter(isProperty)
    .forEach(name => {
      dom[name] = element.props[name]
    });

  element.props.children.forEach(child =>
    render(child, dom)
  );
  container.appendChild(dom);
}
```

create the DOM node, but check for text elements.
Assign element props to the node.
Recursively do the same for each child.
Append new node to container.

Didact now renders JSX to the DOM