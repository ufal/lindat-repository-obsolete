<?xml version="1.0" encoding="UTF-8" ?>
<!-- 


    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/
	Developed by DSpace @ Lyncode <dspace@lyncode.com>
	
	> http://www.openarchives.org/OAI/2.0/oai_dc.xsd

 -->
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:doc="http://www.lyncode.com/xoai"
    xmlns:xalan="http://xml.apache.org/xslt"
    exclude-result-prefixes="doc xalan"
	version="1.0">
	<xsl:output omit-xml-declaration="yes" method="xml" indent="yes" />
	
	<xsl:template match="/">
		<olac:olac 
            xmlns:olac="http://www.language-archives.org/OLAC/1.1/" 
            xmlns:dc="http://purl.org/dc/elements/1.1/"
            xmlns:dcterms="http://purl.org/dc/terms/" 
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
            xsi:schemaLocation="http://www.language-archives.org/OLAC/1.1/ http://www.language-archives.org/OLAC/1.1/olac.xsd"
        >
            <!-- title -->
            <xsl:for-each select="xalan:distinct(doc:metadata/doc:element[@name='dc']/doc:element[@name='title']/doc:element/doc:field[@name='value'])">
                <dc:title><xsl:value-of select="."/></dc:title>
            </xsl:for-each>

            <!-- abstract -->
            <xsl:for-each select="xalan:distinct(doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='abstract']/doc:element/doc:field[@name='value'])">
                <dcterms:abstract><xsl:value-of select="."/></dcterms:abstract>
            </xsl:for-each>

            <!-- available -->
            <xsl:for-each select="xalan:distinct(doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='available']/doc:element/doc:field[@name='value'])[1]">
                <dcterms:available><xsl:value-of select="."/></dcterms:available>
            </xsl:for-each>

            <!-- citation -->
            <xsl:for-each select="xalan:distinct(doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='uri']/doc:element/doc:field[@name='value'])">
                <dcterms:bibliographicCitation><xsl:value-of select="."/></dcterms:bibliographicCitation>
            </xsl:for-each>

            <!-- contributors -->
            <xsl:for-each select="xalan:distinct(doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name!='author']/doc:element/doc:field[@name='value']|doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element/doc:field[@name='value'])">
                <dc:contributor><xsl:value-of select="."/></dc:contributor>
            </xsl:for-each>

            <!-- creator -->
            <xsl:for-each select="xalan:distinct(doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='author']/doc:element/doc:field[@name='value']|doc:metadata/doc:element[@name='dc']/doc:element[@name='creator']/doc:element/doc:field[@name='value'])">
                <dc:creator><xsl:value-of select="."/></dc:creator>
            </xsl:for-each>

            <!-- date -->
            <xsl:for-each select="xalan:distinct(doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='accessioned' or @name='updated']/doc:element/doc:field[@name='value']|doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element/doc:field[@name='value'])[1]">
                <dc:date><xsl:value-of select="."/></dc:date>
            </xsl:for-each>
 
            <!-- submitted -->
            <xsl:for-each select="xalan:distinct(doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='submitted']/doc:element/doc:field[@name='value'])">
                <dcterms:dateSubmitted><xsl:value-of select="."/></dcterms:dateSubmitted>
            </xsl:for-each>

            <!-- description -->
            <xsl:for-each select="xalan:distinct(doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='sponsorship' or @name='statementofresponsibility' or @name='uri' or @name='version']/doc:element/doc:field[@name='value']|doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element/doc:field[@name='value'])">
                <dc:description><xsl:value-of select="."/></dc:description>
            </xsl:for-each>

            <!-- identifier -->
            <xsl:for-each select="xalan:distinct(doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name!='citation']/doc:element/doc:field[@name='value']|doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element/doc:field[@name='value'])">
                <dc:identifier><xsl:value-of select="."/></dc:identifier>
            </xsl:for-each>

            <!-- isreplacedby -->
            <xsl:for-each select="xalan:distinct(doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='isreplacedby']/doc:element/doc:field[@name='value'])">
                <dcterms:isReplacedBy><xsl:value-of select="."/></dcterms:isReplacedBy>
            </xsl:for-each>

            <!-- publisher -->
            <xsl:for-each select="xalan:distinct(doc:metadata/doc:element[@name='dc']/doc:element[@name='publisher']/doc:element/doc:field[@name='value'])">
                <dc:publisher><xsl:value-of select="."/></dc:publisher>
            </xsl:for-each>

            <!-- replaces -->
            <xsl:for-each select="xalan:distinct(doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='replaces']/doc:element/doc:field[@name='value'])">
                <dcterms:replaces><xsl:value-of select="."/></dcterms:replaces>
            </xsl:for-each>

            <!-- rights -->
            <xsl:for-each select="xalan:distinct(doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element[@name='uri']/doc:element/doc:field[@name='value']|doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element/doc:field[@name='value'])">
                <dc:rights><xsl:value-of select="."/></dc:rights>
            </xsl:for-each>

            <!-- subject -->
            <xsl:for-each select="xalan:distinct(doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']//doc:field[@name='value'])">
                <dc:subject><xsl:value-of select="."/></dc:subject>
            </xsl:for-each>

            <!-- type -->
            <xsl:for-each select="xalan:distinct(doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element/doc:field[@name='value'])">
                <dc:type><xsl:value-of select="."/></dc:type>
            </xsl:for-each>

		</olac:olac>
	</xsl:template>
</xsl:stylesheet>
