package com.netfinworks.site.domainservice.repository;

import java.util.List;

import com.netfinworks.site.domain.domain.info.RoleResourceRelation;


// TODO 角色关系维护
public interface RoleResourceRepository {

	List<RoleResourceRelation> loadAllResourceRelation();
}
