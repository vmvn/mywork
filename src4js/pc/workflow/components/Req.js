import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import * as ReqAction from '../actions/req'

import {Button,Table,Spin,Popover } from 'antd'
import FormLayout from './form/FormLayout'
import Sign from './sign/Sign'
import ImgZoom from './sign/ImgZoom'
import Status from './status/Status'
import Resources from './resources/Resources'
import Share from './share/Share'


import {WeaNewTop,WeaNewTopReq,WeaRightMenu,WeaLoading} from 'weaCom'
import {Synergy} from 'weaPortal';

import PropTypes from 'react-router/lib/PropTypes'
import Immutable from 'immutable'
const is = Immutable.is;

import {WeaErrorPage,WeaTools,WeaPopoverHrm} from 'weaCom'

class Req extends React.Component {
    static contextTypes = {
        router: PropTypes.routerShape
    }
    constructor(props) {
		super(props);
        const {requestid,preloadkey,comemessage} = props.location.query;
        const {actions} = props;
        actions.initFormLayout(requestid, preloadkey,comemessage);
    }
    resetHeight(){
		let height = jQuery(".wea-new-top-req-content").height() ? jQuery(".wea-new-top-req-content").height() : 500;
        jQuery(".req-workflow-map").height(height-5);
	}
    componentWillReceiveProps(nextProps,nextState){
    	if(window.location.pathname == '/spa/workflow/index.jsp' && nextProps.formValue && nextProps.formValue.getIn(['field-1','value']) && document.title !== nextProps.formValue.getIn(['field-1','value']))
    		document.title = nextProps.formValue.getIn(['field-1','value'])
    }
    componentDidMount() {
		const {actions,isShowSignInput,reqIsReload,params} = this.props;
		this.resetHeight()
		actions.reqIsSubmit(false);
		actions.reqIsReload(false);
		actions.isClickBtnReview(false);
		actions.setShowUserlogid('');
		actions.setIsLoadingLog(false);
		const that = this;
		
		//滚动加载
		jQuery('.wea-new-top-req-content').scroll(function(){
            formImgLazyLoad(jQuery('.wea-new-top-req-content'));        //图片懒加载
			const {logParams,isLoadingLog,logCount,logList,params} = that.props;
			const signListType = params.get('signListType');
			const top =jQuery(this).scrollTop();
			if(signListType){
				const windowheight = jQuery('.wea-new-top-req-content').height();
				const bodyheight = jQuery('.wea-popover-hrm-relative-parent').height();
				const pgnumber = logParams.get('pgnumber');
				if((top + windowheight) == bodyheight && !isLoadingLog && logList.size < logCount){
					actions.setIsLoadingLog(true);
					actions.scrollLoadSign({pgnumber:parseInt(pgnumber)+1,firstload:false});
				}
			}
			
			jQuery('#edui_fixedlayer>div').css('display','none');
		});
    }
	// componentDidUpdate(){
	// 	const {reqIsSubmit,reqIsReload,actions,requestid,pathBack} = this.props;
	// 	const {router} = this.context;
	// 	this.resetHeight()
	// 	if(reqIsSubmit){
	// 		//window.opener.reLoad();
	// 		try{
	// 			window.opener._table.reLoad();
	// 		}catch(e){}
	// 		try{
	// 			//刷新门户流程列表
	// 			jQuery(window.opener.document).find('#btnWfCenterReload').click();
	// 		}catch(e){}
			
	// 		window.close();
	// 		//router.push(`/main/workflow/${pathBack}`);
	// 	}
	
	// 	if(reqIsReload){
	// 		actions.initFormLayout(requestid);
	// 		actions.reqIsReload(false);
	// 	}
	// }
    shouldComponentUpdate(nextProps,nextState) {
        return this.props.loading!==nextProps.loading||
        !is(this.props.params,nextProps.params)||
        !is(this.props.tableInfo,nextProps.tableInfo)||
        !is(this.props.formValue,nextProps.formValue)||
        !is(this.props.formLayout,nextProps.formLayout)||
        !is(this.props.formValue4Detail,nextProps.formValue4Detail)||
        !is(this.props.reqTabKey,nextProps.reqTabKey)||
        !is(this.props.workflowStatus,nextProps.workflowStatus)||
        !is(this.props.logList,nextProps.logList)||
        !is(this.props.logListTabKey,nextProps.logListTabKey)||
        !is(this.props.logCount,nextProps.logCount)||
        !is(this.props.resourcesKey,nextProps.resourcesKey)||
        !is(this.props.resourcesDatas,nextProps.resourcesDatas)||
        !is(this.props.resourcesCount,nextProps.resourcesCount)||
        !is(this.props.resourcesColumns,nextProps.resourcesColumns)||
        !is(this.props.resourcesOperates,nextProps.resourcesOperates)||
        !is(this.props.resourcesCurrent,nextProps.resourcesCurrent)||
        !is(this.props.resourcesPageSize,nextProps.resourcesPageSize)||
        !is(this.props.resourcesTabKey,nextProps.resourcesTabKey)||
        !is(this.props.rightMenu,nextProps.rightMenu)||
        !is(this.props.reqIsSubmit,nextProps.reqIsSubmit)||
        !is(this.props.cellInfo,nextProps.cellInfo)||
        !is(this.props.reqIsReload,nextProps.reqIsReload)||
        !is(this.props.isShowSignInput,nextProps.isShowSignInput)||
        this.props.scriptcontent !== nextProps.scriptcontent||
        this.props.custompagehtml !== nextProps.custompagehtml||
        this.props.reqsubmiterrormsghtml !== nextProps.reqsubmiterrormsghtml||
        !is(this.props.isclickbtnreview,nextProps.isclickbtnreview)||
        this.props.signFields !== nextProps.signFields||
        this.props.showBackToE8 !== nextProps.showBackToE8||
        this.props.showSearchDrop !== nextProps.showSearchDrop||
        !is(this.props.isShowUserheadimg,nextProps.isShowUserheadimg)||
        //性能测试
        this.props.reqLoadDuration !== nextProps.reqLoadDuration ||
        this.props.jsLoadDuration !== nextProps.jsLoadDuration ||
        this.props.apiDuration !== nextProps.apiDuration ||
        this.props.dispatchDuration !== nextProps.dispatchDuration ||
        
        this.props.showuserlogids !== nextProps.showuserlogids||
        this.props.reqRequestId !== nextProps.reqRequestId;
    }
    
    componentWillUnmount() {
        const {actions} = this.props;
        actions.clearForm();
        actions.setReqTabKey('1');
    }
    render() {
        const {reqLoadDuration,jsLoadDuration,apiDuration,dispatchDuration,
        	signFields,showSearchDrop,params,formLayout,tableInfo,formValue,formValue4Detail,loading,markInfo,logList,cellInfo,location,logCount,workflowStatus,actions,logParams,resourcesDatas,
            resourcesOperates,resourcesCount,resourcesColumns,resourcesCurrent,resourcesPageSize,resourcesTabKey,reqTabKey,logListTabKey,isShowSignInput,initSignInput,scriptcontent,
            custompagehtml,isShowUserheadimg,reqsubmiterrormsghtml,isclickbtnreview,rightMenu,showBackToE8,showuserlogids,reqRequestId,relLogParams} = this.props;
        const {requestid} = location.query;
        const titleName = params?params.get("titlename"):"";
        const isshared = params?params.get("isshared"):"";
        const userId = params?params.get("f_weaver_belongto_userid"):"";
        const ismanagePage = params?params.get('ismanagePage'):'';
        const etables = formLayout.getIn(["eformdesign","etables"]);
        const workflowid = params?params.get("workflowid"):"";
        const colheads = etables?etables.getIn(["emaintable","colheads"]):null;
        const rowheads = etables?etables.getIn(["emaintable","rowheads"]):null;
        const colattrs = etables?etables.getIn(["emaintable","colattrs"]):null;
        const rowattrs = etables?etables.getIn(["emaintable","rowattrs"]):null;
        const ec = etables?etables.getIn(["emaintable","ec"]):null;
        const ecMap = ec?Immutable.Map(ec.map(v => [v.get('id'), v])):null;
        const requestLogParams = Immutable.fromJS(logParams.get('requestLogParams') ? JSON.parse(logParams.get('requestLogParams')) : {});
        const forward = rightMenu?rightMenu.get('forward'):'';
        const current = logListTabKey > 2 ? (relLogParams.get('pgnumber') ? relLogParams.get('pgnumber') :1):(logParams.get('pgnumber') ? logParams.get('pgnumber') : 1 );
        let tabDatas = [
			{title:'流程表单',key:"1"},
           	{title:'流程图',key:"2"},
           	{title:'流程状态',key:"3"},
           	{title:'相关资源',key:"4"},
		];
		if(requestid > 0 && isshared && isshared == '1' && false){
			tabDatas.push({title:'流程共享',key:"5"})
		}
        let style = {margin:"0 auto"};
        let find = false;
        colheads && colheads.map((v,k)=>{
            if(v.indexOf("%")>=0) {
                find = true;
            }
        })
        if(find) style.width = "100%";
        let hiddenarea = [];
        params.get('hiddenarea') && params.get('hiddenarea').mapEntries(o =>{
			hiddenarea.push(<input type="hidden" id={o[0]} name={o[0]} value={o[1]}/>)
		})
        let formarea = [];
        formValue.mapEntries && formValue.mapEntries(f => {
            let domfieldid = f[0];
            let domfieldvalue = f[1] && f[1].get("value");
            if(domfieldid == "field-1")
                domfieldid = "requestname";
            else if(domfieldid == "field-2")
                domfieldid = "requestlevel";
            else if(domfieldid == "field-3")
                domfieldid = "messageType"
            else if(domfieldid == "field-5")
                domfieldid = "chatsType";
            formarea.push(<input type="hidden" id={domfieldid} name={domfieldid} value={domfieldvalue} />)
        });
        return (
            <div>
            	<WeaRightMenu btns={this.getHideButtons()} >
 				<WeaNewTopReq 
                	title={<div dangerouslySetInnerHTML={{__html: titleName}} />} 
                	loading={loading} 
                	icon={<i className='icon-portal-workflow' />} 
                	iconBgcolor='#55D2D4' 
                	buttons={this.getButtons()} 
                	hideButtons={this.getHideButtons()} 
                	tabDatas={tabDatas} 
                    selectedKey={reqTabKey}
                    onChange={this.changeData.bind(this)}>
 					<WeaPopoverHrm>
                    	<div className='wea-req-workflow-wrapper'>
                    		{reqTabKey == '1' &&
                    			<div id="reqsubmiterrormsghtml" dangerouslySetInnerHTML={{__html:reqsubmiterrormsghtml}}></div>
                    		}
                    		<div className='wea-req-workflow-form' style={{display:reqTabKey == '1' ? 'block' : 'none',margin:"0 auto"}}>
			                    {etables&&
			                    <FormLayout 
	                                symbol="emaintable"
			                        className="excelMainTable" 
			                        etables={etables} 
			                        colheads={colheads} 
			                        rowheads={rowheads}
                                    rowattrs={rowattrs}
                                    colattrs={colattrs}
			                        ecMap={ecMap} 
	                                cellInfo={cellInfo}
			                        style={style}
                                    tableInfo={tableInfo}
                                    formValue={formValue}
			                        formValue4Detail={formValue4Detail}  />
			                    }
			                </div>
	                        <div id="scriptcontent" style={{display:reqTabKey == '1' ? 'block' : 'none'}}></div>
	                        <div id="custompage" style={{display:reqTabKey == '1' ? 'block' : 'none'}}></div>
	                        <input type="hidden" id="e9form_review" value='1'/>
                    		<div className='wea-req-workflow-loglist' style={{display:reqTabKey == '1' ? 'block' : 'none'}}>
				                {etables &&<Sign 
					                signinputinfo={params.get('signinputinfo')}
					                logList={logList}
					                actions={actions}
					                logListTabKey={logListTabKey}
					                isShowUserheadimg={isShowUserheadimg}
					                userId={userId}
	                    			requestLogParams = {requestLogParams}
					                onChange={n=>actions.setlogParams({pgnumber:n,firstload:false})}
					                current={current}
					                pagesize={logParams.get('logpagesize') ? logParams.get('logpagesize') : 10}
					                total={logCount ? logCount : 0}
					                onPageSizeChange={n=>actions.setLogPagesize({logpagesize:n})}
					                ismanagePage={ismanagePage}
					                isShowSignInput={isShowSignInput}
	                    			signFields={signFields}
	                    			showSearchDrop={showSearchDrop}
	                    			forward={forward}
	                    			params={params}
	                    			showuserlogids={showuserlogids}
	                    			reqRequestId={reqRequestId}
	                            />}
			                </div>
                    	{
                    		<div className='wea-req-workflow-picture' style={{display:reqTabKey == '2' ? 'block' : 'none'}}>
                    			<iframe className='req-workflow-map' src='' style={{border:0,width:'100%'}}></iframe>
                    		</div>
                    	}
                    	{
                    		<div className='wea-req-workflow-status' style={{display:reqTabKey == '3' ? 'block' : 'none'}}>
                    			<Status datas={workflowStatus} />
                    		</div>
                    	}
                    	{
                    		reqTabKey == '4' && 
	                    		<Resources 
	                    			loading={loading}
	                    			actions={actions}
	                    			datas={resourcesDatas}
	                    			operates={resourcesOperates}
	                    			columns={resourcesColumns}
	                    			count={resourcesCount}
	                    			current={resourcesCurrent}
                                    pageSize={resourcesPageSize}
	                    			tabKey={resourcesTabKey}
	                    			/>
                    	}
                    	{
                    		reqTabKey == '5' && false && 
	                    		<Share />
                    	}
		                { reqTabKey == '1' && etables && window.location.pathname == '/spa/workflow/index.jsp' && 
		                	<Popover trigger="click" content={
		                		<div>
				                	<p>js加载耗时: {jsLoadDuration} 毫秒</p>
				                	<p>接口耗时: {apiDuration} 毫秒</p>
				                	<p>渲染耗时: {dispatchDuration} 毫秒</p>
				            	</div>
		                	}>
			                	<div style={{width:'100%',textAlign:'center',color:'#d4d4d4'}}>
			                		页面加载耗时: <span style={{fontWeight:800}}>{reqLoadDuration / 1000}</span> 秒
			                	</div>
		                	</Popover>
		                }
                    	</div>
                    </WeaPopoverHrm>
 				</WeaNewTopReq>
 				</WeaRightMenu>
                <form>
                	{hiddenarea}
                	{formarea}
                </form>
                <div className='back_to_old_req' 
                	onMouseEnter={()=>actions.setShowBackToE8(true)} 
                	onMouseLeave={()=>actions.setShowBackToE8(false)}
                	onClick={()=>{openFullWindowHaveBarForWFList('/workflow/request/ViewRequest.jsp?requestid=' + requestid + '&isovertime=0',848)}}>
                	<div style={{display: showBackToE8 ? 'block' : 'none'}}>
	                	<p>E8</p>
	                	<p>模式</p>
                	</div>
                </div>
                <ImgZoom />
                <Synergy pathname='/workflow/req' workflowid={workflowid} requestid={requestid} />
            </div>
        )
    }
    changeData(key){
    	const {actions,location,workflowStatus,resourcesKey,params} = this.props
    	const {requestid} = location.query;
    	const workflowid = params?params.get("workflowid"):"";
    	const nodeid = params?params.get("nodeid"):"";
    	const modeid = params?params.get("modeid"):"";
    	const formid = params?params.get("formid"):"";
    	const isbill = params?params.get("isbill"):"";
    	
    	key == '2' && !jQuery('.req-workflow-map').attr('src') && jQuery('.req-workflow-map').attr('src',
    	`/workflow/request/WorkflowRequestPictureInner.jsp?f_weaver_belongto_userid=&f_weaver_belongto_usertype=&fromFlowDoc=&modeid=${modeid}&requestid=${requestid}&workflowid=${workflowid}&nodeid=${nodeid}&isbill=${isbill}&formid=${formid}&showE9Pic=1`);
    	key == "3" && is(workflowStatus,Immutable.fromJS({})) && actions.getWorkflowStatus(requestid);
    	key == "4" && resourcesKey.get('key0') == '' && actions.getResourcesKey(requestid);
    	actions.setReqTabKey(key);
    }
    getButtons() {
    	const {rightMenu,pathBack,loading,actions,params} = this.props;
        const {router} = this.context;
        const ismanagePage = params?params.get('ismanagePage'):'';
        let btnArr = [];
        rightMenu && !is(rightMenu,Immutable.fromJS({})) && rightMenu.get('rightMenus').map(m=>{
        	let fn = m.get('menuFun').indexOf('this') >= 0 ? `${m.get('menuFun').split('this')[0]})` : m.get('menuFun');
        	m.get('isTop') == '1' && btnArr.length < 4 && btnArr.push(<Button type="primary" disabled={loading} onClick={()=>{eval(fn)}}>{m.get('menuName')}</Button>)
        });
       	window.location.pathname != '/spa/workflow/index.jsp' && btnArr.push(<Button type="ghost" onClick={this.gobackpage.bind(this,router,ismanagePage)}>返回</Button>)
//      btnArr.push(<Button type="ghost" onClick={()=>{router.push(`/main/workflow/${pathBack}`)}}>返回</Button>)

        return btnArr
    }
    getHideButtons() {
    	const {rightMenu} = this.props;
        let btnArr = [];
        rightMenu && !is(rightMenu,Immutable.fromJS({})) && rightMenu.get('rightMenus').map(m=>{
        	let fn = m.get('menuFun').indexOf('this') >= 0 ? `${m.get('menuFun').split('this')[0]})` : m.get('menuFun');
	        btnArr.push(<a href="javascript:void(0)" onClick={()=>{eval(fn)}}><i className={m.get('menuIcon') || 'icon-top-search'} style={{marginRight:10,verticalAlign:'middle'}} />{m.get('menuName')}</a>)
        });
        return btnArr
    }
    
    doReviewE9(actions){
		actions.isClickBtnReview(true);
    }
    
    gobackpage(router,ismanagePage){
    	if(ismanagePage == '1'){
    		UEUtil.getUEInstance('remark').destroy();
    	}
    	router.goBack();
    }
}

const fliterNull = (obj,param)=>{
    if(!obj) return null;
    if(!obj[param]) return null;
    return obj[param];
}

class MyErrorHandler extends React.Component {
    render(){
        const hasErrorMsg = this.props.error && this.props.error!=="";
        return (
            <WeaErrorPage msg={hasErrorMsg?this.props.error:"对不起，该页面异常，请联系管理员！"} />
        );
    }
}

Req = WeaTools.tryCatch(React, MyErrorHandler, {error: ""})(Req);

function mapStateToProps(state) {
    const {workflowReq,workflowlistDoing} = state;
    return {
        params:workflowReq.get("params"),
        loading:workflowReq.get("loading"),
        formLayout:workflowReq.get("formLayout"),
        tableInfo:workflowReq.get("tableInfo"),
        formValue:workflowReq.get("formValue"),
        formValue4Detail:workflowReq.get("formValue4Detail"),
        logList:workflowReq.get("logList"),
        logParams:workflowReq.get("logParams"),
        logCount:workflowReq.get("logCount"),
        logListTabKey:workflowReq.get("logListTabKey"),
        cellInfo:workflowReq.get("cellInfo"),
        workflowStatus:workflowReq.get("workflowStatus"),
        resourcesKey:workflowReq.get("resourcesKey"),
        resourcesDatas:workflowReq.get("resourcesDatas"),
        resourcesCount:workflowReq.get("resourcesCount"),
        resourcesColumns:workflowReq.get("resourcesColumns"),
        resourcesOperates:workflowReq.get("resourcesOperates"),
        resourcesCurrent:workflowReq.get("resourcesCurrent"),
        resourcesPageSize:workflowReq.get("resourcesPageSize"),
        resourcesTabKey:workflowReq.get("resourcesTabKey"),
        rightMenu:workflowReq.get("rightMenu"),
        reqTabKey:workflowReq.get("reqTabKey"),
        reqIsSubmit:workflowReq.get("reqIsSubmit"),
        pathBack:workflowlistDoing.get("nowRouterWfpath"),
        reqIsReload:workflowReq.get("reqIsReload"),
        requestid:workflowReq.get("params").get('requestid'),
        isShowSignInput:workflowReq.get('isShowSignInput')?workflowReq.get('isShowSignInput'):false,
        scriptcontent:workflowReq.get("scriptcontent"),
        custompagehtml:workflowReq.get('custompagehtml'),
        isShowUserheadimg:workflowReq.get('isShowUserheadimg'),
        reqsubmiterrormsghtml:workflowReq.getIn(['dangerouslyhtml','reqsubmiterrormsghtml']),
        isclickbtnreview:workflowReq.getIn(['btnStatus','isclickbtnreview']),
        signFields:workflowReq.get('signFields'),
        showSearchDrop:workflowReq.get('showSearchDrop'),
        showBackToE8:workflowReq.get('showBackToE8'),
        showuserlogids:workflowReq.get('showuserlogids'),
        reqRequestId:workflowReq.get('reqRequestId'),
        relLogParams:workflowReq.get('relLogParams'),
        isLoadingLog:workflowReq.get('isLoadingLog'),
        reqLoadDuration:workflowReq.get('reqLoadDuration'),
        jsLoadDuration: workflowReq.get('jsLoadDuration'),
		apiDuration: workflowReq.get('apiDuration'),
		dispatchDuration: workflowReq.get('dispatchDuration'),
    }
}

function mapDispatchToProps(dispatch) {
    return {
        actions: bindActionCreators(ReqAction, dispatch)
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(Req);