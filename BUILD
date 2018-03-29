load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "gerrit_plugin",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
)

gerrit_plugin(
    name = "its-phabricator",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-Module: com.googlesource.gerrit.plugins.its.phabricator.PhabricatorModule",
        "Gerrit-ReloadMode: reload",
        "Implementation-Title: Plugin its-phabricator",
        "Implementation-URL: https://gerrit.googlesource.com/plugins/its-phabricator",
    ],
    resources = glob(["src/main/**/*"]),
    deps = [
        "//plugins/its-base",
    ],
)

junit_tests(
    name = "its_phabricator_tests",
    testonly = 1,
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["its-phabricator"],
    deps = [
        ":its-phabricator__plugin_test_deps",
    ],
)

java_library(
    name = "its-phabricator__plugin_test_deps",
    testonly = 1,
    visibility = ["//visibility:public"],
    exports = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":its-phabricator__plugin",
        "//plugins/its-base:its-base",
        "//plugins/its-base:its-base_tests-utils",
    ],
)
