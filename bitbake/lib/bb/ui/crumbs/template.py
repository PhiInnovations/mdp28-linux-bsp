#
# BitBake Graphical GTK User Interface
#
# Copyright (C) 2011        Intel Corporation
#
# Authored by Shane Wang <shane.wang@intel.com>
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License version 2 as
# published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

import gobject
import os
import re

class File(gobject.GObject):

    def __init__(self, pathfilename, suffix):
        if not pathfilename.endswith(suffix):
            pathfilename = "%s%s" % (pathfilename, suffix)
        gobject.GObject.__init__(self)
        self.pathfilename = pathfilename

    def readFile(self):
        if not os.path.isfile(self.pathfilename):
            return None
        if not os.path.exists(self.pathfilename):
            return None

        with open(self.pathfilename, 'r') as f:
            contents = f.readlines()
        f.close()

        return contents

    def writeFile(self, contents):
        if os.path.exists(self.pathfilename):
            orig = "%s.orig" % self.pathfilename
            if os.path.exists(orig):
                os.remove(orig)
            os.rename(self.pathfilename, orig)

        with open(self.pathfilename, 'w') as f:
            f.write(contents)
        f.close()

class ConfigFile(File):
    """
    This object does save general config file. (say bblayers.conf, or local.conf). Again, it is the base class for other template files and image bb files.
    """
    def __init__(self, pathfilename, suffix = None, header = None):
        if suffix:
            File.__init__(self, pathfilename, suffix)
        else:
            File.__init__(self, pathfilename, ".conf")
        if header:
            self.header = header
        else:
            self.header = "# Config generated by Hob\n\n"
        self.dictionary = {}

    def setVar(self, var, val):
        if isinstance(val, list):
            liststr = ""
            if val:
                i = 0
                for value in val:
                    if i < len(val) - 1:
                        liststr += "%s " % value
                    else:
                        liststr += "%s" % value
                    i += 1
            self.dictionary[var] = liststr
        else:
            self.dictionary[var] = val

    def save(self):
        contents = self.header
        for var, val in self.dictionary.items():
            contents += "%s = \"%s\"\n" % (var, val)
        File.writeFile(self, contents)

class HobTemplateFile(ConfigFile):
    """
    This object does save or load hob specific file.
    """
    def __init__(self, pathfilename):
        ConfigFile.__init__(self, pathfilename, ".hob", "# Hob Template generated by Hob\n\n")

    def getVar(self, var):
        if var in self.dictionary: 
            return self.dictionary[var]
        else:
            return ""

    def getVersion(self):
        contents = ConfigFile.readFile(self)

        pattern = "^\s*(\S+)\s*=\s*(\".*?\")"

        for line in contents:
            match = re.search(pattern, line)
            if match:
                if match.group(1) == "VERSION":
                    return match.group(2).strip('"')
        return None

    def load(self):
        contents = ConfigFile.readFile(self)
        self.dictionary.clear()

        pattern = "^\s*(\S+)\s*=\s*(\".*?\")"

        for line in contents:
            match = re.search(pattern, line)
            if match:
                var = match.group(1)
                val = match.group(2).strip('"')
                self.dictionary[var] = val
        return self.dictionary

class RecipeFile(ConfigFile):
    """
    This object is for image bb file.
    """
    def __init__(self, pathfilename):
        ConfigFile.__init__(self, pathfilename, ".bb", "# Recipe generated by Hob\n\ninherit core-image\n")

class TemplateMgr(gobject.GObject):

    __gRecipeVars__ = ["DEPENDS", "IMAGE_INSTALL"]

    def __init__(self):
        gobject.GObject.__init__(self)
        self.template_hob = None
        self.bblayers_conf = None
        self.local_conf = None
        self.image_bb = None

    @classmethod
    def convert_to_template_pathfilename(cls, filename, path):
        return "%s/%s%s%s" % (path, "template-", filename, ".hob")

    @classmethod
    def convert_to_image_pathfilename(cls, filename, path):
        return "%s/%s%s%s" % (path, "hob-image-", filename, ".bb")

    def open(self, filename, path):
        self.template_hob = HobTemplateFile(TemplateMgr.convert_to_template_pathfilename(filename, path))
        self.image_bb = RecipeFile(TemplateMgr.convert_to_image_pathfilename(filename, path))

    def setVar(self, var, val):
        if var in TemplateMgr.__gRecipeVars__:
            self.image_bb.setVar(var, val)

        self.template_hob.setVar(var, val)

    def save(self):
        self.image_bb.save()
        self.template_hob.save()

    def getVersion(self, path):
        return HobTemplateFile(path).getVersion()

    def load(self, path):
        self.template_hob = HobTemplateFile(path)
        self.dictionary = self.template_hob.load()

    def getVar(self, var):
        return self.template_hob.getVar(var)

    def destroy(self):
        if self.template_hob:
            del self.template_hob
            template_hob = None
        if self.image_bb:
            del self.image_bb
            self.image_bb = None
