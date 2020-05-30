import os
import sys

# As this plugin is typically only sym-linked into a gerrit checkout and both os.getcwd and
# os.path.abspath follow symbolic links, they would not allow us to find the gerrit root
# directory. So we have to resort to the PWD environment variable to find the place we're
# symlinked to.
#
# We append __file__ to avoid having to require to run it from a well-know directory.
ABS_FILE_PARTS = os.path.join(os.getenv('PWD'), __file__).split(os.sep)
PLUGIN_NAME = ABS_FILE_PARTS[-3]
GERRIT_ROOT = os.sep.join(ABS_FILE_PARTS[:-4])

sys.path = [GERRIT_ROOT] + sys.path
from tools.workspace_status import revision

def get_plugin_revision(name):
    os.chdir(os.path.join(GERRIT_ROOT, 'plugins', name))
    ret=revision(GERRIT_VERSION)
    return ret

os.chdir(GERRIT_ROOT)
GERRIT_VERSION=revision()

ITS_BASE_VERSION=get_plugin_revision('its-base')
PLUGIN_RAW_VERSION=get_plugin_revision(PLUGIN_NAME)

PLUGIN_FULL_VERSION="%s(its-base:%s)" % (PLUGIN_RAW_VERSION, ITS_BASE_VERSION)

print("STABLE_BUILD_%s_LABEL %s" % (PLUGIN_NAME.upper(), PLUGIN_FULL_VERSION))
