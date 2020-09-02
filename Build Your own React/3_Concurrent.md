# 3. Concurrent Mode

we need a refactor because our recursive render call will block the thread if there’s too many children.

```
let nextUnitOfWork = null
​
function workLoop(deadline) {
  let shouldYield = false
  while (nextUnitOfWork && !shouldYield) {
    nextUnitOfWork = performUnitOfWork(
      nextUnitOfWork
    )
    shouldYield = deadline.timeRemaining() < 1
  }
  requestIdleCallback(workLoop)
}
​
requestIdleCallback(workLoop)
​
function performUnitOfWork(nextUnitOfWork) {
  // TODO
}
```

break up the work and let the browser insert higher priority items in-between if needed 
requestIdleCallback runs a loop when the main thread is idle (React uses a scheduler package now, but it is conceptually the same). It also gives us a time until the browser needs control again

we’ll set a unitOfWork by calling performUnitOfWork which will perform the work and call the next unit