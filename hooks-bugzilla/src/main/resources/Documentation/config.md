Plugin @PLUGIN@
===============

This plugin allows to associate Bugzilla bugs to Git commits thanks to
the Gerrit listener interface.

Comment links
----------------

Git commits are associated to Bugzilla bugs reusing the existing Gerrit
[commitLink configuration]i[1] to extract the issue ID from commit comments.

[1]: ../../../Documentation/config-gerrit.html#_a_id_commentlink_a_section_commentlink

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

    [commentLink "Bugzilla"]
    match = (\\([Bb][Uu][Gg][ ]*[1-9][0-9]*\\))
    html = "<a href=\"http://mybugzilla.org/show_bug.cgi?id=$1\">$1</a>"
    association = SUGGESTED

Once a Git commit with a comment link is detected, the Bugzilla bug ID
is extracted and a new comment added to the issue, pointing back to
the original Git commit.

Bugzilla connectivity
---------------------

In order for Gerrit to connect to Bugzilla/XML-RPC url and credentials
are required in your gerrit.config / secure.config under the [bugzilla] section.

Example:

    [bugzilla]
    url=http://mybugzilla.org
    username=bzuser
    passsword=bzpass

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
    Test connectivity to http://mybugzilla.org [N/?]: y
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

Issues workflow automation
--------------------------

Bugzilla plugin is able to automate status transition on the issues based on
code-review actions performed on Gerrit; actions are performed on Bugzilla using
the username/password provided during Gerrit init.
Transition automation is driven by `$GERRIT_SITE/issue-state-transition.config` file.

Syntax of the status transition configuration file is the following:

    [action "<issue-status-action>"]
    change=<change-action>
    verified=<verified-value>
    code-review=<code-review-value>

`<issue-status-action>`
:	Action to perform on Bugzila issue when all the condition in the stanza are met.

`<change-action>`
:	Action performed on Gerrit change-id, possible values are:
	`created, commented, merged, abandoned, restored`

`<verified-value>`
:	Verified flag added on Gerrit with values from -1 to +1

`<code-review-value>`
:	Code-Review flag added on Gerrit with values from -2 to +2

Note: multiple conditions in the action stanza are optional but at least one must be present.

Example:

    [action "Start Progress"]
    change=created

    [action "Resolve Issue"]
    verified=+1
    code-review=+2

    [action "Close Issue"]
    change=merged

    [action "Stop Progress"]
    change=abandoned

The above example defines four status transition on Jira, based on the following conditions:

* Whenever a new Change-set is created on Gerrit, start progress on the Jira issue
* Whenever a change is verified and reviewed with +2, transition the Jira issue to resolved
* Whenever a change is merged to branch, mark the Jira transition the Jira issue to closed
* Whenever a change is abandoned, stop the progress on Jira issue