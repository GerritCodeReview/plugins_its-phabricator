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
  username=USERNAME_TO_CONNECT_TO_BUGZILLA
  certificate=CERTIFICATE_FOR_ABOVE_USERNAME
```

Note that the certificate is not the user's password. It is … well … the users
certificate … which is a 255 character long sequence of lowercase letters and
digits. To get the certificate, run

```
arc install-certificate http://my.phabricator.instance.example.org
```

And follow the instructions on the screen. Once the procedure of logging in to
the Phabricator instance in your browser and copying tokens around is complete,
you'll find the certificate in `~/.arcrc`.

[Back to @PLUGIN@ documentation index][index]

[index]: index.html
