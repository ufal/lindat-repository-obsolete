# coding=utf-8
# See main file for licence
# pylint: disable=W0702,R0201

"""
  Settings module.
"""
import os
import re


def empty_check(f):
    return 0, "<plain text>"

settings = {


    # name
    "name": u"Ufal file integrity checker",

    # logger config - read from _logger
    "logger_config": os.path.join( os.path.dirname(__file__),
                                   "logger.config"),

    # check for db params
    "dspace_cfg_relative": "../../../config/dspace.cfg",
    "config_dist_relative": [
      "../../../../config/local.conf",
      "../../../../../config/local.conf",
    ],

    # assetstore structure
    "input_dir_glob": "*/*/*/*",

    "mime_type": {
        "application/zip": "unzip -t %s",
        "application/x-gzip": "gunzip -t %s",
        "application/x-bzip2": "bunzip2 -t %s",
        "text/plain": empty_check,
    }

}  # settings

