Phabricator connectivity
========================

In order for @PLUGIN@ to connect to your Phabricator instance, url (without
trailing “/api”, “/conduit” or some such), and certificate are required in
your site's `etc/gerrit.config` or `etc/secure.config` under the `@PLUGIN@`
section.

Example:

```
[@PLUGIN@]
  url = http://my.phabricator.instance.example.org
  token = TOKEN_AS_DESCRIBED_BELOW
```

You can get your token by going to http://my.phabricator.instance.example.org/conduit/login/
Tokens typically start in `cli-` and are followed by letters and digits, as
for example `cli-zoenau772kfsrofqxt7cn55q4rng`.

[Back to @PLUGIN@ documentation index][index]

[index]: index.html
