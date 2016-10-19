/*
Yonda-UI ver 1.0.0
github @vincentpan1992
更新于 20160422
*/
//diaLog弹窗
//通用diaLog组件变量
$(document).on("click",".closeDiaLog-alert",function(){
	$("#dialog-alert,#dialog-confirm").remove();
});
var yonda={	  
	Msg:function(title,msg){
		$("body").append("<div class='diaLog-alert animationBounceIn' id='dialog-alert'><h2 class='dialog-alertTitle' id='alertTitle'>"+title+"</h2><div class='diaLog-alertDesc'><p id='alertMsg'>"+msg+"</p></div><div class='diaLog-alertBtnArea'><div class='diaLogBtn closeDiaLog-alert'>确定</div></div></div>");
		//creatDialog(config);
	},
	Tip:function(msg){
		if($("#yd-tip").length==0){
			$("body").append("<div class='yd-tip animationBounceIn' id='yd-tip'>"+msg+"</div>");
			setTimeout(function(){
				$("#yd-tip").remove();
			},2000);
		}
	},
	Confirm:function(config,callback){
		if($("#chm-dialog").length==0){
			$("body").append("<div class='diaLog-alert animationBounceIn' style='display:none' id='dialog-confirm'><h2 class='dialog-alertTitle' id='confirmTitle'></h2><div class='diaLog-alertDesc'><p id='confirmMsg'></p></div><div class='diaLog-alertBtnArea'><div class='diaLog-alertBtnLeft'>确定</div><div class='diaLog-alertBtnRight closeDiaLog'>取消</div></div></div>");
			//creatDialog(config);
		}
		//callback调用方法：rec('+callback+')
	 	callback = callback || function(){};
	}
}
//回调函数
function rec(callback){
	$(".chm-dialog").addClass("bounceOut");
	setTimeout(function() { 
		$("#chm-dialog").remove();
	}, 500); 
	callback();
}
function creatDiaLog(type,config){

}
function closeDialog(){
	$("#diaLog-alert,#dialog-confirm").remove();
}

$(document).on("click",".yd-checkbox",function(){
	stat=$(this).attr("check");
	select=$(this).attr("select");
	if(select=="true"){		
		if(stat=="uncheck"){		
			$(this).addClass("yd-checkboxSelected");
			$(this).attr("check","check");
			$(this).parents("li").find(".yd-layouts-right,.yd-layouts-left").css("color","#000");
			return false;
		}
		else if(stat=="check"){
			$(this).removeClass("yd-checkboxSelected");
			$(this).attr("check","uncheck");
			$(this).parents("li").find(".yd-layouts-right,.yd-layouts-left").css("color","#999");
			return false;
		}
	}
	return false;
});
$(".yd-input-noBorder").bind("textInput",function(e){
	type=$(this).attr("inputtype");//input propertychange
	value=$(this).val();
	/*if(type=="bankcard"){
		if(event.keyCode==8){
			if(value){
				for(var i=0;i<value.length;i++){
					var newStr=value.replace(/\s$/g,'');
				}
				$(this).val(newStr);
			}
		}
		else{
			for(var i=0;i<value.length;i++){
				var arr=value.split('');
				if((i+1)%5==0){
					arr.splice(i,0,' ');
				}
			}
			test=arr.join('');
			$(this).val(test);
		}
	}*/
});
$(".yd-input-noBorder").bind("input propertychange",function(e){
	notnull=$(this).attr("notnull")
	value=$(this).val();
	if(notnull=="notnull"){
		if(value!=""){
			$(".yd-btn-big").removeClass("yd-btn-disabled");
		}
		else{
			$(".yd-btn-big").addClass("yd-btn-disabled");
		}
	}
});
wWith=$("body").width();
wWith=wWith*0.60;
$(".yd-input-noBorder").css("width",wWith);
$("#authCode,#authCode2").css("width","125px");