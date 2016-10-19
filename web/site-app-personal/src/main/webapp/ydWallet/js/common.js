// 获取URL中指定字符串的值
function getQueryString(name) {
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
	var r = window.location.search.substr(1).match(reg);
	if (r != null) return r[2]; return "";
}

// ajax
function ajax(url, params) {
	return $.ajax({
	  dataType: "json",
	  url: url,
	  data: params,
	  cache: false,
	  type: "POST",
	  error: ajaxError
	});
}

function ajaxError(XMLHttpRequest, textStatus, errorThrown) {
	$('#loading').hide();
	$('#loading_tj').hide();
    var message = "";
    switch (errorThrown) {
        case 'Request Time-out':
            message = "请求超时.";
            break;
        case 'Not Found':
            message = "请求失败！";
            break;
        default:
        	//console.log(errorThrown);
        	message = "网络连接失败！";
	        break;
    }
    $("#tan_window1 .content").html(message);
    easyDialog.open({
		container : 'tan_window1'
	});
	return false;
}
var linkPre = "/ydWallet";
// 待处理....
// 测试环境、生产环境
var gurl = "";
// 开发环境
//var gurl = "http://771d132f.ngrok.natapp.cn/site-app-personal/";
//http://558e1932.ngrok.natapp.cn/site-app-personal/ydWallet/recharge/bind.html?time=1465801615316
// /site-app-personal/ydWallet/ 待处理....
// 生产环境
//var gurl = "https://wx.yongdapay.com/";
// 测试环境
//var gurl = "https://weixin.yongdapay.com/";
var gErrCode = {
	"F001": "暂不支持手机号码充值"
};
function getErrCodeMessage(response){
	var errCode = response.code;
	return gErrCode[errCode] == undefined ? response.message : gErrCode[errCode];
}

var regexEnum = {
	float:"^(([0-9]+\\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\\.[0-9]+)|([0-9]*[1-9][0-9]*))$",   //非负浮点数（正浮点数 + 0）
	int:"^[0-9]*[1-9][0-9]*$",    //正整数
	email: "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$", //邮件
	domain: "/^(((\w)|(-))+[.]){1,}(com|net|cn|org|edu|gov|mil|arts|web)$/",	//域名
	idcard:"^[1-9]([0-9]{17}|[0-9]{16}([0-9]|X))$",			//身份证 18位 或17位末尾X
	phone:"^(13[0-9]{9}|14[57][0-9]{8}|15[012356789][0-9]{8}|17[0-9]{9}|18[0-9]{9})$"				//手机
};

var tools = {
	"refreshVeriCode": function(jImg){
		jImg.attr("src", gurl + "/pvc" + "?time=" + Math.random());
		$("#" + jImg.data("for")).val("");
		return false;
	},
	"isNull": function(jObj){
		return !jObj || jObj == null || jObj == "";
	},
	"disableBtn": function(jBtn,disabled){
		if(disabled){
			jBtn.attr("disabled", true).addClass("yd-btn-disabled");
			if(jBtn.data("text-submit") != undefined){
				jBtn.text(jBtn.data("text-submit"));
			}
		}
		else{
			jBtn.attr("disabled", false).removeClass("yd-btn-disabled");
			if(jBtn.data("text") != undefined){
				jBtn.text(jBtn.data("text"));
			}
		}
	},

	"ajaxSubmit": function(options){
		var jBtnSubmit = options.jBtnSubmit || null;

		var isHasBtnSubmit = !(jBtnSubmit == null);
		if(isHasBtnSubmit) tools.disableBtn(jBtnSubmit, true);
		function _undisableBtn(){
			if(isHasBtnSubmit) tools.disableBtn(jBtnSubmit, false);
		}

		return $.ajax({
			"type":options.type || "POST",
			"url":options.url,
			"async":options.async || true,
			//"contentType":"application/json",
			"dataType":"json",
			"xhrFields": {
				"withCredentials": true
			},
			"crossDomain": true,
			"data":options.data || {},
			"cache":false,
			"success":function (response,e) {
				// 返回成功
				if(response && response.success){
					//alert("suc_suc");
					if(typeof (options.suc_suc) == "function"){
						options.suc_suc(response);
					}
					else{
						yonda.Tip("操作成功");
					}
				}
				// 返回失败
				else{
					//alert("suc_err");
					if(typeof (options.suc_err) == "function"){
						options.suc_err(response);
					}
					else{
						switch (response.code){
							// 未登录、未绑定
							// {"code":"NOT_LOGIN","message":"未登录","model":"debug","success":false}
							case "NOT_LOGIN":
								//window.location.href = "bind.html?backurl=" + window.location.href;
								// 待处理....
								window.location.href = linkPre + "/bind.html?time=" + new Date().getTime();
								break;
							default :
								$("body").show();
								yonda.Tip(getErrCodeMessage(response) || "操作失败");
						}
						if(typeof (options.suc_err2) == "function"){
							options.suc_err2(response);
						}
					}
				}
			},
			"error":function(request,errorinfo,errorThrown){
				//alert("error");
				if(typeof (options.error) == "function"){
					options.error(request,errorinfo,errorThrown);
				}
				else{
					switch (errorThrown) {
						case 'Request Time-out':
							yonda.Tip("亲，请求超时，请稍后再试~");
							break;
						case 'Not Found':
							yonda.Tip("亲，您请求的服务不存在~");
							break;
						default:
							yonda.Tip("亲，系统繁忙，请稍后再试~");
							break;
					}
				}
				_undisableBtn();
			},
			"complete": function (XHR, TS) {
				//alert("complete");
				if(typeof (options.complete) == "function"){
					options.complete(XHR, TS);
				}
				XHR = null;
				_undisableBtn();
			}
		});// $.ajax
	},
	// 限额转成万
	"limit": function(data){
		//return isNaN(parseFloat(data)) ? "不限" : (parseFloat(data) / 10000) + "万";
		return isNaN(parseFloat(data)) ? "不限" : parseFloat(data).toFixed(2);
	},
	"getQueryString":function(name,noUnescape){
		var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
		var r = window.location.search.substr(1).match(reg);

		if (r != null) return r[2]; return null;
	},

	// 检测：必填项
	checkMust: function(jInput, message, isNoSpace){
		var val = jInput.val();
		if(isNoSpace == true){
			val = val.replace(/\s/g,"");
		}
		if(val == ""){
			yonda.Tip(message);
			return false;
		}

		return true;
	},

	// 检测：必填项
	checkMinLength: function(jInput, minLength, message, isNoSpace){
		var val = jInput.val();
		if(isNoSpace == true){
			val = val.replace(/\s/g,"");
		}
		if(val.length < minLength){
			yonda.Tip(message);
			return false;
		}

		return true;
	},

	// 检测：正则表达式 regexEnums 支持用||分割多个正则
	checkRegex: function(jInput, message, regexEnums, isNoSpace){
		var arrRegexEnum = regexEnums.split("||");
		var len = arrRegexEnum.length;
		var val = jInput.val();
		if(isNoSpace == true){
			val = val.replace(/\s/g,"");
		}
		for(var i = 0; i < len; i++){
			if (RegExp(arrRegexEnum[i]).test(val)) {
				return true;
			}
		}
		yonda.Tip(message);
		return false;
	},

	// 检测：身份证格式 和 有效性
	"checkIdCard": function(jInput, message){
		var that = this;
		var value = jInput.val().replace(/\s/g,"");

		function isTrueValidateCodeBy18IdCard(idCard){
			var Wi = [ 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1],// 加权因子
				ValideCode = [ 1, 0, 10, 9, 8, 7, 6, 5, 4, 3, 2],// 身份证验证位值.10代表X
				sum = 0;// 声明加权求和变量

			idCard = idCard.split("");
			if (idCard[17].toLowerCase() == 'x') {
				idCard[17] = 10;// 将最后位为x的验证码替换为10方便后续操作
			}
			for ( var i = 0; i < 17; i++) {
				sum += Wi[i] * idCard[i];// 加权求和
			}
			var valCodePosition = sum % 11;// 得到验证码所位置
			return idCard[17] == ValideCode[valCodePosition];
		}

		function isValidityBrithBy18IdCard(idCard18){
			var year =  idCard18.substring(6,10),
				month = idCard18.substring(10,12),
				day = idCard18.substring(12,14);
			var temp_date = new Date(year,parseFloat(month) - 1, parseFloat(day));
			// 这里用getFullYear()获取年份，避免千年虫问题
			return !(temp_date.getFullYear() != parseFloat(year)
			|| temp_date.getMonth() != parseFloat(month) - 1
			|| temp_date.getDate() != parseFloat(day));
		}

		function isValidityBrithBy15IdCard(idCard15){
			var year =  idCard15.substring(6,8),
				month = idCard15.substring(8,10),
				day = idCard15.substring(10,12);
			var temp_date = new Date(year,parseFloat(month) - 1, parseFloat(day));
			// 对于老身份证中的你年龄则不需考虑千年虫问题而使用getYear()方法
			return !(temp_date.getYear() != parseFloat(year)
			||temp_date.getMonth() != parseFloat(month) - 1
			||temp_date.getDate() != parseFloat(day));
		}

		// 校验身份证有效性
		function idCardValidate(idCard){
			idCard = $.trim(idCard);//去掉字符串头尾空格
			if (idCard.length == 15) {
				return isValidityBrithBy15IdCard(idCard);//进行15位身份证的验证
			} else if (idCard.length == 18) {
				//进行18位身份证的基本验证和第18位的验证
				return (isValidityBrithBy18IdCard(idCard) && isTrueValidateCodeBy18IdCard(idCard));
			} else {
				return false;
			}
		}

		if(that.checkRegex(jInput, message, regexEnum.idcard, true)
			&& idCardValidate(value)){
			return true;
		}
		else{
			yonda.Tip(message);
			return false;
		}
	},

	// 获取密码强度 登录
	"checkStrong": function(jInput, message){

		var pw = jInput.val();

		if (pw.length < 5 || pw.length > 20){
			yonda.Tip(message);
			return false;
			//return 0;
		}

		// 判断该 Unicode 的类型
		function charMode(unicode){
			if (unicode >= 48 && unicode <= 57)// 数字
				return 1;
			if (unicode >= 65 && unicode <= 90)// 大写字母
				return 2;
			if (unicode >= 97 && unicode <= 122)// 小写字母
				return 4;
			if(unicode == 32)// 空格
				return 9;
			else
				return 8;
		}

		// 虽然不懂，但是挺好用，返回字符类型的总数
		function bitTotal(num){
			var modes = 0;
			for (var i = 0; i < 4; i++) {
				if (num & 1) modes++;
				num >>>= 1;
			}
			return modes;
		}

		var Modes = 0,
			isN = false,// 是否包含数字
			isE = false,// 是否包含字母
			firstS = "",// 第一个字符
			isAllFirstS = true,// 是否都是同一个字符
			isHaveSpecial = false,// 是否存在特殊字符
			isHaveNotAllow = false;// 是否存在不允许的字符

		for(var i = 0; i < pw.length; i++){
			if(i == 0) firstS = pw.charCodeAt(0);
			else{ if(firstS != pw.charCodeAt(i)) isAllFirstS = false; }
			var mode = charMode(pw.charCodeAt(i));
			if(mode == 8) { isHaveSpecial = true; break;}// 不能包含特殊字符
			if(mode == 9) { isHaveNotAllow = true; break; }// 不能包含空格
			if(mode == 1) isN = true;
			if(mode == 2 || mode == 4) isE = true;

			Modes |= mode;
		}

		if(isHaveSpecial){
			yonda.Tip("密码不能包含特殊字符");
			return false;
		}
		if (isHaveNotAllow){// 如果包含空格那么返回12
			yonda.Tip("密码不能包含空格");
			return false;
			//return 12;
		}
		//if (isAllFirstS) return 11;
		if(isN && isE){
			return true;
		}
		else{
			yonda.Tip(message);
			return false;
		}
	},

	// 获取密码强度 支付
	"checkStrong2": function(jInput, message){

		var pw = jInput.val();

		if (pw.length < 7 || pw.length > 23){
			yonda.Tip("密码长度为7-23位");
			return false;
			//return 0;
		}

		// 判断该 Unicode 的类型
		function charMode(unicode){
			if (unicode >= 48 && unicode <= 57)// 数字
				return 1;
			if (unicode >= 65 && unicode <= 90)// 大写字母
				return 2;
			if (unicode >= 97 && unicode <= 122)// 小写字母
				return 4;
			if(unicode == 32)// 空格
				return 9;
			else
				return 8;
		}

		// 虽然不懂，但是挺好用，返回字符类型的总数
		function bitTotal(num){
			var modes = 0;
			for (var i = 0; i < 4; i++) {
				if (num & 1) modes++;
				num >>>= 1;
			}
			return modes;
		}

		var Modes = 0,
			isN = false,// 是否包含数字
			isE = false,// 是否包含字母
			firstS = "",// 第一个字符
			isAllFirstS = true,// 是否都是同一个字符
			isHaveSpecial = false,// 是否存在特殊字符
			isHaveNotAllow = false;// 是否存在不允许的字符

		for(var i = 0; i < pw.length; i++){
			if(i == 0) firstS = pw.charCodeAt(0);
			else{ if(firstS != pw.charCodeAt(i)) isAllFirstS = false; }
			var mode = charMode(pw.charCodeAt(i));
			//if(mode == 8) { isHaveSpecial = true; break;}// 不能包含特殊字符
			if(mode == 9) { isHaveNotAllow = true; break; }// 不能包含空格
			if(mode == 1) isN = true;
			if(mode == 2 || mode == 4) isE = true;

			Modes |= mode;
		}

		/*if(isHaveSpecial){
			yonda.Tip("密码不能包含特殊字符");
			return false;
		}*/
		if (isHaveNotAllow){// 如果包含空格那么返回12
			yonda.Tip("密码不能包含空格");
			return false;
			//return 12;
		}
		//if (isAllFirstS) return 11;
		if(bitTotal(Modes) < 2){
			yonda.Tip(message);
			return false;
		}
		else{
			return true;
			//return bitTotal(Modes);
		}
	},

	"phoneFormat": function(phone){
		phone = phone.toString();
		return phone.substring(0,3) + " " + phone.substring(3,7) + " " + phone.substring(7,11);
	}
};

Date.prototype.format = function(format)
{
	var o = {
		"M+" : this.getMonth()+1, //month
		"d+" : this.getDate(),    //day
		"h+" : this.getHours(),   //hour
		"m+" : this.getMinutes(), //minute
		"s+" : this.getSeconds(), //second
		"q+" : Math.floor((this.getMonth()+3)/3),  //quarter
		"S" : this.getMilliseconds() //millisecond
	};
	if(/(y+)/.test(format)) format=format.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
	for(var k in o)
		if(new RegExp("("+ k +")").test(format))
			format = format.replace(RegExp.$1, RegExp.$1.length==1 ? o[k] :	("00"+ o[k]).substr((""+ o[k]).length));
	return format;
};

var curUrl = window.location.href;
var storage = window.sessionStorage;
if(curUrl.indexOf("bind.html") < 0
	&& curUrl.indexOf("signUp.html") < 0
	&& curUrl.indexOf("payPassword.html") < 0
	&& curUrl.indexOf("getPwd.html") < 0){
	storage.setItem("curUrl",curUrl);
}

$("#J_ShouYe").click(function(){
	window.location.href = linkPre + "/main.html?time=" + new Date().getTime();
});
$("#J_FaXian").click(function(){
	window.location.href = linkPre + "/discover.html?time=" + new Date().getTime();
});
$("#J_WoDe").click(function(){
	window.location.href = linkPre + "/me.html?time=" + new Date().getTime();
});

$(".J_Link").click(function(){
	var jThis = $(this);
	var link = jThis.data("href"),// 链接地址加/
		_linkPre = jThis.data("linkpre");
	if(!!link)
		window.location.href = (_linkPre == "/" ? "" : linkPre) + link + "?time=" + new Date().getTime();
});

$(".J_LinkToken").click(function(){
	var jThis = $(this);
	var link = jThis.data("href"),// 链接地址加/
		_linkPre = jThis.data("linkpre");
	if(!!link){
		// 判断是否登录，获取token
		tools.ajaxSubmit({
			"type": "GET",
			"url": gurl + "/insurance/remote/token",
			"suc_suc": function(response){
				// 已登录 则取token，并跳转
				window.location.href = (_linkPre == "/" ? "" : linkPre) + link + "?t=" + response.data.token + "&time=" + new Date().getTime();
			}
		});// ajaxSubmit
	}
});

//var _script = '<script src="https://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>';
//_script = '<script async src="https://testfp.udcredit.com/sdk/device-fingerprint/web?partnerCode=201601141404&appKey=wIqeiVOn6oxloDvzbD3X"></script>';
//_script += '<script async src="/ydWallet/js/yongda_ud_fp.js"></script>';
//https://testfp.udcredit.com
//https://service.udcredit.com:10000
//$("body").append(_script);