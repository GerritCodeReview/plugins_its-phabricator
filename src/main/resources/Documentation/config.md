Plugin @PLUGIN@
===============

This plugin allows to associate Bugzilla bugs to Git commits thanks to
the Gerrit listener interface.

Comment links
----------------

Git commits are associated to Bugzilla bugs reusing the existing Gerrit
[commitLink configuration][1] to extract the issue ID from commit comments.

[1]: ../../../Documentation/config-gerrit.html#__a_id_commentlink_a_section_commentlink

Additionally you need to specify the enforcement policy for git commits
with regards to issue-tracker associations; the following values are supported:

MANDATORY
:	 One or more issue-ids are required in the git commit message, otherwise
	 the git push will be rejected.

SUGGESTED
:	 Whenever git commit message does not contain one or more issue-ids,
	 a warning message is displayed as a suggestion on the client.

OPTIONAL
:	 Bug-ids are liked when found on git commit message, no warning are
	 displayed otherwise.

Example:

    [commentLink "its-bugzilla"]
    match = \\([Bb][Uu][Gg][ ]*([1-9][0-9]*)\\)
    html = "<a href=\"http://mybugzilla.org/show_bug.cgi?id=$1\">(bug $1)</a>"
    association = SUGGESTED

Once a Git commit with a comment link is detected, the Bugzilla bug ID
is extracted and a new comment added to the issue, pointing back to
the original Git commit.

Note that the plugin relies on $1 holding the numeric id, so we cannot
have match group 1 spanning over the whole “(Bug 4711)”.

Be sure to label the commentLink “its-bugzilla” with all lowercase to
match the config section's name below.

Bugzilla connectivity
---------------------

In order for Gerrit to connect to Bugzilla/XML-RPC url and credentials
are required in your gerrit.config / secure.config under the [its-bugzilla] section.

Example:

    [its-bugzilla]
    url=http://mybugzilla.org
    username=bzuser
    password=bzpass

Bugzilla credentials and connectivity details are asked and verified during the Gerrit init.

Gerrit init integration
-----------------------

Bugzilla plugin is integrated as a Gerrit init step in order to simplify and guide
through the configuration of Bugzilla integration and connectivity check, avoiding
bogus settings to prevent Gerrit plugin to start correctly.

Gerrit init example:

    *** Bugzilla connectivity
    ***

    Bugzilla URL (empty to skip)       [http://mybugzilla.org]:
    Bugzilla username                  [admin]:
    Change admin's password        [y/N]? y
    admin's password               : *****
                  confirm password : *****
    Test connectivity to http://mybugzilla.org [y/N]: y
    Checking Bugzilla connectivity ... [OK]

    *** Bugzilla issue-tracking association
    ***

    Bugzilla bug number regex       [([A-Z]+-[0-9]+)]:
    Issue-id enforced in commit message [MANDATORY/?]: ?
           Supported options are:
           mandatory
           suggested
           optional
    Issue-id enforced in commit message [MANDATORY/?]: suggested

GitWeb integration
----------------

When Gerrit gitweb is configured, an additional direct link from Bugzilla to GitWeb
will be created, pointing exactly to the Git commit ID containing the Bugzilla bug ID.


Automatic comments and actions
------------------------------

Setting up which event in gerrit (E.g.: “Change Merged”, or “User
‘John Doe’ voted ‘+2’ for ‘Code-Review’ on a change”) should causes
what action on the ITS (e.g.: “Set issue's status to ‘Resolved’”) is
configured through a xref:config-rule-base[rule base] in
`etc/its/action.config`.

To turn off the legacy event handling of older 'its-*' plugins and
stop unwanted legacy comments, add the following settings to the
'its-bugzilla' section of 'etc/gerrit.config':

----
commentOnChangeAbandoned = false
commentOnChangeMerged = false
commentOnChangeRestored = false
commentOnChangeCreated = false
commentOnCommentAdded = false
commentOnPatchSetCreated = false
commentOnRefUpdatedGitWeb = false
----



[[config-rule-base]]
Rule base for Actions
~~~~~~~~~~~~~~~~~~~~~

In this part we describe, how to specify which events in gerrit (E.g.:
“Change Merged”, or “User ‘John Doe’ voted ‘+2’ for ‘Code-Review’ on a
change”) should causes what action (e.g.: “Set issue's status to
‘Resolved’”) on the ITS.

Actions on the ITS and conditions for the action to take place are
configured through the rule base in `etc/its/actions.config` in the
site directory. The rule base is a git config file, and may contain an
arbirary number of rules. Each rule can have an arbitrary number of
conditions and actions. A rule fires all associated actions, once all
of its conditions are met.

A simple `etc/its/actions.config` may look like
----
[rule "rule1"]
        event-type = change-merged
        action = add-standard-comment
[rule "rule2"]
        event-type = comment-added
        approval-Code-Review = -2,-1
        action = add-comment Oh my Goodness! Someone gave a negative code review in gerrit on an associated change.
----

This snippet defines two rules ('rule1', and 'rule2'). On merging a
change that's associated to some issues, 'rule1' adds a predefined
standard comment for “Change Merge” to each such issue. If someone
adds a comment to a change that is associated to some issues and votes
“-2”, or “-1” for “Code-Review”, 'rule2' adds the comment “Oh my
Goodness! Someone gave a negative code review in gerrit on an
associated change.” to each such issue.

The order of rules in `etc/its/action.config` need not be
respected. So in the above example, do not rely on 'rule1' being
evaluated before 'rule2'.

Rules
~~~~~

Each rule consists of three items: A name, a set of conditions, and a
set of actions.

A rule's name ('rule1', and 'rule2' in the above example) are
currently not used and only provided for convenience.

Each rule line setting the option 'action' is interpreted as
action. Any other lines of a rule are considered a condition.

Each of a rule's actions is taken for events that meet all of a
rule's conditions. If a rule contains more than one action
specifications, the order in which they are given need not be
respected.

There is no upper limit on the number of elements in a rules set of
conditions, and set of actions. Each of those sets may be empty.

Conditions
~~~~~~~~~~

The conditions are lines of the form
----
name = value1, value2, ..., valueN
----
and (if 'value1' is not +!+) match if the event comes with a property
'name' having 'value1', or 'value2', or ..., or 'valueN'. So for
example to match events that come with an 'association' property
having 'subject', or 'footer-Bug', the following condition can be
used:
----
association = subject,footer-Bug
----

If 'value1' is +!+, the conditon matches if the event does not come
with a property 'name' having 'value2', or ..., or 'valueN'. So for
example to match events that do not come with a 'status' property
having 'DRAFT', the following condition can be used:
----
status = !,DRAFT
----

[[event-properties]]
Event Properties
~~~~~~~~~~~~~~~~

The properties exposed by events depend on the kind of event.

For all events, the event's class name is provided in the 'event'
property. Most native gerrit events provide the 'event-type'
property. So 'event-type' (or 'event' for other events fired by
plugins) allows you to write filters that fire only for a certain type
of event.

The common properties for each event are

'event'::
  The event's class name.
'issue'::
  Issue to which this event is associated. Each event is associated to
  exactly one issue. If for example an event is fired for a commit
  message, that would contain more than one issue id (say issue “23”,
  and issue “47"), then the event is duplicated and sent once for each
  associated issue (i.e.: once with 'issue' being +23+, and once with
  'issue' being +47+).
'association'::
  How the issue of property 'issue' got associated to this event. An
  event typically has several 'association' properties. Possible
  values are:
  'somewhere'::: issue id occurs somewhere in the commit message of the
    change/the most recent patch set.
  'subject'::: issue id occurs in the first line of the commit message
    of the change/the most recent patch set.
  'body'::: issue id occurs after the subject but before the footer
    of the commit message of the change/the most recent patch set.
  'footer'::: issue id occurs in the last paragraph after the subject
    of the commit message of the change/the most recent patch set.
  'footer-<Key>'::: issue id occurs in the footer of the commit
    message of the change/the most recent patch set, and is in a line
    with a key (part before the colon).
    +
    So for example, if the footer would contain a line
+
----
Fixes-Issue: issue 4711
----
+
then a property 'association' with value +footer-Fixes-Issue+ would
get added to the event for issue “4711”.

  'added@<Association-Value>':::
    (only for events that allow to determine the patch set number. So
    for example, this 'association' property is not set for
    RevUpdatedEvents)
    +
    issue id occurs at '<Association-Value>' in the most recent patch
    set of the change, and either the event is for patch set 1 or the
    issue id does not occur at '<Association-Value>' in the previous
    patch set.
    +
    So for example if issue “4711” occurs in the subject of patch set
    3 (the most recent patch set) of a change, but not in patch set 2.
    When adding a comment to this change, the event for issue “4711”
    would get a property 'association' with value +added@subject+.

The further properties are listed in the event's
corresponding subsection below:

* <<event-properties-ChangeAbandonedEvent,ChangeAbandonedEvent>>
* <<event-properties-ChangeMergedEvent,ChangeMergedEvent>>
* <<event-properties-ChangeRestoredEvent,ChangeRestoredEvent>>
* <<event-properties-CommentAddedEvent,CommentAddedEvent>>
* <<event-properties-DraftPublishedEvent,DraftPublishedEvent>>
* <<event-properties-PatchSetCreatedEvent,PatchSetCreatedEvent>>
* <<event-properties-RefUpdatedEvent,RefUpdatedEvent>>
* <<event-properties-change,Common properties for events on a change>>
* <<event-properties-patch-set,Common properties for events on a patch set>>

[[event-properties-ChangeAbandonedEvent]]
ChangeAbandonedEvent
^^^^^^^^^^^^^^^^^^^^

'abandoner-email'::
  email address of the user abandoning the change.
'abandoner-name'::
  name of the user abandoning the change.
'abandoner-username'::
  username of the user abandoning the change.
'event'::
  +com.google.gerrit.server.events.ChangeAbandonedEvent+
'event-type'::
  +change-abandoned+
'reason'::
  reason why the change has been abandoned.

In addition to the above properties, the event also provides
properties for the abandoned <<event-properties-change,change>>, and
it's most recent <<event-properties-patch-set,patch set>>.

[[event-properties-ChangeMergedEvent]]
ChangeMergedEvent
^^^^^^^^^^^^^^^^^^^

'event'::
  +com.google.gerrit.server.events.ChangeMergedEvent+
'event-type'::
  +change-merged+
'submitter-email'::
  email address of the user causing the merge of the change.
'submitter-name'::
  name of the user causing the merge of the change.
'submitter-username'::
  username of the user causing the merge of the change.

In addition to the above properties, the event also provides
properties for the merged <<event-properties-change,change>>, and
it's most recent <<event-properties-patch-set,patch set>>.

[[event-properties-ChangeRestoredEvent]]
ChangeRestoredEvent
^^^^^^^^^^^^^^^^^^^

'event'::
  +com.google.gerrit.server.events.ChangeRestoredEvent+
'event-type'::
  +change-restored+
'reason'::
  reason why the change has been restored.
'restorer-email'::
  email address of the user restoring the change.
'restorer-name'::
  name of the user restoring the change.
'restorer-username'::
  username of the user restoring the change.

In addition to the above properties, the event also provides
properties for the restored <<event-properties-change,change>>, and
it's most recent <<event-properties-patch-set,patch set>>.

[[event-properties-CommentAddedEvent]]
CommentAddedEvent
^^^^^^^^^^^^^^^^^

NOTE: For consistency with the other events, the 'author-...'
properties of the CommentAddedEvent do not refer to the author of the
comment, but refer to the author of the change's latest patch set. The
author of the comment is accessible via the 'commenter-...'
properties.

'commenter-email'::
  email address of the comment's author.
'commenter-name'::
  name of the comment's author.
'commenter-username'::
  username of the comment's author.
'comment'::
  added comment itself.
'event'::
  +com.google.gerrit.server.events.CommentAddedEvent+
'event-type'::
  +comment-added+

For each new or changed approval that has been made for this change, a
property of key 'approval-<LabelName>' and the approval's value as
value is added. So for example voting “-2” for the approval
“Code-Review” would add the following property:

'approval-Code-Review'::
  +-2+

In addition to the above properties, the event also provides
properties for the <<event-properties-change,change>> the comment was
added for, and it's most recent <<event-properties-patch-set,patch
set>>.

[[event-properties-DraftPublishedEvent]]
DraftPublishedEvent
^^^^^^^^^^^^^^^^^^^

'event'::
  +com.google.gerrit.server.events.DraftPublishedEvent+
'event-type'::
  +draft-published+

In addition to the above properties, the event also provides
properties for the uploaded <<event-properties-patch-set,patch set>>,
and the <<event-properties-change,change>> it belongs to.

[[event-properties-PatchSetCreatedEvent]]
PatchSetCreatedEvent
^^^^^^^^^^^^^^^^^^^

'event'::
  +com.google.gerrit.server.events.PatchSetCreatedEvent+
'event-type'::
  +patchset-created+

In addition to the above properties, the event also provides
properties for the uploaded <<event-properties-patch-set,patch set>>,
and the <<event-properties-change,change>> it belongs to.

[[event-properties-RefUpdatedEvent]]
RefUpdatedEvent
^^^^^^^^^^^^^^^

'event'::
  +com.google.gerrit.server.events.RefUpdatedEvent+
'event-type'::
  +ref-updated+
'project'::
  full name of the project from which a ref was updated.
'ref'::
  git ref that has been updated (Typcially the branch, as for example
  +master+).
'revision'::
  git commit hash the rev is pointing to now.
'revision-old'::
  git commit hash the rev was pointing to before.
'submitter-email'::
  email address of the user that updated the ref.
'submitter-name'::
  name of the user that updated the ref.
'submitter-username'::
  username of the user that updated the ref.

[[event-properties-change]]
Common properties for events on a change
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

'branch'::
  name of the branch the change belongs to.
'change-id'::
  Change-Id for the change („I-followed by 40 hex digits” string).
'change-number'::
  number for the change (plain integer).
'change-url'::
  url of the change.
'owner-email'::
  email address of the change's owner.
'owner-name'::
  name of the change's owner.
'owner-username'::
  username of the change's owner.
'project'::
  full name of the project the change belongs to.
'subject'::
  first line of the change's most recent patch set's commit message.
'status'::
  status of the change ('null', 'NEW', 'SUBMITTED', 'DRAFT', 'MERGED',
  or 'ABANDONED' )
  +
  This property will typically be 'null' unless the used gerrit
  incorporates
  https://gerrit-review.googlesource.com/#/c/47042/[upstream change
  47042].
'topic'::
  name of the topic the change belongs to.

[[event-properties-patch-set]]
Common properties for events on a patch set
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

'author-email'::
  email address of this patch set's author.
'author-name'::
  name of this patch set's author.
'author-username'::
  username of this patch set's author.
'created-on'::
  Timestamp of creation of the patch set (Seconds since 1st January 1970).
'deletions'::
  number of lines deleted by the patch set.
'insertions'::
  number of lines inserted by the patch set.
'is-draft'::
  'true', if the patch set is a draft patch set, 'false' otherwise.
'parents'::
  A list of git commit hashes that are parents to the patch set.
'patch-set-number'::
  patch set's number within the change.
'ref'::
  git ref for the patch set (For the 5-th patch set of change 4711, this
  will be +refs/changes/11/4711/5+).
'revision'::
  git commit hash of the patch set
'uploader-email'::
  email address of the user that uploaded this patch set.
'uploader-name'::
  name of the user that uploaded this patch set.
'uploader-username'::
  username of the user that uploaded this patch set.

Actions
~~~~~~~

Lines of the form
----
action = name param1 param2 ... paramN
----
represent the action 'name' being called with parameters 'param1',
'param2', ... 'paramN'.

The following actions are available:

<<action-add-comment,add-comment>>::
  adds the parameters as issue comment
<<action-add-standard-comment,add-standard-comment>>::
  adds a predefined standard comment for certain events
<<action-add-velocity-comment,add-velocity-comment>>::
  adds a rendered Velocity template as issue comment.
<<action-log-event,log-event>>::
  appends the event's properties to gerrit's log.
<<action-set-resolution,set-resolution>>::
  sets the resolution of the issue
<<action-set-status,set-status>>::
  sets the status of the issue
<<action-set-status-and-resolution,set-status-and-resolution>>::
  sets the status of the issue

Further actions may be provided by 'its-base' based plugins.

[[action-add-comment]]
Action: add-comment
^^^^^^^^^^^^^^^^^^^

The 'add-comment' action adds the given parameters as comment to any associated rule.

So for example
----
action = add-comment This is a sample command
----
would add a comment “This is a sample command” to associated issues.

If no parameters are given, no comment gets added.

[[action-add-standard-comment]]
Action: add-standard-comment
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The 'add-standard-comment' action adds predefined comments to
associated issues for change abandoned, merged, restored, and patch
set created events. For other events, no comment is added to the
associated issues.

The added comments contain the person responsible for the event
(abandoner, merger, ...), the change's subject, a reason (if one has
been given), and a link to the change.

[[action-add-velocity-comment]]
Action: add-velocity-comment
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The 'add-velocity-comment' action renders a Velocity template for the
event and adds the output as comment to any associated issue.

So for example
----
action = add-velocity-comment TemplateName
----
would render the template `etc/its/templates/TemplateName.vm` add the
output as comment to associated issues.

If 'TemplateName' is “inline”, the Velocity template to render is not
loaded from a file, but the template is built by joining the remaining
parameters. So for example
----
action = add-velocity-comment inline Sample template using $subject property.
----
would render “Sample template using $subject property.” as Velocity
template.

If 'TemplateName' is not “inline”, further parameters get ignored.

Any <<event-properties,property>> of the event may be used from
templates. So for example +$subject+ in the above example refers to
the event's subject property, and +$change-number+ would refer to the
change's number.

Additionally, the context's 'its' property provides an object that
allows to format links using the its' syntax:

'formatLink( url )'::
  Formats a link to a url.
  +
  So for example upon adding a comment to a change, the following rule
  formats a link to the change:
+
----
[rule "formatLinkSampleRule"]
        event-type = comment-added
        action = add-velocity-comment inline Comment for change $change-number added. See ${its.formatLink($change-url)}
----

'formatLink( url, caption )'::
  Formats a link to a url using 'caption' to represent the url.
  +
  So for example upon adding a comment to a change, the following rule
  formats a link to the change using the change number as link
  capition:
+
----
[rule "formatLinkSampleRule"]
        event-type = comment-added
        action = add-velocity-comment inline Comment for change ${its.formatLink($change-url, $change-number)} added.
-----

[[action-log-event]]
Action: log-event
^^^^^^^^^^^^^^^^^

The 'log-event' action appends the event's properties to gerrit's log.

Logging happens at the info level per default, but can be overriden by
adding the desired log level as parameter. Supported values are
'error', 'warn', 'info', and 'debug'). So for example
----
action = log-event error
----
appends the event's properties to gerrit's log at error level. All
other parameters are ignored.

This action is useful, when testing rules or trying to refine
conditions on rules, as it make the available properties visible.

[[action-set-resolution]]
Action: set-resolution
^^^^^^^^^^^^^^^^^^^^^^

The 'set-resolution' action sets the issue's resolution. The first
parameter is the resolution to set. So for example
----
action = set-resolution WORKSFORME
----
sets the issue's status to WORKSFORME.

If you want to set the status and the resolution, use the
'set-status-and-resolution' action, so you can set both status and
resolution in one go.

[[action-set-status]]
Action: set-status
^^^^^^^^^^^^^^^^^^

The 'set-status' action sets the issue's status. The first parameter
is the status to set. So for example
----
action = set-status CONFIRMED
----
sets the issue's status to CONFIRMED.

If you want to set the status to a value that also requires a
resolution, use the 'set-status-and-resolution' action, so you can set
both status and resolution in one go.

[[action-set-status-and-resolution]]
Action: set-status-and-resolution
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The 'set-status-and-resolution' action sets both the issue's status
and it's resolution in one go. The first parameter denotes the status
to set, the second parameter denotes the resolution to set.

So for example
----
action = set-status-and-resolution RESOLVED FIXED
----
sets the issue's status to RESOLVED and it's resolution to FIXED.
