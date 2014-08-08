<?xml version="1.0" encoding="UTF-8"?>
<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->

<!--

This stylsheet to handle exception display is a modified version of the
base apache cocoon stylesheet, this is still under the Apache license.

The original author is unknown.
Scott Phillips adapted it for Manakin's need.

modified for LINDAT/CLARIN
-->

<xsl:stylesheet version="1.0"
                xmlns:dri="http://di.tamu.edu/DRI/1.0/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ex="http://apache.org/cocoon/exception/1.0"
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">

  <xsl:include href="themes/UFAL/lib/xsl/lindat/header.xsl"/>
  <xsl:include href="themes/UFAL/lib/xsl/lindat/footer.xsl"/>

  <xsl:param name="realPath"/>

  <xsl:param name="contextPath"/>

  <xsl:variable name="request-uri" select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='request'][@qualifier='URI']"/>

  <!-- let sitemap override default page title -->
  <xsl:param name="pageTitle">An error has occurred</xsl:param>

  <!-- let sitemap override default context path -->

  <xsl:template match="ex:exception-report">
    <html>


      <xsl:call-template name="buildHead"/>

      <body id="lindat-repository">
        <xsl:call-template name="buildHeader"/>
        <div id="ds-main">
                <div id="ds-content-wrapper">
                            <div id="ds-content" class="clearfix">
    
    <div id="exception-wrapper">    
        <div id="exception-icon" style="float:right; width: 200px">                     
          <img title="Session Expired">
            <xsl:attribute name="src">
               <xsl:value-of select="concat($contextPath,'/themes/UFAL/images/watch.gif')"/>
            </xsl:attribute>
          </img>
        </div>
                                                    
				<div id="ds-body" style="float:left; width: 710px">
                                <h1><xsl:value-of select="$pageTitle"/></h1>
                                <BR/>
                                <h2>Please, try logging in again.</h2>
                                <BR/>
                                
                                <div>In case that there are other problems, please contact the <a>
                                <xsl:attribute name="href">
                                    <xsl:value-of select="$contextPath"/>
                                    <xsl:text>/contact</xsl:text>
                                </xsl:attribute>site administrator with details below.</a></div><BR/>
    <div id="exception-detail-button" class="toggle-onclick-once-next">                                
      Details 
      <img title="Session Expired">
        <xsl:attribute name="src">
           <xsl:value-of select="concat($contextPath,'/themes/UFAL/images/button_plus.png')"/>
        </xsl:attribute>
      </img>
    </div>                                
                                
    <div id="exception-detail">
        <p class="message">
          <xsl:value-of select="@class"/>:
          <xsl:apply-templates select="ex:message" mode="breakLines"/>
          <xsl:if test="ex:location">
             <br/><span style="font-weight: normal"><xsl:apply-templates select="ex:location"/></span>
          </xsl:if>
        </p>

        <p><span class="description">Cocoon stacktrace</span>
           <span class="switch" id="locations-switch" onclick="toggle('locations')">[hide]</span>
        </p>
        <div id="locations">
          <xsl:for-each select="ex:cocoon-stacktrace/ex:exception">
            <xsl:sort select="position()" order="descending"/>
            <strong>
               <xsl:apply-templates select="ex:message" mode="breakLines"/>
            </strong>
            <table>
               <xsl:for-each select="ex:locations/*[string(.) != '[cause location]']">
                 <!-- [cause location] indicates location of a cause, which
                      the exception generator outputs separately -->
                <tr class="row-{position() mod 2}">
                   <td><xsl:call-template name="print-location"/></td>
                   <td><em><xsl:value-of select="."/></em></td>
                </tr>
              </xsl:for-each>
            </table>
            <br/>
           </xsl:for-each>
        </div>

        <xsl:apply-templates select="ex:stacktrace"/>
				</div>


                            </div>
                </div>
        </div>
        
      </div> <!-- exception-detail -->
    </div>   <!-- exception-wrapper -->                          
        
        <xsl:call-template name="buildFooter" />
</body>
    </html>
  </xsl:template>

  <xsl:template match="ex:stacktrace|ex:full-stacktrace">
      <p class="stacktrace">
       <span class="description">Java <xsl:value-of select="translate(local-name(), '-', ' ')"/></span>
       <span class="switch" id="{local-name()}-switch" onclick="toggle('{local-name()}')">[hide]</span>
       <pre id="{local-name()}">
         <xsl:value-of select="translate(.,'&#13;','')"/>
       </pre>
      </p>
  </xsl:template>

  <xsl:template match="ex:location">
   <xsl:if test="string-length(.) > 0">
     <em><xsl:value-of select="."/></em>
     <xsl:text> - </xsl:text>
   </xsl:if>
   <xsl:call-template name="print-location"/>
  </xsl:template>

  <xsl:template name="print-location">
     <xsl:choose>
       <xsl:when test="contains(@uri, $realPath)">
         <xsl:text>context:/</xsl:text>
         <xsl:value-of select="substring-after(@uri, $realPath)"/>
       </xsl:when>
       <xsl:otherwise>
         <xsl:value-of select="@uri"/>
       </xsl:otherwise>
      </xsl:choose>
      <xsl:text> - </xsl:text>
      <xsl:value-of select="@line"/>:<xsl:value-of select="@column"/>
  </xsl:template>

  <!-- output a text by splitting it with <br>s on newlines
       can be uses either by an explicit call or with <apply-templates mode="breakLines"/> -->
  <xsl:template match="node()"  mode="breakLines" name="breakLines">
     <xsl:param name="text" select="string(.)"/>
     <xsl:choose>
        <xsl:when test="contains($text, '&#10;')">
           <xsl:value-of select="substring-before($text, '&#10;')"/>
           <br/>
           <xsl:call-template name="breakLines">
              <xsl:with-param name="text" select="substring-after($text, '&#10;')"/>
           </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
           <xsl:value-of select="$text"/>
        </xsl:otherwise>
     </xsl:choose>
  </xsl:template>

    <xsl:template name="buildHead">
        <head>
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

            <!-- Always force latest IE rendering engine (even in intranet) & Chrome Frame -->
            <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>

            <!--  Mobile Viewport Fix
                  j.mp/mobileviewport & davidbcalhoun.com/2010/viewport-metatag
            device-width : Occupy full width of the screen in its current orientation
            initial-scale = 1.0 retains dimensions instead of zooming out if page height > device height
            maximum-scale = 1.0 retains dimensions instead of zooming in if page width < device width
            -->
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0"/>

            <link rel="shortcut icon">
                <xsl:attribute name="href">
                        <xsl:value-of select="$contextPath"/>
                    <xsl:text>/themes/UFAL/images/favicon.ico</xsl:text>
                </xsl:attribute>
            </link>
            <link rel="apple-touch-icon">
                <xsl:attribute name="href">
                        <xsl:value-of select="$contextPath"/>
                    <xsl:text>/themes/UFAL/images/apple-touch-icon.png</xsl:text>
                </xsl:attribute>
            </link>

            <meta name="Generator">
              <xsl:attribute name="content">
                <xsl:text>DSpace 1.8.2</xsl:text>
              </xsl:attribute>
            </meta>
            <!-- Add stylsheets -->
                <link rel="stylesheet" type="text/css">
                    <xsl:attribute name="media">
                        <xsl:value-of select="@qualifier"/>
                    </xsl:attribute>
                    <xsl:attribute name="href">
                        <xsl:value-of select="$contextPath"/>
                        <xsl:text>/themes/UFAL/lib/css/authority-control.css</xsl:text>
                    </xsl:attribute>
                </link>

                <!-- Add Lindat stylesheet -->
                <link rel="stylesheet" type="text/css" href="{$contextPath}/themes/UFAL/lib/lindat/public/css/lindat.css" />

                <style>
                          #ds-content { padding-top: 20px; /* Added because content is too close to header */ }
                          span.switch { cursor: pointer; margin-left: 5px; text-decoration: underline; }
                          span.description { color: #336699; font-weight: bold; }

                </style>
                <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js" type="text/javascript">&#160;</script>
                <script type="text/javascript">
                        jQuery(document).ready(
                                function (){
                                        jQuery("#repository_path").attr("href", "<xsl:value-of select="$contextPath"/>");
                                        toggle('locations');
                                        toggle('stacktrace');
                                }
                        );

          function toggle(id) {
            var element = document.getElementById(id);
            with (element.style) {
              if ( display == "none" ) {
                display = ""
              } else {
                display = "none"
              }
            }

            var text = document.getElementById(id + "-switch").firstChild;
            if (text.nodeValue == "[show]") {
              text.nodeValue = "[hide]";
            } else {
              text.nodeValue = "[show]";
            }
          }

        jQuery(document)
    		.ready(
				function() {

 					jQuery(".toggle-onclick-once-next").each(function() {
						// initial hide
						var tthis = jQuery(this);
                        tthis.addClass( 'ui-link-legend' );
                        tthis.addClass( 'ui-toggle-hover' );
						var totoggle = tthis.next();
						totoggle.hide();
						// toggle on click
						tthis.click(function() {
                            tthis.hide();
							tthis.unbind('click');
							tthis.removeClass('ui-link-legend');
							tthis.removeClass('ui-toggle-hover');
							totoggle.show();
							return false;
						});
					});
                }
         
         );


                </script>
            <!-- Add the title in -->
            <title>
                <xsl:value-of select="$pageTitle"/>
            </title>
        </head>
    </xsl:template>


</xsl:stylesheet>
