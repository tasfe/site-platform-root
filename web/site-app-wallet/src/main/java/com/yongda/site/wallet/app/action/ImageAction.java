package com.yongda.site.wallet.app.action;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.netfinworks.common.lang.StringUtil;
import com.netfinworks.site.core.common.KaptchaProducer;
import com.netfinworks.site.core.common.util.RadomUtil;
import com.netfinworks.ufs.client.UFSClient;
import com.yongda.site.wallet.app.WebDynamicResource;
import com.yongda.site.wallet.app.action.common.BaseAction;

@SuppressWarnings("deprecation")
@Controller
public class ImageAction extends BaseAction {
    protected static final Logger logger = LoggerFactory.getLogger(ImageAction.class);

    @Autowired
    private KaptchaProducer       kaptchaProducer;
    
    @Resource(name="webResource")
	private WebDynamicResource webResource;
    
    @Resource(name = "pfsMgrUfsClient")
    private UFSClient           ufsClient;

    /**
     * 生成图片验证码
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value="/pvc",method = RequestMethod.GET)
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        response.setDateHeader("Expires", 0);
        // Set standard HTTP/1.1 no-cache headers.
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        response.setHeader("Pragma", "no-cache");
        // return a jpeg
        response.setContentType("image/jpeg");
        // create the text for the image
        kaptchaProducer.createRandomCode(request, response);
        logger.info("从seesion里取登录验证码：{}",
                request.getSession().getAttribute(KaptchaProducer.KAPTCHA_KEY));
    }

    public static boolean validateRandCode(HttpServletRequest request, String randCode)
            throws Exception {
        logger.info("kaptcha validate:"
                + request.getSession().getAttribute(KaptchaProducer.KAPTCHA_KEY));

        String realCode = (String) request.getSession().getAttribute(KaptchaProducer.KAPTCHA_KEY);
        if ((randCode == null) || !randCode.equalsIgnoreCase(realCode)) {
            request.getSession().setAttribute(KaptchaProducer.KAPTCHA_KEY,
                    "" + RadomUtil.createRadom());
            return false;
        }
        return true;
    }
    
    
    /**
     * 获取UFS图片
     * @param fileName
     * @param model
     * @param request
     * @param response
     */
	@RequestMapping(value = "/image", method = RequestMethod.GET)
	public void getFile(String fileName, ModelMap model, HttpServletRequest request,
                        HttpServletResponse response) {
		if (StringUtil.isBlank(fileName)) {
            model.put("ERROR_MESSAGE", "文件路径不能为空");
        }
		String filePath = fileName;
		if(!filePath.startsWith("http://"))
			filePath = StringUtils.trim(webResource.getUploadFilePath()) + StringUtils.trim(filePath);
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
    
    protected String getFileName(String filePath) {
		return filePath.substring(filePath.lastIndexOf(".")+1, filePath.length());
	}
    
	private void buildResponse(String fileName, HttpServletRequest request,
			HttpServletResponse response) {
		String fileNameParam = request.getHeader("User-Agent").indexOf(
				"Firefox") != -1 ? convertString4FireFox(fileName)
				: convertString(fileName);
		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ fileNameParam + "\";");
		response.setContentType("application/octet-stream");
	}

	private String convertString4FireFox(String s) {
		String returnStr = s;
		try {
			returnStr = new String(s.getBytes("UTF-8"), "ISO8859-1");
		} catch (Exception e) {
			logger.warn(e.getMessage());
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

	private boolean isImage(String str) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("jpg", "jpg");
		map.put("jpeg", "jpeg");
		map.put("gif", "gif");
		map.put("bmp", "bmp");
		map.put("png", "png");
		if ((null == map.get(str.toLowerCase()))
				|| "".equals(map.get(str.toLowerCase()))) {
			return false;
		}
		return true;
	}
}
