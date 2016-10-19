package com.netfinworks.site.domain.domain.member;

import java.io.Serializable;
import java.util.Comparator;
import java.util.TreeSet;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class TreeNode implements Comparable<TreeNode>, Serializable {

    private static final long serialVersionUID = -8540409314246279253L;

    private String            id;
    private String            name;
    private Boolean           open             = true;
    private Boolean           checked;
    private int               sort;
    private String            pid;
    private TreeSet<TreeNode> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public TreeSet<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(TreeSet<TreeNode> children) {
        this.children = children;
    }

    public TreeNode() {

    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public TreeNode(String id, String name, Boolean open, Boolean checked, int sort, String pid,
                    TreeSet<TreeNode> children) {
        super();
        this.id = id;
        this.name = name;
        this.open = open;
        this.checked = checked;
        this.sort = sort;
        this.pid = pid;
        this.children = children;
    }

    @Override
    public int compareTo(TreeNode treeNode) {
        if(this.getSort()-treeNode.getSort()>0){
            return 1;
        }else{
            return -1;
        }
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
    

}
