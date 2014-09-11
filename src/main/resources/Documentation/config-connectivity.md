Bugzilla connectivity
=====================

In order for Gerrit to connect to Bugzilla,

1. [make sure that your Bugzilla instance has the XML-RPC interface
   enabled][rpc-enabled], and
2. [provide url, user and password to @PLUGIN@][gerrit-configuration].


[rpc-enabled]: #rpc-enabled
<a name="rpc-enabled">Checking XML-RPC availability</a>
-------------------------------------------------------

Assuming the Bugzilla instance you want to connect to is at
`http://my.bugzilla.instance.example.org/`, open

```
http://my.bugzilla.instance.example.org/xmlrpc.cgi
```

in your browser. If you get an empty page without errors, the XML-RPC
interface is enabled. You can continue by [providing the needed Gerrit
configuration][gerrit-configuration].

If you get an error page saying

```
The XML-RPC Interface feature is not available in this Bugzilla.
```

the XML-RPC interface needs to be enabled. To do so, log in to the
server that's running your Bugzilla instance, go to Bugzilla's
directory, run

```
./checksetup.pl --check-modules
```

and install the missing modules. Then re-check the XML-RPC interface
availability as above.

[gerrit-configuration]: #gerrit-configuration
<a name="gerrit-configuration">Gerrit configuration</a>
-------------------------------------------------------

In order for @PLUGIN@ to connect to the XML-RPC service of your
Bugzilla instance, url (without trailing "/xmlrpc.cgi") and
credentials are required in your site's `etc/gerrit.config` or
`etc/secure.config` under the `@PLUGIN@` section.

Example:

```
[@PLUGIN@]
  url=http://my.bugzilla.instance.example.org
  username=USERNAME_TO_CONNECT_TO_BUGZILLA
  password=PASSWORD_FOR_ABOVE_USERNAME
```

[Back to @PLUGIN@ documentation index][index]

[index]: index.html
