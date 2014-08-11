<?xml version="1.0" encoding="UTF-8" ?>
<!-- Created for LINDAT/CLARIN based on DIM2DataCite https://guidelines.openaire.eu/wiki/OpenAIRE_Guidelines:_For_Data_Archives -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:odc3="http://datacite.org/schema/kernel-3" xmlns:doc="http://www.lyncode.com/xoai"
	exclude-result-prefixes="doc" version="1.0">
	<xsl:output omit-xml-declaration="yes" method="xml" indent="yes" />

	<xsl:template match="/">
		<oai_datacite xmlns="http://schema.datacite.org/oai/oai-1.0/"
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://schema.datacite.org/oai/oai-1.0/ http://schema.datacite.org/oai/oai-1.0/oai_datacite.xsd">
			<isReferenceQuality>true</isReferenceQuality>
			<schemaVersion>3.0</schemaVersion>
			<datacentreSymbol></datacentreSymbol>
			<payload>
				<resource xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
					xsi:schemaLocation="http://datacite.org/schema/kernel-3 http://schema.datacite.org/meta/kernel-3/metadata.xsd">
					<xsl:call-template name="Identifier_M" />
					<xsl:call-template name="Creator_M" />
					<xsl:call-template name="Title_M" />
					<xsl:call-template name="Publisher_M" />
					<xsl:call-template name="PublicationYear_M" />
					<xsl:call-template name="Subject_R" />
					<xsl:call-template name="Contributor_MAO" />
					<xsl:call-template name="Date_M" />
					<xsl:call-template name="Language_R" />
					<xsl:call-template name="ResourceType_R" />
					<xsl:call-template name="AlternateIdentifier_O" />
					<xsl:call-template name="RelatedeIdentifier_MA" />
					<xsl:call-template name="Size_O" />
					<xsl:call-template name="Format_O" />
					<xsl:call-template name="Version_O" />
					<xsl:call-template name="Rights_MA" />
					<xsl:call-template name="Description_MA" />
					<xsl:call-template name="GeoLocation_R" />
				</resource>
			</payload>
		</oai_datacite>
	</xsl:template>

	<xsl:template name="Identifier_M">
		<identifier identifierType="URL">
			<xsl:value-of
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element/doc:element/doc:field[@name='value']" />
		</identifier>
	</xsl:template>

	<xsl:template name="Creator_M">
		<creators>
			<xsl:choose>
				<xsl:when
					test="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='author']/doc:element/doc:field[@name='value']">
					<xsl:call-template name="_process_creators" />
				</xsl:when>
				<xsl:otherwise>
					<creator>
						<creatorName>(:unkn) unknown</creatorName>
					</creator>
				</xsl:otherwise>
			</xsl:choose>
		</creators>
	</xsl:template>

	<xsl:template name="Title_M">
		<titles>
			<xsl:choose>
				<xsl:when
					test="doc:metadata/doc:element[@name='dc']/doc:element[@name='title']/doc:element/doc:field[@name='value']">
					<title>
						<xsl:value-of
							select="doc:metadata/doc:element[@name='dc']/doc:element[@name='title']/doc:element/doc:field[@name='value']" />
					</title>
				</xsl:when>
				<xsl:otherwise>
					<title>(:unas) unassigned</title>
				</xsl:otherwise>
			</xsl:choose>
		</titles>
	</xsl:template>

	<xsl:template name="Publisher_M">
		<publisher>
			<xsl:choose>
				<xsl:when
					test="doc:metadata/doc:element[@name='dc']/doc:element[@name='publisher']/doc:element/doc:field[@name='value']">
					<xsl:value-of
						select="doc:metadata/doc:element[@name='dc']/doc:element[@name='publisher']/doc:element/doc:field[@name='value']" />
				</xsl:when>
				<!-- Or fixed repository name? -->
				<xsl:otherwise>
					(:unkn) unknown
				</xsl:otherwise>
			</xsl:choose>
		</publisher>
	</xsl:template>

	<xsl:template name="PublicationYear_M">
		<!-- TODO bit unclear what goes here if embargo see also Date_M -->
		<publicationYear>
			<xsl:value-of
				select="substring(doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='issued']/doc:element/doc:field[@name='value'],1,4)" />
		</publicationYear>
	</xsl:template>

	<xsl:template name="Subject_R" />
	<xsl:template name="Contributor_MAO" />

	<xsl:template name="Date_M">
		<!-- Use “Issued” for the date the resource is published or distributed. 
			To indicate the end of an embargo period, use “Available”. To indicate the 
			start of an embargo period, use “Accepted”. DataCite v3.0 further recommends 
			use of “Created” and “Submitted”. -->
		<!-- TODO implement embargoes and possibly other dates -->
		<dates>
			<date dateType="Issued">
			<xsl:value-of
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='issued']/doc:element/doc:field[@name='value']" />
			</date>
		</dates>
	</xsl:template>

	<xsl:template name="Language_R" />
	<xsl:template name="ResourceType_R" />
	<xsl:template name="AlternateIdentifier_O" />
	<xsl:template name="RelatedeIdentifier_MA" />
	<xsl:template name="Size_O" />
	<xsl:template name="Format_O" />
	<xsl:template name="Version_O" />
	<xsl:template name="Rights_MA" />
	<xsl:template name="Description_MA" />
	<xsl:template name="GeoLocation_R" />

	<xsl:template name="_process_creators">
		<xsl:for-each
			select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='author']/doc:element/doc:field[@name='value']">
			<creator>
				<creatorName>
					<xsl:value-of select="." />
				</creatorName>
			</creator>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
