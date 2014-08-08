<?xml version="1.0" encoding="UTF-8" ?>
<!-- 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:doc="http://www.lyncode.com/xoai" 
    xmlns:itemUtil="cz.cuni.mff.ufal.utils.ItemUtil"
    xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:configuration="org.dspace.core.ConfigurationManager"
    xmlns:ms="http://www.ilsp.gr/META-XMLSchema"
    xmlns:olac="http://experimental.loc/olac"
    xmlns:cmd="http://www.clarin.eu/cmd/"
    exclude-result-prefixes="doc xalan itemUtil configuration ms"
    version="1.0">
    <xsl:import href="metadataFormats/metasharev2.xsl"/>
    <xsl:import href="metadataFormats/olac-dcmiterms.xsl"/>
    
    <xsl:output omit-xml-declaration="yes" method="xml" indent="yes" xalan:indent-amount="4"/>
    <xsl:namespace-alias stylesheet-prefix="ms" result-prefix="cmd"/>
    <!-- #default probably not working -->
    <xsl:namespace-alias stylesheet-prefix="olac" result-prefix="cmd"/>


    <xsl:variable name="handle" select="/doc:metadata/doc:element[@name='others']/doc:field[@name='handle']/text()"/>
    <xsl:variable name="dc_identifier_uri" select="/doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='uri']/doc:element/doc:field[@name='value']"/>
    <xsl:variable name="modifyDate" select="substring-before(/doc:metadata/doc:element[@name='others']/doc:field[@name='lastModifyDate']/text(),' ')"/>
    <xsl:variable name="dsURL" select="configuration:getProperty('dspace.url')"/>
    
    <xsl:template match="/">
        <xsl:variable name="uploaded_md" select="itemUtil:getUploadedMetadata($handle)"/>
        <xsl:choose>
            <xsl:when test="$uploaded_md != ''">
                <xsl:copy-of select="$uploaded_md"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="ConstructCMDI"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="ConstructCMDI">
        <cmd:CMD CMDVersion="1.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.clarin.eu/cmd/ http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1349361150622/xsd">
            <xsl:call-template name="AdministrativeMD"/>
            <xsl:call-template name="Components"/>
        </cmd:CMD>
    </xsl:template>
    
    <xsl:template name="AdministrativeMD">
        <xsl:call-template name="Header"/>
        <xsl:call-template name="Resources"/>
    </xsl:template>
    
    <xsl:template name="Header">
        <cmd:Header>
            <cmd:MdCreationDate><xsl:value-of select="$modifyDate"/></cmd:MdCreationDate>
            <cmd:MdSelfLink><xsl:value-of select="$dc_identifier_uri"/>@format=cmdi</cmd:MdSelfLink>
            <cmd:MdProfile>clarin.eu:cr1:p_1349361150622</cmd:MdProfile>
            <cmd:MdCollectionDisplayName><xsl:value-of select="/doc:metadata/doc:element[@name='others']/doc:field[@name='owningCollection']/text()"/></cmd:MdCollectionDisplayName>
        </cmd:Header>
    </xsl:template>

	<xsl:template name="Resources">
		<cmd:Resources>
			<cmd:ResourceProxyList>
				<cmd:ResourceProxy>
					<xsl:attribute name="id">lp_<xsl:value-of select="/doc:metadata/doc:element[@name='others']/doc:field[@name='itemId']/text()" /></xsl:attribute>
					<cmd:ResourceType>LandingPage</cmd:ResourceType>
					<cmd:ResourceRef>
						<xsl:value-of select="$dc_identifier_uri" />
					</cmd:ResourceRef>
				</cmd:ResourceProxy>
				<xsl:call-template name="ProcessSourceURI"/>
				<xsl:call-template name="ProcessBitstreams"/>
			</cmd:ResourceProxyList>
			<cmd:JournalFileProxyList/>
			<cmd:ResourceRelationList/>
		</cmd:Resources>
	</xsl:template>
	
	<!-- Omit the special "ORE" bitstream and also the consent to publish the data -->
	<xsl:template name="ProcessBitstreams">
	   <xsl:for-each select="/doc:metadata/doc:element[@name='bundles']/doc:element[@name='bundle']/doc:field[@name='name' and text()!='ORE' and text()!='LICENSE']/../doc:element[@name='bitstreams']/doc:element[@name='bitstream']">
	       <cmd:ResourceProxy>
	                   <xsl:attribute name="id">_<xsl:value-of select="./doc:field[@name='id']/text()"/></xsl:attribute>
                       <cmd:ResourceType><xsl:attribute name="mimetype"><xsl:value-of select="./doc:field[@name='format']/text()"/></xsl:attribute>Resource</cmd:ResourceType>
                       <cmd:ResourceRef><xsl:value-of select="concat($dsURL,'/bitstream/handle/',$handle,'/',./doc:field[@name='name']/text(),'?sequence=',./doc:field[@name='sid']/text())"/></cmd:ResourceRef>
           </cmd:ResourceProxy>
	   </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="ProcessSourceURI">
	   <xsl:for-each select="xalan:distinct(doc:metadata/doc:element[@name='dc']/doc:element[@name='source']/doc:element[@name='uri']/doc:element/doc:field[@name='value'])">
	       <cmd:ResourceProxy>
	           <xsl:attribute name="id">uri_<xsl:value-of select="position()"/></xsl:attribute>
	           <cmd:ResourceType><xsl:attribute name="mimetype">text/html</xsl:attribute>Resource</cmd:ResourceType>
	           <cmd:ResourceRef><xsl:value-of select="."/></cmd:ResourceRef>
	       </cmd:ResourceProxy>
	   </xsl:for-each>
	</xsl:template>
	
	<xsl:template name="Components">
		<cmd:Components>
			<cmd:data>
				<xsl:call-template name="OLAC_DCMI"/>
				<xsl:call-template name="ResourceInfo">
					<xsl:with-param name="ns" select='"http://www.clarin.eu/cmd/"'/>
				</xsl:call-template>
			</cmd:data>
		</cmd:Components>
	</xsl:template>
</xsl:stylesheet>
