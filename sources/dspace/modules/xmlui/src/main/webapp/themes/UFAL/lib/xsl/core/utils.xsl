<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->
<!--
    This stylesheet contains helper templates for things like i18n and standard attributes.

    Author: art.lowel at atmire.com
    Author: lieven.droogmans at atmire.com
    Author: ben at atmire.com
    Author: Alexey Maslov

	modified for LINDAT/CLARIN

-->

<xsl:stylesheet xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
	xmlns:dri="http://di.tamu.edu/DRI/1.0/"
	xmlns:mets="http://www.loc.gov/METS/"
	xmlns:xlink="http://www.w3.org/TR/xlink/"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
	xmlns:xhtml="http://www.w3.org/1999/xhtml"
	xmlns:mods="http://www.loc.gov/mods/v3"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns="http://www.w3.org/1999/xhtml"
	exclude-result-prefixes="i18n dri mets xlink xsl dim xhtml mods dc">

    <xsl:output indent="yes" />

    <!--added classes to differentiate between collections, communities and items-->
    <xsl:template match="dri:reference" mode="summaryList">
        <xsl:variable name="externalMetadataURL">
            <xsl:text>cocoon:/</xsl:text>
            <xsl:value-of select="@url"/>
            <!-- Since this is a summary only grab the descriptive metadata, and the thumbnails -->
            <!-- xsl:text>?sections=dmdSec,fileSec&amp;fileGrpTypes=THUMBNAIL</xsl:text-->
            <!-- An example of requesting a specific metadata standard (MODS and QDC crosswalks only work for items)->
            <xsl:if test="@type='DSpace Item'">
                <xsl:text>&amp;dmdTypes=DC</xsl:text>
            </xsl:if>-->
        </xsl:variable>
        <xsl:comment> External Metadata URL: <xsl:value-of select="$externalMetadataURL"/> </xsl:comment>
            <xsl:attribute name="class">
                <xsl:choose>
                    <xsl:when test="contains(@type, 'Community')">
                        <xsl:text>community </xsl:text>
                    </xsl:when>
                    <xsl:when test="contains(@type, 'Collection')">
                        <xsl:text>collection </xsl:text>
                    </xsl:when>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test="position() mod 2 = 0">even</xsl:when>
                    <xsl:otherwise>odd</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:apply-templates select="document($externalMetadataURL)" mode="summaryList"/>
            <xsl:apply-templates />
    </xsl:template>

    <!-- template for rendering file sizes in human readable format -->
    <xsl:template name="format-size">
        <xsl:param name="size" />
        <xsl:choose>
            <xsl:when test="$size &lt; 1024">
                <xsl:value-of select="format-number($size, '0.##')" />
                 <xsl:text>&#xa;</xsl:text>
                <i18n:text>xmlui.dri2xhtml.METS-1.0.size-bytes</i18n:text>
            </xsl:when>
            <xsl:when test="$size &lt; 1024 * 1024">
                <xsl:value-of select="format-number($size div 1024, '0.##')" />
                 <xsl:text>&#xa;</xsl:text>
                <i18n:text>xmlui.dri2xhtml.METS-1.0.size-kilobytes</i18n:text>
            </xsl:when>
            <xsl:when test="$size &lt; 1024 * 1024 * 1024">
                <xsl:value-of select="format-number($size div (1024 * 1024), '0.##')" />
                <xsl:text>&#xa;</xsl:text>
                <i18n:text>xmlui.dri2xhtml.METS-1.0.size-megabytes</i18n:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="format-number($size div (1024 * 1024 * 1024), '0.##')" />
                <xsl:text>&#xa;</xsl:text>
                <i18n:text>xmlui.dri2xhtml.METS-1.0.size-gigabytes</i18n:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>

