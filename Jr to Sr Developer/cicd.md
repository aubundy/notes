# CI/CD

Coordinating the work of teams of developers is notoriously difficult. Devs have different schedules, coding styles, etc. that can impact the work.

## Continuous Integration

Practice where developers deploy code into a shared report consistently, preferrably multiple times a day. Each integration can be automatically approved by a build and test process.

### Continuous Delivery

Practice of keeping your codebase deployable at any point. The releasing of new merges to deployment is automated partially.

### Continuous Deployment

Same as continuous delivery except merged code is automatically released to deployment.

## Software Pipeline

1. Local test, linting, typechecking
2. Code formatting
3. Pull request to Github
4. CircleCI server builds code and tests it
5. Code is passed for code review
6. Merge and deploy

[Other CI tools](https://code-maze.com/top-8-continuous-integration-tools/)

## [CircleCI](https://circleci.com/)

1. Connect Github repo
2. Create .circleci folder
3. Create config.yml file
```
version: 2.1 // define circleci version
jobs:
  build:
    docker:
      - image: circleci/node:latest // creates docker image to test code
    steps:
      - checkout
      - run: npm install
      - run: CI=true npm run build
  test:
    docker:
      - image: circleci/node:latest
    steps:
      - checkout
      - run: npm install
      - run: npm run test
  hithere:
    docker:
      - image: circleci/node:latest
    steps:
      - checkout
      - run: echo "Hellloooo!"
workflows: // run jobs one after another
  version: 2
  build-test-and-lint:
    jobs:
      - build
      - hithere
      - test: // test job won't run until hithere has completed
          requires:
            - hithere
```

Can add anything you can accomplish programmatically to this pipeline. Webpack-bundle-analyzer, test coverage checkers, .js file size, etc.

### Precommit hooks

Run tasks automatically before committing your code.

`npm install --save-dev --save-exact prettier`
`npm install pretty-quick husky --dev`

In package.json:

```
{
    "scripts": {
        "precommit": "pretty-quick --staged"
    }
}
```
The above script will automatically run without having to add a job to circleci. It will only run on newly committed files.