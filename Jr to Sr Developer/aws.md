# Amazon Web Services

1. S3 - object storage service. Great for user photos
2. EC2 - basic server to run code on
3. Lambda - auto-scaled server for individual functions (like notifications)
4. Cloudfront - CDN that works well with static files
5. DynamoDB - auto-scaled NoSQL db similar to Redis

AWS Flow is an important DevOps concept. We might now have to deal with it very much, but it is important to have in the back of our minds as we write code.

## Monolithic vs. Microservices

Having one codebase vs. multiple individual services with their own business logic, focused on doing one thing really well. Tools like AWS and Docker make microservices possible.

## Amazon Lambda

Traditionally, we build our web apps with controll over HTTP requests, deploying the server, provisioning and managing the resources, etc. There's a few problems with this. 

1. We are charged for our server even if no one is accessing it.
2. We are responsible for making sure the server is updated, accessible, and scalable.
3. We are responsible for the security of our app.

"Serverless" means we hand our code over to a cloud provider, and we only pay for what gets used.

Lambda creates a container that runs our code when it is requested. It is slow for the first request as it needs to build the container, but it is fast after that.

serverless (the npm package) works really well with Lambda.

```
'use strict'; 
module.exports.hello = async (event, context) => {  
    return {    
        statusCode: 200,    
        body: JSON.stringify({      
            message: 'Go Serverless v1.0! Your function executed successfully!',      
            input: event,    
        }),  
    };    
// Use this code if you don't use the http event with the LAMBDA-PROXY integration  
// return { message: 'Go Serverless v1.0! Your function executed successfully!', event };
};
```

## serverless

`npm install -g serverless` - installs on computer

`sls create -t aws-nodejs` - creates nodejs service with a handler.js file and serverless.yml file

### .yml file

```
region: // change your server location
environment: // add env variables
events: // change the triggers that cause your function to run
    http:
    s3:
    ...
schedule: // set a recurring time for the function to run
```

## IAM

Controls permissions within AWS

1. Create new user
2. Give programmatic access (if for lambda)
3. Select policy level (administrator for testing, not production)

`sls config credentials --provider aws --key KEYFROMAWS --secret SECRETFROMAWS`

[Amazon Docs on IAM](https://docs.aws.amazon.com/IAM/latest/UserGuide/access_policies_manage.html)
[serverless on IAM](https://www.serverless.com/framework/docs/providers/aws/guide/iam/)

## Deploying a lambda function

1. Update function name
2. Set stage to env
3. Update service name to match lambda name in AWS
4. `sls invoke -f hello` or `sls invoke local -f hello`
5. Update `events` inside .yml
6. `sls deploy`
7. Copy url to use in `fetch()`