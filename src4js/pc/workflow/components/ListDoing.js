import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import * as ListAction from '../actions/list'
import forEach from 'lodash/forEach'
import isEmpty from 'lodash/isEmpty'
import BatchSubmit from './list/BatchSubmit'
import {Synergy} from 'weaPortal';

import {WeaTable} from '../../coms/index'

const WeaTableAction = WeaTable.action;

import {
    WeaTop,
    WeaTab,
    WeaLeftTree,
    WeaLeftRightLayout,
    WeaSearchGroup,
    WeaRightMenu,
    WeaPopoverHrm
} from 'ecCom'

import {WeaErrorPage,WeaTools} from 'ecCom'

import {Button,Form,Modal,message} from 'antd'
const createForm = Form.create;
const FormItem = Form.Item;

import cloneDeep from 'lodash/cloneDeep'
import objectAssign from 'object-assign'

import PropTypes from 'react-router/lib/PropTypes'
import Immutable from 'immutable'
const is = Immutable.is;

let _this = null;

class ListDoing extends React.Component {
//	static contextTypes = {
//      router: PropTypes.routerShape
//  }
    constructor(props) {
		super(props);
		_this = this;
    }
    componentDidMount() {
    	const {actions} = this.props;
        actions.setNowRouterWfpath('listDoing');
        actions.initDatas({method:"all"});
        actions.doSearch();
    }
    componentWillReceiveProps(nextProps) {
        const keyOld = this.props.location.key;
        const keyNew = nextProps.location.key;
        if(keyOld!==keyNew) {
            const {actions,isClearNowPageStatus} = this.props;
            actions.unmountClear(isClearNowPageStatus);
            actions.isClearNowPageStatus(false);

            actions.setNowRouterWfpath('listDoing');
            actions.initDatas({method:"all"});
            actions.doSearch();
        }
        if(window.location.pathname.indexOf('/spa/workflow/index') >= 0 && nextProps.title && document.title !== nextProps.title)
            document.title = nextProps.title;
    }
    shouldComponentUpdate(nextProps,nextState) {
        return !is(this.props.title,nextProps.title)||
        !is(this.props.leftTree,nextProps.leftTree)||
        !is(this.props.leftTreeCount,nextProps.leftTreeCount)||
        !is(this.props.leftTreeCountType,nextProps.leftTreeCountType)||
        !is(this.props.topTab,nextProps.topTab)||
        !is(this.props.topTabCount,nextProps.topTabCount)||
        !is(this.props.searchParams,nextProps.searchParams)||
        !is(this.props.searchParamsAd,nextProps.searchParamsAd)||
        !is(this.props.orderFields,nextProps.orderFields)||
        !is(this.props.showSearchAd,nextProps.showSearchAd)||
        !is(this.props.showBatchSubmit,nextProps.showBatchSubmit) ||
        !is(this.props.phrasesObj,nextProps.phrasesObj) ||
        !is(this.props.sharearg,nextProps.sharearg) ||
        !is(this.props.selectedTreeKeys,nextProps.selectedTreeKeys)||
        !is(this.props.comsWeaTable,nextProps.comsWeaTable)||
        !is(this.props.isSpaForm,nextProps.isSpaForm)||
        !is(this.props.conditioninfo,nextProps.conditioninfo)||
        !is(this.props.isClearNowPageStatus,nextProps.isClearNowPageStatus);
    }
    componentWillUnmount(){
    	const {actions,isClearNowPageStatus} = this.props;
        actions.unmountClear(isClearNowPageStatus);
        actions.isClearNowPageStatus(false);
    }
    render() {
        let that = this;
        const isSingle = window.location.pathname.indexOf('/spa/workflow/index') >= 0;
        const {comsWeaTable,topTab,topTabCount,actions,title,searchParams,showSearchAd,searchParamsAd,showBatchSubmit,phrasesObj} = this.props;
        const loading = comsWeaTable.get('loading');
        const selectedRowKeys = comsWeaTable.get('selectedRowKeys');
        return (
            <div>
            	{isSingle && <WeaPopoverHrm />}
            	<WeaRightMenu datas={this.getRightMenu()} onClick={this.onRightMenuClick.bind(this)}>
                <WeaTop
                	title={title}
                	loading={loading}
                	icon={<i className='icon-portal-workflow' />}
                	iconBgcolor='#55D2D4'
                	buttons={this.getButtons()}
                	buttonSpace={10}
                	showDropIcon={true}
                	dropMenuDatas={this.getRightMenu()}
                	onDropMenuClick={this.onRightMenuClick.bind(this)}
                >
                <WeaLeftRightLayout defaultShowLeft={true} leftCom={this.getTree()} leftWidth={25}>
                    <WeaTab
                        buttonsAd={this.getTabButtonsAd()}
                    	searchType={['base','advanced']}
                    	searchsBaseValue={searchParamsAd.get('requestname')}
                        setShowSearchAd={bool=>{actions.setShowSearchAd(bool)}}
                        hideSearchAd={()=> actions.setShowSearchAd(false)}
                        searchsAd={<Form horizontal>{this.getSearchs()}</Form>}
                        showSearchAd={showSearchAd}
                        datas={topTab && topTab.toJS()}
                        selectedKey={searchParams.get('viewcondition')}
                        counts={topTabCount && topTabCount.toJS()}
                        onSearch={v=>{actions.doSearch()}}
                        onSearchChange={v=>{actions.saveOrderFields({requestname:{name:'requestname',value:v}})}}
                        keyParam="viewcondition"  //主键
                        countParam="groupid" //数量
                        onChange={this.changeData.bind(this)} />
                    <WeaTable 
                    	hasOrder={true}
                    	needScroll={true}
                    	/>
                </WeaLeftRightLayout>
                {
                    showBatchSubmit &&
                        <BatchSubmit actions={actions} showBatchSubmit={showBatchSubmit} phrasesObj={phrasesObj} selectedRowKeys={`${selectedRowKeys && selectedRowKeys.toJS()}`} />
                }
                </WeaTop>
                </WeaRightMenu>
                <Synergy pathname='/workflow/listDoing' requestid="-1" />
            </div>
        )
    }
    onRightMenuClick(key){
    	const {actions,comsWeaTable} = this.props;
    	const selectedRowKeys = comsWeaTable.get('selectedRowKeys');
    	if(key == '0'){
    		actions.doSearch();
    		actions.setShowSearchAd(false)
    	}
    	if(key == '1'){
    		actions.batchSubmitClick({checkedKeys:`${selectedRowKeys.toJS()}`})
    	}
    	if(key == '2'){
    		actions.setColSetVisible(true);
    		actions.tableColSet(true)
    	}
    }
    getRightMenu(){
    	const {comsWeaTable,sharearg,actions} = this.props;
    	const selectedRowKeys = comsWeaTable.get('selectedRowKeys');
        const hasBatchBtn = sharearg && sharearg.get("hasBatchBtn");
    	let btns = [];
    	btns.push({
    		icon: <i className='icon-Right-menu--search'/>,
    		content:'搜索'
    	});
        btns.push({
            icon: <i className='icon-Right-menu-batch'/>,
            content:'批量提交',
            disabled: hasBatchBtn !=="true" || !selectedRowKeys || !`${selectedRowKeys.toJS()}`
        })
    	btns.push({
    		icon: <i className='icon-Right-menu-Custom'/>,
    		content:'显示定制列'
    	})
    	return btns
    }
    getSearchs() {
        return [
            (<WeaSearchGroup needTigger={true} title={this.getTitle()} showGroup={this.isShowFields()} items={this.getFields()}/>),
            (<WeaSearchGroup needTigger={true} title={this.getTitle(1)} showGroup={this.isShowFields(1)} items={this.getFields(1)}/>)
        ]
    }
    getTitle(index = 0) {
        const {conditioninfo} = this.props;
        return !isEmpty(conditioninfo.toJS()) && conditioninfo.toJS()[index].title
    }
    isShowFields(index = 0) {
        const {conditioninfo} = this.props;
        return !isEmpty(conditioninfo.toJS()) && conditioninfo.toJS()[index].defaultshow
    }
    // 0 常用条件，1 其他条件
    getFields(index = 0) {
        const {conditioninfo} = this.props;
        const fieldsData = !isEmpty(conditioninfo.toJS()) && conditioninfo.toJS()[index].items;
        let items = [];
        forEach(fieldsData, (field) => {
            items.push({
                com:(<FormItem
                    label={`${field.label}`}
                    labelCol={{span: `${field.labelcol}`}}
                    wrapperCol={{span: `${field.fieldcol}`}}>
                        {WeaTools.switchComponent(this.props, field.key, field.domkey, field)}
                    </FormItem>),
                colSpan:1
            })
        })
        return items;
    }
    changeData(theKey) {
        const {actions} = this.props;
        actions.setShowSearchAd(false);
        //const keyArr = theKey?theKey.split("_"):[];
        actions.doSearch({
            viewcondition:theKey //keyArr.length>1?keyArr[1]:""
        },{});
    }
    getTree() {
        const {leftTree,leftTreeCount,leftTreeCountType,actions,topTab,searchParams,selectedTreeKeys,loading} = this.props;
        return (
            <WeaLeftTree
                datas={leftTree && leftTree.toJS()}
                counts={leftTreeCount && leftTreeCount.toJS()}
                countsType={leftTreeCountType && leftTreeCountType.toJS()}
                selectedKeys={selectedTreeKeys && selectedTreeKeys.toJS()}
                loading={loading}
                onFliterAll={()=>{
                	actions.setShowSearchAd(false);
                	actions.setSelectedTreeKeys([]);
                    actions.saveOrderFields();
                    actions.initDatas({method:"all"});
                    actions.doSearch({
                    	method:'all',
                        viewcondition:0,
                    	workflowid:"",
                        wftype:""
                    });
                }}
                onSelect={(key,topTabCount,countsType)=>{
                	actions.setShowSearchAd(false);
                	let viewc = '0';
                	actions.setSelectedTreeKeys([key]);
                	topTab.map(t=>{
                        if(countsType && countsType.name && t.get('groupid') == countsType.name) viewc = t.get('viewcondition')
                	})
                	
                	const workflowid = key.indexOf("wf_")===0 ? key.substring(3) : '';
                	const workflowtype = key.indexOf("type_")===0 ? key.substring(5) : '';
                	let workflowidShowName = '';
                	let workflowtypeShowName = '';
                	leftTree && leftTree.map(l=>{
                		if(l.get('domid') == key) workflowtypeShowName = l.get('name');
                		l.get('childs') && l.get('childs').map(c=>{
                			if(c.get('domid') == key) workflowidShowName = c.get('name');
                		})
                	})
                	const fieldsObj = {
                		workflowid:{name:'workflowid',value:workflowid,valueSpan:workflowidShowName},
                		workflowtype:{name:'workflowtype',value:workflowtype,valueSpan:workflowtypeShowName},
                	};
                	
                	actions.saveOrderFields(fieldsObj);
                    actions.doSearch({
                        method:key.indexOf("type_")===0 ? "reqeustbytype" : "reqeustbywfid",
                        wftype: workflowtype,
                        workflowid: workflowid,
                        viewcondition: viewc,
                    });
                }} />
        )
    }
    getTabButtonsAd() {
    	const {actions} = this.props;
        return [
            (<Button type="primary" onClick={()=>{actions.doSearch();actions.setShowSearchAd(false)}}>搜索</Button>),
            (<Button type="ghost" onClick={()=>{actions.saveOrderFields({})}}>重置</Button>),
            (<Button type="ghost" onClick={()=>{actions.setShowSearchAd(false)}}>取消</Button>)
        ]
    }
    getButtons() {
    	const {comsWeaTable,sharearg,actions} = this.props;
    	const selectedRowKeys = comsWeaTable.get('selectedRowKeys');
        const hasBatchBtn = sharearg && sharearg.get("hasBatchBtn");
    	let btns = [];
        if(hasBatchBtn == "true"){
            btns.push(<Button type="primary" disabled={!(selectedRowKeys && `${selectedRowKeys.toJS()}`)}
                onClick={()=>{actions.batchSubmitClick({checkedKeys:`${selectedRowKeys.toJS()}`})}}>批量提交</Button>)
        }
        return btns;
    }
}

window.openSPA = function(routeUrl,id) {
    //console.log("routeUrl :",routeUrl," id:",id);
    //const {router} = _this.context;
    const {actions,history} = _this.props;
    actions.isClearNowPageStatus(true);
    history.push(routeUrl);
    // history.push({
    //     pathname:routeUrl.split("?")[0],
    //     query:{
    //         requestid:id
    //     }
    // });
}

let Sys = {};
let ua = navigator.userAgent.toLowerCase();
let s;
(s = ua.match(/msie ([\d.]+)/)) ? Sys.ie = s[1] :
(s = ua.match(/firefox\/([\d.]+)/)) ? Sys.firefox = s[1] :
(s = ua.match(/chrome\/([\d.]+)/)) ? Sys.chrome = s[1] :
(s = ua.match(/opera.([\d.]+)/)) ? Sys.opera = s[1] :
(s = ua.match(/version\/([\d.]+).*safari/)) ? Sys.safari = s[1] : 0;

//以下进行测试
// if(Sys.ie) document.write('IE: '+Sys.ie);
// if(Sys.firefox) document.write('Firefox: '+Sys.firefox);
// if(Sys.chrome) document.write('Chrome: '+Sys.chrome);
// if(Sys.opera) document.write('Opera: '+Sys.opera);
// if(Sys.safari) document.write('Safari: '+Sys.safari);



window.openSPA4SingleTab = function(routeUrl,id) {
    window.openSPA4Single(routeUrl,id,true);
}

window.openSPA4Single = function(routeUrl,id,opentab) {
    let obj = jQuery("#hiddenPreLoader").length>0?jQuery("#hiddenPreLoader"):jQuery("#hiddenPreLoaderSingle");

    const preLoadReqInfo = routeUrl =>{
        let url = "/api/workflow/request/reqinfo?actiontype=loadRight&ispreload=1&";
        url += routeUrl.split("?")[1];
        jQuery.ajax({
            type : "GET",
            url : url
        });
    }
    let spaWin = null;
    if(Sys.chrome) {
        $(window).mousedown(function(){});
        obj.attr("src","");
        obj.attr("src","/spa/workflow/index.html");
    }

    const _timekey = new Date().getTime();
    routeUrl += "&preloadkey=" + _timekey;
    preLoadReqInfo(routeUrl);

    const width = screen.availWidth - 10;
	const height = screen.availHeight - 50;
    let szFeatures = "";
    if(!opentab){
	szFeatures = "top=0,";
	szFeatures += "left=0,";
	szFeatures += "width=" + width + ",";
	szFeatures += "height=" + height + ",";
	szFeatures += "directories=no,";
	szFeatures += "status=yes,toolbar=no,location=no,";
	szFeatures += "menubar=no,";
	szFeatures += "scrollbars=yes,";
	szFeatures += "resizable=yes";
    }
    if(Sys.chrome) {
        obj.on("load",function(){
            let spaWin = window.open("/spa/workflow/index.html#"+routeUrl, "_blank", szFeatures);
            if(!spaWin) message.warning("对不起，流程表单弹窗被chrome阻止，请点击浏览器地址栏尾部配置例外！",5);
            $(window).unbind("mousedown");
            obj.unbind("load");
        });
    }
    else {
        window.open("/spa/workflow/index.html#"+routeUrl, "", szFeatures);
    }
}

class MyErrorHandler extends React.Component {
    render(){
        const hasErrorMsg = this.props.error && this.props.error!=="";
        return (
            <WeaErrorPage msg={hasErrorMsg?this.props.error:"对不起，该页面异常，请联系管理员！"} />
        );
    }
}

ListDoing = WeaTools.tryCatch(React, MyErrorHandler, {error: ""})(ListDoing);

ListDoing = createForm({
	onFieldsChange(props, fields) {
        props.actions.saveOrderFields({...props.orderFields.toJS(), ...fields});
    },
	mapPropsToFields(props) {
		return props.orderFields.toJS();
  	}
})(ListDoing);

function mapStateToProps(state) {
	const {workflowlistDoing,comsWeaTable} = state;
    return {
        title: workflowlistDoing.get('title'),
		leftTree: workflowlistDoing.get('leftTree'),
		leftTreeCount: workflowlistDoing.get('leftTreeCount'),
		leftTreeCountType: workflowlistDoing.get('leftTreeCountType'),
		topTab: workflowlistDoing.get('topTab'),
		topTabCount: workflowlistDoing.get('topTabCount'),
		searchParams: workflowlistDoing.get('searchParams'),
		searchParamsAd: workflowlistDoing.get('searchParamsAd'),
		orderFields: workflowlistDoing.get('orderFields'),
		showSearchAd: workflowlistDoing.get('showSearchAd'),
		selectedTreeKeys: workflowlistDoing.get('selectedTreeKeys'),
		isSpaForm: workflowlistDoing.get('isSpaForm'),
		isClearNowPageStatus: workflowlistDoing.get('isClearNowPageStatus'),
		sortParams: workflowlistDoing.get('sortParams'),
		conditioninfo: workflowlistDoing.get('conditioninfo'),
		showBatchSubmit: workflowlistDoing.get('showBatchSubmit'),
		phrasesObj: workflowlistDoing.get('phrasesObj'),
		sharearg: workflowlistDoing.get('sharearg'),
		//table
        comsWeaTable: comsWeaTable.get(comsWeaTable.get('tableNow')), //绑定整个table
    }
}

function mapDispatchToProps(dispatch) {
    return {
        actions: bindActionCreators({...ListAction,...WeaTableAction}, dispatch)
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(ListDoing);
