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
        "Implementation-Vendor: Wikimedia Foundation",
        "Implementation-URL: https://gerrit.googlesource.com/plugins/its-phabricator",
    ],
    resources = glob(["src/main/**/*"]),
    deps = [
        "//lib:gson",
        "//lib/httpcomponents:httpclient",
        "//lib/httpcomponents:httpcore",
        "//plugins/its-base",
    ],
)

junit_tests(
    name = "its_phabricator_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["its-phabricator"],
    deps = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":its-phabricator__plugin",
        "//plugins/its-base:its-base",
        "//plugins/its-base:its-base_tests-utils",
    ],
)
