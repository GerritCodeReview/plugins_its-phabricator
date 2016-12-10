load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "gerrit_plugin",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
)

gerrit_plugin(
  name = 'its-phabricator',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(["src/main/**/*"]),
  manifest_entries = [
    'Gerrit-Module: com.googlesource.gerrit.plugins.its.phabricator.PhabricatorModule',
    'Gerrit-ReloadMode: reload',
    'Gerrit-ApiType: plugin',
    'Implementation-Title: Plugin its-phabricator',
    'Implementation-Vendor: Wikimedia Foundation',
    'Implementation-URL: https://gerrit.googlesource.com/plugins/its-phabricator',
  ],
  deps = [
    '//lib/httpcomponents:httpcore',
    '//lib/httpcomponents:httpclient',
    '//lib:gson',
  ],
)

junit_tests(
    name = "its-phabricator_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["its-phabricator"],
    deps = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":its-phabricator__plugin",
        "//plugins/its-base:its-base_tests-utils",
        "//lib/easymock:easymock",
        "//lib:guava",
        "//lib/guice:guice",
        "//lib:junit",
        "//lib/log:api",
        "//lib/log:impl_log4j",
        "//lib/log:log4j",
        "//lib/powermock:powermock-api-easymock",
        "//lib/powermock:powermock-api-support",
        "//lib/powermock:powermock-core",
        "//lib/powermock:powermock-module-junit4",
        "//lib/powermock:powermock-module-junit4-common",
        "//lib/powermock:powermock-reflect",
        "//lib/httpcomponents:httpclient",
        "//lib:gson",
    ],
)
