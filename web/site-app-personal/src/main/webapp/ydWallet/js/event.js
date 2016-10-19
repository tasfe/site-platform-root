$(".getPwWrap").css("height",$(window).height());
$(".clearInput").click(function(){
	$(this).parent().find("input").val("");
});
var countdown=60;
function settime(val) {
	btn=$(val);
	if (countdown==0) { 
		btn.text("获取短信");
		//btn.attr("onClick","settime(this)");
		btn.removeClass("sendMsgGray");
		countdown=60; 
		return false;
	}
	else{ 
		if(!btn.hasClass("sendMsgGray")){
			btn.addClass("sendMsgGray");
		}
		btn.text(countdown+"秒后重发");	 
		//btn.attr("onClick","return false;");
		countdown--;
	}
	setTimeout(function() { 
		settime(val);
	},1000);
}
function sendMsgActive(){
	var status=$("#sendMsg").text();
	if(status=="获取短信"){
		$("#sendMsg").removeClass("sendMsgGray");
		//$("#sendMsg").attr("onClick","settime(this)");
	}
}
function sendMsgUnActive(){
	$("#sendMsg").addClass("sendMsgGray").text("获取短信");
}
$("#payCheck1").click(function(){
	stat=$(this).attr("check");
	select=$(this).attr("select");
	if($("#selectPayWay2").length!=0){
		if(select=="true"){	
			blance=$("#bankInfo").text();
			blance=blance.replace(/[^\d.]/g,'');
			blance=parseFloat(blance);
			amount=$("#tradeAmount").text();
			amount=amount.replace(/[^\d.]/g,'');
			amount=parseFloat(amount);
			cardPay=amount-blance;
			if(stat=="uncheck"){						
				if(blance!=0){
					$("#zhzf").text(parseFloat(cardPay));
				}
			}
			else if(stat=="check"){
				if(blance!=0){
					$("#zhzf").text("");
				}
			}
		}
	}
});
(function($){ 
// 输入框格式化 
$.fn.bankInput = function(options){ 
var defaults = { 
min : 10, // 最少输入字数 
max : 25, // 最多输入字数 
deimiter : ' ', // 账号分隔符 
onlyNumber : true, // 只能输入数字 
copy : true // 允许复制 
}; 
var opts = $.extend({}, defaults, options); 
var obj = $(this); 
obj.css({imeMode:'Disabled',borderWidth:'1px',color:'#000',fontFamly:'Times New Roman'}).attr('maxlength', opts.max); 
if(obj.val() != '') obj.val( obj.val().replace(/\s/g,'').replace(/(\d{4})(?=\d)/g,"$1"+opts.deimiter) ); 
obj.bind('keyup',function(event){ 
if(opts.onlyNumber){ 
if(!(event.keyCode>=48 && event.keyCode<=57)){ 
this.value=this.value.replace(/\D/g,''); 
} 
} 
this.value = this.value.replace(/\s/g,'').replace(/(\d{4})(?=\d)/g,"$1"+opts.deimiter); 
}).bind('dragenter',function(){ 
return false; 
}).bind('onpaste',function(){ 
return !clipboardData.getData('text').match(/\D/); 
}).bind('blur',function(){ 
this.value = this.value.replace(/\s/g,'').replace(/(\d{4})(?=\d)/g,"$1"+opts.deimiter); 
if(this.value.length < opts.min){ 
obj.focus(); 
} 
}) 
} 
// 列表显示格式化 
$.fn.bankList = function(options){ 
var defaults = { 
deimiter : ' ' // 分隔符 
}; 
var opts = $.extend({}, defaults, options); 
return this.each(function(){ 
$(this).text($(this).text().replace(/\s/g,'').replace(/(\d{4})(?=\d)/g,"$1"+opts.deimiter)); 
}) 
} 
})(jQuery); 
$(".closeDiaLog-alert").click(function(){
	$(".dialogWrap").fadeOut();
	$("#ydWx-diaLog").hide();
})
function tipMsg(msg){
	$(".dialogWrap").fadeIn();
	$("#ydWx-diaLog").show();
	$("#ydWx-diaLogText").text(msg);
}
function pwdErrorDiaLog(msg){
  	$(".dialogWrap").fadeIn();
	$("#ydWx-diaLog").show();
	$("#ydWx-diaLogText").text(msg);
}
function clearKG(ss){ 
	return ss.replace(/[ ]/g,""); 
} 
$("#passwordEye").click(function(){
	stat=$(this).attr("password");
	if(stat=="hide"){
		$(this).removeClass("icon-pw1");
		$(this).addClass("icon-pw2");
		$(this).attr("password","show");
		$(".yd-input-noBorder[ydType='password']").attr("type","text");
	}
	else if(stat=="show"){
		$(this).removeClass("icon-pw2");
		$(this).addClass("icon-pw1");
		$(this).attr("password","hide");
		$(".yd-input-noBorder[ydType='password']").attr("type","password");
	}
});