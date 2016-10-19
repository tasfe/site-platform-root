package com.yongda.site.app.common.converter;

import java.beans.PropertyEditorSupport;

import com.netfinworks.common.lang.StringUtil;

/**
 * <p>过滤掉String两侧的空格</p>
 * @author eric
 * @version $Id: DateCustomEditor.java, v 0.1 2011-3-7 下午04:00:10 fuchunjie Exp $
 */
public class StringTrimCustomConverter extends PropertyEditorSupport {

    private final boolean doStringTrim;

    public StringTrimCustomConverter() {
        doStringTrim = false;
    }

    public StringTrimCustomConverter(boolean doStringTrim) {
        this.doStringTrim = doStringTrim;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (!doStringTrim || StringUtil.isEmpty(text)) {
            setValue(text);
        } else {
            setValue(text.trim());
        }
    }

    /**
     * toString()
     */
    public String getAsText() {
        return (getValue() == null) ? null : getValue().toString();
    }
}
