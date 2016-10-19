/**
 * Copyright 2013 sina.com, Inc. All rights reserved.
 * sina.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netfinworks.site.web.action.paypasswd;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.netfinworks.common.lang.StringUtil;
import com.netfinworks.site.core.common.RestResponse;
import com.netfinworks.site.core.common.constants.CommonConstant;
import com.netfinworks.site.core.common.util.FileUploader;
import com.netfinworks.site.domain.domain.member.EnterpriseMember;
import com.netfinworks.site.domain.domain.response.UsfUploadFileResponse;
import com.netfinworks.site.domain.exception.BizException;
import com.netfinworks.site.ext.integration.member.PayPasswdService;
import com.netfinworks.site.web.WebDynamicResource;
import com.netfinworks.site.web.action.common.BaseAction;
import com.netfinworks.ufs.client.UFSClient;

/**
 *
 * <p>支付密码controller</p>
 * @author Guan Xiaoxu
 * @version $Id: PaypasswdAction.java, v 0.1 2013-12-6 下午4:55:43 Guanxiaoxu Exp $
 */
@Controller
public class ImgUploadAction extends BaseAction {

    protected Logger                log                = LoggerFactory.getLogger(getClass());

    public final static String      CAPTCHA_SET_PAYPWD = "_SET_PAYPWD";
    @Resource(name = "payPasswdService")
    private PayPasswdService payPasswdService;
    @Autowired
    private UFSClient           ufsClient;

	@Value("${uploadFileSize}")
	private String uploadFileSize;
	
	@Resource(name = "webResource")
	private WebDynamicResource webResource;

	/**
	 * 上传文件
	 * 
	 * @param model
	 * @param request
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/my/uploadFile123.htm", method = RequestMethod.POST)
	public String uploadFile(HttpServletRequest request, MultipartFile file_select1, MultipartFile file_select2,
			MultipartFile file_select3, MultipartFile file_select4, MultipartFile file_select5,
			MultipartFile file_select6, MultipartFile file_select4_1, MultipartFile file_select4_2,
			MultipartFile file_select4_3, MultipartFile file_select4_4, HttpServletResponse response)
			throws Exception {
		// 判断传进来的是哪个文件控件
		MultipartFile file = null;
		if (file_select1 != null) {
			file = file_select1;
		}
		if (file_select2 != null) {
			file = file_select2;
		}
		if (file_select3 != null) {
			file = file_select3;
		}
		if (file_select4 != null) {
			file = file_select4;
		}
		if (file_select5 != null) {
			file = file_select5;
		}
		if (file_select6 != null) {
			file = file_select6;
		}
		if (file_select4_1 != null) {
			file = file_select4_1;
		}
		if (file_select4_2 != null) {
			file = file_select4_2;
		}
		if (file_select4_3 != null) {
			file = file_select4_3;
		}
		if (file_select4_4 != null) {
			file = file_select4_4;
		}
		String fileName = file.getOriginalFilename();
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		if (isImage(fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()))) {
			if (logger.isInfoEnabled()) {
				logger.info("文件名称为：{}，上传的文件的大小为：{}", fileName, file.getSize());
			}
			if (file.getSize() <= Long.valueOf(uploadFileSize)) {
				try {
					EnterpriseMember user = getUser(request);
					String frontFileName = FileUploader.createFileName(user.getMemberId(), file.getOriginalFilename());
					UsfUploadFileResponse responseFront = payPasswdService.saveFile(file, frontFileName);
					int index = responseFront.getDownloadUrl().lastIndexOf("/");
					log.info("图片路径：" + responseFront.getDownloadUrl());
					out.print("{\"success\":true,\"message\":\"" + responseFront.getDownloadUrl().substring(index + 1)
							+ "\"}");
					out.flush();
					out.close();
					return null;

				} catch (Exception e) {
					log.error("ufs调用有误，上传失败！错误信息:{},{}", e.getMessage(), e);
					out.print("{\"success\":false,\"message\":\"文件上传失败，请重试！\"}");
					out.flush();
					out.close();
					return null;
				}
			} else {
				log.warn("1`上传图片过大！");
				out.print("{\"success\":false,\"message\":\"上传图片过大！\"}");
				out.flush();
				out.close();
				return null;
			}
		} else {
			log.warn("1`上传图片格式不对！");
			out.print("{\"success\":false,\"message\":\"上传图片格式不对！\"}");
			out.flush();
			out.close();
		}
		return null;

	}

	/**
	 * 异步上传文件
	 * 
	 * @param req
	 * @return
	 * @throws BizException
	 */
	@RequestMapping(value = "/my/uploadFile.htm", method = RequestMethod.POST)
	@ResponseBody
	public ModelAndView ajaxUploadFile(HttpServletRequest request, MultipartFile file_select, String input_path_id,
			String input_path, String input_imgurl_id) {
		RestResponse restP = new RestResponse();

		Map<String, Object> data = new HashMap<String, Object>();

		data.put("input_path_id", input_path_id);
		data.put("input_path", input_path);
		data.put("input_imgurl_id", input_imgurl_id);

		restP.setData(data);

		MultipartFile file = null;
		if (file_select != null) {
			file = file_select;
		} else {
			data.put("errorMsg", "上传文件失败");
			return new ModelAndView(CommonConstant.URL_PREFIX + "/static/fileupload", "response", restP);
		}
		String fileName = file.getOriginalFilename();
		if (isImage(fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()))) {
			if (logger.isInfoEnabled()) {
				logger.info("文件名称为：{}，上传的文件的大小为：{}", fileName, file.getSize());
			}
			if (file.getSize() <= Long.valueOf(uploadFileSize)) {
				try {
					EnterpriseMember user = getUser(request);
					String frontFileName = FileUploader.createFileName(user.getMemberId(), file.getOriginalFilename());
					UsfUploadFileResponse responseFront = payPasswdService.saveFile(file, frontFileName);
					int index = responseFront.getDownloadUrl().lastIndexOf("/");
					log.info("图片路径：" + responseFront.getDownloadUrl());
					data.put("input_imgurl", responseFront.getDownloadUrl().substring(index + 1));
					data.put("success", "true");

				} catch (Exception e) {
					log.error("ufs调用有误，上传失败！错误信息:{},{}", e.getMessage(), e);
					data.put("errorMsg", "上传文件失败");
					data.put("success", "false");
				}
			} else {
				log.warn("上传图片过大！");
				data.put("success", "false");
				data.put("errorMsg", "上传图片过大");
			}
		} else {
			log.warn("上传图片格式不对！");
			data.put("success", "false");
			data.put("errorMsg", "上传图片格式不对");
		}
		return new ModelAndView(CommonConstant.URL_PREFIX + "/static/fileupload", "response", restP);
	}

	/**
	 * 文件上传中转页面
	 * 
	 * @param request
	 * @param resp
	 * @param env
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/my/goFile.htm", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView goFile(HttpServletRequest request, HttpServletResponse resp)
			throws Exception {

		RestResponse restP = new RestResponse();

		return new ModelAndView(CommonConstant.URL_PREFIX + "/static/fileupload", "response", restP);
	}

    /**
     * 上传身份证
     *
     * @param model
     * @param request
     * @return
     * @throws IOException 
     */
    @RequestMapping(value = "/my/uploadFrontFile.htm", method = RequestMethod.POST)
    public String doPayBackPassWord(HttpServletRequest request,@RequestParam MultipartFile[] frontFile, HttpServletResponse response) throws IOException {
                String fileName=frontFile[0].getOriginalFilename();
        response.setContentType("text/plain; charset=UTF-8");        
        PrintWriter out = response.getWriter(); 
        if(isImage(fileName.substring(fileName.lastIndexOf(".")+1,fileName.length()))){
            if(frontFile[0].getSize()<=2097152){      
                        try {
                            MultipartFile front  =  frontFile[0]; 
                            EnterpriseMember user = getUser(request);                           
                            String frontFileName = FileUploader.createFileName(user.getMemberId(), front.getOriginalFilename());
                            UsfUploadFileResponse responseFront = payPasswdService.saveFile(front,frontFileName);
                            log.info("图片路径："+responseFront.getDownloadUrl());
                            //out.print("0`" + "http://www.baidu.com/img/sslm1_logo.gif");
                            out.print("0`" + responseFront.getDownloadUrl());                            
                            out.flush(); 
                            out.close();
                            return null;
        
                        } catch (BizException e) {
                            log.error("ufs调用有误，上传失败！");
                            e.printStackTrace();        
                            out.print("1`文件上传失败，请重试！！");
                            out.flush(); 
                            out.close();
                            return null;
                        }
            }else{
                out.print("1`上传图片过大！");
                out.flush(); 
                out.close();
                return null;
            }
        }else{
            out.print("1`上传图片格式不对！");
            out.flush(); 
            out.close();
        }
        return null;
        
  }
    /**
     * 上传身份证
     *
     * @param model
     * @param request
     * @return
     * @throws IOException 
     */
    @RequestMapping(value = "/my/uploadBackFile.htm", method = RequestMethod.POST)
    public String doPayBackPassWd(HttpServletRequest request,@RequestParam MultipartFile[] backFile, HttpServletResponse response) throws IOException {
        String fileName=backFile[0].getOriginalFilename();
        log.info("------type--------"+fileName.substring(fileName.lastIndexOf(".")+1,fileName.length()));
        log.info("-------size--------"+backFile[0].getSize());
        response.setContentType("text/plain; charset=UTF-8");        
        PrintWriter out = response.getWriter(); 
        if(isImage(fileName.substring(fileName.lastIndexOf(".")+1,fileName.length()))){
            if(backFile[0].getSize()<=2097152){      
                        try {
                            MultipartFile front  =  backFile[0]; 
                            EnterpriseMember user = getUser(request);                           
                            String frontFileName = FileUploader.createFileName(user.getMemberId(), front.getOriginalFilename());
                            UsfUploadFileResponse responseFront = payPasswdService.saveFile(front,frontFileName);
                            log.info("图片url："+responseFront.getDownloadUrl());
                            //out.print("0`" + "http://www.baidu.com/img/sslm1_logo.gif");
                            out.print("0`" + responseFront.getDownloadUrl());                            
                            out.flush();  
                            out.close();
                            return null;
        
                        } catch (BizException e) {
                            log.error("ufs调用有误，上传失败！");
                            e.printStackTrace();        
                            out.print("1`文件上传失败，请重试！！");
                            out.flush();  
                            out.close();
                            return null;
                        }
            }else{
                out.print("1`上传图片过大！");
                out.flush();   
                out.close();
                return null;
            }
        }else{
            out.print("1`上传图片格式不对！");
            out.flush(); 
            out.close();
        }
        return null;
        
                    }
    @RequestMapping(value = "/site/getFile.htm", method = RequestMethod.GET)
	public void getFile(String fileName, ModelMap model, HttpServletRequest request,
                        HttpServletResponse response) {
		if (StringUtil.isBlank(fileName)) {
            model.put("ERROR_MESSAGE", "文件路径不能为空");
        }
		String filePath = StringUtils.trim(webResource.getUploadFilePath()) + StringUtils.trim(fileName);
        HttpClient client = new DefaultHttpClient();
        HttpUriRequest requestGet = new HttpGet(filePath);
        requestGet.addHeader(
            "authorization",
            "Basic "
                    + Base64.encodeBase64String((ufsClient.getUser() + ":" + ufsClient
                        .getPassword()).getBytes()));
        try {
            buildResponse(getFileName(filePath), request, response);
            HttpResponse resp = client.execute(requestGet);
            Reader reader = new InputStreamReader(resp.getEntity().getContent(), "ISO8859-1");
            PrintWriter out = response.getWriter();
            while (true) {
                int read = reader.read();
                if (read == -1) {
					break;
				}
                out.write(read);
            }
            reader.close();
            out.close();
        } catch (ClientProtocolException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {

        }

    }



    private static String getFileName(String filePath) {
        String file = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
        return file;
    }
    private void buildResponse(String fileName, HttpServletRequest request,
                               HttpServletResponse response) {
        // String ext = fileName.substring(fileName.lastIndexOf(".") +
        // 1).toLowerCase();
        // response.setContentType(this.getContentType(ext) + ";charset=" +
        // CONTENT_ENCODE);
        String fileNameParam = request.getHeader("User-Agent").indexOf("Firefox") != -1 ? convertString4FireFox(fileName)
            : convertString(fileName);
        response
            .setHeader("Content-Disposition", "attachment; filename=\"" + fileNameParam + "\";");
        response.setContentType("application/octet-stream");
    }
    private String convertString4FireFox(String s) {
        String returnStr = s;
        try {
            returnStr = new String(s.getBytes("UTF-8"), "ISO8859-1");
        } catch (Exception e) {
            log.warn(e.getMessage());
        }

        return returnStr;
    }
    protected String convertString(String s) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if ((c >= 0) && (c <= 255)) {
                sb.append(c);
            } else {
                byte[] b;

                try {
                    b = Character.toString(c).getBytes("UTF-8");
                } catch (Exception ex) {
                    b = new byte[0];
                }

                for (byte element : b) {
                    int k = element;

                    if (k < 0) {
                        k += 256;
                    }

                    sb.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }

        return sb.toString();
    }
    private boolean isImage(String str){
        Map<String,String> map=new HashMap<String, String>();
        map.put("jpg", "jpg");
        map.put("jpeg", "jpeg");
        map.put("gif", "gif");
        map.put("bmp", "bmp");
        map.put("png", "png");
        if((null==map.get(str.toLowerCase()))||"".equals(map.get(str.toLowerCase()))){
            return false;
        }
        return true;
    }

}
