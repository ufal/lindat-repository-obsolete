# coding=utf-8
# This work is licensed!
# pylint: disable=W0702,R0201,C0111,W0613,R0914

"""
  Main entry point.
"""

import sys
import logging
import getopt
import time
import os
from settings import settings as settings_inst
import utils
# initialise logging
logging.basicConfig(level=logging.DEBUG, format='%(asctime)-15s %(message)s')
_logger = logging.getLogger()


def get_credentials( env ):
    """
        Parse dspace.cfg if found
    """
    for dspace_cfg in env["config_dist_relative"]:
        dspace_cfg = os.path.join( os.getcwd( ), dspace_cfg )
        if os.path.exists( dspace_cfg ):
            env["config_dist_relative"][0] = dspace_cfg
            break
    dspace_cfg = os.path.join( os.getcwd( ), env["config_dist_relative"][0] )
    if not os.path.exists( dspace_cfg ):
        _logger.info( "Could not find [%s]", dspace_cfg )
        dspace_cfg, prefix = os.path.join( os.getcwd( ), env["dspace_cfg_relative"] ), ""
        # try dspace.cfg
        if not os.path.exists( dspace_cfg ):
            _logger.info( "Could not find [%s]", dspace_cfg )
            dspace_cfg = None

    # not found
    if dspace_cfg is None:
        return None, None, None

    # get the variables
    #
    _logger.info( "Parsing [%s]", dspace_cfg )

    conf_db = "lr.db.url"
    conf_db_name = "lr.database"
    conf_u = "lr.db.username"
    conf_p = "lr.db.password"

    db_username = None
    db_pass = None
    db = None
    if os.path.exists( dspace_cfg ):
        lls = [ x.strip() for x in open( dspace_cfg, "r" ).readlines() ]
        for l in lls:
            if l.startswith( conf_u ):
                db_username = l.split( "=" )[1].strip( )
            elif l.startswith( conf_p ):
                db_pass = l.split( "=" )[1].strip( )
            elif l.startswith( conf_db ):
                db = l.split( "=" )[1].strip( )
                db = "/".join(db.split( "/" )[:-1]).strip()
            elif l.startswith( conf_db_name ):
                db_name = l.split( "=" )[1].strip( )
    return db_name, db_username, db_pass



class dspace_db(object):

    def __init__(self, env):
        self.env = env
        self.con = None
        db, u, p = get_credentials( self.env )
        if db is None:
            _logger.info( "DSpace config could not be parsed correctly" )
            return
        _logger.info( "Trying to connect to [%s] under [%s]", db, u )
        import bpgsql
        self.con = bpgsql.connect(
            username=u, password=p, host="127.0.0.1", dbname=db )
        self.cursor = None

    def ok(self):
        return self.con is not None

    def __enter__(self):
        self.cursor = self.con.cursor( )
        return self.cursor

    def __exit__(self, type, value, traceback):
        if self.cursor is not None:
            self.cursor.close( )
        self.con.close( )


#=======================================
# important stuff
#=======================================

def metadata(env):
    """
        Metadata info.
    """
    dspace = dspace_db(env)
    if dspace.ok():
        from ftor import do
        with dspace as c:
            do(c)


#=======================================
# help
#=======================================

def help_msg(env):
    """ Returns help message. """
    _logger.warning( u"""\n%s help. Supported commands:\n""", env["name"] )


def version_msg(env):
    """ Return the current application version. """
    return _logger.warning( u"Version: %s", env["name"] )


#=======================================
# command line
#=======================================

def parse_command_line(env):
    """ Parses the command line arguments. """
    try:
        options = []
        input_options = sys.argv[1:]
        opts, _ = getopt.getopt( input_options, "", options )
    except getopt.GetoptError:
        help_msg( env )
        sys.exit( 1 )

    # for option, param in opts:
    #     if option == "--help":
    #         env["print_info"] = False
    #         return help_msg
    #     if option == "--version":
    #         return version_msg
    #     if option == "--metadata":
    #         what_to_do = metadata
    #
    # if what_to_do:
    #     return what_to_do
    #
    # what to do but really?
    return metadata


#=======================================
# main
#=======================================

if __name__ == "__main__":
    lasted = time.time( )

    _logger.info( u"Starting at " + utils.host_info( ) )

    # do what was specified or default
    ret_code = 0
    try:
        what_to_do_callable = parse_command_line( settings_inst )
        ret_code = what_to_do_callable( settings_inst )
    except Exception, e_inst:
        _logger.critical( "An exception occurred, ouch:\n%s", e_inst )
        raise
    finally:
        lasted = time.time( ) - lasted
        _logger.info( "Stopping after [%f] secs.", lasted )
    sys.exit( ret_code )
