# 5. Render and Commit Phases

since we add a new node to the DOM every time we work on an element, the user will see an incomplete UI if the browser interrupts our work before rendering. We need to remove the part of `performUnitOfWork` that mutates the DOM

```
function render(element, container) {
  wipRoot = {
    dom: container,
    props: {
      children: [element],
    },
  }
  nextUnitOfWork = wipRoot
}
​
let nextUnitOfWork = null
let wipRoot = null
```

keep track of the ‘work in progress’ fiber tree root

```
function commitRoot() {
  commitWork(wipRoot.child)

  wipRoot = null
}
​
function commitWork(fiber) {
  if (!fiber) {
    return
  }
  const domParent = fiber.parent.dom
  domParent.appendChild(fiber.dom)
  commitWork(fiber.child)
  commitWork(fiber.sibling)
}

function workLoop(deadline) {
  let shouldYield = false
  while (nextUnitOfWork && !shouldYield) {
    nextUnitOfWork = performUnitOfWork(
      nextUnitOfWork
    )
    shouldYield = deadline.timeRemaining() < 1
  }
​
  if (!nextUnitOfWork && wipRoot) {
    commitRoot()
  }
​
  requestIdleCallback(workLoop)
}
```

once we finish the work, we commit the whole fiber tree to the DOM in the `commitRoot` function.