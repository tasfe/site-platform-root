package com.netfinworks.site.web.util;

import java.util.List;
import java.util.TreeSet;

import com.meidusa.fastjson.JSONArray;
import com.netfinworks.site.domain.domain.member.TreeNode;

public class TreeUtil {
    
    /**
     * 将节点列表转换成有序树结构 
     * @param treeNodeList
     * @return
     */
    public static TreeSet<TreeNode> convertTreeSet(List<TreeNode> treeNodeList){
        TreeSet<TreeNode> treeNodeTreeSet = new TreeSet<TreeNode>(); 
        for(TreeNode treeNode1 : treeNodeList){  
            boolean mark = false;  
            for(TreeNode treeNode2 : treeNodeList){  
                if(treeNode1.getPid()!=null && treeNode1.getPid().equals(treeNode2.getId())){  
                    mark = true;  
                    if(treeNode2.getChildren() == null)  {
                        treeNode2.setChildren(new TreeSet<TreeNode>());  
                    }
                    treeNode2.getChildren().add(treeNode1);   
                    break;  
                }  
            }  
            if(!mark){  
                treeNodeTreeSet.add(treeNode1);   
            }  
        }  
        return treeNodeTreeSet;
    }
    
    public static String convertTreeJson(List<TreeNode> treeNodeList){
        TreeSet<TreeNode> treeNodeTreeSet =convertTreeSet(treeNodeList);
        return JSONArray.toJSONString(treeNodeTreeSet);  
    }
   
}
