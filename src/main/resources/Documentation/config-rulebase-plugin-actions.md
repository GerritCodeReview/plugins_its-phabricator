@PLUGIN@-specific actions
=========================

In addition to the [basic actions][basic-actions], @PLUGIN@ also
provides:

[`set-resolution`][action-set-resolution]
: sets the resolution of the issue

[`set-status`][action-set-status]
: sets the status of the issue

[`set-status-and-resolution`][action-set-status-and-resolution]
: sets both the status and resolution of the issue

[basic-actions]: config-rulebase-common.html#actions

[action-set-resolution]: #action-set-resolution
### <a name="action-set-resolution">Action: set-resolution</a>

The `set-resolution` action sets the issue's resolution. The first
parameter is the resolution to set. So for example

```
  action = set-resolution WORKSFORME
```

sets the issue's status to `WORKSFORME`.

If you want to set the status and the resolution, use the
`set-status-and-resolution` action, so you can set both status and
resolution in one go.



[action-set-status]: #action-set-status
### <a name="action-set-status">Action: set-status</a>

The `set-status` action sets the issue's status. The first parameter
is the status to set. So for example

```
  action = set-status CONFIRMED
```

sets the issue's status to `CONFIRMED`.

If you want to set the status to a value that also requires a
resolution, use the `set-status-and-resolution` action, so you can set
both status and resolution in one go.



[action-set-status-and-resolution]: #action-set-status-and-resolution
### <a name="action-set-status-and-resolution">Action: set-status-and-resolution</a>

The `set-status-and-resolution` action sets both the issue's status
and it's resolution in one go. The first parameter denotes the status
to set, the second parameter denotes the resolution to set.

So for example

```
  action = set-status-and-resolution RESOLVED FIXED
```

sets the issue's status to `RESOLVED` and it's resolution to `FIXED`.



[Back to @PLUGIN@ documentation index][index]

[index]: index.html