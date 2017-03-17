package com.api.workflow.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import weaver.conn.RecordSet;
import weaver.crm.Maint.CustomerInfoComInfo;
import weaver.docs.category.SecCategoryComInfo;
import weaver.docs.docs.DocImageManager;
import weaver.docs.docs.DocReadTagUtil;
import weaver.general.BaseBean;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.User;
import weaver.hrm.company.DepartmentComInfo;
import weaver.hrm.resource.ResourceComInfo;
import weaver.systeminfo.SystemEnv;
import weaver.workflow.mode.FieldInfo;
import weaver.workflow.report.ReportAuthorization;
import weaver.workflow.request.RequestLogOperateName;
import weaver.workflow.request.RequestRemarkRight;
import weaver.workflow.request.SubWorkflowManager;
import weaver.workflow.request.WFLinkInfo;
import weaver.workflow.request.WFShareAuthorization;
import weaver.workflow.workflow.WFManager;
import weaver.workflow.workflow.WFSubDataAggregation;
import weaver.workflow.workflow.WorkflowRequestComInfo;

import com.alibaba.fastjson.JSON;
import com.api.workflow.util.ServiceUtil;

public class RequestLogService extends BaseBean{

    private HttpServletRequest request;
    private HttpServletResponse response;

    // request params
    private int requestid;
    private User user;
    private int workflowid;
    private int nodeid;

    private int desrequestid;
    private String isurger;
    private boolean isworkflowhtmldoc;
    private boolean isprint;
    private Map<String, Object> requestLogDatas;
    private RecordSet recordSet;
    private String f_weaver_belongto_userid;
    private String f_weaver_belongto_usertype;
    private HttpSession session;

    private ResourceComInfo ResourceComInfo;
    private DepartmentComInfo DepartmentComInfo;
    private CustomerInfoComInfo CustomerInfoComInfo;
    private String loadmethod;

    private void init() {
        session = request.getSession();
        workflowid = Util.getIntValue(request.getParameter("workflowid"), 0);
        nodeid = Util.getIntValue(request.getParameter("nodeid"), 0);
        isworkflowhtmldoc = "1".equals(Util.null2String(session.getAttribute("isworkflowhtmldoc" + requestid)));
        desrequestid = Util.getIntValue(request.getParameter("desrequestid"), 0);
        isurger = Util.null2String(request.getParameter("isurger"));
        isprint = "true".equalsIgnoreCase(Util.null2String(request.getParameter("isprint")));
        //门户查看签字意见
        if("portal".equals(loadmethod)){
    		String sql = "select t.nodeid,t.workflowid from workflow_currentoperator t left join workflow_nodebase t1 on t.nodeid  = t1.id  where t.requestid=? and t.userid=? and t.usertype=? order by t.id desc";
    		int usertype = "2".equals(user.getLogintype()) ? 1 : 0;
    		recordSet.executeQuery(sql, requestid, user.getUID(), usertype);
    		if (recordSet.next()) {
    			nodeid = Util.getIntValue(recordSet.getString(1), 0);
    			workflowid = Util.getIntValue(recordSet.getString(2), 0);
    		}
    		if (nodeid < 1) {
    			sql = "select t.currentnodeid from workflow_requestbase t left join workflow_nodebase t1 on t.currentnodeid = t1.id  where t.requestid= ?";
    			recordSet.executeQuery(sql, requestid);
    			if (recordSet.next()) {
    				nodeid = Util.getIntValue(recordSet.getString(1), 0);
    			}
    		}
        }

        requestLogDatas = new HashMap<String, Object>();
        try {
            ResourceComInfo = new ResourceComInfo();
            DepartmentComInfo = new DepartmentComInfo();
            CustomerInfoComInfo = new CustomerInfoComInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RequestLogService(HttpServletRequest request, HttpServletResponse response, boolean needinit) {
        this.request = request;
        this.response = response;
        recordSet = new RecordSet();
        requestid = Util.getIntValue(request.getParameter("requestid"));
        f_weaver_belongto_userid = request.getParameter("f_weaver_belongto_userid");
        f_weaver_belongto_usertype = request.getParameter("f_weaver_belongto_usertype");
        user = HrmUserVarify.getUser(request, response, f_weaver_belongto_userid, f_weaver_belongto_usertype);
        loadmethod = Util.null2String(request.getParameter("loadmethod"));
        if(needinit)
        	init();
    }

    public Map<String, Object> loadRequestLogInfo() throws Exception {
        WFManager wfManager = new WFManager();
        int userid  = user.getUID();
        long start = System.currentTimeMillis();
        boolean isdebug = (userid==8 || userid==80 || userid==1215||userid==1348||userid==3724||userid==4548);
		if(isdebug){
			System.out.println("requestlog-111-requestid-"+requestid+"-userid-"+userid+"-"+ (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
		}
        
        // 判断意见区域是否不显示
        String isHideArea = "0";
        String thisHideInputTemp = "";
        // String isHideInput = "0";
        recordSet.executeSql("select ishidearea,ishideinput,ismode from workflow_flownode where workflowId=" + workflowid + " and nodeId=" + nodeid);
        if (recordSet.next()) {
            isHideArea = "" + Util.getIntValue(recordSet.getString("ishidearea"), 0);
            thisHideInputTemp = Util.null2String(recordSet.getString("ishideinput"));
        }

        requestLogDatas.put("isHideInput", thisHideInputTemp);
        requestLogDatas.put("isHideArea", isHideArea);

        if ("0".equals(isHideArea)) {
    		if(isdebug){
    			System.out.println("requestlog-112-requestid-"+requestid+"-userid-"+userid+"-"+ (System.currentTimeMillis() - start));
    			start = System.currentTimeMillis();
    		}
            SubWorkflowManager.loadRelatedRequest(request);
    		if(isdebug){
    			System.out.println("requestlog-113-requestid-"+requestid+"-userid-"+userid+"-"+ (System.currentTimeMillis() - start));
    			start = System.currentTimeMillis();
    		}

            int initrequestid = requestid;
            if(!"portal".equals(loadmethod)){
            	loadShowTabCondition(initrequestid);
            }
    		if(isdebug){
    			System.out.println("requestlog-114-requestid-"+requestid+"-userid-"+userid+"-"+ (System.currentTimeMillis() - start));
    			start = System.currentTimeMillis();
    		}

            requestid = initrequestid;
            List<String> canViewIds = loadCanViewIds(wfManager);
            
    		if(isdebug){
    			System.out.println("requestlog-115-requestid-"+requestid+"-userid-"+userid+"-"+ (System.currentTimeMillis() - start));
    			start = System.currentTimeMillis();
    		}

            String viewLogIds = "";
            // 流程共享查看签字意见end
            String nodeids="-1";
            if (canViewIds.size() > 0) {
                for (int a = 0; a < canViewIds.size(); a++) {
                    viewLogIds += (String) canViewIds.get(a) + ",";
                    nodeids=nodeids.concat("," + canViewIds.get(a));
                }
                viewLogIds = viewLogIds.substring(0, viewLogIds.length());
            } else {
                viewLogIds = "-1";
            }
            
            List<Map<String,Object>> viewnodes = new ArrayList<Map<String,Object>>();
            recordSet.executeSql("select id,nodename from workflow_nodebase where id in ("+nodeids+") order by id");
            while(recordSet.next()){
            	Map<String,Object> nodeinfo =  new HashMap<String,Object>();
            	nodeinfo.put("id", recordSet.getString("id"));
            	nodeinfo.put("name", recordSet.getString("nodename"));
            	viewnodes.add(nodeinfo);
            }
            
            requestLogDatas.put("viewnodes", viewnodes);
            requestLogDatas.put("viewLogIds", viewLogIds);

            loadWfRelatedParams(wfManager);
            requestid = initrequestid;
            
    		if(isdebug){
    			System.out.println("requestlog-116-requestid-"+requestid+"-userid-"+userid+"-"+ (System.currentTimeMillis() - start));
    			start = System.currentTimeMillis();
    		}

            // 与我相关的签字意见数 e8标准已去掉 
            /*       
     		RequestSignRelevanceWithMe reqsignwm = new RequestSignRelevanceWithMe();
            String logids = reqsignwm.getRelevanceinfo(workflowid + "", requestid + "", user.getUID() + "");
            if (logids != null && logids.length() > 0) {
                WFLinkInfo wfLinkInfo = new WFLinkInfo();
                wfLinkInfo.setRequest(request);
                int atcount = wfLinkInfo.getRequestLogTotalCount(requestid, workflowid, viewLogIds, " and t1.logid in (" + logids + ")");
                requestLogDatas.put("atcount", atcount);
            }
            */
        }

        return requestLogDatas;
    }

    private void loadShowTabCondition(int initrequestid) {
        List<String> canviewwf = new ArrayList<String>();
        List<SignRequestInfo> allrequestInfos =  new ArrayList<SignRequestInfo>();
        int mainrequestid = 0;
        String subWfSetId = "0";
        String isDiff = "";

        String canviewworkflowid = "-1";
        boolean hasMainReq = false;
        boolean hasChildReq = false;
        boolean hasParallelReq = false;

        boolean hasOldMainReq = false;
        boolean hasOldChildReq = false;
        boolean hasOldParallelReq = false;

        String isReadMain = "0"; // 子流程是否可查看主流程签字意见
        String isReadMainNodes = ""; // 子流程可查看主流程签字意见的范围
        String isReadParallel = "0"; // 平行流程是否可查看签字意见
        String isReadParallelNodes = ""; // 平行流程是否可查看签字意见的范围

        /* 查询当前请求的主请求 */
        recordSet.executeSql("select sub.subwfid,sub.isSame,sub.mainrequestid,req.requestname,req.workflowid from workflow_subwfrequest sub left join workflow_requestbase req on req.requestid=sub.mainrequestid where sub.subrequestid=" + requestid);
        if (recordSet.next()) {
            if (recordSet.getInt("mainrequestid") > -1) {
                subWfSetId = Util.null2String(recordSet.getString("subwfid"));
                isDiff = Util.null2String(recordSet.getString("isSame"));
                mainrequestid = recordSet.getInt("mainrequestid");
                SignRequestInfo _srequestinfo  = new SignRequestInfo();
                _srequestinfo.setRequestid(String.valueOf(mainrequestid));
                _srequestinfo.setRequestname(recordSet.getString("requestname"));
                _srequestinfo.setRelwfid(recordSet.getString("workflowid"));
                
                _srequestinfo.setType("main");
                allrequestInfos.add(_srequestinfo);
                hasMainReq = true;
            }
        }

        String mainworkflowid_temp = "";
        // 老数据
        if (!hasMainReq) {
            recordSet.executeSql("select requestname,mainrequestid from workflow_requestbase where requestid = " + requestid);
            if (recordSet.next()) {
                if (recordSet.getInt("mainrequestid") > -1) {
                    mainrequestid = recordSet.getInt("mainrequestid");
                    recordSet.executeSql("select workflowid,requestname from workflow_requestbase where requestid = " + mainrequestid);
                    if (recordSet.next()) {
                        mainworkflowid_temp = recordSet.getString("workflowid");
                        String reqname2 = recordSet.getString("requestname");
                        int mainworkflowid = -1;
                        recordSet.executeSql("select workflowid from workflow_requestbase where requestid = " + requestid);
                        if (recordSet.next()) {
                            mainworkflowid = recordSet.getInt("workflowid");
                        }
                        recordSet.executeSql("select 1 from Workflow_SubwfSet where mainworkflowid = " + mainworkflowid_temp + " and subworkflowid =" + workflowid
                                + " and isread = 1 union select 1 from Workflow_TriDiffWfDiffField a, Workflow_TriDiffWfSubWf b where a.id=b.triDiffWfDiffFieldId and b.isRead=1 and a.mainworkflowid=" + mainworkflowid + " and b.subWorkflowId=" + workflowid);
                        if (recordSet.next()) {
                            SignRequestInfo _srequestinfo  = new SignRequestInfo();
                            _srequestinfo.setRequestid(String.valueOf(mainrequestid));
                            _srequestinfo.setRequestname(reqname2);
                            _srequestinfo.setType("main");
                            _srequestinfo.setRelwfid(mainworkflowid_temp);
                            allrequestInfos.add(_srequestinfo);
                            
                            hasMainReq = true;
                            hasOldMainReq = true;
                        }
                    }
                }
            }
        }
        if ("".equals(mainworkflowid_temp)) {
            mainworkflowid_temp = "-1";
        }
        // mainworkflowid_temp
        recordSet.executeSql("select distinct subworkflowid from Workflow_SubwfSet where mainworkflowid in (" + mainworkflowid_temp + ", " + workflowid + ") and isread = 1 ");
        while (recordSet.next()) {
            canviewworkflowid += "," + recordSet.getString("subworkflowid");
        }

        recordSet.executeSql("select distinct b.subworkflowid from Workflow_TriDiffWfDiffField a, Workflow_TriDiffWfSubWf b where a.id=b.triDiffWfDiffFieldId and b.isRead=1 and a.mainworkflowid in (" + mainworkflowid_temp + "," + workflowid + ")");
        while (recordSet.next()) {
            canviewworkflowid += "," + recordSet.getString("subworkflowid");
        }

        /* 如果当前请求拥有主请求，则才会拥有平行请求。平行请求是指：同一个主请求在同一触发设置中触发的其他请求 */
        /* 查询当前请求的平行请求 */
        if (hasMainReq) {
            recordSet.executeSql(" select sub.subrequestid requestid,req.requestname,req.workflowid from workflow_subwfrequest sub left join workflow_requestbase req on req.requestid=sub.subrequestid where sub.mainrequestid=" + mainrequestid + " and sub.subwfid=" + subWfSetId
                    + " and sub.subrequestid <> " + initrequestid);
            while (recordSet.next()) {
                SignRequestInfo _srequestinfo  = new SignRequestInfo();
                _srequestinfo.setRequestid(recordSet.getString("requestid"));
                _srequestinfo.setRequestname(recordSet.getString("requestname"));
                _srequestinfo.setRelwfid(recordSet.getString("workflowid"));
                _srequestinfo.setType("parallel");
                allrequestInfos.add(_srequestinfo);
                
                canviewwf.add(recordSet.getString("requestid"));
                hasParallelReq = true;
            }

            if(!"-1".equals(mainworkflowid_temp)){
	            recordSet.executeSql("select requestid,requestname,workflowid from workflow_requestbase where mainrequestid = " + mainworkflowid_temp);
	            while (recordSet.next()) {
	            	if((","+canviewworkflowid+",").contains(recordSet.getString("workflowid"))){
	            		SignRequestInfo _srequestinfo  = new SignRequestInfo();
	            		_srequestinfo.setRequestid(recordSet.getString("requestid"));
	            		_srequestinfo.setRequestname(recordSet.getString("requestname"));
	            		_srequestinfo.setRelwfid(recordSet.getString("workflowid"));
	            		_srequestinfo.setType("parallel");
	            		if(allrequestInfos.contains(_srequestinfo)){
	            			continue;
	            		}
	            		allrequestInfos.add(_srequestinfo);
	            		
	            		canviewwf.add(recordSet.getString("requestid"));
	            		if (!(initrequestid + "").equals(recordSet.getString("requestid"))) {
	            			hasParallelReq = true;
	            			hasOldParallelReq = true;
	            		}
	            	}
	            }
            }
        }

        /* 查询主流程和平行流程查看范围 */
        if (hasMainReq && !hasOldMainReq) {
            /* 触发不同流程和相同流程的配置不在同一张表中，需要判断后查询 */
            if ("1".equals(isDiff)) {
                recordSet.executeSql("select isreadMainWfNodes,isreadMainwf, isreadParallelwfNodes,isreadParallelwf,subworkflowid from workflow_tridiffwfsubwf where id = " + subWfSetId);
            } else {
                recordSet.executeSql("select isreadMainWfNodes,isreadMainwf, isreadParallelwfNodes,isreadParallelwf,subworkflowid from workflow_subwfset where id = " + subWfSetId);
            }
            if (recordSet.next()) {
                isReadMain = Util.null2String(recordSet.getString("isreadMainwf"));
                isReadMainNodes = Util.null2String(recordSet.getString("isreadMainWfNodes"));
                isReadParallel = Util.null2String(recordSet.getString("isreadParallelwf"));
                isReadParallelNodes = Util.null2String(recordSet.getString("isreadParallelwfNodes"));
                String subworkflowid = Util.null2String(recordSet.getString("subworkflowid"));
                if("all".equals(isReadParallelNodes)){
                	recordSet.executeSql("select nodeid from workflow_flownode where workflowid  = " +subworkflowid );
                	String subworkflownodeids  = "-1";
                	while(recordSet.next()){
                		subworkflownodeids += ","+recordSet.getString("nodeid");
                	}
                	isReadParallelNodes = subworkflownodeids;
                }
            }
        }

        /* 查询当前请求的子请求 */
        recordSet.executeSql("select sub.subwfid,sub.isSame,sub.subrequestid requestid,req.requestname,req.workflowid from workflow_subwfrequest sub left join workflow_requestbase req on req.requestid=sub.subrequestid where sub.mainrequestid='" + initrequestid + "' order by sub.subrequestid desc");
        Map<String, String> triggerIsDiffMap = new HashMap<String, String>();
        Map<String, String> requestSettingMap = new HashMap<String, String>();
        while (recordSet.next()) {
        	SignRequestInfo _srequestinfo  = new SignRequestInfo();
            _srequestinfo.setRequestid(recordSet.getString("requestid"));
            _srequestinfo.setRequestname(recordSet.getString("requestname"));
            _srequestinfo.setRelwfid(recordSet.getString("workflowid"));
            _srequestinfo.setType("sub");
            allrequestInfos.add(_srequestinfo);
        	
            canviewwf.add(recordSet.getString("requestid"));
            hasChildReq = true;

            subWfSetId = Util.null2String(recordSet.getString("subwfid"));
            isDiff = Util.null2String(recordSet.getString("isSame"));

            requestSettingMap.put(recordSet.getString("requestid"), subWfSetId);
            triggerIsDiffMap.put(subWfSetId, isDiff);
        }
        /** 161014 zzw 添加判断 * */
        if (requestid > 0 && !"-1".equals(canviewworkflowid)) {
            recordSet.executeSql("select requestid,requestname,workflowid from workflow_requestbase where mainrequestid = " + requestid + " and workflowid in (" + canviewworkflowid + ")");
            while (recordSet.next()) {
                SignRequestInfo _srequestinfo  = new SignRequestInfo();
                _srequestinfo.setRequestid(recordSet.getString("requestid"));
                _srequestinfo.setRequestname(recordSet.getString("requestname"));
                _srequestinfo.setRelwfid(recordSet.getString("workflowid"));
                _srequestinfo.setType("sub");
                if(allrequestInfos.contains(_srequestinfo)){
                	continue;
                }
                allrequestInfos.add(_srequestinfo);

                canviewwf.add(recordSet.getString("requestid"));
                hasChildReq = true;
                hasOldChildReq = true;
            }
        }

        //加载签字意见查看范围
        List<TriggerSetting> _triggerSettings = new ArrayList<TriggerSetting>();
        Map<String, TriggerSetting> triggerSettingMap = new HashMap<String, TriggerSetting>();
        boolean canreadsubreqsign = hasOldChildReq;
        if (hasChildReq) {
            Iterator<String> _settingIds = triggerIsDiffMap.keySet().iterator();
            while (_settingIds.hasNext()) {
                String _triggerSettingId = _settingIds.next();
                String _isDiff = triggerIsDiffMap.get(_triggerSettingId);

                /* 触发不同流程和相同流程的配置不在同一张表中，需要判断后查询 */
                if ("1".equals(_isDiff)) {
                    recordSet.executeSql("select id,isreadNodes,isread,subworkflowid from workflow_tridiffwfsubwf  where id = " + _triggerSettingId);
                } else {
                    recordSet.executeSql("select id,isreadNodes,isread,subworkflowid from workflow_subwfset where id = " + _triggerSettingId);
                }

                if (recordSet.next()) {
                    String _settingId = Util.null2String(recordSet.getString("id"));
                    // 主流程是否可查看子流程签字意见
                    String _isRead = Util.null2String(recordSet.getString("isread"));
                    // 主流程可查看子流程签字意见的范围
                    String _isReadNodes = Util.null2String(recordSet.getString("isreadNodes"));
                    // 子流程id
                    String subworkflowid = Util.null2String(recordSet.getString("subworkflowid"));
                    
                    if("all".equals(_isReadNodes)){
                    	recordSet.executeSql("select nodeid from workflow_flownode where workflowid  = " +subworkflowid );
                    	_isReadNodes = "-1";
                    	while(recordSet.next()){
                    		_isReadNodes += ","+recordSet.getString("nodeid");
                    	}
                    }
                    
                    TriggerSetting _triggerSetting = new TriggerSetting();
                    _triggerSetting.setSettingId(_settingId);
                    _triggerSetting.setIsRead(_isRead);
                    _triggerSetting.setIsReadNodes(_isReadNodes);

                    _triggerSettings.add(_triggerSetting);
                }
            }

            // 将list转化为，方便通过settingId直接获取setting
            for (int i = 0; i < _triggerSettings.size(); i++) {
                TriggerSetting _triggerSetting = _triggerSettings.get(i);
                if("1".equals(_triggerSetting.getIsRead()))  canreadsubreqsign = true;
                triggerSettingMap.put(_triggerSetting.getSettingId(), _triggerSetting);
            }
        }
        
        // 签字意见tab页控制
        // 与我相关
        boolean isRelatedTome = !(isprint || isworkflowhtmldoc);
        // 主流程
        boolean hasMainWfRight = hasMainReq && (("1".equals(isReadMain) && !(isprint || isworkflowhtmldoc)) || hasOldMainReq);
        // 子流程
        boolean hasChildWfRight = (hasChildReq && !(isprint || isworkflowhtmldoc)) && canreadsubreqsign;
        // 平行流程
        boolean hasParallelWfRight = hasParallelReq && ("1".equals(isReadParallel) || hasOldParallelReq) && !(isprint || isworkflowhtmldoc);
        //加载主子流程相关信息
        
        Iterator<SignRequestInfo> it =  allrequestInfos.iterator();
        while(it.hasNext()){
        	  SignRequestInfo signRequestInfo = it.next();
		      int temprequestid = Util.getIntValue(signRequestInfo.getRequestid());
		      String type = signRequestInfo.getType();
		      String signshowname = "";
		      String canReadNodes = "-1";
		      if(("main".equals(type) && !hasMainWfRight) || ("sub".equals(type) && !hasChildWfRight) || ("parallel".equals(type) && !hasParallelWfRight)){
		    	  it.remove();
		      }else{
			      if("main".equals(type)){
			    	  signshowname = SystemEnv.getHtmlLabelName(21254,user.getLanguage());
			    	  signshowname += (" " + "<a href=javaScript:openFullWindowHaveBar('/workflow/request/ViewRequest.jsp");
			    	  signshowname += ("?requestid="+temprequestid+"&relaterequest="+initrequestid+"&isrequest=3&isovertime=0&desrequestid="+requestid+"')>");
			    	  signshowname +=" " + signRequestInfo.getRequestname() +"</a>";
			    	  signshowname +=" "+SystemEnv.getHtmlLabelName(504,user.getLanguage())+":";
			          
			    	  if(isReadMain.equals("1")){
			              if(isReadMainNodes.equals("all")){
			    		      recordSet.executeSql("select distinct nodeid from workflow_requestlog where requestid = "+temprequestid);
			    		      String tempviewLogIds = "";
			    		      while(recordSet.next()){
			    		          tempviewLogIds += recordSet.getString("nodeid")+",";
			    		      }
			    		      tempviewLogIds +="-1";
			              	  canReadNodes = tempviewLogIds;
			              }else{
			              	  canReadNodes = isReadMainNodes;
			              }
			          }
			          if (hasOldMainReq) {
					      recordSet.executeSql("select distinct nodeid from workflow_requestlog where requestid = "+temprequestid);
					      String tempviewLogIds = "";
					      while(recordSet.next()){
					          tempviewLogIds += recordSet.getString("nodeid")+",";
					      }
					      tempviewLogIds +="-1";
			              canReadNodes = tempviewLogIds;
			          }
			      }else if("sub".equals(type)){
				      String _triggerSettingId = requestSettingMap.get("" + temprequestid);   
				      if (_triggerSettingId != null && !"".equals(_triggerSettingId)) {
				          TriggerSetting _triggerSetting = triggerSettingMap.get(_triggerSettingId);
				          if(_triggerSetting != null && _triggerSetting.getIsRead().equals("1")){
				        	  /*可读时才显示列表*/
				              canReadNodes = _triggerSetting.getIsReadNodes();
				              signshowname = SystemEnv.getHtmlLabelName(19344,user.getLanguage());
				              signshowname += (" " + "<a href=javaScript:openFullWindowHaveBar('/workflow/request/ViewRequest.jsp");
				              signshowname += ("?requestid="+temprequestid+"&relaterequest="+initrequestid+"&isrequest=2&isovertime=0&desrequestid="+requestid+"')>");
				              signshowname +=" " + signRequestInfo.getRequestname().toString()+"</a>";
				              signshowname +=" " + SystemEnv.getHtmlLabelName(504,user.getLanguage());
				          }
				      } else {
				    	  	it.remove();
				    	  	continue;
				      }
			    	  
			      }else if("parallel".equals(type)){
			    	  signshowname = SystemEnv.getHtmlLabelName(21255,user.getLanguage());
			    	  signshowname += (" " + "<a href=javaScript:openFullWindowHaveBar('/workflow/request/ViewRequest.jsp");
			    	  signshowname += ("?requestid="+temprequestid+"&relaterequest="+initrequestid+"&isrequest=4&isovertime=0&desrequestid="+requestid+"')>");
			    	  signshowname +=" " + signRequestInfo.getRequestname()+"</a>";
			    	  signshowname +=" " + SystemEnv.getHtmlLabelName(504,user.getLanguage());
			          
			          if(isReadParallel.equals("1")){
		              	  canReadNodes = isReadParallelNodes;
			          }
			      }
			      
			      signRequestInfo.setRelviewlogs(canReadNodes);
			      signRequestInfo.setSignshowname(signshowname);
		      }
        }

        requestLogDatas.put("isRelatedTome", isRelatedTome);
        requestLogDatas.put("hasMainWfRight", hasMainWfRight);
        requestLogDatas.put("hasChildWfRight", hasChildWfRight);
        requestLogDatas.put("hasOldChildReq", hasOldChildReq);
        //requestLogDatas.put("_triggerSettings", _triggerSettings);
        requestLogDatas.put("hasParallelWfRight", hasParallelWfRight);
        requestLogDatas.put("hasMainReq", hasMainReq);
        requestLogDatas.put("hasMainReq", hasMainReq);
        requestLogDatas.put("isReadMain", isReadMain);
        requestLogDatas.put("hasChildReq", hasChildReq);
        requestLogDatas.put("hasOldMainReq", hasOldMainReq);
        requestLogDatas.put("hasParallelReq", hasParallelReq);
        requestLogDatas.put("isReadParallel", isReadParallel);
        requestLogDatas.put("hasOldParallelReq", hasOldParallelReq);
        requestLogDatas.put("isReadMainNodes", isReadMainNodes);
        requestLogDatas.put("isReadParallelNodes", isReadParallelNodes);
        requestLogDatas.put("allrequestInfos", allrequestInfos);
    }

    /**
     * 加载流程相关参数
     * 
     * @param requestLogDatas
     * @param recordSet
     * @param wfManager
     * @throws Exception
     */
    private void loadWfRelatedParams(WFManager wfManager) throws Exception {
        // 获取是否开启签章，启用签章时，禁用引用按钮
        String tempIsFormSignature = null;
        recordSet.executeSql("select isFormSignature from workflow_flownode where workflowId=" + workflowid + " and nodeId=" + nodeid);
        if (recordSet.next()) {
            tempIsFormSignature = Util.null2String(recordSet.getString("isFormSignature"));
        }
        requestLogDatas.put("isFormSignature", tempIsFormSignature);

        String sqlTemp = "select nodeid from workflow_flownode where workflowid = " + workflowid + " and nodetype = '0'";
        recordSet.executeSql(sqlTemp);
        recordSet.next();
        String creatorNodeId = recordSet.getString("nodeid");
        requestLogDatas.put("creatorNodeId", creatorNodeId);

        // -----------------------------------
        // 预留流程签字意见每次加载条数 START
        // -----------------------------------
        boolean issplitload = Util.null2String(request.getParameter("loadmethod")).equals("split");	//是否分页加载
        int wfsignlddtcnt = 10;
        if(issplitload){
	        recordSet.executeSql("select pageSize from ecology_pagesize where pageId = 'SIGNVIEW_VIEWID' and userid=" + user.getUID());
	        if (recordSet.next())
	            wfsignlddtcnt = recordSet.getInt("pageSize");
        }else{
        	wfsignlddtcnt = 14;
        }
        if (isprint || isworkflowhtmldoc) {
            wfsignlddtcnt = Integer.MAX_VALUE;
        }
        
        if("portal".equals(loadmethod)){
        	wfsignlddtcnt = Util.getIntValue(request.getParameter("wfsignlddtcnt"),5);
        }
        
        requestLogDatas.put("wfsignlddtcnt", wfsignlddtcnt);

        String txStatus = "1";// 默认显示头像
        recordSet.executeSql("select status from WorkflowSignTXStatus where userid=" + user.getUID());
        if (recordSet.next()) {
            txStatus = recordSet.getString("status");
        }
        requestLogDatas.put("txStatus", txStatus);

        wfManager.setWfid(workflowid);
        wfManager.getWfInfo();
        String orderbytype = Util.null2String(wfManager.getOrderbytype());

        requestLogDatas.put("orderbytype", orderbytype);
    }

    /**
     * 加载可查看签字意见的节点ID
     * 
     * @param recordSet
     * @param wfManager
     * @return
     * @throws Exception
     */
    private List<String> loadCanViewIds(WFManager wfManager) throws Exception {
    	String reportid = Util.null2String((String) session.getAttribute(user.getUID() + "_" + requestid + "reportid"));
        RecordSet recordSet1 = new RecordSet();
        List<String> canViewIds = new ArrayList<String>();
        String viewNodeId = "-1";
        String tempNodeId = "-1";
        String singleViewLogIds = "-1";
        recordSet.executeSql("select nodeid from workflow_currentoperator where requestid=" + requestid + " and userid=" + user.getUID() + " order by receivedate desc ,receivetime desc");
        if (recordSet.next()) {
            viewNodeId = recordSet.getString("nodeid");
            recordSet1.executeSql("select viewnodeids from workflow_flownode where workflowid=" + workflowid + " and nodeid=" + viewNodeId);
            if (recordSet1.next()) {
                singleViewLogIds = recordSet1.getString("viewnodeids");
            }

            if ("-1".equals(singleViewLogIds)) {// 全部查看
                recordSet1.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowid + " and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid=" + requestid + "))");
                while (recordSet1.next()) {
                    tempNodeId = recordSet1.getString("nodeid");
                    if (!canViewIds.contains(tempNodeId)) {
                        canViewIds.add(tempNodeId);
                    }
                }
            } else if (singleViewLogIds == null || "".equals(singleViewLogIds)) {// 全部不能查看
            } else {// 查看部分
                String tempidstrs[] = Util.TokenizerString2(singleViewLogIds, ",");
                for (int i = 0; i < tempidstrs.length; i++) {
                    if (!canViewIds.contains(tempidstrs[i])) {
                        canViewIds.add(tempidstrs[i]);
                    }
                }
            }
        }

        String isfromreport = Util.null2String((String) session.getAttribute(user.getUID() + "_" + requestid + "isfromreport"));
        ReportAuthorization ru = new ReportAuthorization();
        if ("1".equals(isfromreport) && requestid != 0) {
            if (ru.checkUserReportPrivileges(reportid, String.valueOf(requestid), user)) {
                recordSet.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowid + " and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid=" + requestid + "))");
                while (recordSet.next()) {
                    tempNodeId = recordSet.getString("nodeid");
                    if (!canViewIds.contains(tempNodeId)) {
                        canViewIds.add(tempNodeId);
                    }
                }

            }
        }
        String isfromflowreport = Util.null2String((String) session.getAttribute(user.getUID() + "_" + requestid + "isfromflowreport"));
        if ("1".equals(isfromflowreport) && requestid != 0) {
            if (ru.checkFlowReport(reportid, String.valueOf(requestid), user)) {
                recordSet.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowid + " and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid=" + requestid + "))");
                while (recordSet.next()) {
                    tempNodeId = recordSet.getString("nodeid");
                    if (!canViewIds.contains(tempNodeId)) {
                        canViewIds.add(tempNodeId);
                    }
                }
            }
        }

        // 处理相关流程的查看权限
        String allsubrequestid = "";

        if (desrequestid != 0) {
            recordSet.executeSql("select workflowid from workflow_requestbase where requestid = " + desrequestid);
            if (recordSet.next()) {
                wfManager.setWfid(recordSet.getInt("workflowid"));
                wfManager.getWfInfo();
            }
            String issignview = wfManager.getIssignview();
            allsubrequestid = WFSubDataAggregation.getAllSubRequestIds(desrequestid);
            if ("1".equals(issignview)) {
                recordSet.executeSql("select  a.nodeid from  workflow_currentoperator a  where a.requestid=" + requestid + " and  exists (select 1 from workflow_currentoperator b where b.isremark in ('2','4') and b.requestid=" + desrequestid + "  and  a.userid=b.userid) and userid=" + user.getUID()
                        + " order by receivedate desc ,receivetime desc");
                if (recordSet.next()) {
                    viewNodeId = recordSet.getString("nodeid");
                    recordSet1.executeSql("select viewnodeids from workflow_flownode where workflowid=" + workflowid + " and nodeid=" + viewNodeId);
                    if (recordSet1.next()) {
                        singleViewLogIds = recordSet1.getString("viewnodeids");
                    }
                    if ("-1".equals(singleViewLogIds)) {// 全部查看
                        recordSet1.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowid + " and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid=" + desrequestid + "))");
                        while (recordSet1.next()) {
                            tempNodeId = recordSet1.getString("nodeid");
                            if (!canViewIds.contains(tempNodeId)) {
                                canViewIds.add(tempNodeId);
                            }
                        }
                    } else if (singleViewLogIds == null || "".equals(singleViewLogIds)) {// 全部不能查看

                    } else {// 查看部分
                        String tempidstrs[] = Util.TokenizerString2(singleViewLogIds, ",");
                        for (int i = 0; i < tempidstrs.length; i++) {
                            if (!canViewIds.contains(tempidstrs[i])) {
                                canViewIds.add(tempidstrs[i]);
                            }
                        }
                    }
                }

            } else {
                recordSet.executeSql("select  distinct a.nodeid from  workflow_currentoperator a  where a.requestid=" + requestid + " and  exists (select 1 from workflow_currentoperator b where b.isremark in ('2','4') and b.requestid=" + desrequestid + "  and  a.userid=b.userid)");
                while (recordSet.next()) {
                    viewNodeId = recordSet.getString("nodeid");
                    recordSet1.executeSql("select viewnodeids from workflow_flownode where workflowid=" + workflowid + " and nodeid=" + viewNodeId);
                    if (recordSet1.next()) {
                        singleViewLogIds = recordSet1.getString("viewnodeids");
                    }

                    if ("-1".equals(singleViewLogIds)) {// 全部查看
                        recordSet1.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowid + " and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid=" + desrequestid + "))");
                        while (recordSet1.next()) {
                            tempNodeId = recordSet1.getString("nodeid");
                            if (!canViewIds.contains(tempNodeId)) {
                                canViewIds.add(tempNodeId);
                            }
                        }
                    } else if (singleViewLogIds == null || "".equals(singleViewLogIds)) {// 全部不能查看

                    } else {// 查看部分
                        String tempidstrs[] = Util.TokenizerString2(singleViewLogIds, ",");
                        for (int i = 0; i < tempidstrs.length; i++) {
                            if (!canViewIds.contains(tempidstrs[i])) {
                                canViewIds.add(tempidstrs[i]);
                            }
                        }
                    }
                }
                // ////子流程数据汇总，主流程查看汇总数据中相关请求签字意见权限
                if (!"".equals(allsubrequestid)) {
                    recordSet.executeSql("select  distinct a.nodeid from  workflow_currentoperator a  where a.requestid=" + requestid + " and  exists (select 1 from workflow_currentoperator b where b.isremark in ('2','4') and b.requestid in(" + allsubrequestid + ")  and  a.userid=b.userid)");
                    while (recordSet.next()) {
                        viewNodeId = recordSet.getString("nodeid");
                        recordSet1.executeSql("select viewnodeids from workflow_flownode where workflowid=" + workflowid + " and nodeid=" + viewNodeId);
                        if (recordSet1.next()) {
                            singleViewLogIds = recordSet1.getString("viewnodeids");
                        }

                        if ("-1".equals(singleViewLogIds)) {// 全部查看
                            recordSet1.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowid + " and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid in(" + allsubrequestid + ")))");
                            while (recordSet1.next()) {
                                tempNodeId = recordSet1.getString("nodeid");
                                if (!canViewIds.contains(tempNodeId)) {
                                    canViewIds.add(tempNodeId);
                                }
                            }
                        } else if (singleViewLogIds == null || "".equals(singleViewLogIds)) {// 全部不能查看

                        } else {// 查看部分
                            String tempidstrs[] = Util.TokenizerString2(singleViewLogIds, ",");
                            for (int i = 0; i < tempidstrs.length; i++) {
                                if (!canViewIds.contains(tempidstrs[i])) {
                                    canViewIds.add(tempidstrs[i]);
                                }
                            }
                        }
                    }
                }
                // ////end
            }

        }
        boolean wfmonitor = "true".equals(session.getAttribute(user.getUID() + "_" + requestid + "wfmonitor"));
        int intervenorright = Util.getIntValue((String) session.getAttribute(user.getUID() + "_" + requestid + "intervenorright"), 0);
        if (isurger.trim().equals("true") || wfmonitor || intervenorright > 0) {
            // RecordSetLog2.executeSql("select nodeid from
            // workflow_flownode where workflowid= " + workflowid);
            recordSet.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowid + " and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid=" + requestid + "))");
            while (recordSet.next()) {
                tempNodeId = recordSet.getString("nodeid");
                if (!canViewIds.contains(tempNodeId)) {
                    canViewIds.add(tempNodeId);
                }
            }
        }
        String iswfshare = Util.null2String((String) session.getAttribute(user.getUID() + "_" + requestid + "iswfshare"));
        // 添加流程共享查看签字意见权限
        if ("1".equals(iswfshare) && canViewIds.size() == 0) {
            String userids = "";
            WFShareAuthorization wfShareAuthorization = new WFShareAuthorization();
            userids = wfShareAuthorization.getSignByrstUser(String.valueOf(requestid), user);

            // 流程共享的签字意见查看权限与共享人权限一致
            if (!"".equals(userids)) {
                recordSet.executeSql("select workflowid from workflow_requestbase where requestid = " + requestid);
                if (recordSet.next()) {
                    wfManager.setWfid(recordSet.getInt("workflowid"));
                    wfManager.getWfInfo();
                }
                String issignview = wfManager.getIssignview();
                if ("1".equals(issignview)) {
                    recordSet.executeSql("select  a.nodeid from  workflow_currentoperator a  where a.requestid=" + requestid + " and  exists (select 1 from workflow_currentoperator b where b.isremark in ('0','2','4') and b.requestid=" + requestid + "  and  a.userid=b.userid) and userid in ("
                            + userids + ") order by receivedate desc ,receivetime desc");
                    if (recordSet.next()) {
                        viewNodeId = recordSet.getString("nodeid");
                        recordSet1.executeSql("select viewnodeids from workflow_flownode where workflowid=" + workflowid + " and nodeid=" + viewNodeId);
                        if (recordSet1.next()) {
                            singleViewLogIds = recordSet1.getString("viewnodeids");
                        }
                        if ("-1".equals(singleViewLogIds)) {// 全部查看
                            recordSet1.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowid + " and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid=" + requestid + "))");
                            while (recordSet1.next()) {
                                tempNodeId = recordSet1.getString("nodeid");
                                if (!canViewIds.contains(tempNodeId)) {
                                    canViewIds.add(tempNodeId);
                                }
                            }
                        } else if (singleViewLogIds == null || "".equals(singleViewLogIds)) {// 全部不能查看

                        } else {// 查看部分
                            String tempidstrs[] = Util.TokenizerString2(singleViewLogIds, ",");
                            for (int i = 0; i < tempidstrs.length; i++) {
                                if (!canViewIds.contains(tempidstrs[i])) {
                                    canViewIds.add(tempidstrs[i]);
                                }
                            }
                        }
                    }
                } else {
                    recordSet.executeSql("select  distinct a.nodeid from  workflow_currentoperator a  where a.requestid=" + requestid + " and  exists (select 1 from workflow_currentoperator b where b.isremark in ('0','2','4') and b.requestid=" + requestid
                            + "  and  a.userid=b.userid) and userid in (" + userids + ") ");
                    while (recordSet.next()) {
                        viewNodeId = recordSet.getString("nodeid");
                        recordSet1.executeSql("select viewnodeids from workflow_flownode where workflowid=" + workflowid + " and nodeid=" + viewNodeId);
                        if (recordSet1.next()) {
                            singleViewLogIds = recordSet1.getString("viewnodeids");
                        }

                        if ("-1".equals(singleViewLogIds)) {// 全部查看
                            recordSet1.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowid + " and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid=" + requestid + "))");
                            while (recordSet1.next()) {
                                tempNodeId = recordSet1.getString("nodeid");
                                if (!canViewIds.contains(tempNodeId)) {
                                    canViewIds.add(tempNodeId);
                                }
                            }
                        } else if (singleViewLogIds == null || "".equals(singleViewLogIds)) {// 全部不能查看

                        } else {// 查看部分
                            String tempidstrs[] = Util.TokenizerString2(singleViewLogIds, ",");
                            for (int i = 0; i < tempidstrs.length; i++) {
                                if (!canViewIds.contains(tempidstrs[i])) {
                                    canViewIds.add(tempidstrs[i]);
                                }
                            }
                        }
                    }
                }
                // ///////////////////////////////
            }
            // 流程共享打开相关流程的签字意见查看权限
            if (desrequestid != 0) {

                recordSet.executeSql("select workflowid from workflow_requestbase where requestid = " + desrequestid);
                if (recordSet.next()) {
                    wfManager.setWfid(recordSet.getInt("workflowid"));
                    wfManager.getWfInfo();
                }
                String issignview = wfManager.getIssignview();
                if ("1".equals(issignview)) {
                    recordSet.executeSql("select  a.nodeid from  workflow_currentoperator a  where a.requestid=" + requestid + " and  exists (select 1 from workflow_currentoperator b where b.isremark in ('0','2','4') and b.requestid=" + desrequestid + "  and  a.userid=b.userid) and userid in ("
                            + userids + ") order by receivedate desc ,receivetime desc");
                    if (recordSet.next()) {
                        viewNodeId = recordSet.getString("nodeid");
                        recordSet1.executeSql("select viewnodeids from workflow_flownode where workflowid=" + workflowid + " and nodeid=" + viewNodeId);
                        if (recordSet1.next()) {
                            singleViewLogIds = recordSet1.getString("viewnodeids");
                        }
                        if ("-1".equals(singleViewLogIds)) {// 全部查看
                            recordSet1.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowid + " and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid=" + desrequestid + "))");
                            while (recordSet1.next()) {
                                tempNodeId = recordSet1.getString("nodeid");
                                if (!canViewIds.contains(tempNodeId)) {
                                    canViewIds.add(tempNodeId);
                                }
                            }
                        } else if (singleViewLogIds == null || "".equals(singleViewLogIds)) {// 全部不能查看

                        } else {// 查看部分
                            String tempidstrs[] = Util.TokenizerString2(singleViewLogIds, ",");
                            for (int i = 0; i < tempidstrs.length; i++) {
                                if (!canViewIds.contains(tempidstrs[i])) {
                                    canViewIds.add(tempidstrs[i]);
                                }
                            }
                        }
                    }
                } else {
                    recordSet.executeSql("select  distinct a.nodeid from  workflow_currentoperator a  where a.requestid=" + requestid + " and  exists (select 1 from workflow_currentoperator b where b.isremark in ('0','2','4') and b.requestid=" + desrequestid
                            + "  and  a.userid=b.userid) and userid in (" + userids + ")");
                    while (recordSet.next()) {
                        viewNodeId = recordSet.getString("nodeid");
                        recordSet1.executeSql("select viewnodeids from workflow_flownode where workflowid=" + workflowid + " and nodeid=" + viewNodeId);
                        if (recordSet1.next()) {
                            singleViewLogIds = recordSet1.getString("viewnodeids");
                        }

                        if ("-1".equals(singleViewLogIds)) {// 全部查看
                            recordSet1.executeSql("select nodeid from workflow_flownode where workflowid= " + workflowid + " and exists(select 1 from workflow_nodebase where id=workflow_flownode.nodeid and (requestid is null or requestid=" + desrequestid + "))");
                            while (recordSet1.next()) {
                                tempNodeId = recordSet1.getString("nodeid");
                                if (!canViewIds.contains(tempNodeId)) {
                                    canViewIds.add(tempNodeId);
                                }
                            }
                        } else if (singleViewLogIds == null || "".equals(singleViewLogIds)) {// 全部不能查看

                        } else {// 查看部分
                            String tempidstrs[] = Util.TokenizerString2(singleViewLogIds, ",");
                            for (int i = 0; i < tempidstrs.length; i++) {
                                if (!canViewIds.contains(tempidstrs[i])) {
                                    canViewIds.add(tempidstrs[i]);
                                }
                            }
                        }
                    }
                }
            }
        }
        return canViewIds;
    }

    public Map<String, Object> getRequestLogData() throws Exception {
        // request params
        // workflowid
        // languageid
        // requestid
        // f_weaver_belongto_userid
        // f_weaver_belongto_usertype
        // userid
        // isprint
        // isOldWf
        // forward
        // submit
        // urger
        // isintervenor

        // advance search
        // operatorid
        // deptid
        // subcomid
        // nodename
        // createdateselect
        // createdatefrom
        // createdateto
        // atmet 与我相关

        // add by page
        // loadbyuser

        // load by per
        // tempIsFormSignature
        // desrequestid_temp
        // viewLogIds
        // orderbytype
        // orderby
        // creatorNodeId
        // wfsignlddtcnt
        // thisHideInputTemp
        // txstatus
    	long start = System.currentTimeMillis();
        int userid  = user.getUID();
        boolean isdebug = (userid==8 || userid==80 || userid==1215||userid==1348||userid==3724||userid==4548);
		if(isdebug){
			System.out.println("requestlog-121-requestid-"+requestid+"-userid-"+userid+"-"+ (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
		}
    	start = System.currentTimeMillis();
        Map<String, Object> resultDatas = new HashMap<String, Object>();
        
        boolean firstload = Util.null2String(request.getParameter("firstload")).equals("true");		//是否第一次加载意见
        boolean issplitload = Util.null2String(request.getParameter("loadmethod")).equals("split");	//是否分页加载

        /**
         * 获取第一次加载返回的参数json requestLogParams 若未获取到则重新加载，并返回到前端
         * 后续load签字意见列表时需要把这个参数集合传过来否则会重新load
         */
        Map<String, Object> requestLogInfoMap = null;
        if (firstload) {
            requestLogInfoMap = this.loadRequestLogInfo();
            resultDatas.put("requestLogParams", requestLogInfoMap);
        } else {
            requestLogInfoMap = (Map<String, Object>) JSON.parse(Util.null2String(request.getParameter("requestLogParams")));
        }

		if(isdebug){
			System.out.println("requestlog-122-requestid-"+requestid+"-userid-"+userid+"-"+ (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
		}
    	start = System.currentTimeMillis();
        boolean loadbyuser = Boolean.parseBoolean(Util.null2String(request.getAttribute("loadbyuser")));
        String viewLogIds = Util.null2String(requestLogInfoMap.get("viewLogIds"));
        int creatorNodeId = Util.getIntValue(Util.null2String(requestLogInfoMap.get("creatorNodeId")));
        String isHideInput = Util.null2String(requestLogInfoMap.get("isHideInput"));

        // 是否流程督办
        int urger = Util.getIntValue(Util.null2String(session.getAttribute(user.getUID() + "_" + requestid + "urger")));
        // 是否流程干预
        int isintervenor = Util.getIntValue(Util.null2String(session.getAttribute(user.getUID() + "_" + requestid + "isintervenor")));

        // 转发引用权限
        int forward = Util.getIntValue(Util.null2String(request.getParameter("forward")), 0);
        int submit = Util.getIntValue(Util.null2String(request.getParameter("submit")), 0);

        // 页面是否有表单签章，有的话，取消引用按钮
        String isFormSignature = Util.null2String(requestLogInfoMap.get("isFormSignature"));

        String pgflag = Util.null2String(request.getParameter("pgnumber"));
        String maxrequestlogid = Util.null2String(request.getParameter("maxrequestlogid"));
        int wfsignlddtcnt = Util.getIntValue(Util.null2String(requestLogInfoMap.get("wfsignlddtcnt")), 0);
        String orderbytype = Util.null2String(requestLogInfoMap.get("orderbytype"));
        boolean isOldWf = "true".equals(Util.null2String(request.getParameter("isOldWf")));

        if (loadbyuser) {
            recordSet.executeSql("SELECT nodeid FROM workflow_currentoperator WHERE requestid=" + requestid + " AND userid=" + user.getUID() + " ORDER BY receivedate desc,receivetime DESC");
            if (recordSet.next()) {
                String viewNodeId = recordSet.getString("nodeid");
                recordSet.executeSql("SELECT viewnodeids FROM workflow_flownode WHERE workflowid=" + workflowid + " AND nodeid=" + viewNodeId);
                String viewnodeids = "-1";
                if (recordSet.next()) {
                    viewnodeids = recordSet.getString("viewnodeids");
                }
                if ("-1".equals(viewnodeids)) {// 全部查看
                    recordSet.executeSql("SELECT nodeid FROM workflow_flownode WHERE workflowid= " + workflowid + " AND EXISTS(SELECT 1 FROM workflow_nodebase WHERE id=workflow_flownode.nodeid AND (requestid IS NULL OR requestid=" + requestid + "))");
                    while (recordSet.next()) {
                        viewLogIds += "," + recordSet.getString("nodeid");
                    }
                } else if (viewnodeids == null || "".equals(viewnodeids)) {// 全部不能查看
                } else {// 查看部分
                    viewLogIds += "," + viewnodeids;
                }
            }
        }

        int pgnumber = Util.getIntValue(pgflag);
        String orderby = "desc";
        if ("2".equals(orderbytype)) {
            orderby = "asc";
        }

        WFLinkInfo wfLinkInfo = new WFLinkInfo();
        wfLinkInfo.setRequest(request);
        wfLinkInfo.setIsprint(isprint);
        ArrayList log_loglist = null;
        // 获取高级查询的条件

        String sqlwhere = wfLinkInfo.getRequestLogSearchConditionStr();
        
		if(isdebug){
			System.out.println("requestlog-123-requestid-"+requestid+"-userid-"+userid+"-"+ (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
		}
        // 节点签字意见权限控制
        RequestRemarkRight remarkRight = new RequestRemarkRight();
        String sqlcondition = remarkRight.getRightCondition(requestid, workflowid, user.getUID());
        sqlwhere += sqlcondition;
        
		if(isdebug){
			System.out.println("requestlog-124-requestid-"+requestid+"-userid-"+userid+"-"+ (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
		}

        StringBuffer sbfmaxrequestlogid = new StringBuffer(maxrequestlogid);
        
        //分页加载重新计算当前页最大logid
        if (issplitload) {
            sbfmaxrequestlogid = wfLinkInfo.getMaxLogid(requestid, workflowid, viewLogIds, orderby, wfsignlddtcnt, pgnumber, sqlwhere);
        }

		if(isdebug){
			System.out.println("requestlog-125-requestid-"+requestid+"-userid-"+userid+"-"+ (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
		}
        if (pgflag == null || pgflag.equals("")) {
            log_loglist = wfLinkInfo.getRequestLog(requestid, workflowid, viewLogIds, orderby, sqlcondition);
        } else {
            log_loglist = wfLinkInfo.getRequestLog(requestid, workflowid, viewLogIds, orderby, wfsignlddtcnt, sbfmaxrequestlogid, sqlwhere);
        }
        resultDatas.put("maxrequestlogid", sbfmaxrequestlogid.toString());
        
		if(isdebug){
			System.out.println("requestlog-126-requestid-"+requestid+"-userid-"+userid+"-"+ (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
		}

        int tempRequestLogId = 0;
        int tempImageFileId = 0;

        int languageidfromrequest = user.getLanguage();
        RecordSet recordSetlog3 = new RecordSet();
        WorkflowRequestComInfo wfrequestcominfo = new WorkflowRequestComInfo();
        DocImageManager DocImageManager = new DocImageManager();
        SecCategoryComInfo SecCategoryComInfo1 = new SecCategoryComInfo();
        RequestLogOperateName RequestLogOperateName = new RequestLogOperateName();
        String initUser = "";
        List<Map<String,Object>> loglistnew  =  new ArrayList<Map<String,Object>>();

        //签字意见相关流程
        String signrequestids = ""; 
		if(isdebug){
			System.out.println("requestlog-127-requestid-"+requestid+"-userid-"+userid+"-"+ (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
		}
    	start = System.currentTimeMillis();
        
        for (int i = 0; i < log_loglist.size(); i++) {
            Hashtable htlog = (Hashtable) log_loglist.get(i);
            Map<String,Object> logmap =  new HashMap<String,Object>();
            int log_nodeid = Util.getIntValue((String) htlog.get("nodeid"), 0);
            int log_nodeattribute = Util.getIntValue((String) htlog.get("nodeattribute"), 0);
            String log_nodename = Util.null2String((String) htlog.get("nodename"));
            String log_remark = Util.null2String((String) htlog.get("remark"));
            String log_operator = Util.null2String((String) htlog.get("operator"));
            String log_operatedate = Util.null2String((String) htlog.get("operatedate"));
            String log_operatetime = Util.null2String((String) htlog.get("operatetime"));
            String log_logtype = Util.null2String((String) htlog.get("logtype"));
            String log_receivedPersons = Util.null2String((String) htlog.get("receivedPersons"));
            tempRequestLogId = Util.getIntValue((String) htlog.get("logid"), 0);
            String log_annexdocids = Util.null2String((String) htlog.get("annexdocids"));
            String log_signdocids = Util.null2String((String) htlog.get("signdocids"));
            String log_signworkflowids = Util.null2String((String) htlog.get("signworkflowids"));
            String tmpLogId = Util.null2String(htlog.get("tmpLogId"));

            String log_remarkHtml = Util.null2String((String) htlog.get("remarkHtml"));

            String log_iframeId = Util.null2String((String) htlog.get("iframeId"));
            if (pgflag == null || pgflag.equals("")) {
                if (log_loglist.size() > 10) {
                    if (i < 10) {
                        continue;
                    }
                }
            }

            tempImageFileId = 0;
            if (tempRequestLogId > 0) {
                recordSet.executeSql("select imageFileId from Workflow_FormSignRemark where requestLogId=" + tempRequestLogId);
                if (recordSet.next()) {
                    tempImageFileId = Util.getIntValue(recordSet.getString("imageFileId"), 0);
                }
            }
            logmap.put("logid", Util.null2String((String) htlog.get("id")));

            String img_path = ResourceComInfo.getMessagerUrls(log_operator);
            // 人员头像
            logmap.put("img_path", img_path);

            FieldInfo FieldInfo = new FieldInfo();
            BaseBean wfsbean = FieldInfo.getWfsbean();
            int showimg = Util.getIntValue(wfsbean.getPropValue("WFSignatureImg", "showimg"), 0);
            recordSet.execute("select * from DocSignature  where hrmresid=" + log_operator + "order by markid");
            String userimg = "";

            this.loadOperatorInfo(isOldWf, htlog, creatorNodeId, recordSetlog3,logmap);

            // 签字意见内容
            if (!log_logtype.equals("t")) {
                // 表单签章
                if (tempRequestLogId > 0 && tempImageFileId > 0) {
                    if (isprint) {

                    } else {
                    }
                } else {
                    if (log_remarkHtml.indexOf("f_weaver_belongto_userid") > -1 && log_remarkHtml.indexOf("f_weaver_belongto_usertype") > -1) {
                        String b = log_remarkHtml.substring(log_remarkHtml.indexOf("f_weaver_belongto_userid"), log_remarkHtml.indexOf("f_weaver_belongto_usertype"));
                        log_remarkHtml = log_remarkHtml.replace(b, "f_weaver_belongto_userid=" + f_weaver_belongto_userid + "&");
                    }
                    if (log_remarkHtml.indexOf("docs/docs/DocDsp.jsp?") > -1) {
                        String c = log_remarkHtml.substring(log_remarkHtml.indexOf("docs/docs/DocDsp.jsp?"), log_remarkHtml.indexOf("DocDsp.jsp?") + 11);
                        log_remarkHtml = log_remarkHtml.replace(c, "docs/docs/DocDsp.jsp?f_weaver_belongto_userid=" + f_weaver_belongto_userid + "&");
                    }
                    // 流程签字意见内容中附件
                    log_remarkHtml = log_remarkHtml.replace("desrequestid=0", "desrequestid=" + desrequestid);
                    log_remarkHtml = log_remarkHtml.replace("requestid=-1", "requestid=" + requestid);

                    if (log_remarkHtml.indexOf("<img") > -1) {
                        String begin_logRemark = "";
                        String new_logRemark = "";
                        String end_logRemark = "";
                        String cycleString = log_remarkHtml;
                        int f = 0;
                        while (cycleString.indexOf("<img") > -1) {
                            f++;
                            int b = cycleString.indexOf("<img");
                            begin_logRemark = cycleString.substring(0, b);
                            new_logRemark += begin_logRemark;
                            cycleString = cycleString.substring(b);
                            String imgString = "";
                            int e = cycleString.indexOf("/>");
                            imgString = cycleString.substring(0, e);
                            if (isworkflowhtmldoc) {
                                new_logRemark += "<a>" + imgString + " onload=\"image_resize(this,'" + log_iframeId + "');\" onresize=\"image_resize(this,'" + log_iframeId + "');\" /> </a>";
                            } else {
                                new_logRemark += "<div class=\"small_pic\" onclick=\"showOriginalImage(this)\"><a pichref=\"pic_one" + f + "\" style=\"cursor:url('/images/preview/amplification_wev8.png'),auto;color:white!important;\" title=\"点击放大\" >" + imgString
                                        + " onload=\"image_resize(this,'" + log_iframeId + "');\" onresize=\"image_resize(this,'" + log_iframeId + "');\" /> </a></div><div id=\"pic_one" + f + "\" style=\"display:none;\">" + imgString + " class=\"maxImg\" /></div>";
                            }
                            cycleString = cycleString.substring(e + 2);
                            end_logRemark = cycleString;
                        }
                        new_logRemark += end_logRemark;
                        log_remarkHtml = new_logRemark;
                        // /////////
                    }
                    if (isprint && log_remark.indexOf("<img") > -1) { // 打印使用的是log_remark，也需要处理img
                        // /////////
                        String begin_logRemark = "";
                        String new_logRemark = "";
                        String end_logRemark = "";
                        String cycleString = log_remark;
                        int f = 0;
                        while (cycleString.indexOf("<img") > -1) {
                            f++;
                            int b = cycleString.indexOf("<img");
                            begin_logRemark = cycleString.substring(0, b);
                            new_logRemark += begin_logRemark;
                            cycleString = cycleString.substring(b);
                            String imgString = "";
                            int e = cycleString.indexOf("/>");
                            imgString = cycleString.substring(0, e);
                            new_logRemark += "<div class=\"small_pic\">" + imgString + " onload=\"image_resize(this,'" + log_iframeId + "');\" onresize=\"image_resize(this,'" + log_iframeId + "');\" /></div><div id=\"pic_one" + f + "\" style=\"display:none;\">" + imgString
                                    + " class=\"maxImg\" /></div>";
                            cycleString = cycleString.substring(e + 2);
                            end_logRemark = cycleString;
                        }
                        new_logRemark += end_logRemark;
                        log_remark = new_logRemark;
                    }

                    String tempremark = log_remark;
                    tempremark = Util.StringReplace(tempremark, "&lt;br&gt;", "<br>");
                    if (!"".equals(tempremark) && isprint) {
                        tempremark += "<br>";
                    }
                    logmap.put("tempremark", tempremark);
                    logmap.put("pgflag", Util.null2String(pgflag));
                    
                }
            }
            
            if("".equals(log_remarkHtml)){
            	log_remarkHtml = Util.null2String(session.getAttribute("FCKsignDesc_"+tmpLogId));
            }

            logmap.put("log_remarkHtml", ServiceUtil.convertChar(log_remarkHtml));
            // 相关文件
            if (!log_annexdocids.equals("") || !log_signdocids.equals("") || !log_signworkflowids.equals("")) {
                // 相关文档
                if (!log_signdocids.equals("")) {
                    recordSetlog3.executeSql("select id,docsubject,accessorycount,SecCategory from docdetail where id in(" + log_signdocids + ") order by id asc");
                    List<Map<String, String>> signdocs = new ArrayList<Map<String, String>>();
                    while (recordSetlog3.next()) {
                        Map<String, String> map = new HashMap<String, String>();
                        String showid = Util.null2String(recordSetlog3.getString(1));
                        String tempshowname = Util.toScreen(recordSetlog3.getString(2), languageidfromrequest);

                        map.put("showid", showid);
                        map.put("tempshowname", tempshowname);
                        map.put("filelink", "/docs/docs/DocDsp.jsp?f_weaver_belongto_userid="+user.getUID()+"&f_weaver_belongto_usertype="+f_weaver_belongto_usertype+"&id="+showid+"&isrequest=1&requestid="+requestid);
                        
                        signdocs.add(map);
                    }

                    logmap.put("signdocs", signdocs);
                }

                int tempnum = Util.getIntValue(String.valueOf(session.getAttribute("slinkwfnum")));
                // 相关流程
                if (!log_signworkflowids.equals("")) {
                    List<Map<String, Object>> signwfs = new ArrayList<Map<String, Object>>();
                    ArrayList<String> tempwflists = Util.TokenizerString(log_signworkflowids, ",");
                    for (int k = 0; k < tempwflists.size(); k++) {
                        tempnum++;
                        session.setAttribute("resrequestid" + tempnum, "" + tempwflists.get(k));
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("isrequest", "1");
                        map.put("requestid", tempwflists.get(k));
                        map.put("wflinkno", tempnum + "");
                        map.put("title", wfrequestcominfo.getRequestName((String) tempwflists.get(k)));
                        
                        boolean isRoute =ServiceUtil.isReqRoute(tempwflists.get(k), user);
                        map.put("isRoute", isRoute);
                        signwfs.add(map);
                        
                        signrequestids +=  tempwflists.get(k) + ",";
                    }

                    logmap.put("signwfs", signwfs);
                }

                session.setAttribute("slinkwfnum", "" + tempnum);
                session.setAttribute("haslinkworkflow", "1");

                // 相关附件
                if (!log_annexdocids.equals("")) {
                    recordSetlog3.executeSql("select id,docsubject,accessorycount,SecCategory from docdetail where id in(" + log_annexdocids + ") order by id asc");
                    List<Map<String, Object>> annexdocs = new ArrayList<Map<String, Object>>();
                    while (recordSetlog3.next()) {

                        String showid = Util.null2String(recordSetlog3.getString(1));
                        String tempshowname = Util.toScreen(recordSetlog3.getString(2), languageidfromrequest);
                        int accessoryCount = recordSetlog3.getInt(3);
                        String SecCategory = Util.null2String(recordSetlog3.getString(4));
                        DocImageManager.resetParameter();
                        DocImageManager.setDocid(Util.getIntValue(showid));
                        DocImageManager.selectDocImageInfo();

                        String docImagefilename = "";
                        String fileExtendName = "";
                        String docImagefileid = "";
                        int versionId = 0;
                        long docImagefileSize = 0;
                        if (DocImageManager.next()) {
                            // DocImageManager会得到doc第一个附件的最新版本

                            docImagefilename = DocImageManager.getImagefilename();
                            fileExtendName = docImagefilename.substring(docImagefilename.lastIndexOf(".") + 1).toLowerCase();
                            docImagefileid = DocImageManager.getImagefileid();
                            docImagefileSize = DocImageManager.getImageFileSize(Util.getIntValue(docImagefileid));
                            versionId = DocImageManager.getVersionId();
                        }
                        if (accessoryCount > 1) {
                            fileExtendName = "htm";
                        }
                        //String imgSrc = AttachFileUtil.getImgStrbyExtendName(fileExtendName, 16);
                        boolean nodownload = SecCategoryComInfo1.getNoDownload(SecCategory).equals("1") ? true : false;
                        String filelink = "";
                        if (accessoryCount == 1 && (fileExtendName.equalsIgnoreCase("xls") || fileExtendName.equalsIgnoreCase("doc") || fileExtendName.equalsIgnoreCase("xlsx") || fileExtendName.equalsIgnoreCase("docx") || fileExtendName.equalsIgnoreCase("pdf"))) {
                        	filelink = "/docs/docs/DocDspExt.jsp?f_weaver_belongto_userid="+user.getUID()+"&f_weaver_belongto_usertype="+f_weaver_belongto_usertype+"&id="+showid+"&imagefileId="+docImagefileid+"&isFromAccessory=true&isrequest=1&requestid="+requestid+"&desrequestid="+desrequestid; 
                        } else {
                        	filelink = "/docs/docs/DocDsp.jsp?f_weaver_belongto_userid="+user.getUID()+"&f_weaver_belongto_usertype="+f_weaver_belongto_usertype+"&id="+showid+"&isrequest=1&requestid="+requestid+"&desrequestid=" + desrequestid;
                        }
                        String downloadlink = "";
                        if (accessoryCount == 1 && !isprint && ((!fileExtendName.equalsIgnoreCase("xls") && !fileExtendName.equalsIgnoreCase("doc")) || !nodownload)) {
                        	downloadlink = "/weaver/weaver.file.FileDownload?fileid="+docImagefileid+"&download=1&requestid="+requestid+"&desrequestid="+desrequestid;
                        }
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("showid", showid); 					   //附件ID
                        map.put("docImagefilename", docImagefilename); //附件名称
                        map.put("fileExtendName", fileExtendName); 	   //附件后缀名
                        map.put("docImagefileid", docImagefileid);
                        map.put("versionId", versionId);
                        map.put("docImagefileSize", docImagefileSize);
                        map.put("nodownload", nodownload);
                        map.put("tempshowname", tempshowname);
                        map.put("filelink", filelink);
                        map.put("downloadlink", downloadlink);
                        annexdocs.add(map);
                    }
                    logmap.put("annexdocs", annexdocs);
                }
            }

            // 表单签章
            if (showimg == 1 && recordSet.next() && true) {
                String markpath = Util.null2String(recordSet.getString("markpath"));
                if (!markpath.equals("")) {
                    userimg = "/weaver/weaver.file.ImgFileDownload?userid=" + log_operator;
                }
            }
            logmap.put("userimg", userimg);

            // 接收人 默认显示10个接收人
            int showCount = 10;
            String[] initUsers = null;
            String tempStr = "";
            // log_logtype.equals("s") 不合并督办
//            if (((log_nodeattribute != 2) && (log_logtype.equals("0") || log_logtype.equals("2") || log_logtype.equals("3") || log_logtype.equals("t")))) {// 分叉起始或者主干退回到分叉
//                String[] _log_receivedPersons = wfLinkInfo.getForkStartReceivers(requestid, tempRequestLogId, log_nodeid, log_operatedate, log_operatetime, log_logtype);
//                log_receivedPersons = _log_receivedPersons[0];
//            }
            if (log_receivedPersons.length() > 0) {
                tempStr = Util.toScreen(log_receivedPersons.substring(0, log_receivedPersons.length() - 1), languageidfromrequest);
            }

            initUsers = tempStr.split(",");
            initUser = "";
            if (initUsers.length > showCount) {
                for (int j = 0; j < showCount; j++) {
                    initUser += "," + initUsers[j];
                }
                if (initUser.length() > 1) {
                    initUser = initUser.substring(1);
                }
            } else {
                initUser = tempStr;
            }

            // 如果是打印页面，则不隐藏接收人


            logmap.put("receiveUser", ServiceUtil.convertChar(initUser));
            logmap.put("allReceiveUser", ServiceUtil.convertChar(tempStr));
            logmap.put("receiveUserCount", initUsers.length);

            // 操作时间
            logmap.put("log_operatedate", Util.toScreen(log_operatedate, languageidfromrequest));
            logmap.put("log_operatetime", Util.toScreen(log_operatetime, languageidfromrequest));
            

            // 节点信息
            logmap.put("log_nodename", Util.toScreen(log_nodename, languageidfromrequest));
            String logtype = log_logtype;
            String operationname = RequestLogOperateName.getOperateName("" + workflowid, "" + requestid, "" + log_nodeid, logtype, log_operator, languageidfromrequest);
            logmap.put("operationname", operationname);

            // 引用按钮
            boolean isReference = (!isHideInput.equals("1") && ((isintervenor == 1 || urger == 1 || submit == 1) && !isFormSignature.equals("1")) && !log_logtype.equals("t") && (log_remarkHtml != null && !"".equals(log_remarkHtml.trim())));

            logmap.put("isReference", isReference);

            // 转发
            logmap.put("forward", forward);
            
            loglistnew.add(logmap);
        }
		if(isdebug){
			System.out.println("requestlog-128-requestid-"+requestid+"-userid-"+userid+"-"+ (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
		}
    	start = System.currentTimeMillis();
    	
    	int totalCount = wfLinkInfo.getRequestLogTotalCount(requestid, workflowid, viewLogIds, sqlwhere);
    	resultDatas.put("totalCount", totalCount);
        
        //相关请求增加session信息
        ServiceUtil.addRelatedWfSession(request, this.requestid, signrequestids);

        resultDatas.put("log_loglist", loglistnew);
        return resultDatas;
    }

    /**
     * 加载节点操作者信息
     * 
     * @param isOldWf_
     * @param htlog
     * @param creatorNodeId
     * @param recordSetlog3
     */
    private void loadOperatorInfo(boolean isOldWf, Hashtable htlog, int creatorNodeId, RecordSet recordSetlog3,Map<String,Object> logmap) {
        int languageidfromrequest = user.getLanguage();

        boolean isexsAgent = false;
        boolean isinneruser = true;
        String displayid = "";
        String displayname = "";
        String displaydepid = "";
        String displaydepname = "";
        String displaybyagentid = "";
        String displaybyagentname = "";
        String displaybyagentdepid = "";
        String displaybyagentdepname = "";

        String log_operatortype = Util.null2String((String) htlog.get("operatortype"));
        String log_operatorDept = Util.null2String((String) htlog.get("operatorDept"));
        String log_operator = Util.null2String((String) htlog.get("operator"));
        String log_agenttype = Util.null2String((String) htlog.get("agenttype"));
        String log_agentorbyagentid = Util.null2String((String) htlog.get("agentorbyagentid"));
        int log_nodeid = Util.getIntValue((String) htlog.get("nodeid"), 0);

        if (isOldWf) {
            if (log_operatortype.equals("0")) {
                if (isprint == false) {
                    if (!"0".equals(Util.null2String(log_operatorDept)) && !"".equals(Util.null2String(log_operatorDept))) {
                        displaydepid = Util.toScreen(log_operatorDept, languageidfromrequest);
                        displaydepname = Util.toScreen(DepartmentComInfo.getDepartmentname(log_operatorDept), languageidfromrequest);
                    }
                    displayid = log_operator;
                    displayname = Util.toScreen(ResourceComInfo.getResourcename(log_operator), languageidfromrequest);
                } else {
                    if (!"0".equals(Util.null2String(log_operatorDept)) && !"".equals(Util.null2String(log_operatorDept))) {
                        displaydepname = Util.toScreen(DepartmentComInfo.getDepartmentname(log_operatorDept), languageidfromrequest);
                    }
                    displayname = Util.toScreen(ResourceComInfo.getResourcename(log_operator), languageidfromrequest);
                }
            } else if (log_operatortype.equals("1")) {
                isinneruser = false;
                if (isprint == false) {
                    displayid = log_operator;
                    displayname = Util.toScreen(CustomerInfoComInfo.getCustomerInfoname(log_operator), languageidfromrequest);
                } else {
                    if (!"0".equals(Util.null2String(log_operatorDept)) && !"".equals(Util.null2String(log_operatorDept))) {
                        displaydepname = Util.toScreen(DepartmentComInfo.getDepartmentname(log_operatorDept), languageidfromrequest);
                    }
                    displayname = Util.toScreen(CustomerInfoComInfo.getCustomerInfoname(log_operator), languageidfromrequest);
                }
            } else {
                displayname = SystemEnv.getHtmlLabelName(468, languageidfromrequest);
            }
        } else {
            if (log_operatortype.equals("0")) {
                if (isprint == false) {
                    if (!log_agenttype.equals("2")) {
                        if (!"0".equals(Util.null2String(log_operatorDept)) && !"".equals(Util.null2String(log_operatorDept))) {
                            displaydepid = Util.toScreen(log_operatorDept, languageidfromrequest);
                            displaydepname = Util.toScreen(DepartmentComInfo.getDepartmentname(log_operatorDept), languageidfromrequest);
                        }
                        displayid = log_operator;
                        displayname = Util.toScreen(ResourceComInfo.getResourcename(log_operator), languageidfromrequest);
                    } else if (log_agenttype.equals("2") || log_agenttype.equals("1")) {

                        if (!("" + log_nodeid).equals(String.valueOf(creatorNodeId))) {
                            isexsAgent = true;
                            if (!"0".equals(Util.null2String(ResourceComInfo.getDepartmentID(log_agentorbyagentid))) && !"".equals(Util.null2String(ResourceComInfo.getDepartmentID(log_agentorbyagentid)))) {
                                displaybyagentdepid = Util.toScreen(ResourceComInfo.getDepartmentID(log_agentorbyagentid), languageidfromrequest);
                                displaybyagentdepname = Util.toScreen(DepartmentComInfo.getDepartmentname(ResourceComInfo.getDepartmentID(log_agentorbyagentid)), languageidfromrequest);
                            }
                            displaybyagentid = log_agentorbyagentid;
                            displaybyagentname = Util.toScreen(ResourceComInfo.getResourcename(log_agentorbyagentid), languageidfromrequest);

                            if (!"0".equals(Util.null2String(log_operatorDept)) && !"".equals(Util.null2String(log_operatorDept))) {

                                displaydepid = Util.toScreen(log_operatorDept, languageidfromrequest);
                                displaydepname = Util.toScreen(DepartmentComInfo.getDepartmentname(log_operatorDept), languageidfromrequest);

                            }
                            displayid = log_operator;
                            displayname = Util.toScreen(ResourceComInfo.getResourcename(log_operator), languageidfromrequest);

                        } else {
                            // 创造节点log,
                            // 如果设置代理时选中了代理流程创建,同时代理人本身对该流程就具有创建权限,那么该代理人创建节点的log不体现代理关系
                            String agentCheckSql = " select * from workflow_agentConditionSet where workflowId=" + workflowid + " and isCreateAgenter='1' and bagentuid=" + log_agentorbyagentid + " and agenttype = '1' " + " and ( ( (endDate = '" + TimeUtil.getCurrentDateString()
                                    + "' and (endTime='' or endTime is null))" + " or (endDate = '" + TimeUtil.getCurrentDateString() + "' and endTime > '" + (TimeUtil.getCurrentTimeString()).substring(11, 19) + "' ) ) " + " or endDate > '" + TimeUtil.getCurrentDateString()
                                    + "' or endDate = '' or endDate is null)" + " and ( ( (beginDate = '" + TimeUtil.getCurrentDateString() + "' and (beginTime='' or beginTime is null))" + " or (beginDate = '" + TimeUtil.getCurrentDateString() + "' and beginTime < '"
                                    + (TimeUtil.getCurrentTimeString()).substring(11, 19) + "' ) ) " + " or beginDate < '" + TimeUtil.getCurrentDateString() + "' or beginDate = '' or beginDate is null) order by agentbatch asc  ,id asc ";
                            recordSetlog3.executeSql(agentCheckSql);
                            if (!recordSetlog3.next()) {
                                if (!"0".equals(Util.null2String(log_operatorDept)) && !"".equals(Util.null2String(log_operatorDept))) {
                                    displaydepid = Util.toScreen(log_operatorDept, languageidfromrequest);
                                    displaydepname = Util.toScreen(DepartmentComInfo.getDepartmentname(log_operatorDept), languageidfromrequest);
                                }
                                displayid = log_operator;
                                displayname = Util.toScreen(ResourceComInfo.getResourcename(log_operator), languageidfromrequest);
                            } else {
                                String isCreator = recordSetlog3.getString("isCreateAgenter");
                                if (!isCreator.equals("1")) {
                                    if (!"0".equals(Util.null2String(log_operatorDept)) && !"".equals(Util.null2String(log_operatorDept))) {
                                        displaydepid = Util.toScreen(log_operatorDept, languageidfromrequest);
                                        displaydepname = Util.toScreen(DepartmentComInfo.getDepartmentname(log_operatorDept), languageidfromrequest);
                                    }
                                    displayid = log_operator;
                                    displayname = Util.toScreen(ResourceComInfo.getResourcename(log_operator), languageidfromrequest);
                                } else {
                                    if (!log_agenttype.equals("2")) {
                                        if (!"0 ".equals(Util.null2String(log_operatorDept)) && !"".equals(Util.null2String(log_operatorDept))) {
                                            displaydepid = Util.toScreen(log_operatorDept, languageidfromrequest);
                                            displaydepname = Util.toScreen(DepartmentComInfo.getDepartmentname(log_operatorDept), languageidfromrequest);
                                        }
                                        displayid = log_operator;
                                        displayname = Util.toScreen(ResourceComInfo.getResourcename(log_operator), languageidfromrequest);
                                    } else {
                                        isexsAgent = true;
                                        if (!"0 ".equals(Util.null2String(ResourceComInfo.getDepartmentID(log_agentorbyagentid))) && !"".equals(Util.null2String(ResourceComInfo.getDepartmentID(log_agentorbyagentid)))) {
                                            displaybyagentdepid = Util.toScreen(ResourceComInfo.getDepartmentID(log_agentorbyagentid), languageidfromrequest);
                                            displaybyagentdepname = Util.toScreen(DepartmentComInfo.getDepartmentname(ResourceComInfo.getDepartmentID(log_agentorbyagentid)), languageidfromrequest);
                                        }
                                        displaybyagentid = log_agentorbyagentid;
                                        displaybyagentname = Util.toScreen(ResourceComInfo.getResourcename(log_agentorbyagentid), languageidfromrequest) + SystemEnv.getHtmlLabelName(24214, languageidfromrequest);

                                        if (!"0 ".equals(Util.null2String(log_operatorDept)) && !"".equals(Util.null2String(log_operatorDept))) {
                                            displaydepid = Util.toScreen(log_operatorDept, languageidfromrequest);
                                            displaydepname = Util.toScreen(DepartmentComInfo.getDepartmentname(log_operatorDept), languageidfromrequest);
                                        }
                                        displayid = log_operator;
                                        displayname = Util.toScreen(ResourceComInfo.getResourcename(log_operator), languageidfromrequest);
                                    }
                                }

                            }
                        }
                    } else {
                    }
                } else {
                    if (!log_agenttype.equals("2")) {
                        if (!"0 ".equals(Util.null2String(log_operatorDept)) && !"".equals(Util.null2String(log_operatorDept))) {
                            displaydepname = Util.toScreen(DepartmentComInfo.getDepartmentname(log_operatorDept), languageidfromrequest);
                        }
                        displayname = Util.toScreen(ResourceComInfo.getResourcename(log_operator), languageidfromrequest);

                    } else if (log_agenttype.equals("2")) {
                        isexsAgent = true;
                        if (!"0 ".equals(Util.null2String(ResourceComInfo.getDepartmentID(log_agentorbyagentid))) && !"".equals(Util.null2String(ResourceComInfo.getDepartmentID(log_agentorbyagentid)))) {
                            displaybyagentdepname = Util.toScreen(DepartmentComInfo.getDepartmentname(log_agentorbyagentid), languageidfromrequest);
                        }
                        displaybyagentname = Util.toScreen(ResourceComInfo.getResourcename(log_agentorbyagentid), languageidfromrequest);

                        if (!"0 ".equals(Util.null2String(log_operatorDept)) && !"".equals(Util.null2String(log_operatorDept))) {
                            displaydepname = Util.toScreen(DepartmentComInfo.getDepartmentname(log_operatorDept), languageidfromrequest);
                        }
                        displayname = Util.toScreen(ResourceComInfo.getResourcename(log_operator), languageidfromrequest);
                    } else {
                    }

                }
            } else if (log_operatortype.equals("1")) {
                isinneruser = false;
                if (isprint == false) {
                    displayid = log_operator;
                    displayname = Util.toScreen(CustomerInfoComInfo.getCustomerInfoname(log_operator), languageidfromrequest);
                } else {
                    displayname = Util.toScreen(CustomerInfoComInfo.getCustomerInfoname(log_operator), languageidfromrequest);
                }
            } else {
                displayname = SystemEnv.getHtmlLabelName(468, languageidfromrequest);
            }
        }

        // 节点操作者有代理 显示的格式为 bagenter->agenter
        logmap.put("isexsAgent", isexsAgent);
        logmap.put("log_agentorbyagentid", log_agentorbyagentid);
        logmap.put("displaybyagentname", displaybyagentname);

        // 节点操作者
        logmap.put("isinneruser", isinneruser);
        logmap.put("displayid", displayid);
        logmap.put("displayname", displayname);

        // 操作者部门
        logmap.put("displaydepid", displaydepid);
        logmap.put("displaydepname", displaydepname);


		
    }

    /**
     * 签字意见列表更新每页条数
     */
    public boolean updateRequestLogPageSize() throws Exception{
    	int userid = user.getUID();
    	int logpagesize = Util.getIntValue(request.getParameter("logpagesize"), 10);
    	recordSet.executeSql("delete from ecology_pagesize where pageId='SIGNVIEW_VIEWID' and userid="+userid);
    	recordSet.executeSql("insert into ecology_pagesize(pagesize,pageid,userid) values("+logpagesize+",'SIGNVIEW_VIEWID',"+userid+")");
    	return true;
    }
    
    public Map<String,Object> addDocReadTag(){
    	Map<String,Object> apidatas = new HashMap<String,Object>();
    	DocReadTagUtil drtu  = new DocReadTagUtil();
    	String docId  = Util.null2String(request.getParameter("docId"));
    	drtu.addDocReadTag(docId, f_weaver_belongto_userid, f_weaver_belongto_usertype, request.getRemoteAddr());
    	return apidatas;
    }
    
    
    /* 查询子流程查看范围 */
    class TriggerSetting {
        private String settingId;
        private String isRead;
        private String isReadNodes;

        public void setSettingId(String settingId) {
            this.settingId = settingId;
        }

        public String getSettingId() {
            return this.settingId;
        }

        public void setIsRead(String isRead) {
            this.isRead = isRead;
        }

        public String getIsRead() {
            return this.isRead;
        }

        public void setIsReadNodes(String isReadNodes) {
            this.isReadNodes = isReadNodes;
        }

        public String getIsReadNodes() {
            return this.isReadNodes;
        }
    }
    
    
    class SignRequestInfo{
    	private String requestid;
    	private String requestname;
    	//main:主流程  sub:子流程:parallel 平行流程
    	private String type;
    	
    	private String signshowname;
    	private String relviewlogs;
    	private String relwfid;
    	private Boolean loadbyuser;
    	
		public String getRequestid() {
			return requestid;
		}
		public void setRequestid(String requestid) {
			this.requestid = requestid;
		}
		public String getRequestname() {
			return requestname;
		}
		public void setRequestname(String requestname) {
			this.requestname = requestname;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getSignshowname() {
			return signshowname;
		}
		public void setSignshowname(String signshowname) {
			this.signshowname = signshowname;
		}
		public String getRelviewlogs() {
			return relviewlogs;
		}
		public void setRelviewlogs(String relviewlogs) {
			this.relviewlogs = relviewlogs;
		}
		public String getRelwfid() {
			return relwfid;
		}
		public void setRelwfid(String relwfid) {
			this.relwfid = relwfid;
		}
		public Boolean getLoadbyuser() {
			return loadbyuser;
		}
		public void setLoadbyuser(Boolean loadbyuser) {
			this.loadbyuser = loadbyuser;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((requestid == null) ? 0 : requestid.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final SignRequestInfo other = (SignRequestInfo) obj;
			if (requestid == null) {
				if (other.requestid != null)
					return false;
			} else if (!requestid.equals(other.requestid))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		} 
		
		
    }  
}
