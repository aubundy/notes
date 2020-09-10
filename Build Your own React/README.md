# [Build Your own React](https://pomb.us/build-your-own-react/)

Notes and code from Build Your own React by Rodrigo Pombo. 

---

Below is the epilogue to his post:

Besides helping you understand how React works, one of the goals of this post is to make it easier for you to dive deeper in the React codebase. That’s why we used the same variable and function names almost everywhere.

For example, if you add a breakpoint in one of your function components in a real React app, the call stack should show you:

`workLoop`
`performUnitOfWork`
`updateFunctionComponent`

We didn’t include a lot of React features and optimizations. For example, these are a few things that React does differently:

In Didact, we are walking the whole tree during the render phase. React instead follows some hints and heuristics to skip entire sub-trees where nothing changed.
We are also walking the whole tree in the commit phase. React keeps a linked list with just the fibers that have effects and only visit those fibers.
Every time we build a new work in progress tree, we create new objects for each fiber. React recycles the fibers from the previous trees.
When Didact receives a new update during the render phase, it throws away the work in progress tree and starts again from the root. React tags each update with an expiration timestamp and uses it to decide which update has a higher priority.
And many more…
There are also a few features that you can add easily:

use an object for the style prop
flatten children arrays
`useEffect` hook
reconciliation by key
If you add any of these or other features to Didact send a pull request to the [GitHub repo](https://github.com/pomber/didact), so others can see it.