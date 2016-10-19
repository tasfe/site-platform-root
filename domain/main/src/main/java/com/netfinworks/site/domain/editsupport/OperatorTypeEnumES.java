package com.netfinworks.site.domain.editsupport;

import java.beans.PropertyEditorSupport;

import org.apache.commons.lang.StringUtils;

import com.netfinworks.site.domain.enums.OperatorTypeEnum;

public class OperatorTypeEnumES extends PropertyEditorSupport {
    
    @Override
    public void setAsText(String code)throws java.lang.IllegalArgumentException{
        if(StringUtils.isNotBlank(code)){
            OperatorTypeEnum enumObject=OperatorTypeEnum.getByCode(Integer.parseInt(code));
            if(enumObject!=null){
                this.setValue(enumObject);
            }
        }
    }
    
    @Override
    public String getAsText() {
        if(getValue()==null){
            return null;
        }else{
            OperatorTypeEnum enumObject=(OperatorTypeEnum) getValue();
            return enumObject.getMsg();
        }
    }
}
