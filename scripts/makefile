#
# Makefile for this project
# by LINDAT/CLARIN dev team
#
# Note: if you want to update this makefile, create a copy e.g., makefile.project and
# invoke make with "-f makefile.project" parameter
#
# Why makefile? Repository consists of several applictions and tools which 
# must be controlled on different levels:
# - shibboleth
# - handle server
# - apache, tomcat
# - DSpace 
# - cronjobs
#

SHELL=/bin/bash

#======================================================
# Basic dirs
#======================================================
 
# Directories and files

BASE				:=$(patsubst %/,%,$(dir $(abspath $(lastword $(MAKEFILE_LIST)))))
PROJECT_DIR			:=$(BASE)/..
DIR_CONF            :=$(PROJECT_DIR)/config
DIR_SUBST           :=$(DIR_CONF)/_substituted
DIR_CONF_TEMPLATE   :=$(DIR_CONF)/template

DIR_SOURCE          :=$(PROJECT_DIR)/sources
DIR_BUILD           :=$(DIR_SOURCE)/dspace/target/dspace-build
DIR_UTILITIES       :=$(DIR_SOURCE)/utilities
DIR_DEPLOY_LOGS     :=$(PROJECT_DIR)/logs

#======================================================
# Include generated variable file 
#======================================================

# either include variable from config/ or from config/dist 

VARIABLE_FILE       :=$(DIR_CONF)/variable.makefile
VARIABLE_FILE_DIST  :=$(DIR_CONF_TEMPLATE)/variable.makefile

ifneq ("$(wildcard $(VARIABLE_FILE))","")
  $(info Using variables from $(VARIABLE_FILE))
  include $(VARIABLE_FILE)
else
  $(info Using variables from $(VARIABLE_FILE_DIST))
  include $(VARIABLE_FILE_DIST)
endif


#======================================================
# Other variables
#======================================================

# LINDAT/CLARIN specific

LOCAL_CONF              :=$(DIR_CONF)/local.conf
URL_LINDAT_COMMON_GIT   :=https://redmine.ms.mff.cuni.cz/lindat/lindat-common.git

DATABASE_NAME           :=$(shell cd $(BASE) && python setup.lindat.py --get="lr.database" --from="$(LOCAL_CONF)")
UTILITIES_DATABASE_NAME :=$(shell cd $(BASE) && python setup.lindat.py --get="lr.utilities.database" --from="$(LOCAL_CONF)")
DIR_INSTALLATION        :=$(shell cd $(BASE) && python setup.lindat.py --get="lr.dspace.dir" --from="$(LOCAL_CONF)")
DIR_LINDAT_COMMON_THEME :=$(shell cd $(BASE) && python setup.lindat.py --get="lr.common.theme" --from="$(LOCAL_CONF)")

DIR_LINDAT_THEME_IN_WEBAPPS :=$(DIR_INSTALLATION)/webapps/xmlui/themes/UFAL/lib/lindat
DIR_DATABASE_BACKUP         :=$(DIR_INSTALLATION)/../database


# Dspace Configuration

DSPACE_BIN              :=${DIR_INSTALLATION}/bin/dspace
DSPACE_CFG_BASENAME     :=dspace.cfg
DSPACE_CFG              :=$(DIR_SOURCE)/dspace/config/$(DSPACE_CFG_BASENAME)
DSPACE_CFG_EXISTS       :=$(shell (if [ -e $(DSPACE_CFG) ]; then echo 1; else echo 0; fi ))
DSPACE_MVN_PROJECTS     :=-P 'xpdf-mediafilter-support,!dspace-jspui,!dspace-sword-client,!dspace-swordv2,!dspace-sword,!dspace-lni'
# do not change this name unless you change it also in sources e.g., HibernateFunctionalityManager.java
# - TODO
SUBSTITUTED_DSPACE_CFG  :=$(DIR_SUBST)/$(DSPACE_CFG_BASENAME).substituted

# LR Configuration

LR_CFG_BASENAME     	:=lr.cfg
LR_CFG              	:=$(DIR_CONF)/template/$(LR_CFG_BASENAME)
LR_CFG_EXISTS			:=$(shell (if [ -e $(LR_CFG) ]; then echo 1; else echo 0; fi ))
SUBSTITUTED_LR_CFG  	:=$(DIR_SOURCE)/dspace/config/modules/$(LR_CFG_BASENAME)

# Build time - added to substituted config
BUILD_TIME:="\#UFAL Build time\nufal.build_time=`date`"

# makefile specific

MAKE+='--no-print-directory'
GREEN   =\e[0;32m
BLUE    =\e[0;34m
RED     =\e[0;31m
CYAN    =\e[0;36m
NORMAL  =\e[0m

#

TMP_FILE:=$(shell mktemp)

#

DATABASE_RESTORE_DIRECTORY=.
# TODO: from backup.conf
DATABASE_BACKUP_DIRECTORY=/home/backup

# tools

ANT_WITH_CONF:=ant -Dconfig="$(SUBSTITUTED_DSPACE_CFG)" -Doverwrite=true
HIDE_PASSW:=sed '/Password/Id'


#======================================================
# Goals
#======================================================

#======================================================
# setup related
#

setup:
	./setup.prerequisites.sh

add_cronjobs:
	(crontab -l ; echo "00 04     * * 1     cd $(BASE); sudo make send_info") | crontab -
	(crontab -l ; echo "00 03     * * *     cd $(BASE); sudo make update_oai") | crontab -
	(crontab -l ; echo "00 02     * * *     cd $(BASE); sudo make update_statistics") | crontab -
	(crontab -l ; echo "00 01     * * *     cd $(BASE); sudo make update_sitemap") | crontab -
	(crontab -l ; echo "30 00     * * *     cd $(BASE); sudo make lift_embargos") | crontab -
	(crontab -l ; echo "30 01     * * *     cd $(BASE); sudo make update_openaire_cache") | crontab -

print_message:
	@echo -e "$(GREEN)-- IMPORTANT INFORMATION  ---------------------------------------------$(NORMAL)"; 
	@echo -e "$(CYAN)SOURCE DIRECTORY = $(DIR_SOURCE)"; 
	@echo "INSTALLATION DIRECTORY = ${DIR_INSTALLATION}"; 
	@echo "LOCAL CONFIG FILE = ${LOCAL_CONF}"; 
	@echo "DATABASE_NAME  = ${DATABASE_NAME}";
	@echo -e "$(GREEN)=======================================================================$(NORMAL)";


#======================================================
# build related - most used
#

new_deploy:     compile \
                fresh_install \
                postinstall

deploy:         compile \
                install \
                postinstall

deploy_guru:    compile \
                install_guru \
                postinstall

#======================================================
# build related
# $@ - name of target
#

# This command cleans all the target directories from source produced by ant.
deploy_logs:
	@mkdir -p $(DIR_DEPLOY_LOGS)

clean_source: deploy_logs
	@rm -f $(DIR_DEPLOY_LOGS)/$@.log
	@cd $(DIR_SOURCE) && mvn $(DSPACE_MVN_PROJECTS) clean 2>&1 | $(HIDE_PASSW) | tee $(DIR_DEPLOY_LOGS)/$@.log

clean_installation:
	@cd $(DIR_BUILD) && $(ANT_WITH_CONF) clean_backups 2>&1 | $(HIDE_PASSW) | tee  $(DIR_DEPLOY_LOGS)/$@.log
	
substitute_configs:
	@echo "Substituting config values using $(LOCAL_CONF)"
	@if [ "$(DSPACE_CFG_EXISTS)" = "0" ]; then echo -e "$(RED)dpsace.cfg not found.$(NORMAL)" && exit -1; fi
	@cd $(BASE) && python setup.lindat.py \
	    --substitute="$(DSPACE_CFG)" \
	    --with="$(LOCAL_CONF)" \
	    --to="$(SUBSTITUTED_DSPACE_CFG)"
	@if [ "$(LR_CFG_EXISTS)" = "0" ]; then echo -e "$(RED)lr.cfg not found.$(NORMAL)" && exit -1; fi
	@cd $(BASE) && python setup.lindat.py \
	    --substitute="$(LR_CFG)" \
	    --with="$(LOCAL_CONF)" \
	    --to="$(SUBSTITUTED_LR_CFG)"	    
	@echo -e $(BUILD_TIME) >> "$(SUBSTITUTED_DSPACE_CFG)"
	@cd $(BASE) && python setup.lindat.py \
	    --substitute="$(DIR_CONF)/description*xml" \
	    --substitute-alternative="$(DIR_CONF_TEMPLATE)/description*xml" \
	    --with="$(LOCAL_CONF)" \
	    --to="$(DIR_SOURCE)/dspace/config/crosswalks/oai/"

compile: deploy_logs substitute_configs
	@echo "COMPILING SOURCE ..."
	@cd $(DIR_SOURCE) && mvn $(DSPACE_MVN_PROJECTS) package 2>&1 | $(HIDE_PASSW) | tee $(DIR_DEPLOY_LOGS)/$@.log
	@echo "INSTALLING utilities ..."
	@cd $(DIR_SOURCE)/utilities && mvn install -Dlicense.skip=true 2>&1 | tee -a $(DIR_DEPLOY_LOGS)/$@.log
	@tail -n 7 $(DIR_DEPLOY_LOGS)/$@.log

install:        substitute_configs \
                print_message \
                test_databases \
                user_prompt \
                install_dspace

install_guru:   substitute_configs \
                print_message \
                install_dspace

install_dspace: 
	@echo "DEPLOYING LINDAT/CLARIN DSpace ..."
	@cd $(DIR_BUILD) &&  $(ANT_WITH_CONF) update 2>&1 | $(HIDE_PASSW) | tee $(DIR_DEPLOY_LOGS)/$@.log 
	@$(MAKE) clean_installation
	@echo "INSTALLATION SUCCESSFUL ..."

postinstall: update_lindat_common
	rm -rf $(DIR_LINDAT_THEME_IN_WEBAPPS)
	ln -s $(DIR_LINDAT_COMMON_THEME) $(DIR_LINDAT_THEME_IN_WEBAPPS)
	@$(MAKE) grant_rights
	@echo "DEPLOYMENT (INSTALL + POSTINSTALL) SUCCESSFUL"

fresh_install: substitute_configs update_lindat_common
	@echo "INSTALLING FRESH LINDAT/CLARIN DSpace ..."
	@cd $(DIR_BUILD) && $(ANT_WITH_CONF) fresh_install 2>&1 | $(HIDE_PASSW) | tee $(DIR_DEPLOY_LOGS)/$@.log 

# Grant rights to installation
grant_rights:
	sudo chown -R $(TOMCAT_USER):$(TOMCAT_GROUP) $(DIR_INSTALLATION)
	sudo chmod -R g+w $(DIR_INSTALLATION)
	
tools_version_check:
	@cd $(BASE) && python setup.lindat.py --ant-version=1.8
	@cd $(BASE) && python setup.lindat.py --mvn-version=3.0

user_prompt: tools_version_check
	@echo ""
	@if grep -q "BUILD FAILURE" $(DIR_DEPLOY_LOGS)/compile.log; then \
	        echo -e "$(RED)compile.log contains BUILD FAILURE$(NORMAL)" && exit -1; fi
	@echo ""
	@echo -e "$(RED)Do you really wish to continue? (for Yes type Yes)$(NORMAL)"
	@python -c 'import sys; sys.exit(-1) if raw_input() != "Yes" else None'



#======================================================
# databased related
#

test_dspace_database: substitute_configs
	@cd $(DIR_BUILD) && $(ANT_WITH_CONF) -logger org.apache.tools.ant.listener.AnsiColorLogger test_database | $(HIDE_PASSW)

test_utilities_database: substitute_configs
	@cd $(DIR_SOURCE)/utilities && mvn exec:java -Dexec.mainClass="cz.cuni.mff.ufal.lindat.utilities.HibernateFunctionalityManager"

test_databases: test_utilities_database test_dspace_database

create_databases:
	@echo "CREATING DSpace DATABASE .."
	sudo -u postgres createdb "$(DATABASE_NAME)" --owner $(DSPACE_USER) --encoding "UTF-8" --template=template0
	@echo "CREATING UTILITIES DATABASE ..."
	sudo -u postgres createdb "$(UTILITIES_DATABASE_NAME)" --owner $(DSPACE_USER) --encoding "UTF-8" --template=template0
	sudo -u postgres psql $(UTILITIES_DATABASE_NAME) < $(DIR_UTILITIES)/licenses.sql
	@echo "DONE"

update_databases:
	@echo "UPDATING UTILITIES DATABASE ..."
	@echo "RUN e.g., sudo -u postgres psql $(UTILITIES_DATABASE_NAME) < ../sources/utilities/licenses.2014.04.25.sql"
	@echo "UPDATING DSpace DATABASE ..."
	@echo "RUN e.g., sudo -u postgres psql $(DATABASE_NAME) < ./upgrades/dspace.2014.03.18.sql"
	@echo DONE

update_metadata_scheme:
	@echo "Updating metadata scheme with local scheme"
	cd ${DIR_INSTALLATION}/bin; ./dspace registry-loader -dc ../config/registries/local-types.xml

setup_database: substitute_configs
	@cd $(DIR_BUILD) && $(ANT_WITH_CONF) setup_database
	
restore_database:
	@echo "RESTORING DATABASE $(DATABASE_NAME) FROM $(RESTORE_FROM_DATABASE) ..."
	@sudo -u postgres pg_dump --clean "$(RESTORE_FROM_DATABASE)" | sudo -u postgres psql "$(DATABASE_NAME)" > $(DIR_DEPLOY_LOGS)/$@.log
	@echo "DONE"


#======================================================
# build related - advanced
#

update_code: substitute_configs
	@echo "UPDATING CODE ..."
	@cd $(DIR_BUILD) &&  $(ANT_WITH_CONF) update_code 2>&1 | $(HIDE_PASSW) | tee $(DIR_DEPLOY_LOGS)/$@.log 
	@echo "SUCCESSFUL"

update_config: substitute_configs
	@echo "UPDATING CONFIG ..."
	@cd $(DIR_BUILD) &&  $(ANT_WITH_CONF) update_configs 2>&1 | $(HIDE_PASSW) | tee $(DIR_DEPLOY_LOGS)/$@.log 
	@echo "SUCCESSFUL"


#======================================================
# lindat theme related
#

check_lindat_common:
	@echo "Checking lindat common theme into $(DIR_LINDAT_COMMON_THEME)... "
	@test -d $(DIR_LINDAT_COMMON_THEME)/.git || git clone $(URL_LINDAT_COMMON_GIT) $(DIR_LINDAT_COMMON_THEME)

update_lindat_common: check_lindat_common
	cd $(DIR_LINDAT_COMMON_THEME) && git checkout -f $(LINDAT_COMMON_THEME_BRANCH) && git pull


#======================================================
# apache/tomcat related
#

# Command starts all running services to provide webserver (to fully $(MAKE) them run)
start:  start_postgres \
		start_tomcat \
        start_shibboleth \
        start_handle_server \
        start_apache

# Command stops all running services to provide webserver (to fully dislocate them)
stop:	stop_apache \
        stop_handle_server \
        stop_shibboleth \
        stop_tomcat \
        stop_postgres

# This command just restart all the services that are needed to fully reload the apache, tomcat and shibboleth
restart:
	@$(MAKE) stop
	@sleep 3 ;
	@$(MAKE) start

start_apache:
	@echo "Starting apache"	
	@sh $(APACHE) start

stop_apache:
	@echo "Stopping apache"
	@sh $(APACHE) stop

# Command starts tomcat
start_tomcat:
	@echo "Starting tomcat"
	@sh $(TOMCAT) start

# Command stops tomcat
stop_tomcat:
	@echo "Stopping tomcat"
	@sh $(TOMCAT) stop

start_shibboleth:
	@echo "Starting shibboleth"
	@mkdir -p /var/run/shibboleth
	@if [[ -f $(SHIB) ]]; then sh $(SHIB) start; else echo "Shibboleth [$(SHIB)] not present!"; fi

stop_shibboleth:
	@echo "Stopping shibboleth"
	@sh $(BASE)/stop.shibboleth.sh

stop_postgres:
	@echo "Stopping postgres"
	@sh $(POSTGRES) stop
	
start_postgres:
	@echo "Starting postgres"
	@sh $(POSTGRES) start

start_handle_server:
	@if [[ -r $(HANDLE_SERVER) ]]; then echo "Starting handle server"; sh $(HANDLE_SERVER) start; else echo "Handle server not present - ignoring start command"; fi

stop_handle_server:
	@if [[ -r $(HANDLE_SERVER) ]]; then echo "Stopping handle server"; sh $(HANDLE_SERVER) stop; else echo "Handle server not present - ignoring stop command"; fi

#======================================================
# dspace helpers
#

# This command inits statistics that has to be updated to be viewed correctly
init_statistics:
	$(DSPACE_BIN) stat-initial
	$(DSPACE_BIN) stat-report-initial

# This command update statistics that has to be updated to be viewed correctly
update_statistics:
	$(DSPACE_BIN) stat-general
	$(DSPACE_BIN) stat-monthly
	$(DSPACE_BIN) stat-report-general
	$(DSPACE_BIN) stat-report-monthly

# This command updates the sitemap
update_sitemap:
	$(DSPACE_BIN) generate-sitemaps -a

lift_embargos:
	$(DSPACE_BIN) embargo-lifter -v

# This command updates the sitemap
send_info:
	$(DSPACE_BIN) ufal-info -e default

# This command init indicies that has to be updated to be viewed correctly
init_indicies:
	$(DSPACE_BIN) index-init

update_indicies:
	$(DSPACE_BIN) index-update

update_oai:
	LC_ALL=en_US.UTF-8 $(DSPACE_BIN) oai import

# Rebuild oai solr from scratch
rebuild_oai:
	LC_ALL=en_US.UTF-8 $(DSPACE_BIN) oai import -c 

update_discovery:
	@echo 'Updating solr dicovery indicies ...'
	$(DSPACE_BIN) update-discovery-index

generate_handle_list:
	@sudo -u $(DSPACE_USER) psql $(DATABASE_NAME) -c "select handle from handle where resource_id in (select item_id from item where in_archive = 't' and withdrawn = 'f');" | tail -n +3 | head -n -2

update_openaire_cache:
	@sudo -u $(TOMCAT_USER) $(DIR_INSTALLATION)/bin/openaire-refresh-list


#======================================================
# backups
#

substitute_backup_config:
	@cd $(BASE) && python setup.lindat.py \
	    --substitute="$(DIR_CONF_TEMPLATE)/backup2l.conf" \
	    --substitute-alternative="$(DIR_CONF)/backup2l.conf" \
	    --with="$(LOCAL_CONF)" \
	    --to="$(DIR_SUBST)/backup2l.conf.substituted"

# This command makes backup for databases and put it into backuped directory $(DIR_DATABASE_BACKUP)
backup_databases:
	su postgres -l -c 'pg_dump $(DATABASE_NAME) > $(DIRECTORY_POSTGRESQL)/$(DATABASE_NAME)'
	su postgres -l -c 'pg_dump $(UTILITIES_DATABASE_NAME) > $(DIRECTORY_POSTGRESQL)/$(UTILITIES_DATABASE_NAME)'
	# with timestapm
	cp $(DIRECTORY_POSTGRESQL)/$(DATABASE_NAME) $(DIR_DATABASE_BACKUP)/$(DATABASE_NAME).dumped.`date +"%d-%m-%y.at.%T"`
	cp $(DIRECTORY_POSTGRESQL)/$(UTILITIES_DATABASE_NAME) $(DIR_DATABASE_BACKUP)/$(UTILITIES_DATABASE_NAME).dumped.`date +"%d-%m-%y.at.%T"`
	# latest one
	mv $(DIRECTORY_POSTGRESQL)/$(DATABASE_NAME) $(DIR_DATABASE_BACKUP)/$(DATABASE_NAME)
	mv $(DIRECTORY_POSTGRESQL)/$(UTILITIES_DATABASE_NAME) $(DIR_DATABASE_BACKUP)/$(UTILITIES_DATABASE_NAME)

# This is core backuping command. Here we have all the backups of all the key configurations.
backup_dspace: backup_databases substitute_backup_config
	sudo mkdir -p $(DATABASE_BACKUP_DIRECTORY)
	$(BACKUP2l) -c $(DIR_SUBST)/backup2l.conf.substituted -b >> $(DIR_DEPLOY_LOGS)/$@.log
	@echo 'Done backuping.'

restore_backup: substitute_backup_config
	sudo mkdir -p $(DATABASE_RESTORE_DIRECTORY)
	@echo 'Restoring from backup ...'
	cd $(DATABASE_RESTORE_DIRECTORY) && $(BACKUP2l) -c $(DIR_BACKUP_CONFIGURATION)/backup2l.conf.substituted -r >> $(DIR_DEPLOY_LOGS)/$@.log
	@echo 'Done'


#======================================================
# tests
#

selenium_tests:
	@cd $(BASE)/../sources/tests; export DISPLAY=:99; /etc/init.d/xvfb start; ant
	@/etc/init.d/xvfb stop
