Phabricator connectivity
========================

In order for @PLUGIN@ to connect to your Phabricator instance, url (without
trailing “/api”, “/conduit” or some such), user, and certificate are required in
your site's `etc/gerrit.config` or `etc/secure.config` under the `@PLUGIN@`
section.

Example:

```
[@PLUGIN@]
  url = http://my.phabricator.instance.example.org
  token = TOKEN_FOR_ABOVE_USERNAME
```

You can get your token by going to http://my.phabricator.instance.example.org/conduit/login/

[Back to @PLUGIN@ documentation index][index]

[index]: index.html
