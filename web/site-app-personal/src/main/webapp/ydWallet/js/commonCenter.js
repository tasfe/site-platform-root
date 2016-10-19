//请求中心 - 通用部分
$("#confirm-next").click(function(){
	$(this).find(".yd-btnText").hide();
	$(this).find(".yd-btnLoading").show();
	stat=$(this).attr("stat");
	yhCode= $("bankInfo2").attr("bankid");
	//银行信息 根据case不同而不同，所以bankinfo在if中赋值
	setTimeout(function(){
		/*
		stat 1 余额不足未绑卡
		stat 2 余额不足未绑卡以外的其他2种情况
		*/
		if(stat=="4"){
			getPayTradeInfo(4,0);
			//绑卡入口 way1代表余额不足未绑卡的情况
			window.location.href="addBankCard.html?way=1";
		}
		else if(stat=="3"){
			//余额不足已绑卡;
			useBalanceCheck=$("#payCheck1").attr("check");
			if(useBalanceCheck=="check"){				
				getPayTradeInfo(3,1);
				useBalance=1;
			}
			else{
				getPayTradeInfo(3,0);
				useBalance=0;
			}
			window.location.href="payPassword.html?&useBalance="+useBalance+"&bind=1";
		}
		else if(stat=="2"){

		}
		else if(stat=="1"){
			useBalanceStat=$("#useBalanceInput").val();
			if(useBalanceStat=="1"){
				getPayTradeInfo(1,1);
				useBalance=1;
			}
			else{
				getPayTradeInfo(1,0);
				useBalance=0;
			}
			window.location.href="payPassword.html?useBalance="+useBalance+"&bind=1";
		}

	},500);
});
$("#addBankCard-next").click(function(){
	if($(this).hasClass("yd-btn-disabled")){
		return false;
	}
	cardNumber=$("#addCardInput").val();
	cardNumber=clearKG(cardNumber);
	checkCard(cardNumber);
});
$("#payPassword-next").click(function(){
	if($(this).hasClass("yd-btn-disabled")){
		return false;
	}
	type=getQueryString("payType");
	pwd=$("#payPassword").val();
	if(type=="cz"){
		if(checkPayPassword(pwd)){
			sessionStorage.setItem("pwd",pwd);
			submitCz(pwd);
		}
	}
	else{
		if(checkPayPassword(pwd)){
			sessionStorage.setItem("pwd",pwd);
			submitPay(pwd);
		}
	}

});
$("#msgCheck-next").click(function(){
	pwd=sessionStorage.getItem("pwd");
	authCode=$("#authCode").val();
	toPay(pwd,authCode);

});
//重发校验码
/*$("#sendMsg").click(function(){
	var clickFlag=$(this).attr("onClick");
	if(clickFlag=="return false;"){
		return false;
	}

	if($(this).attr("onClick")=="settime(this)"){
		//sendCheckMsg();
	}
});*/
$("#newCard").click(function(){
	//绑卡入口 way2表示从收银台绑卡界面进入
	getPayTradeInfo(1,0);
	window.location.href="addBankCard.html?way=2";
})
//收银台显示
function getCashierDeskInfo(){
sessionStorage.clear();
token=getQueryString("token");
postdata={"token":token,"memberType":1,"noNeedPayPassword":false};
url=gurl+"/cashier/h5/cashier.htm";
$.ajax({
  type:'POST',
  url:url,
  data:postdata,
  xhrFields: {
       withCredentials: true
  },
  crossDomain: true,
  success:function(data){
    if(data.success){
      tradeInfo=data.dataMap.cashierShowDomain.tradeVoucherList[0].summary;
      tradeAmount=data.dataMap.cashierShowDomain.tradeVoucherList[0].amount.amount;
      loginName=data.dataMap.cashierShowDomain.loginName;
      $("#loginName").text(loginName);
      $("#titleLoginName").text(loginName);
      $("#tradeInfo").text(tradeInfo);
      $("#tradeAmount").text(tradeAmount+"元");
      $("#payWay_blance").text("可用余额 "+parseFloat(data.dataMap.accountDetail.availableBalance.amount)+" 元");
      $("#loadingYD").hide();
      $(".confirmWrap,#confirm-next").show();
      //检查账户状态
      userStat=checkUserStat(data);
      //构建支付方式列表
      creatPayWayList(data);
      dcdata=JSON.stringify(data);
      sessionStorage.setItem("dcdata",dcdata);
     
    }
    else{
      //登录超时，跳转至登录
     // window.location.href="login.html?token="+postdata.token;
    }
  },
  error:function(){
    yonda.Tip("接口访问异常");
  }

});
}
//检查银行卡
function checkCard(carNum){
	postdata={"cardNo":carNum};
	url=gurl+"/cashier/h5/checkCard.htm";
	$.ajax({
	  type:'POST',
	  url:url,
	  data:postdata,
	  xhrFields: {
	       withCredentials: true
	  },
	  crossDomain: true,
	  success:function(data){
	    if(data.success){
	      carddata=JSON.stringify(data);
	      sessionStorage.setItem("carddata",carddata);
	      way=getQueryString("way");
	      payType=getQueryString("payType");
	      if(data.code=="NoRealName"){
	      	//console.log(data.code);
	      	window.location.href="setBankCard.html?type=no&cardNo="+carNum+"&way="+way+"&payType="+payType;
	      }
	      else{
	      	//console.log(data.code);
			window.location.href="setBankCard.html?type=yes&cardNo="+carNum+"&way="+way+"&payType="+payType;
	      }
	    }
	    else{
	      yonda.Tip(data.message);
	      $("#a-bankCardError").show();
	    }
	  },
	  error:function(){
	    yonda.Tip("接口访问异常");
	  }

	});
}

function checkUserStat(data){
	//判断余额是否充足
	balance=parseFloat(data.dataMap.accountDetail.availableBalance.amount);
	amount=parseFloat(data.dataMap.cashierShowDomain.tradeVoucherList[0].amount.amount);
	signedBankDCList=data.dataMap.signedBankList;
	if(balance>=amount){
		//console.log(balance>=amount)balance>=amount
		//余额充足情况下，判断是否绑卡
		if(signedBankDCList!=null){
			//显示默认支付的卡
			$("#confirm-next").attr("stat","1");
			$("#selectPayWay2").remove();
			$(".yd-checkbox").remove();
			bankName=signedBankDCList[0].bankName;
			bankAccountNumMask=signedBankDCList[0].bankAccountNumMask;
			bankAccountNumMask=bankAccountNumMask.substr(bankAccountNumMask.length-4);			
			$("#bankInfo").text(bankName+"("+bankAccountNumMask+")");
			sessionStorage.setItem("bankid",signedBankDCList[0].bankcardId);
		}
		else{
			//显示余额
			$("#confirm-next").attr("stat","2");
			$("#selectPayWay2").remove();
			$(".yd-checkbox").remove();
			$("#bankInfo").text("账户余额 "+balance+"");
		}
	}
	else{
		//余额不足情况下，判断是否绑卡
		if(signedBankDCList!=null){
			$("#confirm-next").attr("stat","3");
			$("#bankInfo").text("账户余额 "+balance+"");
			$("#selectPayWay").attr("select","false");
			//余额不足时，默认为不允许组合支付，且如果余额等于0，则不允许勾选组合支付			
			$("#payCheck1").removeClass("yd-checkboxSelected");
			if(balance=="0"){
				$("#payIcon1").remove();		
				$("#payCheck1").attr("check","uncheck");
				$("#payCheck1").attr("select","false");
			}
			//余额不足时，设置第二个复选框为默认选择，不可取消勾选
			$("#payCheck2").addClass("yd-checkboxSelected");
			$("#payCheck2").attr("check","check");
			$("#payCheck2").attr("select","false");
			$("#payCheck2").parents("li").find(".yd-layouts-right,.yd-layouts-left").css("color","#000");
			$("#userBlanceWay").hide();
			bankName=signedBankDCList[0].bankName;
			bankAccountNumMask=signedBankDCList[0].bankAccountNumMask;
			bankAccountNumMask=bankAccountNumMask.substr(bankAccountNumMask.length-4);			
			$("#bankInfo2").text(bankName+"("+bankAccountNumMask+")");
			sessionStorage.setItem("bankid",signedBankDCList[0].bankcardId);
		}
		else{
			$("#confirm-next").attr("stat","4");
			$(".yd-btnText").text("使用银行卡支付 "+amount+" 元");
			$("#payIcon1").remove();
			$("#payCheck1").removeClass("yd-checkboxSelected");
			$("#payCheck1").attr("check","uncheck");
			$("#selectPayWay,#payCheck1").attr("select","false");
			$("#bankInfo").text("账户余额 "+balance+"元");
			$("#payCheck2").addClass("yd-checkboxSelected");
			$("#payCheck2").attr("check","check");
			$("#bankInfo2").text("使用银行卡付款");
			$("#selectPayWay2").find(".yd-layouts-right").removeClass("text-gray");
		}

	}
}
function creatPayWayList(data){
	signedBankDCList=data.dataMap.signedBankList;
	bankAccountNumMask=signedBankDCList[0].bankAccountNumMask;
	bankAccountNumMask=bankAccountNumMask.substr(bankAccountNumMask.length-4);
	if(signedBankDCList!=null){		
		for(i=0;i<signedBankDCList.length;i++){
			singleLimit=signedBankDCList[i].singleLimit;
			$("#userBlanceWay").before("<li payid='' type='payWay' bankid='"+signedBankDCList[i].bankcardId+"' number='"+bankAccountNumMask+"'><span class='yd-layouts-left'><i class='bank-icon "+signedBankDCList[i].bankCode+"' iconType=''>&nbsp;</i></span><span class='bankInfo yd-layouts-center'><span class='bankName'>"+signedBankDCList[i].bankName+"("+bankAccountNumMask+")</span><br><span class='yd-inlineBlock bankMoney'>单笔可用额度 "+singleLimit+" 元</span></span><span class='yd-layouts-right selectBank'></span></li>");
		}
	}
}
//提交快捷支付
function submitPay(pwd){
	dcdataStr=sessionStorage.getItem("dcdata");
	dcdata=$.parseJSON(dcdataStr);
	carddataStr=sessionStorage.getItem("carddata");
	carddata=$.parseJSON(carddataStr);
	setbankInfoListStr=sessionStorage.getItem("setbankInfoList");
	setbankInfoList=$.parseJSON(setbankInfoListStr);
	cardNo=getQueryString("cardNo");
	bind=getQueryString("bind");
	useBalance=parseInt(getQueryString("useBalance"));
	bankid=sessionStorage.getItem("bankid");
	if(bind!="1"){
		//未绑卡
		if(carddata.dataMap.cardBin.cardType=="DC"){
			//借记卡
			alert("借记卡");
			postdata={"useBalance":useBalance,"instCode":carddata.dataMap.cardBin.bankCode,"instName":carddata.dataMap.cardBin.bankName,"dbcr":carddata.dataMap.cardBin.cardType,"realName":setbankInfoList.add_name,"identityNo":setbankInfoList.add_idCard,"bankCardNo":cardNo,"mobileNum":setbankInfoList.add_phone,"password":pwd};

		}
		else if(carddata.dataMap.cardBin.cardType=="CC"){
			//信用卡
			postdata={"useBalance":useBalance,"instCode":carddata.dataMap.cardBin.bankCode,"instName":carddata.dataMap.cardBin.bankName,"dbcr":carddata.dataMap.cardBin.cardType,"realName":setbankInfoList.add_name,"identityNo":setbankInfoList.add_idCard,"bankCardNo":cardNo,"periodOfMonth":"","periodOfYear":"","cvv2Num":setbankInfoList.add_cvv,"mobileNum":setbankInfoList.add_phone,"password":pwd};
		}
	}
	else{
		//已绑卡
		postdata={"useBalance":useBalance,"instId":bankid,"password":pwd};
	}
	console.log(JSON.stringify(postdata));
	url=gurl+"/cashier/h5/submitPay.htm";
	$.ajax({
	  type:'POST',
	  url:url,
	  data:postdata,
	  xhrFields: {
	       withCredentials: true
	  },
	  crossDomain: true,
	  success:function(data){
	    if(data.success){
	      //如果是直接余额支付，则跳过短信验证环节，直接显示支付结果
	      onlyBlance=sessionStorage.getItem("onlyBlance");
	      if(onlyBlance=="1"){
	      	//alert("onlyBlance");
	      	//sessionStorage.setItem("test1",data);
	      	window.location.href="payResult.html?payType=normal";
	      }
	      else{
	      	window.location.href="msgCheck.html";
	      }
	    }
	    else{
	      yonda.Tip(data.message);
	      $("#a-bankCardError").show();
	    }
	  },
	  error:function(){
	    yonda.Tip("接口访问异常");
	  }

	});
}
function toPay(pwd,authCode){
	postdata={"password":pwd,"authCode":authCode,"useBalance":0};
	url=gurl+"/cashier/h5/pay.htm";
	$.ajax({
	  type:'POST',
	  url:url,
	  data:postdata,
	  xhrFields: {
	       withCredentials: true
	  },
	  crossDomain: true,
	  success:function(data){
	    if(data.success){
	      //yonda.Tip("支付成功！");
	      payResult=JSON.stringify(data);
	      sessionStorage.setItem("payResult",payResult);
	      payType=getQueryString("payType");
	      if(payType=="cz"){
	      	window.location.href="payResult.html?payType=cz";
	      }
	      else{
	      	window.location.href="payResult.html?payType=normal";
	      }
	    }
	    else{
	      yonda.Tip("支付失败！");
	    }
	  },
	  error:function(){
	    yonda.Tip("接口访问异常");
	  }

	});
}
function sendCheckMsg(){
	postdata="";
	url=gurl+"/cashier/h5/rePreQuickPay.htm";
	$.ajax({
	  type:'POST',
	  url:url,
	  data:postdata,
	  xhrFields: {
	       withCredentials: true
	  },
	  crossDomain: true,
	  success:function(data){
	    if(data.success){
	      yonda.Tip("信息发送成功");
	    }
	    else{
	      yonda.Tip("信息未能正确发送");
	      $("#a-bankCardError").show();
	    }
	  },
	  error:function(){
	    yonda.Tip("接口访问异常");
	  }

	});
}
// 获取URL中指定字符串的值
function getQueryString(name) {
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
	var r = window.location.search.substr(1).match(reg);
	if (r != null) return r[2]; return "";
};

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
};

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
};
//请求中心 - 登录流程


//通用函数
function clearKG(ss){ 
	return ss.replace(/[ ]/g,""); 
} 
function checkPayPassword(pwd){
	if(pwd==""||pwd==undefined||pwd=="undefined"){
		yonda.Tip("支付密码不能为空");
		return false;
	}
	return true;
}

//显示用户名
function setTitleId(){
	dcdataStr2=sessionStorage.getItem("dcdata");
	dcdata2=$.parseJSON(dcdataStr2);
	loginName=dcdata2.dataMap.cashierShowDomain.loginName;
	$("#titleId").text(loginName);
}

//输入支付密码时获取订单信息
function getPayTradeInfo(type,useBalance){
	if(type==4){
		amount=$("#tradeAmount").text();
		amount=amount.replace(/[^\d.]/g,'');
		amount=parseFloat(amount);
		sessionStorage.setItem("blance","null");
		sessionStorage.setItem("amount",amount);
		//银行信息后续在绑卡完成时获取
	}
	else if(type==3){
		blance=$("#bankInfo").text();
		blance=blance.replace(/[^\d.]/g,'');
		blance=parseFloat(blance);
		amount=$("#tradeAmount").text();
		amount=amount.replace(/[^\d.]/g,'');
		amount=parseFloat(amount);
		bankInfo=$("#bankInfo2").text();
		sessionStorage.setItem("blance","null");
		sessionStorage.setItem("amount",amount);
		sessionStorage.setItem("cardPay",amount);
		sessionStorage.setItem("bankInfo",bankInfo);
		sessionStorage.setItem("payWay","card");
		if(useBalance==1){
			cardPay=$("#zhzf").text();		
			cardPay=cardPay.replace(/[^\d.]/g,'');
			cardPay=parseFloat(cardPay);
			sessionStorage.setItem("blance",blance);
			sessionStorage.setItem("cardPay",cardPay);
			sessionStorage.setItem("payWay","zuhe");
		}
	}
	else if(type==2){

	}
	else if(type==1){
		bankInfo=$("#bankInfo").text();
		blance=$("#payWay_blance").text();
		blance=blance.replace(/[^\d.]/g,'');
		blance=parseFloat(blance);
		amount=$("#tradeAmount").text();
		amount=amount.replace(/[^\d.]/g,'');
		amount=parseFloat(amount);
		sessionStorage.setItem("amount",amount);
		sessionStorage.setItem("cardPay",amount);
		sessionStorage.setItem("bankInfo",bankInfo);
		if(useBalance==1){
			sessionStorage.setItem("payWay","blance");
			sessionStorage.setItem("onlyBlance","1");
		}
		else if(useBalance==0){
			sessionStorage.setItem("payWay","card");	
		}

	}
}

//充值功能模块
$(document).on("click",".czPayWay",function(){
	bankid=$(this).attr("payid");
	bankInfo=$(this).find(".bankName2").text();
	bind=$("#cz-bindStat").val();
	sessionStorage.setItem("czbankid",bankid);
  	sessionStorage.setItem("czbankInfo",bankInfo);
	/*if(bind=="0"){
		window.location.href="payPassword.html?payType=cz&bind=0";
	}
	else{*/
	window.location.href="payPassword.html?payType=cz&bind=1";
	//}
});
$(".cz-addCard").click(function(){
	window.location.href="addBankCard.html?way=3&payType=cz";
});
function getCzCardList(){
sessionStorage.clear();
token=getQueryString("token");
postdata={"token":token,"memberType":1,"noNeedPayPassword":false};
url=gurl+"/cashier/h5/cashier.htm";
$.ajax({
  type:'POST',
  url:url,
  data:postdata,
  xhrFields: {
       withCredentials: true
  },
  crossDomain: true,
  success:function(data){
    if(data.success){
		//yonda.Tip("请求成功");
		czdata=JSON.stringify(data);
		sessionStorage.setItem("czdata",czdata);
     
    }
    else{
    	yonda.Tip("交易支付超时");
      //登录超时，跳转至登录
     // window.location.href="login.html?token="+postdata.token;
    }
  },
  error:function(){
    yonda.Tip("接口访问异常");
  }
});
}
function submitCz(pwd){
	dcdataStr=sessionStorage.getItem("dcdata");
	dcdata=$.parseJSON(dcdataStr);
	carddataStr=sessionStorage.getItem("carddata");
	carddata=$.parseJSON(carddataStr);
	setbankInfoListStr=sessionStorage.getItem("setbankInfoList");
	setbankInfoList=$.parseJSON(setbankInfoListStr);
	cardNo=getQueryString("cardNo");
	bind=getQueryString("bind");
	useBalance=parseInt(getQueryString("useBalance"));
	bankid=sessionStorage.getItem("czbankid");
	if(bind!="1"){
		//充值-未绑卡
		if(carddata.dataMap.cardBin.cardType=="DC"){
			//借记卡
			postdata={"instCode":carddata.dataMap.cardBin.bankCode,"instName":carddata.dataMap.cardBin.bankName,"dbcr":carddata.dataMap.cardBin.cardType,"realName":setbankInfoList.add_name,"identityNo":setbankInfoList.add_idCard,"bankCardNo":cardNo,"mobileNum":setbankInfoList.add_phone,"password":pwd};

		}
		else if(carddata.dataMap.cardBin.cardType=="CC"){
			//信用卡
			postdata={"instCode":carddata.dataMap.cardBin.bankCode,"instName":carddata.dataMap.cardBin.bankName,"dbcr":carddata.dataMap.cardBin.cardType,"realName":setbankInfoList.add_name,"identityNo":setbankInfoList.add_idCard,"bankCardNo":cardNo,"periodOfMonth":"","periodOfYear":"","cvv2Num":setbankInfoList.add_cvv,"mobileNum":setbankInfoList.add_phone,"password":pwd};
		}
	}
	else{
		//充值-已绑卡
		postdata={"instId":bankid,"password":pwd};
	}
	//console.log(JSON.stringify(postdata));
	url=gurl+"/cashier/h5/submitRechargePay.htm";
	$.ajax({
	  type:'POST',
	  url:url,
	  data:postdata,
	  xhrFields: {
	       withCredentials: true
	  },
	  crossDomain: true,
	  success:function(data){
	    if(data.success){
	      window.location.href="msgCheck.html?payType=cz";
	    }
	    else{
	      yonda.Tip(data.message);
	    }
	  },
	  error:function(){
	    yonda.Tip("接口访问异常");
	  }

	});
}
function czCardList(){
	czdataStr=sessionStorage.getItem("czdata");
	data=$.parseJSON(czdataStr);
	signedBankDCList=data.dataMap.signedBankList;
	   if(signedBankDCList=="null"){
	   		$("#cz-bindStat").val("0");
	   }
	   else{       	
	       for(i=0;i<signedBankDCList.length;i++){
	       		bankName=signedBankDCList[i].bankName;
	       		bankcardId=signedBankDCList[i].bankcardId;
	       		bankAccountNumMask=signedBankDCList[i].bankAccountNumMask;
	       		bankAccountNumMask=bankAccountNumMask.substr(bankAccountNumMask.length-4);
	       		bankIconType=signedBankDCList[i].bankCode;
	       		$("#cz-cardList").before("<li payid='"+bankcardId+"' class='czPayWay'><span class='yd-layouts-left cz-bankIcon'><i class='bank-icon' iconType='"+bankIconType+"' style='background:#CCC'>&nbsp;</i></span><span class='bankInfo yd-layouts-center cz-bankInfo'><span class='bankName2 yd-weight'>"+bankName+"("+bankAccountNumMask+")</span><br><span class='yd-inlineBlock bankMoney'>可用余额：</span></span></li>");
	       		//bankName=signedBankDCList[i].bankName;
	       		$("#cz-bindStat").val("1");
	       }
	   }
}
//登录接口
function login(username,pwd){
	postdata={"userName":username,"passwd":pwd};
	url=gurl+"/cashier/h5/login.htm";
	$.ajax({
	  type:'POST',
	  url:url,
	  data:postdata,
	  xhrFields: {
	       withCredentials: true
	  },
	  crossDomain: true,
	  success:function(data){
	    if(data.success){
	      //返回的URL
	      url=data.redirectUrl;
	      yonda.Tip("登录成功");
	    }
	    else{
	      yonda.Tip(data.message);
	    }
	  },
	  error:function(){
	    yonda.Tip("接口访问异常");
	  }

	});
}
//免登和找回支付密码流程
function checkPayPw(username){
postdata={"userName":username};
	url=gurl+"/cashier/h5/checkAccount.htm";
	$.ajax({
	  type:'POST',
	  url:url,
	  data:postdata,
	  xhrFields: {
	       withCredentials: true
	  },
	  crossDomain: true,
	  success:function(data){
	    if(data.success){
	      //已设置支付密码
	    }
	    else{
	      if(data.code=="NoReg"){
	      	yonda.Tip(data.message);
	      }
	      else if(data.code=="PAYPWD_IS_NULL"){
	      	//未设置支付密码
	      	if(data.dataMap.authInfo==undefined){
	      		fullLoginName=0;
	      	}
	      	else{
	      		fullLoginName=1;
	      		authName=data.dataMap.authInfo.authName;
	      		sessionStorage.setItem("authName",authName);
	      	}
	      	window.location.href="zh-index.html?setPwd=0&fullLoginName="+fullLoginName;
	      }
	    }
	  },
	  error:function(){
	    yonda.Tip("接口访问异常");
	  }

	});
}
function checkCertificates(userName,idcard){
	postdata={"userName":userName,"certificates":idcard};
	url=gurl+"/cashier/h5/checkCertificates.htm";
	$.ajax({
	  type:'POST',
	  url:url,
	  data:postdata,
	  xhrFields: {
	       withCredentials: true
	  },
	  crossDomain: true,
	  success:function(data){
	    if(data.success){
	      window.location.href="msgCheck2.html?phone="
	    }
	    else{
	      	yonda.Tip(data.message);	      
	    }
	  },
	  error:function(){
	    yonda.Tip("接口访问异常");
	  }

	});
}
function doVerifyIdentity(userName,idcard,name){
	postdata={"userName":userName,"certificates":idcard,"realName":realName};
	url=gurl+"/cashier/h5/doVerifyIdentity.htm";
	$.ajax({
	  type:'POST',
	  url:url,
	  data:postdata,
	  xhrFields: {
	       withCredentials: true
	  },
	  crossDomain: true,
	  success:function(data){
	    if(data.success){
	      window.location.href="msgCheck2.html?phone="
	    }
	    else{
	      	yonda.Tip(data.message);	      
	    }
	  },
	  error:function(){
	    yonda.Tip("接口访问异常");
	  }

	});
}
function sendZhMsg(){

}
function checkZhMsg(mobileCaptcha){
	postdata={"userName":username,"mobileCaptcha":mobileCaptcha,"bizType":bizType};
	url=gurl+"/cashier/h5/validateCaptcha.htm";
	$.ajax({
	  type:'POST',
	  url:url,
	  data:postdata,
	  xhrFields: {
	       withCredentials: true
	  },
	  crossDomain: true,
	  success:function(data){
	    if(data.success){
	      window.location.href="zh-password.html?phone="
	    }
	    else{
	      	yonda.Tip(data.message);	      
	    }
	  },
	  error:function(){
	    yonda.Tip("接口访问异常");
	  }

	});
}
function zhSetPayPw(username,pwd,isFirstSetting,uniqueCode){
	postdata={"userName":username,"payPassword":pwd,"isFirstSetting":isFirstSetting,"uniqueCode":uniqueCode};
	url=gurl+"/cashier/h5/setPayPassword.htm";
	$.ajax({
	  type:'POST',
	  url:url,
	  data:postdata,
	  xhrFields: {
	       withCredentials: true
	  },
	  crossDomain: true,
	  success:function(data){
	    if(data.success){
	      yonda.Tip("设置成功");
	    }
	    else{
	      	yonda.Tip(data.message);	      
	    }
	  },
	  error:function(){
	    yonda.Tip("接口访问异常");
	  }

	});
}