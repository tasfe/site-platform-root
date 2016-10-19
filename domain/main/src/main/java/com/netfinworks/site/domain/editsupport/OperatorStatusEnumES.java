package com.netfinworks.site.domain.editsupport;

import java.beans.PropertyEditorSupport;

import org.apache.commons.lang.StringUtils;

import com.netfinworks.site.domain.enums.OperatorStatusEnum;

public class OperatorStatusEnumES extends PropertyEditorSupport {
    
    @Override
    public void setAsText(String code)throws java.lang.IllegalArgumentException{
        if(StringUtils.isNotBlank(code)){
            OperatorStatusEnum enumObject=OperatorStatusEnum.getByCode(Integer.parseInt(code));
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
            OperatorStatusEnum enumObject=(OperatorStatusEnum) getValue();
            return enumObject.getMsg();
        }
    }
}
