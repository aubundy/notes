# TypeScript

Languages can be dynamic or static. Static type languages give you built in documentation and fewer bugs. But it adds a little to your code and adds some complexity. Tests could also provide the same fewer bugs benefit. The development process will slow down a little bit too.

Languages can also have weak or strong typing. Weak languages have type coersion, strong languages do not.

JavaScript is a dynamic and weak typed language. Using [TypeScript](https://www.typescriptlang.org/) is most helpful as your app grows, tests grow, and your development team grows. You'll want your code to be as self-documenting as possible.

## Compiler

You need Node.js downloaded in order to use TypeScript. [How to change Node version using nvm](https://www.sitepoint.com/quick-tip-multiple-versions-node-nvm/)

`sudo npm i -g typescript` 

`tsc` [if tsc command not found](https://stackoverflow.com/questions/39404922/tsc-command-not-found-in-compiling-typescript)

## Setup

Use `.ts` at end of file.

```
const sum = (a: number, b: number) => {
    return a + b;
}
```

`tsc --init` creates a `tsconfig.json` file. Check out the docs on how to setup this file.
`tsc filename.ts --watch` sets up watch mode to compile automatically after you save.

## Types

Boolean
`let isCool: boolean = true;`

Number
`let age: number = 6;`

String
`let eyeColor: string = 'brown';`

Array
`let pets: string[] = ['cat', 'dog', 'pig'];`
`let pet2: Array<string> = ['squirrel', 'fish', 'cow'];`

Object
`let wizard: object = { a: 'John' }`

Undefined
`let meh: undefined = undefined;`

Null
`let no: null = null;`

Tuple
`let basket: [string, number] = ['basketball', 5];`

Enum
```
enum Size { Small = 1, Medium = 2, Large = 3 }
let sizeName: string = Size[2]; // should output 'Medium'
let sizeName: number = Size.Small;
```

Any - useful for whenver you're converting JS code to TS. Be careful!
```
let whatever: any = 'risky business!';
whatever = 5;
```

Void - common with functions that don't return anything
`let sing = (): void => console.log('test');`

Never - tests if function never returns and doesn't have an endpoint (like throwing an error)
`let error = (): never => throw Error('ooops');`

Interface - useful with reusable objects ([type](https://medium.com/@martin_hotell/interface-vs-type-alias-in-typescript-2-7-2a8f1777af4c) vs. [interface](https://www.briangonzalez.org/post/interface-types-vs-type-aliases-typescript))
```
interface RobotArmy {
    count: number,
    type: string,
    magic?: string // may or may not be a part of the object
}

let fightRobotArmy = (robots: RobotArmy): void => {
    console.log('Fight!');
}
```

Type Assertions - tell the compiler to let me assert the type. [Be careful!](https://basarat.gitbook.io/typescript/type-system/type-assertion)
```
interface CatArmy {
    count: number,
    type: string,
    magic: string
}

let dog = {} as CatArmy
dog.count // doesn't work
```

Function
```
let fightRobotArmy2 = (robots: RobotArmy): void => {
    console.log('Fight!');
}

let fightRobotArmy3 = (robots: RobotArmy): number => {
    return robots.count;
}
```

Classes
```
class Animal {
    private sing: string = 'moo', // set to public by default
    constructor(sound: string) {
        this.sing = sound;
    }
    
    greet(): string {
        return `Hello ${this.sing}`;
    }
}

let lion = new Animal('ROAR');
console.log(lion.greet());
lion.sing // doesn't work
```

Union
```
let confused: string | number = 'hello';
confused = 5; // works!
confused = true; // doesn't work
```

Inferences - TypeScript is pretty smart
```
let x: number = 4;
x = 'hi'; // doesn't work
```

## [DefinitelyTyped](https://definitelytyped.org/)

TS lets you use declaration files, which describes the shape of the code it's written in

## Add to CRA project

`npm i --save typescript @types/node @types/react @types/react-dom @types/jest` save-dev?

Rename files to `.tsx`

## [React Example](https://github.com/aneagoie/robofriends-typescript-completed)

```
interface IRobot {
    id: number,
    name: string,
    email: string
}

interface IAppProps {
}

interface IAppState {
    robots: Array<IRobot>,
    searchfield: string
}

class App extends React.Component<IAppProps, IAppState> {
    ...
}
```

1. render the `document.getElementById(...)` as `HTMLElement`
2. add interfaces for props and state
3. use React.SFC<elementprops>
4. use JSX.Element
5. use React.SyntheticEvent<HTMLInputElement>