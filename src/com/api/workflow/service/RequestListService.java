package com.api.workflow.service;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.cloudstore.dev.api.util.Util_TableMap;

import weaver.conn.RecordSet;
import weaver.general.PageIdConst;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.hrm.company.DepartmentComInfo;
import weaver.search.SearchClause;
import weaver.systeminfo.SystemEnv;
import com.api.workflow.bean.RequestListParam;
import com.api.workflow.util.PageUidFactory;
//import weaver.workflow.request.todo.OfsSettingObject;
//import weaver.workflow.request.todo.RequestUtil;
import weaver.workflow.search.WfAdvanceSearchUtil;
import weaver.workflow.search.WorkflowSearchCustom;
import weaver.workflow.workflow.WorkflowComInfo;
import weaver.workflow.workflow.WorkflowVersion;

/**
 * 流程待办/已办/我的请求分页列表数据
 * @author liuzy 2016/12/06
 */
public class RequestListService {

	public Map<String,Object> searchResult(RequestListParam parambean, HttpServletRequest request) throws Exception {
		Map<String,Object> apidatas = new HashMap<String,Object>();
		User user = parambean.getUser();
		SearchClause SearchClause = parambean.getSearchClause();
		Map<String,String> reqparams = parambean.getReqparams();
		if(user == null || SearchClause == null){
			apidatas.put("api_status", false);
			apidatas.put("api_errormsg", "params error");
			return apidatas;
		}
		//字符串型参数统一封装到reqparams，以reqparams内容为第一优先级，将request请求参数添加到reqparams中，满足各中间页面处理request参数
		Enumeration em = request.getParameterNames();
		while(em.hasMoreElements()){
			String paramName = (String) em.nextElement();
			if (!reqparams.containsKey(paramName)){
				reqparams.put(paramName, request.getParameter(paramName));
			}
		}
		
		RecordSet RecordSet = new RecordSet();
		WorkflowComInfo WorkflowComInfo = new WorkflowComInfo();
		DepartmentComInfo DepartmentComInfo = new DepartmentComInfo();
		WorkflowSearchCustom WorkflowSearchCustom = new WorkflowSearchCustom();
		//RequestUtil requestutil = new RequestUtil();
		HttpSession session = request.getSession();

		String workflowid = "";

		//OfsSettingObject ofso = requestutil.getOfsSetting();
		//boolean isopenos = ofso.getIsuse() == 1;// 是否开启异构系统待办
		boolean isopenos = false;
		
		//流程名称反射方法(兼容E8)
		String workflownamereflectmethod = "weaver.workflow.workflow.WorkflowComInfo.getWorkflowname";
		if(isopenos)
			workflownamereflectmethod = "weaver.general.WorkFlowTransMethod.getWorkflowname";
	
		//是否开启SPA单页模式
		String requestnamereflectclass = "weaver.general.WorkFlowTransMethod";
		//String isSPA = Util.null2String(reqparams.get("isSPA"));
		//if("1".equals(isSPA))
			requestnamereflectclass = "com.api.workflow.util.WorkFlowSPATransMethod";

		String offical = Util.null2String(reqparams.get("offical"));
		int officalType = Util.getIntValue(reqparams.get("officalType"), -1);
		int processId = Util.getIntValue(reqparams.get("processId"), 0);
		int sysId = Util.getIntValue(reqparams.get("sysId"), 0);
		String overtimetype = Util.null2String(reqparams.get("overtimetype"));
		String myrequest = Util.null2String(reqparams.get("myrequest"));

		String branchid = "";
		String cdepartmentid = "";
		String isFromMessage = Util.null2String(reqparams.get("isFromMessage"));
		String processing = Util.null2String(reqparams.get("processing"));
		String scope = Util.null2String(reqparams.get("viewScope"));
		String workflowtype = "";
		String wfstatu = "";
		String nodetype = "";
		String fromdate = "";
		String todate = "";
		String creatertype = "";
		String createrid = "";
		String createrid2 = Util.null2String(reqparams.get("createrid2"));
		String requestlevel = "";
		String fromdate2 = "";
		String todate2 = "";
		String querys = Util.null2String(reqparams.get("query"));
		String fromself = Util.null2String(reqparams.get("fromself"));
		String fromselfSql = "", fromselfSqlos = "";
		//String fromselfSql = Util.null2String(reqparams.get("fromselfSql"));
		//String fromselfSqlos = Util.null2String(reqparams.get("fromselfSqlos"));
		String docids = Util.null2String(reqparams.get("docids"));
		String flag = Util.null2String(reqparams.get("flag"));
		int date2during = Util.getIntValue(Util.null2String(reqparams.get("date2during")), 0);
		String timecondition = Util.null2String(reqparams.get("timecondition"));

		try {
			branchid = Util.null2String((String) session.getAttribute("branchid"));
		} catch (Exception e) {
			branchid = "";
		}
		
		String orderby = SearchClause.getOrderClause();
		String orderbyos = SearchClause.getOrderclauseOs();
		if (orderby.equals(""))
			orderby = "t2.receivedate ,t2.receivetime";
		if (orderbyos.equals(""))
			orderbyos = "receivedate ,receivetime";
		
		int iswaitdo = Util.getIntValue(reqparams.get("iswaitdo"), 0);
		int isovertime = Util.getIntValue(reqparams.get("isovertime"), 0);
		if (fromself.equals("1")) {
			//待办、已办、我的请求及高级搜索都fromself传1
			fromselfSql = SearchClause.getWhereClause();
			fromselfSqlos = SearchClause.getWhereclauseOs();
			SearchClause.resetClause();
			
			workflowtype = Util.null2String(reqparams.get("workflowtype"));
			wfstatu = Util.null2String(reqparams.get("wfstatu"));
			workflowid = Util.null2String(reqparams.get("workflowid"));
			nodetype = Util.null2String(reqparams.get("nodetype"));
			fromdate = Util.null2String(reqparams.get("fromdate"));
			todate = Util.null2String(reqparams.get("todate"));
			creatertype = Util.null2String(reqparams.get("creatertype"));
			createrid = Util.null2String(reqparams.get("createrid"));
			requestlevel = Util.null2String(reqparams.get("requestlevel"));
			fromdate2 = Util.null2String(reqparams.get("fromdate2"));
			todate2 = Util.null2String(reqparams.get("todate2"));
			cdepartmentid = Util.null2String(reqparams.get("cdepartmentid"));
		} else {
			workflowid = SearchClause.getWorkflowId();
			nodetype = SearchClause.getNodeType();
			fromdate = SearchClause.getFromDate();
			todate = SearchClause.getToDate();
			creatertype = SearchClause.getCreaterType();
			createrid = SearchClause.getCreaterId();
			requestlevel = SearchClause.getRequestLevel();
			fromdate2 = SearchClause.getFromDate2();
			todate2 = SearchClause.getToDate2();
			cdepartmentid = SearchClause.getDepartmentid();
		}

		/** 自定义查询条件* */
		String customid = Util.null2String(reqparams.get("customname"));// 查询条件id
		String customSearch = "";
		String customResult = "";
		if (!customid.equals("")) {
			// 添加了查询条件
			customSearch = WorkflowSearchCustom.getSearchCustomStr(RecordSet, customid, request);
			customResult = WorkflowSearchCustom.getResultCustomStr(RecordSet, customid, request);
		}

		String cdepartmentidspan = "";
		ArrayList cdepartmentidArr = Util.TokenizerString(cdepartmentid, ",");
		for (int i = 0; i < cdepartmentidArr.size(); i++) {
			String tempcdepartmentid = (String) cdepartmentidArr.get(i);
			if (cdepartmentidspan.equals(""))
				cdepartmentidspan += DepartmentComInfo.getDepartmentname(tempcdepartmentid);
			else
				cdepartmentidspan += "," + DepartmentComInfo.getDepartmentname(tempcdepartmentid);
		}

		String newsql = "";
		String newsqlos = "";
		if (!workflowid.equals("") && !workflowid.equals("0")) {
			if (workflowid.indexOf("-") != -1) {
				newsql += " and t1.workflowid in (" + workflowid + ") ";
			} else {
				newsql += " and t1.workflowid in (" + WorkflowVersion.getAllVersionStringByWFIDs(workflowid) + ") ";
			}
			newsqlos += " and workflowid=" + workflowid;
		}
		if (!timecondition.equals("")) {
			if ("1".equals(timecondition)) {
				newsql += " and t1.createdate>='" + TimeUtil.getToday() + "'";
				newsqlos += " and createdate>='" + TimeUtil.getToday() + "'";
			} else if ("2".equals(timecondition)) {
				newsql += " and t1.createdate>='" + TimeUtil.getFirstDayOfWeek() + "'";
				newsqlos += " and createdate>='" + TimeUtil.getFirstDayOfWeek() + "'";
			} else if ("3".equals(timecondition)) {
				newsql += " and t1.createdate>='" + TimeUtil.getFirstDayOfMonth() + "'";
				newsqlos += " and createdate>='" + TimeUtil.getFirstDayOfMonth() + "'";
			} else if ("4".equals(timecondition)) {
				newsql += " and t1.createdate>='" + TimeUtil.getFirstDayOfSeason() + "'";
				newsqlos += " and createdate>='" + TimeUtil.getFirstDayOfSeason() + "'";
			} else if ("5".equals(timecondition)) {
				newsql += " and t1.createdate>='" + TimeUtil.getFirstDayOfTheYear() + "'";
				newsqlos += " and createdate>='" + TimeUtil.getFirstDayOfTheYear() + "'";
			}
		}
		if (date2during > 0 && date2during < 37) {
			newsql += WorkflowComInfo.getDateDuringSql(date2during);
			newsqlos += WorkflowComInfo.getDateDuringSql(date2during);
		}
		if (fromself.equals("1")) {
			if (!nodetype.equals("")) {
				newsql += " and t1.currentnodetype='" + nodetype + "'";
				newsqlos += " and 1=2 ";
			}
			if (!fromdate.equals("")) {
				newsql += " and t1.createdate>='" + fromdate + "'";
				newsqlos += " and createdate>='" + fromdate + "'";
			}
			if (!todate.equals("")) {
				newsql += " and t1.createdate<='" + todate + "'";
				newsqlos += " and createdate<='" + todate + "'";
			}
			if (!fromdate2.equals("")) {
				newsql += " and t2.receivedate>='" + fromdate2 + "'";
				newsqlos += " and receivedate>='" + fromdate2 + "'";
			}
			if (!todate2.equals("")) {
				newsql += " and t2.receivedate<='" + todate2 + "'";
				newsqlos += " and receivedate<='" + todate2 + "'";
			}
			if (!cdepartmentid.equals("")) {
				String tempWhere = "";
				ArrayList tempArr = Util.TokenizerString(cdepartmentid, ",");
				for (int i = 0; i < tempArr.size(); i++) {
					String tempcdepartmentid = (String) tempArr.get(i);
					if (tempWhere.equals(""))
						tempWhere += "departmentid=" + tempcdepartmentid;
					else
						tempWhere += " or departmentid=" + tempcdepartmentid;
				}
				if (!tempWhere.equals("")) {
					newsql += " and exists(select 1 from hrmresource where t1.creater=id and t1.creatertype='0' and (" + tempWhere + "))";
					newsqlos += " and exists(select 1 from hrmresource where creatorid=id and (" + tempWhere + "))";
				}
			}

			if (!requestlevel.equals("")) {
				newsql += " and t1.requestlevel=" + requestlevel;
				newsqlos += " and 1=2 ";
			}

			if (!querys.equals("1")) {
				if (!fromselfSql.equals("") && !fromselfSql.equalsIgnoreCase("null")) {
					newsql += " and " + fromselfSql;
				}
				if (!fromselfSqlos.equals("") && !fromselfSqlos.equalsIgnoreCase("null")) {
					newsqlos += " " + fromselfSqlos;
				}
			} else {
				if (fromself.equals("1")) {
					newsql += " and  islasttimes=1 ";
					newsqlos += " and  islasttimes=1 ";
				}
			}
		}

		String resourceid = Util.null2String(reqparams.get("resourceid"));
		String CurrentUser = resourceid;

		String userID = String.valueOf(user.getUID());
		int userid = user.getUID();
		String belongtoshow = "";
		RecordSet.executeSql("select * from HrmUserSetting where resourceId = " + userID);
		if (RecordSet.next()) {
			belongtoshow = RecordSet.getString("belongtoshow");
		}
		// QC235172,如果不是查看自己的代办，主从账号统一显示不需要判断
		if (!"".equals(resourceid) && !("" + userid).equals(resourceid))
			belongtoshow = "";

		String userIDAll = String.valueOf(user.getUID());
		String Belongtoids = user.getBelongtoids();
		if (!"".equals(Belongtoids))
			userIDAll = userID + "," + Belongtoids;

		String logintype = "" + user.getLogintype();
		int usertype = 0;
		boolean superior = false; // 是否为被查看者上级或者本身
		if (logintype.equals("2"))
			usertype = 1;
		if (CurrentUser.equals(""))
			CurrentUser = "" + user.getUID();
		if (userID.equals(CurrentUser)) {
			superior = true;
		} else {
			RecordSet.executeSql("SELECT * FROM HrmResource WHERE ID = " + CurrentUser + " AND managerStr LIKE '%," + userID + ",%'");
			if (RecordSet.next())
				superior = true;
		}

		String sqlwhere = "";
		String sqlwhereos = " where 1=1 ";
		String user_sqlstr = "1".equals(belongtoshow) ? userIDAll : user.getUID()+"";
		if (isovertime == 1) {
			if ("0".equals(overtimetype)) {
				sqlwhere = "where t1.requestid = t2.requestid " + " and t2.userid in (" + user_sqlstr
					+ ") and t2.usertype='"+ (Util.getIntValue(logintype, 1) - 1) + "' and t2.islasttimes = 1 "
					+ " and exists (select 1 from workflow_currentoperator c where c.requestid = t2.requestid and c.isremark = '0' and c.isreminded = '1' and (c.isreminded_csh != '1' or c.isreminded_csh is null)) "
					+ " AND exists(select 1 from SysPoppupRemindInfonew z2  "
					+ " where  t1.requestid=z2.requestid and z2.type=10  " + " and z2.userid in (" + user_sqlstr
					+ ") and z2.usertype='" + (Util.getIntValue(logintype, 1) - 1) + "' )";
			} else {
				sqlwhere = "where t1.requestid = t2.requestid " + " and t2.userid in (" + user_sqlstr
					+ ") and t2.usertype='"+ (Util.getIntValue(logintype, 1) - 1) + "' and t2.islasttimes = 1 "
					+ " and exists (select 1 from workflow_currentoperator c where c.requestid = t2.requestid and c.isremark = '0' and c.isreminded_csh = '1') "
					+ " AND exists(select 1 from SysPoppupRemindInfonew z2  "
					+ " where  t1.requestid=z2.requestid and z2.type=10  " + " and z2.userid in ( " + user_sqlstr
					+ " )and z2.usertype='" + (Util.getIntValue(logintype, 1) - 1) + "' )";
			}
		} else {
			if (superior && !flag.equals("")) {
				CurrentUser = userID;
			}
			sqlwhere = "where  (t1.deleted <> 1 or t1.deleted is null or t1.deleted='') and t1.requestid = t2.requestid and t2.userid in ("
				+ user_sqlstr + " ) and t2.usertype=" + usertype;
			if (!Util.null2String(SearchClause.getWhereClause()).equals("")) {
				sqlwhere += " and " + SearchClause.getWhereClause();
			}
			if (!Util.null2String(SearchClause.getWhereclauseOs()).equals("")) {
				sqlwhereos += " " + SearchClause.getWhereclauseOs();
			}
		}
		if (RecordSet.getDBType().equals("oracle")) {
			sqlwhere += " and (nvl(t1.currentstatus,-1) = -1 or (nvl(t1.currentstatus,-1)=0 and t1.creater in ("+ user_sqlstr + "))) ";
		} else {
			sqlwhere += " and (isnull(t1.currentstatus,-1) = -1 or (isnull(t1.currentstatus,-1)=0 and t1.creater in ("+ user_sqlstr + "))) ";
		}

		if (sqlwhereos.equals("")) {
			sqlwhereos = " and userid=" + user.getUID() + " and islasttimes=1 and isremark=0 ";
		}

		// 高级搜索条件
		WfAdvanceSearchUtil conditionutil = new WfAdvanceSearchUtil(request, RecordSet);

		String conditions = "";
		String conditionsos = "";
		if (processing.equals("0")) {
			conditions = conditionutil.getAdVanceSearch4PendingCondition();
			//conditionsos = conditionutil.getAdVanceSearch4PendingConditionOs();
		} else {
			conditions = conditionutil.getAdVanceSearch4OtherCondition();
			//conditionsos = conditionutil.getAdVanceSearch4OtherConditionOs();
		}

		if (!conditions.equals("")) {
			creatertype = "0";
			newsql += conditions;
			newsqlos += conditionsos;
		}
		sqlwhere += " " + newsql;
		sqlwhereos += " " + newsqlos;

		boolean isMultiSubmit = false;		//允许批量提交
		if(iswaitdo == 1 && CurrentUser.equals(userID)){	//待办&&当前用户查看
			String strworkflowid = "";
			String fromhp = Util.null2String(reqparams.get("fromhp"));
			if (fromhp.equals("1")) {
				String eid = Util.null2String(reqparams.get("eid"));
				String tabid = Util.null2String(reqparams.get("tabid"));
				RecordSet.execute("select count(content) as count from workflowcentersettingdetail where type = 'flowid' and eid=" + eid + "and tabId = '" + tabid + "'");
				if (RecordSet.next()) {
					if (RecordSet.getInt("count") > 0) {
						strworkflowid = " in (select content from workflowcentersettingdetail where type = 'flowid' and eid=" + eid + "and tabId = '" + tabid + "' )";
					}
				}
			} else {
				if (!Util.null2String(SearchClause.getWhereClause()).equals("")) {
					String tempstr = SearchClause.getWhereClause();
					if (tempstr.indexOf("t1.workflowid") != -1) {
						int startIndex = tempstr.indexOf("t1.workflowid") + 13;
						if (tempstr.indexOf("and") != -1) {
							if (tempstr.indexOf("(t1.deleted=0") != -1) {
								int startIndex1 = tempstr.indexOf("and");
								int startIndex2 = tempstr.indexOf("and", startIndex1 + 1);
								strworkflowid = tempstr.substring(startIndex, startIndex2);
							} else {
								strworkflowid = tempstr.substring(startIndex, tempstr.indexOf("and"));
							}
							if (strworkflowid.indexOf("(") != -1 && strworkflowid.indexOf(")") == -1)
								strworkflowid += ")";
						} else
							strworkflowid = tempstr.substring(startIndex, tempstr.indexOf(")") + 1);
						if (strworkflowid.indexOf("(") != -1 && strworkflowid.indexOf(")") == -1)
							strworkflowid += ")";
					}
				} else {
					if (!workflowid.equals("")) {
						if (workflowid.indexOf("-") != -1) {
							strworkflowid = " in (" + workflowid + ")";
						} else {
							strworkflowid = " in (" + WorkflowVersion.getAllVersionStringByWFIDs(workflowid) + ")";
						}
					}
				}
			}
			if (strworkflowid.equals("")) {
				RecordSet.executeSql("select count(id) as mtcount from workflow_base where multiSubmit=1");
			} else {
				RecordSet.executeSql("select count(id) as mtcount from workflow_base where id " + strworkflowid+ " and multiSubmit=1");
			}
			if (RecordSet.next() && RecordSet.getInt("mtcount") > 0)
				isMultiSubmit = true;
		}

		boolean hasrequestname = false;
		boolean hascreater = false;
		boolean hascreatedate = false;
		boolean hasworkflowname = false;
		boolean hasrequestlevel = false;
		boolean hasreceivetime = false;
		boolean hasstatus = false;
		boolean hasreceivedpersons = false;
		boolean hascurrentnode = false;
		boolean hasrequestmark = false;
		if (scope.equals("doing")) {
			hasrequestname = true;
			hascreater = true;
			hascreatedate = true;
			hasreceivedpersons = true;
		} else if (scope.equals("done") || scope.equals("complete")) {
			hasrequestname = true;
			hasworkflowname = true;
			hascreater = true;
			hasreceivetime = true;
			hascurrentnode = true;
			hasreceivedpersons = true;
		} else if (scope.equals("mine")) {
			hasrequestname = true;
			hasworkflowname = true;
			hascreatedate = true;
			hascurrentnode = true;
			hasreceivedpersons = true;
		} else {
			hasrequestname = true;
			hasworkflowname = true;
			hascreater = true;
			hascreatedate = true;
			hascurrentnode = true;
			hasreceivedpersons = true;
		}

		// 处理已办排序 start
		String operateDateTimeFieldSql0 = "";
		String operateDateTimeFieldSql = "";
		String operateDateTimeFieldSqlOs = "";
		if (orderby.toLowerCase().indexOf("operatedate") != -1) {
			operateDateTimeFieldSql0 = ",operatedate";
			operateDateTimeFieldSql = ", (case  WHEN t2.operatedate IS NULL  THEN t2.receivedate ELSE t2.operatedate END) operatedate ";
			operateDateTimeFieldSqlOs = ", (case  WHEN operatedate IS NULL  THEN receivedate ELSE operatedate END) operatedate ";
		}

		if (orderby.toLowerCase().indexOf("operatetime") != -1) {
			operateDateTimeFieldSql0 = ",operatetime";
			operateDateTimeFieldSql += ", (case  WHEN t2.operatetime IS NULL  THEN t2.receivetime ELSE t2.operatetime END) operatetime ";
			operateDateTimeFieldSqlOs += ", (case  WHEN operatetime IS NULL  THEN receivetime ELSE operatetime END) operatetime ";
		}
		// 处理已办排序 end
		// 最外层查询字段
		String backfields0 = " requestid,requestmark,createdate, createtime,creater, creatertype, workflowid, requestname, requestnamenew, " +
				"status,requestlevel,currentnodeid,viewtype,userid,receivedate,receivetime,isremark,nodeid,agentorbyagentid,agenttype,isprocessed "
			+ operateDateTimeFieldSql0 + ",systype,workflowtype";
		// 原始查询字段
		String backfields = " t1.requestid,t1.requestmark,t1.createdate, t1.createtime,t1.creater, t1.creatertype, t1.workflowid, t1.requestname, t1.requestnamenew," +
				" t1.status,t1.requestlevel,t1.currentnodeid,t2.viewtype,t2.userid,t2.receivedate,t2.receivetime,t2.isremark,t2.nodeid,t2.agentorbyagentid,t2.agenttype,t2.isprocessed "
			+ operateDateTimeFieldSql + " ,'0' as systype,t2.workflowtype";
		// 异构系统查询字段
		String backfieldsOs = " requestid,'' as requestmark,createdate, createtime,creatorid as creater, 0 as creatertype, workflowid, requestname, requestname as requestnamenew, " +
				"'' as status,0 as requestlevel,-1 as currentnodeid,viewtype,userid,receivedate,receivetime,isremark,0 as nodeid, -1 as agentorbyagentid,'0' as agenttype,'0' as isprocessed "
			+ operateDateTimeFieldSqlOs + ",'1' as systype, sysid as workflowtype";
		String fromSql = " from workflow_requestbase t1,workflow_currentoperator t2 ";
		String sqlWhere = sqlwhere;

		String para2 = "column:requestid+column:workflowid+column:viewtype+" + isovertime + "+" + user.getLanguage()
			+ "+column:nodeid+column:isremark+" + user.getUID()
			+ "+column:agentorbyagentid+column:agenttype+column:isprocessed+column:userid+" + myrequest
			+ "+column:creater";
		String para4 = user.getLanguage() + "+" + user.getUID() + "+column:userid";
		if (!docids.equals("")) {
			fromSql = fromSql + ",workflow_form t4 ";
			sqlWhere = sqlWhere + " and t1.requestid=t4.requestid ";
		}

		if (!superior) {
			if ("1".equals(belongtoshow)) {
				sqlWhere += " AND EXISTS (SELECT 1 FROM workFlow_CurrentOperator workFlowCurrentOperator WHERE t2.workflowid = workFlowCurrentOperator.workflowid AND t2.requestid = workFlowCurrentOperator.requestid AND workFlowCurrentOperator.userid in ("
					+ userIDAll + " ) and workFlowCurrentOperator.usertype = " + usertype + ") ";
			} else {
				sqlWhere += " AND EXISTS (SELECT 1 FROM workFlow_CurrentOperator workFlowCurrentOperator WHERE t2.workflowid = workFlowCurrentOperator.workflowid AND t2.requestid = workFlowCurrentOperator.requestid AND workFlowCurrentOperator.userid in ("
					+ user.getUID() + " ) and workFlowCurrentOperator.usertype = " + usertype + ") ";
			}
		}

		if (!branchid.equals("")) {
			sqlWhere += " AND t1.creater in (select id from hrmresource where subcompanyid1=" + branchid + ")  ";
		}
		sqlWhere += " and t1.workflowid in (select id from workflow_base where  ";
		// 流程类型、流程状态查询条件
		if ("0".equals(wfstatu)) {
			sqlWhere += " isvalid='0' ";
		} else {
			sqlWhere += " (isvalid='1' or isvalid='3') ";
		}
		if (!"".equals(workflowtype)) {
			sqlWhere += " and workflowtype=" + workflowtype + " ";
			sqlwhereos += " and sysid=" + workflowtype + " ";
		}
		if (offical.equals("1")) {
			sqlWhere += " and isWorkflowDoc=1";
			if (officalType == 1) {
				sqlWhere += " and officalType in(1,3)";
				if (processId > 0) {
					String _sql = "select nodeids from workflow_process_relative wpr where officalType in (1,3) and pdid="+ processId;
					RecordSet.executeSql(_sql);
					String nodeids = "";
					while (RecordSet.next()) {
						if (nodeids.equals("")) {
							nodeids = Util.null2String(RecordSet.getString("nodeids"));
						} else {
							nodeids = nodeids + "," + Util.null2String(RecordSet.getString("nodeids"));
						}
					}
					nodeids = nodeids.replaceAll(",{2,}", ",");
					if (nodeids.equals(""))
						nodeids = "-1";
					sqlWhere += " and t2.nodeid in (" + nodeids + ")";
				}
			} else if (officalType == 2) {
				sqlWhere += " and officalType=2";
				if (processId > 0) {
					String _sql = "select nodeids from workflow_process_relative wpr where officalType=2 and pdid="+ processId;
					RecordSet.executeSql(_sql);
					String nodeids = "";
					while (RecordSet.next()) {
						if (nodeids.equals("")) {
							nodeids = Util.null2String(RecordSet.getString("nodeids"));
						} else {
							nodeids = nodeids + "," + Util.null2String(RecordSet.getString("nodeids"));
						}
					}
					nodeids = nodeids.replaceAll(",{2,}", ",");
					if (nodeids.equals(""))
						nodeids = "-1";
					sqlWhere += " and t2.nodeid in (" + nodeids + ")";
				}
			}
		}
		sqlWhere += ")";
		if (!customResult.equals("")) {
			sqlWhere += " and t1.requestid in (" + customResult + ")";
		}
		String temptableString = "";
		String temptablerowString = "";
		if (isopenos) {
			para2 = "column:requestid+column:workflowid+column:viewtype+" + isovertime + "+" + user.getLanguage()
				+ "+column:nodeid+column:isremark+" + user.getUID()
				+ "+column:agentorbyagentid+column:agenttype+column:isprocessed+column:userid+" + myrequest
				+ "+column:creater+column:systype+column:workflowtype";
			fromSql = " from (select " + backfields0 + " from (select " + backfields + " " + fromSql + "" + sqlWhere
				+ " union (select distinct " + backfieldsOs + " from ofs_todo_data " + sqlwhereos + ") ) t1 ) t1 ";
			orderby = " receivedate ";
			temptableString = " <sql backfields=\"" + backfields0 + "\" sqlform=\"" + Util.toHtmlForSplitPage(fromSql)
				+ "\" sqlwhere=\"\"  sqlorderby=\"" + orderby
				+ "\"  sqlprimarykey=\"requestid\" sqlsortway=\"Desc\" sqlisdistinct=\"false\" />";
			//String showname = ofso.getShowsysname();
			String showname = "0";
			if (!showname.equals("0")) {
				temptablerowString = "<col width=\"8%\" text=\"" + SystemEnv.getHtmlLabelName(22677, user.getLanguage())
					+ "\" column=\"workflowtype\"  orderkey=\"workflowtype\" transmethod=\"weaver.workflow.request.todo.RequestUtil.getSysname\" otherpara=\""
					+ showname + "\" />";
			}
		} else {
			temptableString = " <sql backfields=\"" + backfields + "\" sqlform=\"" + fromSql 
				+ "\" sqlwhere=\"" + Util.toHtmlForSplitPage(sqlWhere) + "\"  sqlorderby=\"" + orderby
				+ "\"  sqlprimarykey=\"t1.requestid\" sqlsortway=\"Desc\" sqlisdistinct=\"false\" />";
		}
		
		//================sql串拼装结束，拼接分页组件tablestring=========
		String urlType = "";
		if (scope.equals("doing")) {
			urlType = "1";
			if (offical.equals("1"))
				urlType = "9";
		} else if (scope.equals("done")) {
			urlType = "2";
			if (offical.equals("1"))
				urlType = "10";
		} else if (scope.equals("complete")) {
			urlType = "3";
			if (offical.equals("1"))
				urlType = "11";
		} else if (scope.equals("mine")) {
			urlType = "4";
			if (offical.equals("1"))
				urlType = "12";
		} else {
			urlType = "0";
		}
		String pageId = PageIdConst.getWFPageId(urlType);
		String pageUid = PageUidFactory.getWfPageUid(urlType);
		String pageSize = PageIdConst.getPageSize(pageId, user.getUID());
		String operateString = "";
		String tableString = "";
		if (!userIDAll.equals(String.valueOf(user.getUID()))) {
			String currentUserpara = "column:userid";
			String popedomOtherpara = "column:viewtype+column:isremark+column:isprocessed+column:nodeid+column:workflowid+"+ scope;
			String popedomUserpara = userID + "_" + usertype;
			String popedomLogpara = "column:nodeid";
			String popedomNewwfgpara = "column:workflowid+column:agenttype";
			operateString = "<operates>";
			operateString += " <popedom async=\"false\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultOperation\" otherpara=\""
				+ popedomOtherpara + "\" otherpara2=\"" + popedomUserpara + "\" ></popedom> ";
			// 客户门户时，不显示转发按钮
			if (!user.getLogintype().equals("2")) {
				operateString += "     <operate href=\"javascript:doReview();\"  otherpara=\"" + currentUserpara
					+ "\" text=\"" + SystemEnv.getHtmlLabelName(6011, user.getLanguage()) + "\" index=\"1\"/>";
			}
			operateString += "     <operate href=\"javascript:doPrint();\" otherpara=\"" + currentUserpara
				+ "\" text=\"" + SystemEnv.getHtmlLabelName(257, user.getLanguage()) + "\" index=\"2\"/>";
			operateString += "     <operate href=\"javascript:doReadIt();\" otherpara=\"" + currentUserpara
				+ "\" text=\"" + SystemEnv.getHtmlLabelName(25419, user.getLanguage()) + "\" index=\"0\"/>";
			/*operateString += "     <operate href=\"javascript:doNewwf();\" text=\""
				+ SystemEnv.getHtmlLabelName(16392, user.getLanguage()) + "\" otherpara=\"" + popedomNewwfgpara+ "\" index=\"3\"/>";*/
			operateString += "     <operate href=\"javascript:seeFormLog();\" text=\""
				+ SystemEnv.getHtmlLabelName(21625, user.getLanguage()) + "\" otherpara=\"" + popedomLogpara+ "\" index=\"5\"/>";
			if (offical.equals("1") && sysId == 5) {
				operateString += "     <operate isalwaysshow=\"true\" href=\"javascript:taoHong();\" otherpara=\""
					+ isovertime + "\" text=\"" + SystemEnv.getHtmlLabelName(20227, user.getLanguage())+ "\" index=\"6\"/>";
			} else if (offical.equals("1") && sysId == 6) {
				operateString += "     <operate isalwaysshow=\"true\" href=\"javascript:signNature();\" otherpara=\""
					+ isovertime + "\" text=\"" + SystemEnv.getHtmlLabelName(21650, user.getLanguage())+ "\" index=\"6\"/>";
			} else if (offical.equals("1") && sysId == 1) {
				operateString += "     <operate isalwaysshow=\"true\" href=\"javascript:doEditContent();\" otherpara=\""+ isovertime
					+ "\" text=\""+ SystemEnv.getHtmlLabelNames("1265,93", user.getLanguage())+ "\" index=\"6\"/>";
			} else if (offical.equals("1") && sysId == 2) {
				operateString += "     <operate isalwaysshow=\"true\" href=\"javascript:doEditContent();\" otherpara=\""+ isovertime
					+ "\" text=\""+ SystemEnv.getHtmlLabelNames("1265,33697", user.getLanguage())+ "\" index=\"6\"/>";
			} else if (offical.equals("1") && (sysId == 3 || sysId == 4)) {
				operateString += "     <operate isalwaysshow=\"true\" href=\"javascript:doEditContent();\" otherpara=\""+ isovertime
					+ "\" text=\""+ SystemEnv.getHtmlLabelNames("1265,553", user.getLanguage())+ "\" index=\"6\"/>";
			} else if (offical.equals("1") && sysId == 7) {
				operateString += "     <operate isalwaysshow=\"true\" href=\"javascript:doEditContent();\" otherpara=\""+ isovertime
					+ "\" text=\""+ SystemEnv.getHtmlLabelNames("1265,257", user.getLanguage())+ "\" index=\"6\"/>";
			} else if (offical.equals("1") && (sysId == 9 || sysId == 14 || sysId == 15 || scope.equals("done") || scope.equals("mine"))) {
				operateString += "     <operate isalwaysshow=\"true\" href=\"javascript:doEditContent();\" otherpara=\""+ isovertime
					+ "\" text=\""+ SystemEnv.getHtmlLabelNames("1265,553", user.getLanguage())+ "\" index=\"6\"/>";
			}
			operateString += "</operates>";

			if (isMultiSubmit && iswaitdo == 1) {
				if ("1".equals(belongtoshow)) {
					tableString = " <table instanceid=\"workflowRequestListTable\" pageId=\""+ pageId + "\" pageUid=\"" + pageUid
						+ "\"   tabletype=\"checkbox\" pagesize=\""+ pageSize+ "\" >"
						+ " <checkboxpopedom  id=\"checkbox\"  popedompara=\"column:workflowid+column:isremark+column:requestid+column:nodeid+column:userid\" showmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCheckBox\" />";
					tableString += temptableString;
					tableString += operateString + "			<head>";

					tableString += "<col width=\"19%\" display=\""+ hasrequestname+ "\" text=\""+ SystemEnv.getHtmlLabelName(1334, user.getLanguage())
						+ "\" column=\"requestname\" orderkey=\"t1.requestname\" linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_fullwindow\" transmethod=\""+requestnamereflectclass+".getWfNewLinkWithTitle2\"  otherpara=\""
						+ para2+ "\"  pkey=\"requestname+weaver.general.WorkFlowTransMethod.getWfNewLinkWithTitle\" />";
				} else {
					tableString = " <table instanceid=\"workflowRequestListTable\" pageId=\""+ pageId + "\" pageUid=\"" + pageUid
						+ "\"   tabletype=\"checkbox\" pagesize=\""+ pageSize+ "\" >"
						+ " <checkboxpopedom  id=\"checkbox\"  popedompara=\"column:workflowid+column:isremark+column:requestid+column:nodeid+column:userid\" showmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCheckBox\" />";
					tableString += temptableString;
					tableString += operateString + "<head>";

					tableString += "<col width=\"19%\" display=\""+ hasrequestname+ "\" text=\""+ SystemEnv.getHtmlLabelName(1334, user.getLanguage())
						+ "\" column=\"requestname\" orderkey=\"t1.requestname\" linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_fullwindow\" transmethod=\""+requestnamereflectclass+".getWfNewLinkWithTitle\"  otherpara=\""
						+ para2+ "\"  pkey=\"requestname+weaver.general.WorkFlowTransMethod.getWfNewLinkWithTitle\"  />";
				}
				tableString += temptablerowString;
				tableString += "<col width=\"10%\" display=\""+ hasworkflowname + "\"  text=\""+ SystemEnv.getHtmlLabelName(259, user.getLanguage())
					+ "\" column=\"workflowid\" orderkey=\"t1.workflowid\" transmethod=\""+workflownamereflectmethod+"\" />";
				tableString += "<col width=\"6%\" display=\""+ hascreater + "\"  text=\""+ SystemEnv.getHtmlLabelName(882, user.getLanguage())
					+ "\" column=\"creater\" orderkey=\"t1.creater\"  otherpara=\"column:creatertype\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultName\" />";

				tableString += " <col width=\"10%\" display=\""+ hascreatedate + "\" id=\"createdate\" text=\""+ SystemEnv.getHtmlLabelName(722, user.getLanguage())
					+ "\" column=\"createdate\" orderkey=\"t1.createdate,t1.createtime\" otherpara=\"column:createtime\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCreateTime\" />";
				tableString += "<col width=\"8%\" display=\""+ hasrequestlevel + "\"  id=\"quick\" text=\""+ SystemEnv.getHtmlLabelName(15534, user.getLanguage())
					+ "\" column=\"requestlevel\"  orderkey=\"t1.requestlevel\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultUrgencyDegree\" otherpara=\""
					+ user.getLanguage() + "\"/>";
				tableString += "<col width=\"10%\" display=\""+ hasreceivetime + "\"  text=\""+ SystemEnv.getHtmlLabelName(17994, user.getLanguage())
					+ "\" column=\"receivedate\" orderkey=\"receivedate,receivetime\" otherpara=\"column:receivetime\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCreateTime\" />";
				tableString += "<col width=\"8%\" display=\""+ hascurrentnode + "\"  id=\"hurry\" text=\""+ SystemEnv.getHtmlLabelName(18564, user.getLanguage())
					+ "\" column=\"currentnodeid\" orderkey=\"t1.currentnodeid\" transmethod=\"weaver.general.WorkFlowTransMethod.getCurrentNode\"/>";

				tableString += "<col width=\"8%\" display=\"" + hasstatus + "\"  text=\""+ SystemEnv.getHtmlLabelName(1335, user.getLanguage())
					+ "\" column=\"status\" orderkey=\"t1.status\" />";
				tableString += "<col width=\"15%\" display=\"" + hasreceivedpersons + "\"  text=\""+ SystemEnv.getHtmlLabelName(16354, user.getLanguage()) 
					+ "\" _key=\"unoperators\" column=\"requestid\" otherpara=\""
					+ para4 + "\" transmethod=\"weaver.general.WorkFlowTransMethod.getUnOperators\"/>";
				tableString += "<col width=\"6%\" display=\"false\"  text=\""+ SystemEnv.getHtmlLabelName(19363, user.getLanguage())
					+ "\" _key=\"subwflink\" column=\"requestid\" orderkey=\"t1.requestid\"  linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_self\" transmethod=\"weaver.general.WorkFlowTransMethod.getSubWFLink\"  otherpara=\""
					+ user.getLanguage() + "\"/>";
				tableString += "<col width=\"15%\" display=\"" + hasrequestmark
					+ "\" orderkey=\"t1.requestmark\"  text=\"" + SystemEnv.getHtmlLabelName(19502, user.getLanguage())
					+ "\" column=\"requestmark\"/>";
				tableString += "			</head>" + "</table>";
			} else if ("1".equals(isFromMessage)) {
				if ("1".equals(belongtoshow)) {
					tableString = " <table instanceid=\"workflowRequestListTable\" pageId=\""+ pageId + "\" pageUid=\"" + pageUid
						+ "\"   tabletype=\"checkbox\" pagesize=\""+ pageSize+ "\" >"
						+ " <checkboxpopedom  id=\"checkbox\"  popedompara=\"column:viewtype+column:isremark+column:isprocessed\" showmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchRstCkBoxForMsg\" />";
					tableString += temptableString;
					tableString += operateString + "			<head>";

					tableString += "<col width=\"19%\" display=\""+ hasrequestname + "\" text=\""+ SystemEnv.getHtmlLabelName(1334, user.getLanguage())
						+ "\" column=\"requestname\" orderkey=\"t1.requestname\" linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_fullwindow\" transmethod=\""+requestnamereflectclass+".getWfNewLinkWithTitle2\"  otherpara=\""
						+ para2+ "\"  pkey=\"requestname+weaver.general.WorkFlowTransMethod.getWfNewLinkWithTitle\"  />";
				} else {
					tableString = " <table instanceid=\"workflowRequestListTable\" pageId=\""+ pageId + "\" pageUid=\"" + pageUid
						+ "\"   tabletype=\"checkbox\" pagesize=\""+ pageSize+ "\" >"
						+ " <checkboxpopedom  id=\"checkbox\"  popedompara=\"column:viewtype+column:isremark+column:isprocessed\" showmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchRstCkBoxForMsg\" />";
					tableString += temptableString;
					tableString += operateString + "<head>";

					tableString += "<col width=\"19%\" display=\""+ hasrequestname + "\" text=\""+ SystemEnv.getHtmlLabelName(1334, user.getLanguage())
						+ "\" column=\"requestname\" orderkey=\"t1.requestname\" linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_fullwindow\" transmethod=\""+requestnamereflectclass+".getWfNewLinkWithTitle\"  otherpara=\""
						+ para2+ "\"   pkey=\"requestname+weaver.general.WorkFlowTransMethod.getWfNewLinkWithTitle\" />";
				}
				tableString += temptablerowString;
				tableString += "<col width=\"10%\" display=\""+ hasworkflowname + "\"  text=\""+ SystemEnv.getHtmlLabelName(259, user.getLanguage())
					+ "\" column=\"workflowid\" orderkey=\"t1.workflowid\" transmethod=\""+workflownamereflectmethod+"\" />";

				tableString += "<col width=\"6%\" display=\""+ hascreater + "\"   text=\""+ SystemEnv.getHtmlLabelName(882, user.getLanguage())
					+ "\" column=\"creater\" orderkey=\"t1.creater\"  otherpara=\"column:creatertype\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultName\" />";

				tableString += " <col width=\"10%\" display=\""+ hascreatedate + "\" id=\"createdate\" text=\""+ SystemEnv.getHtmlLabelName(722, user.getLanguage())
					+ "\" column=\"createdate\" orderkey=\"t1.createdate,t1.createtime\" otherpara=\"column:createtime\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCreateTime\" />";
				tableString += "<col width=\"8%\" display=\""+ hasrequestlevel + "\"  id=\"quick\" text=\""+ SystemEnv.getHtmlLabelName(15534, user.getLanguage())
					+ "\" column=\"requestlevel\"  orderkey=\"t1.requestlevel\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultUrgencyDegree\" otherpara=\""
					+ user.getLanguage() + "\"/>";
				tableString += "<col width=\"10%\" display=\""+ hasreceivetime + "\"  text=\""+ SystemEnv.getHtmlLabelName(17994, user.getLanguage())
					+ "\" column=\"receivedate\" orderkey=\"receivedate,receivetime\" otherpara=\"column:receivetime\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCreateTime\" />";
				tableString += "<col width=\"8%\" display=\""+ hascurrentnode + "\"  id=\"hurry\" text=\""+ SystemEnv.getHtmlLabelName(18564, user.getLanguage())
					+ "\" column=\"currentnodeid\" orderkey=\"t1.currentnodeid\" transmethod=\"weaver.general.WorkFlowTransMethod.getCurrentNode\"/>";

				tableString += "<col width=\"8%\" display=\"" + hasstatus + "\"  text=\""+ SystemEnv.getHtmlLabelName(1335, user.getLanguage())
					+ "\" column=\"status\" orderkey=\"t1.status\" />";
				tableString += "<col width=\"15%\" display=\"" + hasreceivedpersons + "\"  text=\""+ SystemEnv.getHtmlLabelName(16354, user.getLanguage()) 
					+ "\" _key=\"unoperators\" column=\"requestid\" otherpara=\""
					+ para4 + "\" transmethod=\"weaver.general.WorkFlowTransMethod.getUnOperators\"/>";
				tableString += "<col width=\"6%\" display=\"false\"  text=\""+ SystemEnv.getHtmlLabelName(19363, user.getLanguage())
					+ "\" _key=\"subwflink\" column=\"requestid\" orderkey=\"t1.requestid\"  linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_self\" transmethod=\"weaver.general.WorkFlowTransMethod.getSubWFLink\"  otherpara=\""
					+ user.getLanguage() + "\"/>";
				tableString += "<col width=\"15%\" display=\"" + hasrequestmark
					+ "\" orderkey=\"t1.requestmark\"  text=\"" + SystemEnv.getHtmlLabelName(19502, user.getLanguage())
					+ "\" column=\"requestmark\"/>";
				tableString += "			</head>" + "</table>";
			} else if ("myall".equals(myrequest) || "myreqeustbywftype".equals(myrequest) || "myreqeustbywfid".equals(myrequest)) {
				if ("1".equals(belongtoshow)) {
					sqlWhere += " and t1.creater = t2.userid";
					tableString = " <table instanceid=\"workflowRequestListTable\" tabletype=\"none\" pageId=\""+ pageId + "\" pageUid=\"" + pageUid
						+ "\" cssHandler=\"com.weaver.cssRenderHandler.request.CheckboxColorRender\" pagesize=\""+ pageSize + "\" >";
					tableString += temptableString;
					tableString += operateString + "			<head>";

					tableString += "<col width=\"19%\" display=\""+ hasrequestname + "\" text=\""+ SystemEnv.getHtmlLabelName(1334, user.getLanguage())
						+ "\" column=\"requestname\" orderkey=\"t1.requestname\" linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_fullwindow\" transmethod=\""+requestnamereflectclass+".getWfNewLinkWithTitle2\"  otherpara=\""
						+ para2 + "\"  pkey=\"requestname+weaver.general.WorkFlowTransMethod.getWfNewLinkWithTitle\"  />";
				} else {
					tableString = " <table instanceid=\"workflowRequestListTable\" tabletype=\"none\" pageId=\""+ pageId + "\" pageUid=\"" + pageUid
						+ "\" cssHandler=\"com.weaver.cssRenderHandler.request.CheckboxColorRender\" pagesize=\""+ pageSize + "\" >";
					tableString += temptableString;
					tableString += operateString + "			<head>";

					tableString += "<col width=\"19%\" display=\""+ hasrequestname
						+ "\" text=\""+ SystemEnv.getHtmlLabelName(1334, user.getLanguage())
						+ "\" column=\"requestname\" orderkey=\"t1.requestname\" linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_fullwindow\" transmethod=\""+requestnamereflectclass+".getWfNewLinkWithTitle\"  otherpara=\""
						+ para2 + "\"  pkey=\"requestname+weaver.general.WorkFlowTransMethod.getWfNewLinkWithTitle\"  />";
				}
				tableString += temptablerowString;
				tableString += " <col width=\"10%\"  display=\""+ hasworkflowname+ "\"  text=\""+ SystemEnv.getHtmlLabelName(259, user.getLanguage())
					+ "\" column=\"workflowid\" orderkey=\"t1.workflowid\" transmethod=\""+workflownamereflectmethod+"\" />";

				tableString += " <col width=\"6%\" display=\""+ hascreater+ "\"  text=\""+ SystemEnv.getHtmlLabelName(882, user.getLanguage())
					+ "\" column=\"creater\" orderkey=\"t1.creater\"  otherpara=\"column:creatertype\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultName\" />";
				tableString += "<col width=\"10%\" display=\""+ hascreatedate+ "\" text=\""+ SystemEnv.getHtmlLabelName(722, user.getLanguage())
					+ "\" column=\"createdate\" orderkey=\"t1.createdate,t1.createtime\" otherpara=\"column:createtime\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCreateTime\" />";
				tableString += " <col width=\"8%\" display=\""+ hasrequestlevel+ "\" text=\""+ SystemEnv.getHtmlLabelName(15534, user.getLanguage())
					+ "\" column=\"requestlevel\"  orderkey=\"t1.requestlevel\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultUrgencyDegree\" otherpara=\""+ user.getLanguage() + "\"/>";
				tableString += " <col width=\"10%\" display=\""+ hasreceivetime+ "\" text=\""+ SystemEnv.getHtmlLabelName(17994, user.getLanguage())
					+ "\" column=\"receivedate\" orderkey=\"receivedate,receivetime\" otherpara=\"column:receivetime\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCreateTime\" />";
				
				tableString += " <col width=\"8%\" display=\""+ hascurrentnode+ "\"  text=\""+ SystemEnv.getHtmlLabelName(18564, user.getLanguage())
					+ "\" column=\"currentnodeid\" orderkey=\"t1.currentnodeid\" transmethod=\"weaver.general.WorkFlowTransMethod.getCurrentNode\"/>";
				tableString += " <col width=\"8%\" display=\"" + hasstatus + "\" text=\""+ SystemEnv.getHtmlLabelName(1335, user.getLanguage())
					+ "\" column=\"status\" orderkey=\"t1.status\" />";
				tableString += " <col width=\"15%\" display=\"" + hasreceivedpersons + "\"   text=\""+ SystemEnv.getHtmlLabelName(16354, user.getLanguage()) 
					+ "\" _key=\"unoperators\" column=\"requestid\"  otherpara=\""
					+ para4 + "\" transmethod=\"weaver.general.WorkFlowTransMethod.getUnOperators\"/>";
				tableString += "<col width=\"6%\" display=\"false\"  text=\""+ SystemEnv.getHtmlLabelName(19363, user.getLanguage())
					+ "\" _key=\"subwflink\" column=\"requestid\" orderkey=\"t1.requestid\"  linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_self\" transmethod=\"weaver.general.WorkFlowTransMethod.getSubWFLink\"  otherpara=\""
					+ user.getLanguage() + "\"/>";
				tableString += "<col width=\"15%\" display=\"" + hasrequestmark
					+ "\" orderkey=\"t1.requestmark\" text=\"" + SystemEnv.getHtmlLabelName(19502, user.getLanguage())
					+ "\" column=\"requestmark\"/>";
				tableString += "			</head>" + "</table>";
			} else {
				if ("1".equals(belongtoshow)) {
					tableString = " <table instanceid=\"workflowRequestListTable\" tabletype=\"none\" pageId=\""+ pageId + "\" pageUid=\"" + pageUid
						+ "\" cssHandler=\"com.weaver.cssRenderHandler.request.CheckboxColorRender\" pagesize=\""+ pageSize + "\" >";
					tableString += temptableString;
					tableString += operateString + "	<head>";

					tableString += "<col width=\"19%\" display=\""+ hasrequestname + "\" text=\""+ SystemEnv.getHtmlLabelName(1334, user.getLanguage())
						+ "\" column=\"requestname\" orderkey=\"t1.requestname\" linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_fullwindow\" transmethod=\""+requestnamereflectclass+".getWfNewLinkWithTitle2\"  otherpara=\""
						+ para2 + "\"  pkey=\"requestname+weaver.general.WorkFlowTransMethod.getWfNewLinkWithTitle\"  />";
				} else {
					tableString = " <table instanceid=\"workflowRequestListTable\" tabletype=\"none\" pageId=\""+ pageId + "\" pageUid=\"" + pageUid
						+ "\" cssHandler=\"com.weaver.cssRenderHandler.request.CheckboxColorRender\" pagesize=\""+ pageSize + "\" >";
					tableString += temptableString;
					tableString += operateString + "			<head>";

					tableString += "<col width=\"19%\" display=\""+ hasrequestname + "\" text=\""+ SystemEnv.getHtmlLabelName(1334, user.getLanguage())
						+ "\" column=\"requestname\" orderkey=\"t1.requestname\" linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_fullwindow\" transmethod=\""+requestnamereflectclass+".getWfNewLinkWithTitle\"  otherpara=\""
						+ para2 + "\"  pkey=\"requestname+weaver.general.WorkFlowTransMethod.getWfNewLinkWithTitle\"  />";
				}
				tableString += temptablerowString;
				tableString += " <col width=\"10%\"  display=\""+ hasworkflowname + "\"  text=\""+ SystemEnv.getHtmlLabelName(259, user.getLanguage())
					+ "\" column=\"workflowid\" orderkey=\"t1.workflowid\" transmethod=\""+workflownamereflectmethod+"\" />";

				tableString += " <col width=\"6%\" display=\""+ hascreater + "\"  text=\""+ SystemEnv.getHtmlLabelName(882, user.getLanguage())
					+ "\" column=\"creater\" orderkey=\"t1.creater\"  otherpara=\"column:creatertype\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultName\" />";
				tableString += "<col display=\""+ hascreatedate + "\" width=\"10%\"  text=\""+ SystemEnv.getHtmlLabelName(722, user.getLanguage())
					+ "\" column=\"createdate\" orderkey=\"t1.createdate,t1.createtime\" otherpara=\"column:createtime\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCreateTime\" />";
				tableString += " <col width=\"8%\" display=\""+ hasrequestlevel + "\"   text=\""+ SystemEnv.getHtmlLabelName(15534, user.getLanguage())
					+ "\" column=\"requestlevel\"  orderkey=\"t1.requestlevel\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultUrgencyDegree\" otherpara=\""
					+ user.getLanguage() + "\"/>";

				tableString += " <col width=\"10%\" display=\""+ hasreceivetime+ "\"  text=\""+ SystemEnv.getHtmlLabelName(17994, user.getLanguage())
					+ "\" column=\"receivedate\" orderkey=\"receivedate,receivetime\" otherpara=\"column:receivetime\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCreateTime\" />";
				tableString += " <col width=\"8%\" display=\""+ hascurrentnode+ "\"   text=\""+ SystemEnv.getHtmlLabelName(18564, user.getLanguage())
					+ "\" column=\"currentnodeid\" orderkey=\"t1.currentnodeid\" transmethod=\"weaver.general.WorkFlowTransMethod.getCurrentNode\"/>";

				tableString += " <col width=\"8%\" display=\"" + hasstatus + "\"    text=\""+ SystemEnv.getHtmlLabelName(1335, user.getLanguage())
					+ "\" column=\"status\" orderkey=\"t1.status\" />";
				tableString += " <col width=\"15%\" display=\"" + hasreceivedpersons + "\"   text=\""+ SystemEnv.getHtmlLabelName(16354, user.getLanguage()) 
					+ "\" _key=\"unoperators\" column=\"requestid\"  otherpara=\""
					+ para4 + "\" transmethod=\"weaver.general.WorkFlowTransMethod.getUnOperators\"/>";
				tableString += "<col width=\"6%\" display=\"false\"  text=\""+ SystemEnv.getHtmlLabelName(19363, user.getLanguage())
					+ "\" _key=\"subwflink\" column=\"requestid\" orderkey=\"t1.requestid\"  linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_self\" transmethod=\"weaver.general.WorkFlowTransMethod.getSubWFLink\"  otherpara=\""
					+ user.getLanguage() + "\"/>";
				tableString += "<col width=\"15%\" display=\"" + hasrequestmark
					+ "\" orderkey=\"t1.requestmark\" text=\"" + SystemEnv.getHtmlLabelName(19502, user.getLanguage())
					+ "\" column=\"requestmark\"/>";
				tableString += "			</head>" + "</table>";
			}
		} else {
			String currentUserpara = "column:userid";
			String popedomOtherpara = "column:viewtype+column:isremark+column:isprocessed+column:nodeid+column:workflowid+" + scope;
			String popedomUserpara = userID + "_" + usertype;
			String popedomLogpara = "column:nodeid";
			String popedomNewwfgpara = "column:workflowid+column:agenttype";

			operateString = "<operates>";
			operateString += " <popedom async=\"false\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultOperation\" otherpara=\""
				+ popedomOtherpara + "\" otherpara2=\"" + popedomUserpara + "\" ></popedom> ";
			
			// QC158666 客户门户时，不显示转发按钮
			if (!user.getLogintype().equals("2")) {
				operateString += "     <operate href=\"javascript:doReview();\" otherpara=\"" + currentUserpara
					+ "\" text=\"" + SystemEnv.getHtmlLabelName(6011, user.getLanguage()) + "\" index=\"1\"/>";
			}
			operateString += "     <operate href=\"javascript:doPrint();\" otherpara=\"" + currentUserpara
				+ "\" text=\"" + SystemEnv.getHtmlLabelName(257, user.getLanguage()) + "\" index=\"2\"/>";
			operateString += "     <operate href=\"javascript:doReadIt();\"  otherpara=\"" + currentUserpara
				+ "\" text=\"" + SystemEnv.getHtmlLabelName(25419, user.getLanguage()) + "\" index=\"0\"/>";
			/*operateString += "     <operate href=\"javascript:doNewwf();\" text=\""
				+ SystemEnv.getHtmlLabelName(16392, user.getLanguage()) + "\" otherpara=\"" + popedomNewwfgpara+ "\" index=\"3\"/>";*/
			operateString += "     <operate href=\"javascript:seeFormLog();\" text=\""
				+ SystemEnv.getHtmlLabelName(21625, user.getLanguage()) + "\" otherpara=\"" + popedomLogpara+ "\" index=\"5\"/>";
			if (offical.equals("1") && sysId == 5) {
				operateString += "     <operate isalwaysshow=\"true\" href=\"javascript:taoHong();\" otherpara=\""
					+ isovertime + "\" text=\"" + SystemEnv.getHtmlLabelName(20227, user.getLanguage())+ "\" index=\"6\"/>";
			} else if (offical.equals("1") && sysId == 6) {
				operateString += "     <operate isalwaysshow=\"true\" href=\"javascript:signNature();\" otherpara=\""
					+ isovertime + "\" text=\"" + SystemEnv.getHtmlLabelName(21650, user.getLanguage())+ "\" index=\"6\"/>";
			} else if (offical.equals("1") && sysId == 1) {
				operateString += "     <operate isalwaysshow=\"true\" href=\"javascript:doEditContent();\" otherpara=\""+ isovertime
					+ "\" text=\""+ SystemEnv.getHtmlLabelNames("1265,93", user.getLanguage())+ "\" index=\"6\"/>";
			} else if (offical.equals("1") && sysId == 2) {
				operateString += "     <operate isalwaysshow=\"true\" href=\"javascript:doEditContent();\" otherpara=\""+ isovertime
					+ "\" text=\""+ SystemEnv.getHtmlLabelNames("1265,33697", user.getLanguage())+ "\" index=\"6\"/>";
			} else if (offical.equals("1") && (sysId == 3 || sysId == 4)) {
				operateString += "     <operate isalwaysshow=\"true\" href=\"javascript:doEditContent();\" otherpara=\""+ isovertime
					+ "\" text=\""+ SystemEnv.getHtmlLabelNames("1265,553", user.getLanguage())+ "\" index=\"6\"/>";
			} else if (offical.equals("1") && sysId == 7) {
				operateString += "     <operate isalwaysshow=\"true\" href=\"javascript:doEditContent();\" otherpara=\""+ isovertime
					+ "\" text=\""+ SystemEnv.getHtmlLabelNames("1265,257", user.getLanguage())+ "\" index=\"6\"/>";
			} else if (offical.equals("1") && (sysId == 9 || sysId == 14 || sysId == 15 || scope.equals("done") || scope.equals("mine"))) {
				operateString += "     <operate isalwaysshow=\"true\" href=\"javascript:doEditContent();\" otherpara=\""+ isovertime
					+ "\" text=\""+ SystemEnv.getHtmlLabelNames("1265,553", user.getLanguage())+ "\" index=\"6\"/>";
			}
			operateString += "</operates>";
			
			if (isMultiSubmit && iswaitdo == 1) {
				tableString = " <table instanceid=\"workflowRequestListTable\" pageId=\""+ pageId + "\" pageUid=\"" + pageUid
					+ "\"   tabletype=\"checkbox\" pagesize=\""+ pageSize+ "\" >"
					+ " <checkboxpopedom  id=\"checkbox\"  popedompara=\"column:workflowid+column:isremark+column:requestid+column:nodeid+column:userid\" showmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCheckBox\" />";
				tableString += temptableString;
				tableString += operateString + "			<head>";
				
				tableString += "<col width=\"19%\" display=\""+ hasrequestname + "\" text=\""+ SystemEnv.getHtmlLabelName(1334, user.getLanguage())
					+ "\" column=\"requestname\"  orderkey=\"t1.requestname\" linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_fullwindow\" transmethod=\""+requestnamereflectclass+".getWfNewLinkWithTitle\"  otherpara=\""
					+ para2 + "\"  pkey=\"requestname+weaver.general.WorkFlowTransMethod.getWfNewLinkWithTitle\"  />";
				tableString += temptablerowString;
				tableString += "<col width=\"10%\" display=\""+ hasworkflowname + "\"  text=\""+ SystemEnv.getHtmlLabelName(259, user.getLanguage())
					+ "\" column=\"workflowid\" orderkey=\"t1.workflowid\" transmethod=\""+workflownamereflectmethod+"\" />";
				tableString += "<col width=\"6%\" display=\""+ hascreater + "\"   text=\""+ SystemEnv.getHtmlLabelName(882, user.getLanguage())
					+ "\" column=\"creater\" orderkey=\"t1.creater\"  otherpara=\"column:creatertype\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultName\" />";

				tableString += " <col width=\"10%\" display=\""+ hascreatedate + "\" id=\"createdate\" text=\""+ SystemEnv.getHtmlLabelName(722, user.getLanguage())
					+ "\" column=\"createdate\" orderkey=\"t1.createdate,t1.createtime\" otherpara=\"column:createtime\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCreateTime\" />";
				tableString += "<col width=\"8%\" display=\""+ hasrequestlevel + "\"  id=\"quick\" text=\""+ SystemEnv.getHtmlLabelName(15534, user.getLanguage())
					+ "\" column=\"requestlevel\"  orderkey=\"t1.requestlevel\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultUrgencyDegree\" otherpara=\""
					+ user.getLanguage() + "\"/>";
				tableString += "<col width=\"10%\" display=\""+ hasreceivetime + "\"  text=\""+ SystemEnv.getHtmlLabelName(17994, user.getLanguage())
					+ "\" column=\"receivedate\" orderkey=\"receivedate,receivetime\" otherpara=\"column:receivetime\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCreateTime\" />";
				tableString += "<col width=\"8%\" display=\"" + hascurrentnode + "\"  id=\"hurry\" text=\"" + SystemEnv.getHtmlLabelName(18564, user.getLanguage())
					+ "\" column=\"currentnodeid\" orderkey=\"t1.currentnodeid\" transmethod=\"weaver.general.WorkFlowTransMethod.getCurrentNode\"/>";

				tableString += "<col width=\"8%\" display=\"" + hasstatus + "\"  text=\"" + SystemEnv.getHtmlLabelName(1335, user.getLanguage())
					+ "\" column=\"status\" orderkey=\"t1.status\" />";
				tableString += "<col width=\"15%\" display=\"" + hasreceivedpersons + "\"  text=\""+ SystemEnv.getHtmlLabelName(16354, user.getLanguage()) 
					+ "\" _key=\"unoperators\" column=\"requestid\" otherpara=\""
					+ para4 + "\" transmethod=\"weaver.general.WorkFlowTransMethod.getUnOperators\"/>";
				tableString += "<col width=\"6%\" display=\"false\"  text=\""+ SystemEnv.getHtmlLabelName(19363, user.getLanguage())
					+ "\" _key=\"subwflink\" column=\"requestid\" orderkey=\"t1.requestid\"  linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_self\" transmethod=\"weaver.general.WorkFlowTransMethod.getSubWFLink\"  otherpara=\""
					+ user.getLanguage() + "\"/>";
				tableString += "<col width=\"15%\" display=\"" + hasrequestmark
					+ "\" orderkey=\"t1.requestmark\"  text=\"" + SystemEnv.getHtmlLabelName(19502, user.getLanguage())
					+ "\" column=\"requestmark\"/>";
				tableString += "			</head>" + "</table>";
			} else if ("1".equals(isFromMessage)) {
				tableString = " <table instanceid=\"workflowRequestListTable\" pageId=\""+ pageId + "\" pageUid=\"" + pageUid
					+ "\"   tabletype=\"checkbox\" pagesize=\""+ pageSize+ "\" >"
					+ " <checkboxpopedom  id=\"checkbox\"  popedompara=\"column:viewtype+column:isremark+column:isprocessed\" showmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchRstCkBoxForMsg\" />";
				tableString += temptableString;
				tableString += operateString + "			<head>";

				tableString += temptablerowString;
				tableString += "<col width=\"19%\" display=\""+ hasrequestname + "\" text=\""+ SystemEnv.getHtmlLabelName(1334, user.getLanguage())
					+ "\" column=\"requestname\" orderkey=\"t1.requestname\" linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_fullwindow\" transmethod=\""+requestnamereflectclass+".getWfNewLinkWithTitle\"  otherpara=\""
					+ para2 + "\"  pkey=\"requestname+weaver.general.WorkFlowTransMethod.getWfNewLinkWithTitle\"  />";
				tableString += "<col width=\"10%\" display=\""+ hasworkflowname + "\"  text=\""+ SystemEnv.getHtmlLabelName(259, user.getLanguage())
					+ "\" column=\"workflowid\" orderkey=\"t1.workflowid\" transmethod=\""+workflownamereflectmethod+"\" />";
				tableString += "<col width=\"6%\" display=\""+ hascreater + "\"   text=\""+ SystemEnv.getHtmlLabelName(882, user.getLanguage())
					+ "\" column=\"creater\" orderkey=\"t1.creater\"  otherpara=\"column:creatertype\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultName\" />";

				tableString += " <col width=\"10%\" display=\""+ hascreatedate
					+ "\" id=\"createdate\" text=\""+ SystemEnv.getHtmlLabelName(722, user.getLanguage())
					+ "\" column=\"createdate\" orderkey=\"t1.createdate,t1.createtime\" otherpara=\"column:createtime\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCreateTime\" />";
				tableString += "<col width=\"8%\" display=\""+ hasrequestlevel
					+ "\"  id=\"quick\" text=\""+ SystemEnv.getHtmlLabelName(15534, user.getLanguage())
					+ "\" column=\"requestlevel\"  orderkey=\"t1.requestlevel\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultUrgencyDegree\" otherpara=\""
					+ user.getLanguage() + "\"/>";
				tableString += "<col width=\"10%\" display=\""+ hasreceivetime + "\"  text=\""+ SystemEnv.getHtmlLabelName(17994, user.getLanguage())
					+ "\" column=\"receivedate\" orderkey=\"receivedate,receivetime\" otherpara=\"column:receivetime\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCreateTime\" />";
				tableString += "<col width=\"8%\" display=\""+ hascurrentnode
					+ "\"  id=\"hurry\" text=\""+ SystemEnv.getHtmlLabelName(18564, user.getLanguage())
					+ "\" column=\"currentnodeid\" orderkey=\"t1.currentnodeid\" transmethod=\"weaver.general.WorkFlowTransMethod.getCurrentNode\"/>";
				tableString += "<col width=\"8%\" display=\"" + hasstatus + "\"  text=\""+ SystemEnv.getHtmlLabelName(1335, user.getLanguage())
					+ "\" column=\"status\" orderkey=\"t1.status\" />";
				tableString += "<col width=\"15%\" display=\"" + hasreceivedpersons + "\"  text=\""+ SystemEnv.getHtmlLabelName(16354, user.getLanguage()) 
					+ "\" _key=\"unoperators\" column=\"requestid\" otherpara=\""
					+ para4 + "\" transmethod=\"weaver.general.WorkFlowTransMethod.getUnOperators\"/>";
				tableString += "<col width=\"6%\" display=\"false\"  text=\""+ SystemEnv.getHtmlLabelName(19363, user.getLanguage())
					+ "\" _key=\"subwflink\" column=\"requestid\" orderkey=\"t1.requestid\"  linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_self\" transmethod=\"weaver.general.WorkFlowTransMethod.getSubWFLink\"  otherpara=\""
					+ user.getLanguage() + "\"/>";
				tableString += "<col width=\"15%\" display=\"" + hasrequestmark
					+ "\" orderkey=\"t1.requestmark\"  text=\"" + SystemEnv.getHtmlLabelName(19502, user.getLanguage())
					+ "\" column=\"requestmark\"/>";
				tableString += "			</head>" + "</table>";
			} else if ("myall".equals(myrequest) || "myreqeustbywftype".equals(myrequest) || "myreqeustbywfid".equals(myrequest)) {
				tableString = " <table instanceid=\"workflowRequestListTable\" tabletype=\"none\" pageId=\"" + pageId + "\" pageUid=\"" + pageUid
					+ "\" cssHandler=\"com.weaver.cssRenderHandler.request.CheckboxColorRender\" pagesize=\""+ pageSize + "\" >";
				tableString += temptableString;
				tableString += operateString + "			<head>";

				tableString += temptablerowString;
				tableString += " <col width=\"19%\" display=\""+ hasrequestname + "\"  text=\""+ SystemEnv.getHtmlLabelName(1334, user.getLanguage())
					+ "\" column=\"requestname\" orderkey=\"t1.requestname\"  linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_fullwindow\" transmethod=\""+requestnamereflectclass+".getWfNewLinkWithTitle\"  otherpara=\""
					+ para2 + "\"  pkey=\"requestname+weaver.general.WorkFlowTransMethod.getWfNewLinkWithTitle\"/>";
				tableString += " <col width=\"10%\"  display=\""+ hasworkflowname + "\"  text=\""+ SystemEnv.getHtmlLabelName(259, user.getLanguage())
					+ "\" column=\"workflowid\" orderkey=\"t1.workflowid\" transmethod=\""+workflownamereflectmethod+"\" />";

				tableString += " <col width=\"6%\" display=\""+ hascreater +"\" text=\""+ SystemEnv.getHtmlLabelName(86, user.getLanguage())
					+ "\" column=\"creater\" orderkey=\"t1.creater\"  otherpara=\"column:creatertype\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultName\" />";
				tableString += "<col width=\"10%\" display=\""+ hascreatedate +"\" text=\""+ SystemEnv.getHtmlLabelName(722, user.getLanguage())
					+ "\" column=\"createdate\" orderkey=\"t1.createdate,t1.createtime\" otherpara=\"column:createtime\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCreateTime\" />";
				tableString += " <col width=\"8%\" display=\""+ hasrequestlevel +"\" text=\""+ SystemEnv.getHtmlLabelName(15534, user.getLanguage())
					+ "\" column=\"requestlevel\"  orderkey=\"t1.requestlevel\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultUrgencyDegree\" otherpara=\""
					+ user.getLanguage() + "\"/>";

				tableString += " <col width=\"10%\" display=\""+ hasreceivetime + "\" text=\""+ SystemEnv.getHtmlLabelName(17994, user.getLanguage())
					+ "\" column=\"receivedate\" orderkey=\"receivedate,receivetime\" otherpara=\"column:receivetime\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCreateTime\" />";
				tableString += " <col width=\"8%\" display=\""+ hascurrentnode + "\" text=\""+ SystemEnv.getHtmlLabelName(18564, user.getLanguage())
					+ "\" column=\"currentnodeid\" orderkey=\"t1.currentnodeid\" transmethod=\"weaver.general.WorkFlowTransMethod.getCurrentNode\"/>";

				tableString += " <col width=\"8%\" display=\"" + hasstatus + "\" text=\""+ SystemEnv.getHtmlLabelName(1335, user.getLanguage())
					+ "\" column=\"status\" orderkey=\"t1.status\" />";
				tableString += " <col width=\"15%\" display=\"" + hasreceivedpersons + "\" text=\""+ SystemEnv.getHtmlLabelName(16354, user.getLanguage()) 
					+ "\" _key=\"unoperators\" column=\"requestid\"  otherpara=\"" + para4 + "\" transmethod=\"weaver.general.WorkFlowTransMethod.getUnOperators\"/>";
				tableString += "<col width=\"6%\" display=\"false\"  text=\""+ SystemEnv.getHtmlLabelName(19363, user.getLanguage())
					+ "\" _key=\"subwflink\" column=\"requestid\" orderkey=\"t1.requestid\"  linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_self\" transmethod=\"weaver.general.WorkFlowTransMethod.getSubWFLink\"  otherpara=\""
					+ user.getLanguage() + "\"/>";
				tableString += "<col width=\"15%\" display=\"" + hasrequestmark + "\" text=\"" + SystemEnv.getHtmlLabelName(19502, user.getLanguage()) 
					+ "\" orderkey=\"t1.requestmark\" column=\"requestmark\"/>";
				tableString += "			</head>" + "</table>";
			} else {
				tableString = " <table instanceid=\"workflowRequestListTable\" tabletype=\"none\" pageId=\"" + pageId + "\" pageUid=\"" + pageUid
					+ "\" cssHandler=\"com.weaver.cssRenderHandler.request.CheckboxColorRender\" pagesize=\""+ pageSize + "\" >";
				tableString += temptableString;
				tableString += operateString + "			<head>";
				tableString += temptablerowString;
				tableString += " <col width=\"19%\" display=\""+ hasrequestname + "\"  text=\""+ SystemEnv.getHtmlLabelName(1334, user.getLanguage())
					+ "\" column=\"requestname\" orderkey=\"requestname\"  linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_fullwindow\" transmethod=\""+requestnamereflectclass+".getWfNewLinkWithTitle\"  otherpara=\""
					+ para2 + "\"  pkey=\"requestname+weaver.general.WorkFlowTransMethod.getWfNewLinkWithTitle\"/>";
				tableString += " <col width=\"10%\"  display=\""+ hasworkflowname + "\"  text=\""+ SystemEnv.getHtmlLabelName(259, user.getLanguage())
					+ "\" column=\"workflowid\" orderkey=\"t1.workflowid\" transmethod=\""+workflownamereflectmethod+"\" />";

				tableString += " <col width=\"6%\" display=\""+ hascreater + "\"  text=\""+ SystemEnv.getHtmlLabelName(882, user.getLanguage())
					+ "\" column=\"creater\" orderkey=\"creater\"  otherpara=\"column:creatertype\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultName\" />";
				tableString += " <col width=\"10%\" display=\""+ hascreatedate+ "\" text=\""+ SystemEnv.getHtmlLabelName(722, user.getLanguage())
					+ "\" column=\"createdate\" orderkey=\"t1.createdate,t1.createtime\" otherpara=\"column:createtime\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCreateTime\" />";
				tableString += " <col width=\"8%\" display=\""+ hasrequestlevel+ "\" text=\""+ SystemEnv.getHtmlLabelName(15534, user.getLanguage())
					+ "\" column=\"requestlevel\"  orderkey=\"t1.requestlevel\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultUrgencyDegree\" otherpara=\""
					+ user.getLanguage() + "\"/>";

				tableString += " <col width=\"10%\" display=\""+ hasreceivetime + "\"  text=\""+ SystemEnv.getHtmlLabelName(17994, user.getLanguage())
					+ "\" column=\"receivedate\" orderkey=\"receivedate,receivetime\" otherpara=\"column:receivetime\" transmethod=\"weaver.general.WorkFlowTransMethod.getWFSearchResultCreateTime\" />";
				tableString += " <col width=\"8%\" display=\""+ hascurrentnode + "\"   text=\""+ SystemEnv.getHtmlLabelName(18564, user.getLanguage())
					+ "\" column=\"currentnodeid\" orderkey=\"t1.currentnodeid\" transmethod=\"weaver.general.WorkFlowTransMethod.getCurrentNode\"/>";

				tableString += " <col width=\"8%\" display=\"" + hasstatus + "\"    text=\""+ SystemEnv.getHtmlLabelName(1335, user.getLanguage())
					+ "\" column=\"status\" orderkey=\"t1.status\" />";
				tableString += " <col width=\"15%\" display=\"" + hasreceivedpersons + "\"   text=\""+ SystemEnv.getHtmlLabelName(16354, user.getLanguage()) 
					+ "\" _key=\"unoperators\" column=\"requestid\"  otherpara=\""
					+ para4 + "\" transmethod=\"weaver.general.WorkFlowTransMethod.getUnOperators\"/>";
				tableString += "<col width=\"6%\" display=\"false\"  text=\""+ SystemEnv.getHtmlLabelName(19363, user.getLanguage())
					+ "\" _key=\"subwflink\" column=\"requestid\" orderkey=\"t1.requestid\"  linkkey=\"requestid\" linkvaluecolumn=\"requestid\" target=\"_self\" transmethod=\"weaver.general.WorkFlowTransMethod.getSubWFLink\"  otherpara=\""
					+ user.getLanguage() + "\"/>";
				tableString += "<col width=\"15%\" display=\"" + hasrequestmark
					+ "\" orderkey=\"t1.requestmark\" text=\"" + SystemEnv.getHtmlLabelName(19502, user.getLanguage())
					+ "\" column=\"requestmark\"/>";
				tableString += "			</head>" + "</table>";
			}
		}
		//System.err.println("tableString---"+tableString);
		//String sessionkey = Util.getEncrypt(Util.getRandom());
		//session.setAttribute(sessionkey, tableString);
		
		String sessionkey = pageUid+"_"+Util.getEncrypt(Util.getRandom());
		Util_TableMap.setVal(sessionkey, tableString);
		
		//批量提交是否需要签字意见
		int multisubmitnotinputsign = 0;
		if(isMultiSubmit && iswaitdo == 1){
			RecordSet.executeSql("select multisubmitnotinputsign from workflow_RequestUserDefault where userId = "+ user.getUID());
			if(RecordSet.next())
				multisubmitnotinputsign = Util.getIntValue(Util.null2String(RecordSet.getString("multisubmitnotinputsign")), 0);
		}
		
		Map<String,String> sharearg = new HashMap<String,String>();
		sharearg.put("flag", flag);
		sharearg.put("overtimetype", overtimetype);
		sharearg.put("multisubmitnotinputsign", multisubmitnotinputsign+"");
		if(isMultiSubmit && iswaitdo == 1 && sysId != 5 && sysId != 8)
			sharearg.put("hasBatchBtn", "true");
		
		apidatas.put("sessionkey", sessionkey);
		apidatas.put("sharearg", sharearg);
		return apidatas;
	}
	
	
}
