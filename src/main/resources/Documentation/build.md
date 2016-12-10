Build
=====

This @PLUGIN@ plugin is built with Bazel.

Clone (or link) both this plugin and also
[plugins/its-base](https://gerrit-review.googlesource.com/#/admin/projects/plugins/its-base)
to the `plugins` directory of Gerrit's source tree.

Then issue

```
  bazel build plugins/@PLUGIN@
```

in the root of Gerrit's source tree to build

The output is created in

```
  bazel-genfiles/plugins/its-phabricator/its-phabricator.jar
```

This project can be imported into the Eclipse IDE:

```
  ./tools/eclipse/project.py
```

To execute the tests run:

```
  bazel test plugins/its-phabricator:its_phabricator_tests
```

[Back to @PLUGIN@ documentation index][index]

[index]: index.html
