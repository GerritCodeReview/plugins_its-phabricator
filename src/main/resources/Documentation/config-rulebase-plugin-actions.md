@PLUGIN@-specific actions
=========================

In addition to the [basic actions][basic-actions], @PLUGIN@ also
provides:

[`add-project`][action-add-project]
: adds a project to the task

[`remove-project`][action-remove-project]
: removes a project from the task

[basic-actions]: config-rulebase-common.html#actions

[action-add-project]: #action-add-project
### <a name="action-add-project">Action: add-project</a>

The `add-project` action adds a project to the task. The first
parameter is the project name to add. So for example

```
  action = add-project MyCoolProject
```

adds the project `MyCoolProject` to the task.

[action-remove-project]: #action-remove-project
### <a name="action-remove-project">Action: remove-project</a>

The `remove-project` action removes a project from the task. The first
parameter is the project name to remove. So for example

```
  action = remove-project MyCoolProject
```

removes the project `MyCoolProject` from the task.


[Back to @PLUGIN@ documentation index][index]

[index]: index.html