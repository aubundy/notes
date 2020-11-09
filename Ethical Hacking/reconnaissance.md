# Information Gathering

* Active - get as much info using our machine as possible
* Passive - learn about the target through other sources

## Type of data
* ip addresses
* phone numbers
* emails
* technologies

## IP Addresses

* `ping`
* `nslookup`
* `whois`

### WhatWeb

Tool used to recognize different technologies (CMS, JS libraries, servers, etc.) used by websites. Automatically runs in 'steathly' mode, which can be used on any website. The more aggresive modes need permissions.

`whatweb` or `whatweb --help` - see the commands we can run with WhatWeb
`whatweb URL -v`

`whatweb IP-IPRANGE --aggression 3 -v --no-errors --log-verbose=results` - run whatweb w/ lvl 3 aggression on an ip range and save verbose results to a file named results

## Gathering Emails

`theHarvester` - comes with Kali Linux. Doesn't always return the best results. Can be spotty.
`theHarvester -d URL -b all` - sometimes play with the source to search (after '-b')

Hunter.io - another option to find out the number of emails available online for a given domain