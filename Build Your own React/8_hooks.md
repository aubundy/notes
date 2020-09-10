# 8. Hooks

Last step! Since we have function components, we can now add state.

Let’s change our example to a classic counter component. We will use `Didact.useState` to get and update the counter value

```
  render,
  useState,
}
​
/** @jsx Didact.createElement */
function Counter() {
  const [state, setState] = Didact.useState(1)
  return (
    <h1 onClick={() => setState(c => c + 1)}>
      Count: {state}
    </h1>
  )
}
const element = <Counter />
const container = document.getElementById("root")
Didact.render(element, container)
```
```
let wipFiber = null
let hookIndex = null
​
function updateFunctionComponent(fiber) {
  wipFiber = fiber
  hookIndex = 0
  wipFiber.hooks = []
  const children = [fiber.type(fiber.props)]
  reconcileChildren(fiber, children)
}
​
function useState(initial) {
    const oldHook =
    wipFiber.alternate &&
    wipFiber.alternate.hooks &&
    wipFiber.alternate.hooks[hookIndex]
  const hook = {
    state: oldHook ? oldHook.state : initial,
    queue: [],
  }

  const actions = oldHook ? oldHook.queue : []
  actions.forEach(action => {
    hook.state = action(hook.state)
  })

  const setState = action => {
    hook.queue.push(action)
    wipRoot = {
      dom: currentRoot.dom,
      props: currentRoot.props,
      alternate: currentRoot,
    }
    nextUnitOfWork = wipRoot
    deletions = []
  }
​
  wipFiber.hooks.push(hook)
  hookIndex++
  return [hook.state, setState]

}
```
Let’s initialize a few global variables so we can use them inside the `useState` function. First set the work in progress fiber, then add a `hooks` array to support multiple calls to `useState` in a row, and keep track of hooks index.

in `useState`, check if we have an old hook (check the `alternate` of the `wipFiber` for hooks with the index). If yes, copy old hook into new hook state. If not, initialize the state. Then add the hook to the fiber, increment hook index by one, and return the state.

`useState` will also return a function to update state. So `setState` receives an action, then we push that action to the queue we added to the hook. Then set a new work in progress root (like in `render`), so the work loop can start a new render phase.

Then we run the action the next time we are rendering the component. We get all of the old hook actions and apply them one by one to the new hook so the state is updated when returned.