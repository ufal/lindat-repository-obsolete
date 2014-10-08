exportFormats = [ "bibtex", "cmdi", "html"];

ufal.citation = {
	
		citationBox: function(container) {
			container.html("");
			var div = jQuery("<div></div>").appendTo(container);
			var URI = container.attr("uri");
			var oaiURI = container.attr("oai");
			var oaiHandle = container.attr("oai-handle");
			var dataTarget = container.attr("data-target");			
			div.addClass("alert bold");
			div.css("padding-right", "10px");
			div.css("font-size", "12px");
			var exportSpan = jQuery("<span class='pull-right'></span>").appendTo(div);
			exportSpan.append("<span class='bold'><i class='fa fa-magic'>&#160;</i>Export to</span>");			
			for(var i=0;i<exportFormats.length;i++) {
				var format = exportFormats[i];				
				var link = jQuery("<a>" + format + "</a>").appendTo(exportSpan);
				link.css("margin-left", "2px");
				link.css("margin-right", "2px");
				link.attr("data-toggle", "modal");
				link.attr("data-target", dataTarget);
				link.addClass("label label-default exportto");
				link.attr("href", oaiURI + "/requeststripped?verb=GetRecord&metadataPrefix=" + format + "&identifier=" + oaiHandle);
				link.click(ufal.citation.exporter_click);				
			}			
			
			div.append("<span>" +
					"<i class='fa fa-info-circle fa-2x pull-left'>&#160;</i>" +
					"Please use the following persistent identifier to cite or link to this item." +
					"<br>" +
					"<a href='" + URI + "'>" + URI + "</a>" +
					"</span>");						
		},
		
		extract_metadata : function(xml_content) {
			return xml_content;
		},

		extract_metadata_bibtex : function(xml_content) {
			try {
				var xml = jQuery.parseXML(xml_content);
				var metadata = jQuery(xml.getElementsByTagNameNS(
						"http://lindat.mff.cuni.cz/ns/experimental/bibtex",
						"bibtex")[0]);
				if (!metadata.exists()) {
					throw "Not found."
				}
				return metadata.text();
			} catch (err) {
				return xml_content;
			}
		},

		extract_metadata_cmdi : function(xml_content) {
			return xml_content;
		},

		extract_metadata_html : function(xml_content) {
			try {
				var xml = jQuery.parseXML(xml_content);
				var metadata = jQuery(xml.getElementsByTagNameNS(
						"http://lindat.mff.cuni.cz/ns/experimental/html",
						"html")[0]).html();
				if (!metadata.exists()) {
					throw "Not found."
				}
				return metadata;
			} catch (err) {
				return xml_content;
			}
		},

		convert_metadata_to_html : function(metadata, name) {
			if(name == "extract_metadata_html") {1
				return metadata;
			}
			else {
				metadata = metadata.replace(/>/g, "&gt;").replace(/</g, "&lt;");
				return '<pre>' + metadata + '</pre>';
			}
		},
		
		exporter_click : function(e) {
			e.preventDefault();
			var url = jQuery(this).attr("href");
			var name = "extract_metadata_" + jQuery(this).html().toLowerCase();
			var targ = jQuery(this).attr("data-target");
			jQuery(targ + " .modal-body").html("<i class='fa fa-spinner fa-spin' style='margin: auto;'>&#160;</i>");
			jQuery(targ).modal('show');
			jQuery
					.ajax({
						url : url,
						context : document.body,
						dataType : 'text'
					})
					.done(
							function(data) {
								var jdata_html = data;
								jdata_html = ufal.citation[name](jdata_html);
								jQuery(targ + " .modal-body").html(ufal.citation.convert_metadata_to_html(jdata_html, name));
							})
					.fail(
							function(data) {
								var jdata_html = data;
								jdata_html = ufal.citation.extract_metadata(jdata_html.responseText);
								if (jdata_html != null) {
									jQuery(targ + " .modal-body")
											.html('<pre>'
													+ jdata_html
													+ '</pre>');
								} else {
									jQuery(
											targ + " .modal-body")
											.html('Failed to load requested data.');
								}
							});			
		}
				
};


jQuery(document).ready(function (){
	jQuery(".citationbox").each(function(){
		ufal.citation.citationBox(jQuery(this));
	});	
});
