Phabricator connectivity
========================

In order for @PLUGIN@ to connect to your Phabricator instance, url (without
trailing “/api”, “/conduit” or some such), user, and certificate are required in
your site's `etc/gerrit.config` or `etc/secure.config` under the `@PLUGIN@`
section.

Example:

```
[@PLUGIN@]
  url=http://my.phabricator.instance.example.org
  token=TOKEN_FOR_ABOVE_USERNAME
```

Note you can also get your token from http://my.phabricator.instance.example.org/conduit/login/
without doing the step below.

```
arc install-certificate http://my.phabricator.instance.example.org
```

And follow the instructions on the screen. Once the procedure of logging in to
the Phabricator instance in your browser and copying tokens around is complete,
you'll find the token in `~/.arcrc`.

[Back to @PLUGIN@ documentation index][index]

[index]: index.html
