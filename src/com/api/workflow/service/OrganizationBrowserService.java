package com.api.workflow.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import weaver.general.Util;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.User;
import weaver.hrm.appdetach.AppDetachComInfo;
import weaver.hrm.company.CompanyComInfo;
import weaver.hrm.company.DepartmentComInfo;
import weaver.hrm.company.SubCompanyComInfo;
import weaver.hrm.companyvirtual.CompanyVirtualComInfo;
import weaver.hrm.companyvirtual.DepartmentVirtualComInfo;
import weaver.hrm.companyvirtual.SubCompanyVirtualComInfo;

public class OrganizationBrowserService {

	private AppDetachComInfo adci = null;

	/**
	 * 获取部门树
	 * 
	 * 
	 * 需要确定是否加载下级部门 默认全部加载
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public Map<String,Object> getCompanyTree(HttpServletRequest request, HttpServletResponse response) {
		Map<String,Object> apidatas = new HashMap<String,Object>();
		User user = HrmUserVarify.getUser(request, response);
		// 虚拟组织
		String virtualCompanyid = Util.null2String(request.getParameter("virtualCompanyid"));
		// 是否加载部门
		boolean isLoadSubDepartment = "1".equals(Util.null2String(request.getParameter("isLoadSubDepartment")));
		// 是否加载所有下级数据
		boolean isLoadAllSub = "1".equals(Util.null2String(request.getParameter("isLoadAllSub")));

		CompanyComInfo companyComInfo = null;
		CompanyVirtualComInfo companyVirtualComInfo = null;
		// 加载顶级分部一级分部
		try {
			companyComInfo = new CompanyComInfo();
			companyVirtualComInfo = new CompanyVirtualComInfo();
			companyVirtualComInfo.setUser(user);
		} catch (Exception e) {
		}

		List<OrgBean> companys = new ArrayList<OrgBean>();

		// 加载虚拟组织列表
		if ("".equals(virtualCompanyid) && companyVirtualComInfo.getCompanyNum() > 0) {
			OrgBean companyInfo = null;
			if (companyComInfo.getCompanyNum() > 0) {
				companyComInfo.setTofirstRow();
				while (companyComInfo.next()) {
					companyInfo = new OrgBean();
					companyInfo.setCompanyid(companyComInfo.getCompanyid());
					companyInfo.setName(companyComInfo.getCompanyname());
					companyInfo.setIsVirtual("0");
					companys.add(companyInfo);
				}
			}

			companyVirtualComInfo.setTofirstRow();
			while (companyVirtualComInfo.next()) {
				companyInfo = new OrgBean();
				companyInfo.setCompanyid(companyVirtualComInfo.getCompanyid());
				companyInfo.setName(companyVirtualComInfo.getVirtualType());
				companyInfo.setIsVirtual("1");
				companys.add(companyInfo);
			}
		}

		Map<String, Object> resultDatas = new HashMap<String, Object>();
		resultDatas.put("companys", companys);

		String companyname = companyComInfo.getCompanyname("1");
		OrgBean root = new OrgBean();
		if ("".equals(virtualCompanyid)) {
			root.setId("0");
			root.setCompanyid("1");
			root.setName(companyname);
			root.setType("0");
			root.setIsVirtual("0");

			// 加载下级分部
			loadSubCompanys(root, isLoadSubDepartment, isLoadAllSub, user);
		} else {
			// 虚拟组织
			root.setId("0");
			root.setCompanyid(virtualCompanyid);
			root.setName(companyname);
			root.setType("0");
			root.setIsVirtual("1");
			loadVirtualSubCompanyInfo(root, isLoadSubDepartment, isLoadAllSub, user);
		}
		resultDatas.put("rootCompany", root);
		apidatas.put("result", resultDatas);
		return apidatas;
	}

	/**
	 * 加载下级分部
	 * 
	 * @param pcompany
	 *            上级分部
	 * @param isLoadSubDepartment
	 *            是否加载下级部门
	 * @param isLoadAllSub
	 *            是否加载所有的下级(部门、分部)
	 * @param user
	 */
	private void loadSubCompanys(OrgBean parentOrg, boolean isLoadSubDepartment, boolean isLoadAllSub, User user) {
		SubCompanyComInfo rs = null;
		try{
			rs = new SubCompanyComInfo();
		}catch(Exception e){
			e.printStackTrace();
		}
		rs.setTofirstRow();

		if (isLoadSubDepartment) {
			// loadsubDepartments
			loadSubDepartments(parentOrg, null, isLoadAllSub);
		}

		List<OrgBean> subOrgs = null;
		if (parentOrg.getSubOrgs() != null) {
			subOrgs = parentOrg.getSubOrgs();
		} else {
			subOrgs = new ArrayList<OrgBean>();
		}

		while (rs.next()) {
			String supsubcomid = rs.getSupsubcomid();
			if ("1".equals(rs.getCompanyiscanceled())){
				continue;
			}
			if (supsubcomid.equals(""))
				supsubcomid = "0";
			if (!supsubcomid.equals(parentOrg.getId()))
				continue;

			String id = rs.getSubCompanyid();
			String name = rs.getSubCompanyname();

			// 检查应用分权
			if (adci == null)
				adci = new AppDetachComInfo(user);
			if (adci.isUseAppDetach()) {
				if (adci.checkUserAppDetach(id, "2") == 0)
					continue;
			}

			OrgBean orgBean = new OrgBean();

			orgBean.setId(id);
			orgBean.setName(name);
			orgBean.setPid(parentOrg.getId());
			orgBean.setType("1");
			orgBean.setIsVirtual("0");
			subOrgs.add(orgBean);

			parentOrg.setIsParent("1");

			// 加载下级分部
			if (isLoadAllSub) {
				loadSubCompanys(orgBean, isLoadSubDepartment, isLoadAllSub, user);
			} else {
				// 更新当前节点isParent状态
				validOrgIsParent(orgBean, isLoadSubDepartment, user);
			}
		}
		parentOrg.setSubOrgs(subOrgs);
	}

	/**
	 * 不加载下级数据时，判断当前节点是否有下级分部
	 * 
	 * @param parentOrg
	 * @param isLoadSubDepartment
	 * @param user
	 */
	private void validOrgIsParent(OrgBean parentOrg, boolean isLoadSubDepartment, User user) {
		SubCompanyComInfo rs = null;
		try{
			rs = new SubCompanyComInfo();
		}catch(Exception e){
			e.printStackTrace();
		}
		rs.setTofirstRow();

		if (isLoadSubDepartment) {
			validOrgIsParent(parentOrg, null);
		}

		while (rs.next()) {
			String supsubcomid = rs.getSupsubcomid();
			if (supsubcomid.equals(""))
				supsubcomid = "0";
			if (!supsubcomid.equals(parentOrg.getId()))
				continue;

			String id = rs.getSubCompanyid();

			// 检查应用分权
			if (adci == null)
				adci = new AppDetachComInfo(user);
			if (adci.isUseAppDetach()) {
				if (adci.checkUserAppDetach(id, "2") == 0)
					continue;
			}

			parentOrg.setIsParent("1");
			break;
		}
	}

	/**
	 * 不加载下级数据时,且开启加载部门，判断当前节点是否有下级部门
	 * 
	 * @param pSubCompany
	 * @param pDepartment
	 */
	private void validOrgIsParent(OrgBean pSubCompany, OrgBean pDepartment) {
		DepartmentComInfo rsDepartment = null;
		try{
			rsDepartment = new DepartmentComInfo();
		}catch(Exception e){
			e.printStackTrace();
		}
		rsDepartment.setTofirstRow();
		String pdepartmentId = pDepartment == null ? "0" : pDepartment.getId();
		String subcompanyid = pSubCompany.getId();

		while (rsDepartment.next()) {
			String supdepid = rsDepartment.getDepartmentsupdepid();
			if (pdepartmentId.equals("0") && supdepid.equals("")) {
				supdepid = "0";
			}
			if (!(rsDepartment.getSubcompanyid1().equals(subcompanyid) && (supdepid.equals(pdepartmentId) || (!rsDepartment.getSubcompanyid1(supdepid).equals(subcompanyid) && pdepartmentId
					.equals("0")))))
				continue;

			pSubCompany.setIsParent("1");
			if (pDepartment != null) {
				pDepartment.setIsParent("1");
			}

			break;
		}
	}

	/**
	 * 加载下级部门
	 * 
	 * @param pcompany
	 * @param pdepartment
	 * @param isLoadAllSub
	 */
	private void loadSubDepartments(OrgBean pSubCompany, OrgBean pDepartment, boolean isLoadAllSub) {
		DepartmentComInfo rsDepartment = null;
		try{
			rsDepartment = new DepartmentComInfo();
		}catch(Exception e){
			e.printStackTrace();
		}
		String pdepartmentId = pDepartment == null ? "0" : pDepartment.getId();
		String subcompanyid = Util.null2String(pSubCompany.getId());
		if("".equals(subcompanyid)){
			subcompanyid = rsDepartment.getSubcompanyid1(pdepartmentId);
		}
		String pId = pDepartment == null ? pSubCompany.getId() : pDepartment.getId();

		List<OrgBean> subOrgs = null;
		if (pDepartment == null && pSubCompany.getSubOrgs() != null) {
			subOrgs = pSubCompany.getSubOrgs();
		} else {
			subOrgs = new ArrayList<OrgBean>();
		}
		rsDepartment.setTofirstRow();
		while (rsDepartment.next()) {
			String supdepid = rsDepartment.getDepartmentsupdepid();
			if("1".equals(rsDepartment.getDeparmentcanceled())){
				continue;
			}
			if (pdepartmentId.equals("0") && supdepid.equals("")) {
				supdepid = "0";
			}
			if (!(rsDepartment.getSubcompanyid1().equals(subcompanyid) && (supdepid.equals(pdepartmentId) || (!rsDepartment.getSubcompanyid1(supdepid).equals(subcompanyid) && pdepartmentId
					.equals("0")))))
				continue;

			String id = rsDepartment.getDepartmentid();
			String name = rsDepartment.getDepartmentname();

			OrgBean orgBean = new OrgBean();
			orgBean.setId(id);
			orgBean.setPid(pId);
			orgBean.setName(name);
			orgBean.setType("2");
			orgBean.setIsVirtual("0");
			orgBean.setPsubcompanyid(pSubCompany.getId());

			pSubCompany.setIsParent("1");
			if (pDepartment != null) {
				pDepartment.setIsParent("1");
			}

			subOrgs.add(orgBean);

			if (isLoadAllSub) {
				loadSubDepartments(pSubCompany, orgBean, isLoadAllSub);
			} else {
				validOrgIsParent(pSubCompany, orgBean);
			}
		}

		if (pDepartment == null) {
			pSubCompany.setSubOrgs(subOrgs);
		} else {
			pDepartment.setSubOrgs(subOrgs);
		}
	}

	/**
	 * 加载虚拟分部
	 * 
	 * @param pcompany
	 * @param isLoadSubDepartment
	 * @param isLoadAllSub
	 * @param user
	 */
	private void loadVirtualSubCompanyInfo(OrgBean parentOrg, boolean isLoadSubDepartment, boolean isLoadAllSub, User user) {
		SubCompanyVirtualComInfo rs = null;
		try{
			rs = new SubCompanyVirtualComInfo();
			if("".equals(Util.null2String(parentOrg.getCompanyid()))){
				parentOrg.setCompanyid(rs.getCompanyid(parentOrg.getId()));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		rs.setTofirstRow();

		// 加载虚拟部门
		if (isLoadSubDepartment) {
			loadVirtualSubDepartments(parentOrg, null, isLoadAllSub);
		}
		
		List<OrgBean> subOrgs = null;
		if (parentOrg.getSubOrgs() != null) {
			subOrgs = parentOrg.getSubOrgs();
		} else {
			subOrgs = new ArrayList<OrgBean>();
		}
		
		while (rs.next()) {
			String id = rs.getSubCompanyid();
			String supsubcomid = rs.getSupsubcomid();
			String comid = rs.getCompanyid();
			
			if("1".equals(rs.getCompanyiscanceled())){
				continue;
			}
			if (!comid.equals(parentOrg.getCompanyid()))
				continue;
			if (supsubcomid.equals(""))
				supsubcomid = "0";
			if (!supsubcomid.equals(parentOrg.getId()))
				continue;

			String name = rs.getSubCompanyname();
			// 检查应用分权
			if (adci == null)
				adci = new AppDetachComInfo(user);
			if (adci.isUseAppDetach()) {
				System.out.println(id);
				int flag = adci.checkUserAppDetach(id, "2");
				if (flag == 0) {
					continue;
				}
			}

			OrgBean virtualOrgBean = new OrgBean();

			virtualOrgBean.setId(id);
			virtualOrgBean.setPid(parentOrg.getId());
			virtualOrgBean.setName(name);
			virtualOrgBean.setCompanyid(parentOrg.getCompanyid());
			virtualOrgBean.setType("1");
			virtualOrgBean.setIsVirtual("1");

			parentOrg.setIsParent("1");

			subOrgs.add(virtualOrgBean);

			if (isLoadAllSub) {
				loadVirtualSubCompanyInfo(virtualOrgBean, isLoadSubDepartment, isLoadAllSub, user);
			} else {
				validVirtualOrgIsParent(virtualOrgBean, isLoadSubDepartment, user);
			}
		}
		parentOrg.setSubOrgs(subOrgs);
	}

	/**
	 * 加载虚拟部门
	 * 
	 * @param pcompany
	 * @param pdepartment
	 * @param isLoadAllSub
	 * @param user
	 */
	private void loadVirtualSubDepartments(OrgBean pVCompany, OrgBean pVDepartment, boolean isLoadAllSub) {
		DepartmentVirtualComInfo rsDepartment = null;
		try{
			rsDepartment = new DepartmentVirtualComInfo();
		}catch(Exception e){
			e.printStackTrace();
		}

		String departmentId = pVDepartment == null ? "0" : pVDepartment.getId();
		String subcompanyid = Util.null2String(pVCompany.getId());
		if("".equals(subcompanyid)){
			subcompanyid = rsDepartment.getSubcompanyid1(departmentId);
		}

		String pId = pVDepartment == null ? pVCompany.getId() : pVDepartment.getId();
		List<OrgBean> subOrgs = null;
		if (pVDepartment == null && pVCompany.getSubOrgs() != null) {
			subOrgs = pVCompany.getSubOrgs();
		} else {
			subOrgs = new ArrayList<OrgBean>();
		}
		rsDepartment.setTofirstRow();
		while (rsDepartment.next()) {
			if("1".equals(rsDepartment.getDeparmentcanceled())){
				continue;
			}
			if (departmentId.equals(rsDepartment.getDepartmentid()))
				continue;
			String supdepid = rsDepartment.getDepartmentsupdepid();
			if (departmentId.equals("0") && supdepid.equals(""))
				supdepid = "0";
			if (!(rsDepartment.getSubcompanyid1().equals(subcompanyid) && (supdepid.equals(departmentId) || (!rsDepartment.getSubcompanyid1(supdepid).equals(subcompanyid) && departmentId.equals("0")))))
				continue;

			String id = rsDepartment.getDepartmentid();
			String name = rsDepartment.getDepartmentname();

			OrgBean virtualOrgBean = new OrgBean();
			virtualOrgBean.setId(id);
			virtualOrgBean.setPid(pId);
			virtualOrgBean.setName(name);
			virtualOrgBean.setType("2");
			virtualOrgBean.setIsVirtual("1");
			virtualOrgBean.setPsubcompanyid(pVCompany.getId());

			pVCompany.setIsParent("1");
			if (pVDepartment != null) {
				pVDepartment.setIsParent("1");
			}

			subOrgs.add(virtualOrgBean);

			if (isLoadAllSub) {
				loadVirtualSubDepartments(pVCompany, virtualOrgBean, isLoadAllSub);
			} else {
				validVirtualOrgIsParent(pVCompany, virtualOrgBean);
			}
		}

		if (pVDepartment == null) {
			pVCompany.setSubOrgs(subOrgs);
		} else {
			pVDepartment.setSubOrgs(subOrgs);
		}
	}

	/**
	 * 
	 * @param parentOrg
	 * @param isLoadSubDepartment
	 * @param user
	 */
	private void validVirtualOrgIsParent(OrgBean parentOrg, boolean isLoadSubDepartment, User user) {
		SubCompanyVirtualComInfo rs = null;
		try{
			rs = new SubCompanyVirtualComInfo();
			if("".equals(Util.null2String(parentOrg.getCompanyid()))){
				parentOrg.setCompanyid(rs.getCompanyid(parentOrg.getId()));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		rs.setTofirstRow();

		// 加载虚拟部门
		if (isLoadSubDepartment) {
			validVirtualOrgIsParent(parentOrg, null);
		}
		while (rs.next()) {
			String id = rs.getSubCompanyid();
			String supsubcomid = rs.getSupsubcomid();
			String comid = rs.getCompanyid();

			if (!comid.equals(parentOrg.getCompanyid()))
				continue;
			if (supsubcomid.equals(""))
				supsubcomid = "0";
			if (!supsubcomid.equals(parentOrg.getId()))
				continue;

			// 检查应用分权
			if (adci == null)
				adci = new AppDetachComInfo(user);
			if (adci.isUseAppDetach()) {
				int flag = adci.checkUserAppDetach(id, "2");
				if (flag == 0) {
					continue;
				}
			}

			parentOrg.setIsParent("1");
			break;
		}
	}

	/**
	 * 
	 * @param pVCompany
	 * @param pVDepartment
	 */
	public void validVirtualOrgIsParent(OrgBean pVCompany, OrgBean pVDepartment) {
		DepartmentVirtualComInfo rsDepartment = null;
		try{
			rsDepartment = new DepartmentVirtualComInfo();
		}catch(Exception e){
			e.printStackTrace();
		}
		rsDepartment.setTofirstRow();

		String departmentId = pVDepartment == null ? "0" : pVDepartment.getId();
		String subcompanyid = pVCompany.getId();

		while (rsDepartment.next()) {
			if (departmentId.equals(rsDepartment.getDepartmentid()))
				continue;
			String supdepid = rsDepartment.getDepartmentsupdepid();
			if (departmentId.equals("0") && supdepid.equals(""))
				supdepid = "0";
			if (!(rsDepartment.getSubcompanyid1().equals(subcompanyid) && (supdepid.equals(departmentId) || (!rsDepartment.getSubcompanyid1(supdepid).equals(subcompanyid) && departmentId.equals("0")))))
				continue;

			pVCompany.setIsParent("1");
			if (pVDepartment != null) {
				pVDepartment.setIsParent("1");
			}

			break;
		}

	}

	/**
	 * 获取下级机构
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public Map<String,Object> getSubOrgList(HttpServletRequest request, HttpServletResponse response) {
		Map<String,Object> apidatas = new HashMap<String,Object>();

		String type = Util.null2String(request.getParameter("type"));
		String id = Util.null2String(request.getParameter("id"));
		String psubcompanyid = Util.null2String(request.getParameter("psubcompanyid"));
		String isVirtual = Util.null2String(request.getParameter("isVirtual"));
		// 是否加载部门
		boolean isLoadSubDepartment = "1".equals(Util.null2String(request.getParameter("isLoadSubDepartment")));

		User user = HrmUserVarify.getUser(request, response);

		OrgBean psubOrgBean = new OrgBean();
		List<OrgBean> result = null;
		if ("0".equals(type)) {
			String companyid = Util.null2String(request.getParameter("companyid"));
			psubOrgBean.setId(id);
			psubOrgBean.setCompanyid(companyid);

			if ("0".equals(isVirtual)) {
				loadSubCompanys(psubOrgBean, isLoadSubDepartment, false, user);
			} else {
				loadVirtualSubCompanyInfo(psubOrgBean, isLoadSubDepartment, false, user);
			}
			result = psubOrgBean.getSubOrgs();
		} else if ("1".equals(type)) {
			psubOrgBean.setId(id);
			psubOrgBean.setType(type);
			psubOrgBean.setIsVirtual(isVirtual);
			if ("0".equals(isVirtual)) {
				loadSubCompanys(psubOrgBean, isLoadSubDepartment, false, user);
			} else {
				loadVirtualSubCompanyInfo(psubOrgBean, isLoadSubDepartment, false, user);
			}
			result = psubOrgBean.getSubOrgs();
		} else if ("2".equals(type)) {
			psubOrgBean.setId(psubcompanyid);

			OrgBean pdepOrgBean = new OrgBean();
			pdepOrgBean.setId(id);
			pdepOrgBean.setIsVirtual(isVirtual);
			if ("0".equals(isVirtual)) {
				loadSubDepartments(psubOrgBean, pdepOrgBean, false);
			} else {
				loadVirtualSubDepartments(psubOrgBean, pdepOrgBean, false);
			}
			result = pdepOrgBean.getSubOrgs();
		}

		apidatas.put("result", result);
		return apidatas;
	}

	private class OrgBean {
		private String id;
		private String isParent;
		private String name;
		private String pid;
		private String type; // 0 公司 1 分部 2 部门
		private String companyid;
		private String isVirtual;
		private String psubcompanyid;

		private List<OrgBean> subOrgs;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getIsParent() {
			return isParent;
		}

		public void setIsParent(String isParent) {
			this.isParent = isParent;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPid() {
			return pid;
		}

		public void setPid(String pid) {
			this.pid = pid;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getCompanyid() {
			return companyid;
		}

		public void setCompanyid(String companyid) {
			this.companyid = companyid;
		}

		public List<OrgBean> getSubOrgs() {
			return subOrgs;
		}

		public void setSubOrgs(List<OrgBean> subOrgs) {
			this.subOrgs = subOrgs;
		}

		public String getIsVirtual() {
			return isVirtual;
		}

		public void setIsVirtual(String isVirtual) {
			this.isVirtual = isVirtual;
		}

		public String getPsubcompanyid() {
			return psubcompanyid;
		}

		public void setPsubcompanyid(String psubcompanyid) {
			this.psubcompanyid = psubcompanyid;
		}
	}
}
