load("//tools/bzl:plugin.bzl", "gerrit_plugin")

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
    ':its-base_stripped',
    '//lib/httpcomponents:httpcore',
    '//lib/httpcomponents:httpclient',
    '//lib:gson',
  ],
)

def strip_jar(
    name,
    src,
    excludes = [],
    visibility = [],
  ):
  name_zip = name + '.zip'
  genrule(
    name = name_zip,
    cmd = 'cp $SRCS $OUT && zip -qd $OUT ' + ' '.join(excludes),
    srcs = [ src ],
    out = name_zip,
    visibility = visibility,
  )
  prebuilt_jar(
    name = name,
    binary_jar = ':' + name_zip,
    visibility = visibility,
  )

strip_jar(
  name = 'its-base_stripped',
  src = '//plugins/its-base:its-base',
  excludes = [
    'Documentation/about.md',
    'Documentation/build.md',
    'Documentation/config-connectivity.md',
    'Documentation/config-rulebase-plugin-actions.md',
  ]
)

java_test(
  name = 'its-phabricator_tests',
  srcs = glob(['src/test/java/**/*.java']),
  labels = ['its-phabricator'],
  deps = GERRIT_PLUGIN_API + GERRIT_TESTS + [
    ':its-phabricator__plugin',
    '//plugins/its-base:its-base_tests-utils',
    ':its-base_stripped',
    '//lib/easymock:easymock',
    '//lib:guava',
    '//lib/guice:guice',
    '//lib:junit',
    '//lib/log:api',
    '//lib/log:impl_log4j',
    '//lib/log:log4j',
    '//lib/powermock:powermock-api-easymock',
    '//lib/powermock:powermock-api-support',
    '//lib/powermock:powermock-core',
    '//lib/powermock:powermock-module-junit4',
    '//lib/powermock:powermock-module-junit4-common',
    '//lib/powermock:powermock-reflect',
    '//lib/httpcomponents:httpclient',
    '//lib:gson',
  ],
)
