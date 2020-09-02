# 0. Review

    const element = <h1 title="foo">Hello</h1>;
    const container = document.getElementById("root");
    ReactDOM.render(element, container);

Define an element, grab a node, then render it. Let’s replace this with vanilla JS. 

Element

replace code inside tags with `createElement(tag name, props, children)`. Babel transforms this for us

```
const element = React.createElement(
  "h1",
  { title: "foo" },
  "Hello"
);
```

is the same as 

```
const element = {
  type: "h1",
  props: {
    title: "foo",
    children: "Hello",
  },
}
```

`type` - string that specifies type of DOM node to create
`props` - object with all keys and values from element along with the children property - string/array with more elements

Render

Where react changes the DOM

```
const node = document.createElement(element.type)
node["title"] = element.props.title
```

Create node from element type then assign element props to that node

```
const text = document.createTextNode("")
text["nodeValue"] = element.props.children
```

Then create nodes for the children

```
node.appendChild(text)
container.appendChild(node)
```
Finally we append textNode to h1 and h1 to the container

Same app as before but without React!