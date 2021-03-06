

const showDetail = (targetele, fieldid) => {
    showAutoCompleteDiv(targetele, fieldid);
}
window.showDetail = showDetail;

const showAutoCompleteDiv = (ele, fieldid) => {
	jQuery("#__brow__detaildiv").remove();

	var target = jQuery(ele);
	var offset = target.offset();
	var scrollObj = jQuery("div.wea-new-top-req-content");
	
	var __x = offset.left - 245/2 +  jQuery(ele).width()/2;
	var __y = offset.top + scrollObj.scrollTop() - scrollObj.offset().top + 5;
	if (jQuery(ele).height() > 20) {
		__x = offset.left - 245/2 + 5;
		__y = __y + 18;
	}
	
	var separator = ",";
	
	var _ids = target.attr("_ids");
	var _names = target.attr("_names");
	var _jobtitles = target.attr("_jobtitles");
	
	var _idArray = _ids.split(separator);
	var _nameArray = _names.split(separator);
	var _jobtitleArray = _jobtitles.split(separator);
	var browgroupHtml = "";
	var _i;
	for (_i=0; _i<_idArray.length; _i++) {
		var className=(_i%2==1?"ac_even":"ac_odd");
		var displaycss = _i > 4 ? " display:none; " : "";
		var resdetailinfohtml = "<span style='display:inline-block;width:80px;overflow: hidden;white-space: nowrap;text-overflow: ellipsis;'><a href=javaScript:openhrm(" + _idArray[_i] + "); onclick='pointerXY(event);' style='overflow: hidden;white-space: nowrap;text-overflow: ellipsis;' title='" + _nameArray[_i] + "'>" + _nameArray[_i] + "</a></span>";
		resdetailinfohtml += "<span style='float:right;display:inline-block;width:130px;overflow: hidden;white-space: nowrap;text-overflow: ellipsis;' title='" + _jobtitleArray[_i] + "'>" + _jobtitleArray[_i] + "</span>";
		
		browgroupHtml += '<li class="'+className+'" _id="'+_idArray[_i]+'" style=\'' + displaycss + '\'>' + resdetailinfohtml +'</li>';
		
		
		
		if (_i > 4 && _i == _idArray.length-1) {
			browgroupHtml += '<li class="'+className+'" title="显示全部" style="text-align:center;" onclick="showalldetail(this, ' + fieldid + ');">显示全部</li>';
		}
	}
	
	if(!!browgroupHtml){
		var autoCompleteDiv = jQuery("<div id='__brow__detaildiv' class='wea-hrmgroup-wrapper' style=\"position:absolute;width:245px;z-index:999;left:" + __x + "px;top:" + __y + "px;\"></div>");
		var autoCompleteDiv_html = jQuery("<div class=\"arrowsblock\"><img src=\"/images/ecology8/workflow/multres/arrows_2_wev8.png\" width=\"22px\" height=\"22px\"></div>"
			+ "<div class='ac_results' style='margin-top:20px;background:#fff;z-index:9;'><ul>"+browgroupHtml+"</ul></div>"
		);
		autoCompleteDiv.append(autoCompleteDiv_html);
		//autoCompleteDiv_html.addClass("ac_results");
         jQuery("div.wea-popover-hrm-relative-parent").append(autoCompleteDiv);
	}
}

const showalldetail = (ele, fieldid) => {
	var target = jQuery(ele);
	var parentDiv = target.closest("div");
	parentDiv.css({
	    "height":parentDiv.height() + "px",
		"overflow" : "auto"
	});
	
	//parentDiv.perfectScrollbar({horizrailenabled:false,zindex:1000});
	
	var othli = target.parent().children().not(":visible");
	othli.show();
	target.hide();
}

window.showalldetail = showalldetail;

jQuery(function () {
	jQuery("table.excelMainTable").live('mouseup', function (e) {
		if (jQuery("#__brow__detaildiv").is(":visible") && !!!jQuery(e.target).closest("#__brow__detaildiv")[0]) {
			jQuery("#__brow__detaildiv").hide();
		}
		if (jQuery("#_browcommgroupblock").is(":visible") && !!!jQuery(e.target).closest("#_browcommgroupblock")[0]) {
			jQuery("#_browcommgroupblock").hide();
		}
		e.stopPropagation();
	});
});


//Iframe区域相关方法
var iframeOperate = (function(){
	
	function showIframeArea(vthis){
		jQuery(vthis).closest("td").find(".iframeLoading").hide();
		jQuery(vthis).show();
	}
	
	//Iframe区域高度自适应高度
	function autoAdjustHeight(vthis){
		var iframeObj = jQuery(vthis);
		var funObj = function(){autoAdjustHeight(vthis)};
		var eachcount = parseInt(iframeObj.attr("eachcount"));
		if(eachcount > 10){
			iframeObj.css("height", "100px");
			return;
		}
		
		var contextObj = iframeObj[0].contentWindow.document;
		var bodyContentHeight = contextObj.body.scrollHeight;
		if(window.console) console.log("scrollHeight:"+bodyContentHeight+"  |offsetHeight:"+contextObj.body.offsetHeight)
		if(bodyContentHeight == 0){
			iframeObj.attr("eachcount", eachcount+1);
			window.setTimeout(funObj, 500);
		}else{
			iframeObj.css("height", bodyContentHeight+"px");
		}
	}

	return{
		//Iframe区域onload事件
		loadingOver: function(vthis){
			if(jQuery(vthis).attr("adjustheight") == "y"){
				window.setTimeout(function(){
					showIframeArea(vthis);
					autoAdjustHeight(vthis);
				},800);
			}else{
				showIframeArea(vthis);
			}
		}
	}
})();

window.iframeOperate = iframeOperate;