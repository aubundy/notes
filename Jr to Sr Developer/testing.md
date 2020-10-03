# Testing

Testing helps code contain fewer bugs, find more bugs in your code, and to know when breaking changes are introduced into your code.

Tests can be run in different environments - in the console, DOM, headless browser like Puppateer, or jsdom.

## 3 Main Categories

1. Unit tests - tests individual functions and classes. Ideally they are pure functions. Cheapest and easiest to implement.
2. Integration tests - tests how different units of code interacts together. Spies and stubs are used here.
3. Automation tests - also known as UI/end to end tests. Tests are implemented in the browser or browser-like environment. Very expensive to implement. Its better to set up a different testing flow for automation tests.

## Testing Libraries

1. 'Scaffolding' to build tests off of -    Jasmine, Jest, Mocha
2. Insertion library -                      Jasmine, Jest, Chai
3. Test Runner -                            Jasmine, Jest, Mocha, Karma
4. Mocks, Spies, and Stubs -                Jasmine, Jest, Sinon
5. Code Coverage -                                   Jest, Istanbul

Others - Ava, Tate, Enzyme, WebdriveIO, TestCafe, Nightmare, Cypress, Nightwatch

CRA comes with Jest preinstalled. Here are instructions to install outside CRA.

`npm install --save-dev jest`

```
"scripts": {
    "test": "jest --watch *.js"
}
```

`.spec.js or .test.js syntax`

## Jest

Writing tests shouldn't be complicated. Structure your code to keep tests simple. Use dependency injection to help your tests and to help write pure functions.
The more tests, the better. 
Make your tests fail first, then make them pass one by one.
[Cheat Sheet](https://github.com/sapegin/jest-cheat-sheet)

`npm test -- --coverage`

### Writing tests

```
import googleSearch from './script';

dbMock = [...];

describe('googleSearch', () => {
    it('is searching google', () => {
        expect(googleSearch('testtest', dbMock)).toEqual([]);
        expect(googleSearch('dog', dbMock)).toEqual(['dog.com', 'dogpictures.com']);
    });

    it('works with undefined and null input', () => {
        expect(googleSearch(undefined, dbMock)).toEqual([]);
        expect(googleSearch(null, dbMock)).toEqual([]);
    });

    it('does not return more than 3 matches', () => {
        expect(googleSearch('.com', dbMock).length).toEqual(3);
    });
});
```

### Asynchronous Tests

```
import node-fetch from 'node-fetch';
import swapi from './script2';

it('calls swapi to get people', (done) => {
    expect.assertions(1);
    swapi.getPeople(fetch).then(data => {
        expect(data.count).toEqual(87);
        done();
    });
});

it('calls swapi to get people with a promise', () => {
    expect.assertions(2);
    return swapi.getPeoplePromise(fetch).then(data => {
        expect(data.count).toEqual(87);
        expect(data.results.length).toBeGreaterThan(5);
    })
});
```

### Mocks/Spies

```
it('getPeople returns count and results', () => {
    const mockFetch = jest.fn().mockReturnValue(Promise.resolve({
        json: () => Promise.resolve({
            count: 87,
            results: [0, 1, 2, 3, 4, 5]
        });
    }));

    expect.assertions(4);
    return swapi.getPeoplePromise(mockFetch).then(data => {
        expect(mockFetch.mock.calls.length).toBe(1);
        expect(mockFetch).toBeCalledWith('https://swapi.co/api/people);
        expect(data.count).toEqual(87);
        expect(data.results.length).toBeGreaterThan(5);
    });
});

```

## React Tests

[Enzyme](https://enzymejs.github.io/enzyme/) simplifies testing React components

### Setup Enzyme

`npm i --save-dev enzyme enzyme-adapter-react-16`

Create `setupTests.js` file.

```
import { configure } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';

configure({ adapter: new Adapter() });
```

### Writing tests

`shallow()` - best for unit tests in React. It only renders the one component
`mount()` - does a full DOM render. Useful when a component interacts with DOM or to test a lifecycle method. But it needs the DOM API to work. jsdom works best (standard for CRA);
`render()` - renders to static HTML. It doesn't need a full DOM.

```
import { shallow, mount, render } from 'enzyme';
import React from 'react';
import Card from './Card';

it('renders Card component', () => {
    <!-- expect(shallow(<Card />).length).toEqual(1); -->
    expect(shallow(<Card />)).toMatchSnapshot();
});
```

Testing components that depend on props to render

```
import { shallow, mount, render } from 'enzyme';
import React from 'react';
import CardList from './CardList';

it('renders CardList component', () => {
    const mockRobots = [
        {
            id: 1,
            name: 'John Snow',
            username: 'JohnJohn',
            email: 'john@gmail.com'
        }
    ];
    expect(shallow(<CardList robots={mockRobots}/>)).toMatchSnapshot();
});
```

Testing stateful components

```
import { shallow, mount, render } from 'enzyme';
import React from 'react';
import CounterButton from './CounterButton';

describe('CounterButton', () => {
    it('renders CounterButton component', () => {
        const mockColor = 'red';
        expect(shallow(<CounterButton color={mockColor}/>)).toMatchSnapshot();
    });

    it('correctly increments the counter', () => {
        const mockColor = 'red';
        const wrapper = shallow(<CounterButton color={mockColor}>);

        wrapper.find('[id='counter']').simulate('click');
        wrapper.find('[id='counter']').simulate('click');
        expect(wrapper.state()).toEqual({ count: 2 });
        wrapper.find('[id='counter']').simulate('keypress');
        expect(wrapper.state()).toEqual({ count: 3 });
        expect(wrapper.props().color).toEqual('red');
    })
})
```

### Connected components

You can use `redux-mock-store` to mock the store. You can also export the component directly. A third option is to separate a component into a redux connected component and UI component that you test.

```
import { shallow } from 'enzyme';
import React from 'react';
import MainPage from './MainPage';

let wrapper;
beforeEach(() => {
    const mockStore = {
        onRequestRobots: jest.fn();
        robots: [],
        searchField: '',
        isPending: false
    }
    wrapper = shallow(<MainPage { ...mockProps }/>);
});

it('renders MainPage component', () => {
    expect(wrapper).toMatchSnapshot();
});

it('filters robots correctly', () => {
    const mockStore2 = {
        onRequestRobots: jest.fn();
        robots: [{
            id: 3,
            name: 'John',
            email: 'john@gmail.com'
        }],
        searchField: 'j',
        isPending: false
    }
    const wrapper2 = shallow(<MainPage { ...mockStore2 }/>);
    expect(wrapper2.instance().filterRobots()).toEqual([{
            id: 3,
            name: 'John',
            email: 'john@gmail.com'
        }]);
});
```

## Testing Redux

### Reducers

```
import { 
    CHANGE_SEARCHFIELD,
    REQUEST_ROBOTS_PENDING,
    REQUEST_ROBOTS_SUCCESS,
    REQUEST_ROBOTS_FAILED
} from './constants;

import * as reducers from './reducers';

describe('searchRobots', () => {
    const initialStateSearch = {
        searchField: ''
    }

    it('should return the initial state', () => {
        expect(reducers.searchRobots(undefined, {})).toEqual(initialStateSearch);
    });

    it('should handle CHANGE_SEARCHFIELD', () => {
        expect(reducers.searchRobots(initialStateSearch, {
            type: CHANGE_SEARCHFIELD,
            payload: 'abc'
        })).toEqual({
            searchField: 'abc'
        });
    });
});

describe('requestRobots', () => {
    const initialStateRobots = {
        robots: [],
        isPending: false
    }

    it('should return the initial state', () => {
        expect(reducers.requestRobots(undefined, {})).toEqual(initialStateRobots);
    });

    it('should handle REQUEST_ROBOTS_PENDING', () => {
        expect(reducers.requestRobots(initialStateRobots, {
            type: REQUEST_ROBOTS_PENDING
        })).toEqual({
            robots: [],
            isPending: true
        });
    });

    it('should handle REQUEST_ROBOTS_SUCCESS', () => {
        expect(reducers.requestRobots(initialStateRobots, {
            type: REQUEST_ROBOTS_SUCCESS,
            payload: [{
                id: '123',
                name: 'test',
                email: 'test@gmail.com'
            }]
        })).toEqual({
            robots: [{
                id: '123',
                name: 'test',
                email: 'test@gmail.com'
            }],
            isPending: false
        });
    });
    
    it('should handle REQUEST_ROBOTS_FAILURE', () => {
        expect(reducers.requestRobots(initialStateRobots, {
            type: REQUEST_ROBOTS_FAILURE,
            payload: 'ERROR'
        })).toEqual({
            robots: 'ERROR',
            robots: [],
            isPending: false
        });
    });
});
```

### Actions

```
import { 
    CHANGE_SEARCHFIELD,
    REQUEST_ROBOTS_PENDING,
    REQUEST_ROBOTS_SUCCESS,
    REQUEST_ROBOTS_FAILED
} from './constants;

import * as actions from './actions';
import configureMockStore from 'redux-mock-store';
import thunkMiddleware from 'redux-thunk';

const mockStore = configureMockStore([thunkMiddleware]); // can export so it's available for all your tests

describe('setSearchField', () => {
    it('creates an action to search robots', () => {
        const text = 'wooo';
        const expectedAction = {
            type: CHANGE_SEARCHFIELD,
            payload: text;
        }

        expect(actions.setSearchField(text)).toEqual(expectedAction);
    });
});

describe('requestRobots', () => {
    it('requests robots API', () => {
        const store = mockStore();
        store.dispatch(action.requestRobots());
        const action = store.getActions();
        const expectedAction = {
            type: REQUEST_ROBOTS_PENDING
        }

        expect(action[0]).toEqual(expectedAction);
    });
});

```

Additional Resources -

[nock](https://github.com/nock/nock)
[supertest](https://github.com/visionmedia/supertest)
[Lean Testing](https://blog.usejournal.com/lean-testing-or-why-unit-tests-are-worse-than-you-think-b6500139a009)