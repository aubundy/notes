# Code Analysis

## Setting up your Environment

1. Fork/clone the repo
2. npm install local packages and open package.json to view dependencies
3. Begin running the programs
4. Check database connector, db tables, etc.
5. Check 3rd party APIs

## Analyzing Code

1. Look through API - endpoints tell you a lot about an app
2. Look through folder structure, read the README, etc.
3. Look through packages imported in the files you're viewing
4. Look through function call stack based on higher level files
5. Look through frontend and repeat 2 - 4
    * For React apps, look through state structure and component structure
    * Focus on different aspects of UX and find the corresponding code
6. Don't immediately criticize the code, you were not there when it was written

## When to update libraries

Libraries update all the time, but not every update should be implemented in our code. We'll use [React Hooks](https://reactjs.org/docs/hooks-intro.html) as a case study

1. Read the documentation and find the motivation for the updates. Why did the developers think this update was so helpful?
    * Difficult to reuse stateful logic between components
    * Complex components quickly become difficult to understand
    * Classes are confusing to humans and machines
2. Confirm you are using the updated version of the library
3. What is the new update and how is it to be used?
    * Hooks are functions that let you 'hook into' React state and lifecycle features while inside a functional component. Before, functional components were for components that didn't have state inside them.
    * useState, useEffect, and custom hooks are some of the new features/conventions
4. Check the documentation for an implementation strategy

[Robofriends with Hooks](https://github.com/aneagoie/robofriends-hooks)