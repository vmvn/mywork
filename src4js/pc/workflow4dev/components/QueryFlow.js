import {bindActionCreators} from 'redux'
import {connect} from 'react-redux'
import * as QueryFlowAction from '../actions/queryFlow'
import {setNowRouterWfpath} from '../actions/list'
import forEach from 'lodash/forEach'
import isEmpty from 'lodash/isEmpty'
import {Synergy} from 'weaPortal';

import {WeaTable} from '../../coms/index'

const WeaTableAction = WeaTable.action;

import {WeaErrorPage,WeaTools} from 'ecCom'

import {
    WeaTab,
    WeaTop,
    WeaSearchGroup,
    WeaRightMenu,
    WeaPopoverHrm,
    WeaLeftRightLayout,
    WeaLeftTree
} from 'ecCom'

import {Button, Form, Modal,message} from 'antd'
const createForm = Form.create;
const FormItem = Form.Item;

import cloneDeep from 'lodash/cloneDeep'
import objectAssign from 'object-assign'

import Immutable from 'immutable'
const is = Immutable.is;
const fromJS = Immutable.fromJS;
const Map = Immutable.Map;

let _this = null;

class QueryFlow extends React.Component {
    constructor(props) {
        super(props);
        _this = this;
        const {query} = props.location;
        this.doInit(query)
    }
    doInit(obj){
    	//hasQuery 是否有url搜索参数
    	//newFieds 是否是高级查询条件
    	//isJSONStr 是否仅有 jsonstr
    	const {actions, location, fields} = this.props;
    	actions.setNowRouterWfpath('queryFlow');
    	let newFields = {};
    	let isJSONStr = false;
    	let hasQuery = !isEmpty(obj);
    	for(let k in obj){
    		newFields[k] = {name:k,value:`${obj[k]}`,dirty:false}
    		newFields[`_${k}`] = {name:`_${k}`,value:`${obj[k]}`,dirty:false}
    		if(k === 'jsonstr') {
    			isJSONStr = true
    		}
    	}
        actions.initDatas();
		actions.updateDisplayTable(hasQuery);
		actions.initTree(isJSONStr ? obj : {});
    	actions.saveFields(isJSONStr ? {} : newFields);
    	actions.setSearchParams(isJSONStr ? obj : {});
    	hasQuery && actions.doSearch(isJSONStr ? obj : {});
    }
    componentDidMount() {

    }
    componentWillReceiveProps(nextProps) {
        const keyOld = this.props.location.key;
        const keyNew = nextProps.location.key;
    	if(window.location.pathname.indexOf('/spa/workflow/index') >= 0 && nextProps.title && document.title !== nextProps.title)
    		document.title = nextProps.title
        if(keyOld!==keyNew) {
            const {actions} = this.props;
            const {query} = nextProps.location;
        	this.doInit(query);
            actions.setSelectedTreeKeys();
            actions.setSelectedRowKeys();
        }

    }
    shouldComponentUpdate(nextProps, nextState) {
        return !is(this.props.title, nextProps.title) ||
            !is(this.props.condition, nextProps.condition) ||
            !is(this.props.searchParamsAd, nextProps.searchParamsAd)||
            !is(this.props.fields, nextProps.fields)||
            !is(this.props.showTable,nextProps.showTable)||
            !is(this.props.comsWeaTable,nextProps.comsWeaTable)||
            !is(this.props.showSearchAd,nextProps.showSearchAd)||
            !is(this.props.btninfo, nextProps.btninfo);
    }
    componentWillUnmount() {
        const {actions} = this.props;
        actions.saveFields();
        actions.setSelectedTreeKeys();
        actions.updateDisplayTable(false);
        actions.setSearchParams();
        actions.setSelectedRowKeys();
        actions.setShowSearchAd(false)
    }
    render() {
        const isSingle = window.location.pathname == '/spa/workflow/index.jsp';
        const {
        	loading,
        	comsWeaTable,
            title,
            dataKey,
            showTable,
            actions,
            searchParamsAd,
            showSearchAd,
            fields
        } = this.props;
        const tablekey = dataKey ? dataKey.split('_')[0] : 'init';
		const tableNow = comsWeaTable.get(tablekey);
		const loadingTable = tableNow.get('loading');
        const selectedRowKeys = tableNow.get('selectedRowKeys');
        return (
            <div className='wea-workflow-query'>
            	{isSingle && <WeaPopoverHrm />}
            	<WeaRightMenu datas={this.getRightMenu()} onClick={this.onRightMenuClick.bind(this)}>
                <WeaTop
                	title={title}
                	loading={loading || loadingTable}
                	icon={<i className='icon-portal-workflow' />}
                	iconBgcolor='#55D2D4'
                	buttons={this.getButtons()}
                	buttonSpace={10}
                	showDropIcon={true}
                	dropMenuDatas={this.getRightMenu()}
                	onDropMenuClick={this.onRightMenuClick.bind(this)}
                >
                   {showTable ? <div style={{height: '100%'}}>
                    	<WeaLeftRightLayout leftCom={this.getTree()} leftWidth={25} >
	                        <WeaTab
	                            onlyShowRight={true}
	                            buttonsAd={this.getTabButtonsAd()}
	                            searchType={['base','advanced']}
	                            searchsBaseValue={searchParamsAd.get('requestname')}
	                            setShowSearchAd={bool=>{actions.setShowSearchAd(bool)}}
                                hideSearchAd={()=> actions.setShowSearchAd(false)}
	                            searchsAd={<Form horizontal>{this.getSearchs(true)}</Form>}
	                            showSearchAd={showSearchAd}
	                            onSearch={v=>{actions.doSearch()}}
	                        	onSearchChange={v=>{actions.saveFields({...fields.toJS(),requestname:{name:'requestname',value:v},_requestname:{name:'_requestname',value:v}})}}
	                            />
	                        <WeaTable 
	                        	sessionkey={dataKey}
		                    	hasOrder={true}
		                    	needScroll={true}
		                    	/>
	                	</WeaLeftRightLayout>
                    </div> :
                    <div className='wea-workflow-query-search'>
                        <Form horizontal>{this.getSearchs()}</Form>
                        <div className='wea-workflow-query-btns'>
                            {this.getSearchButtons()}
                        </div>
                    </div>
                    }
                </WeaTop>
                </WeaRightMenu>
                <Synergy pathname='/workflow/queryFlow' requestid="-1" />
            </div>
        )
    }
    onRightMenuClick(key){
    	const { dataKey, comsWeaTable, actions} = this.props;
		const tablekey = dataKey ? dataKey.split('_')[0] : 'init';
		const tableNow = comsWeaTable.get(tablekey);
    	const selectedRowKeys = tableNow.get('selectedRowKeys');
    	if(key == '0'){
    		actions.doSearch();
    		actions.setShowSearchAd(false);
    		actions.updateDisplayTable(true);
    	}
    	if(key == '1'){
    		actions.batchShareWf(`${selectedRowKeys.toJS()}`)
    	}
    	if(key == '2'){
    		actions.setColSetVisible(dataKey,true);
    		actions.tableColSet(dataKey,true)
    	}
    }
    getRightMenu(){
    	const { dataKey, comsWeaTable, showTable } = this.props;
		const tablekey = dataKey ? dataKey.split('_')[0] : 'init';
		const tableNow = comsWeaTable.get(tablekey);
    	const selectedRowKeys = tableNow.get('selectedRowKeys');
    	let btns = [];
    	btns.push({
    		icon: <i className='icon-Right-menu--search'/>,
    		content:'搜索'
    	});
        showTable && btns.push({
         	icon: <i className='icon-Right-menu-Batch-sharing'/>,
			content:'批量共享',
			disabled: !selectedRowKeys || !`${selectedRowKeys.toJS()}`
        })
    	showTable && btns.push({
    		icon: <i className='icon-Right-menu-Custom'/>,
    		content:'显示定制列'
    	})
    	return btns
    }
    getSearchs(bool = false) {
    	const { condition, showTable } = this.props;
		let group = [];
		condition.toJS().map(c =>{
			let items = [];
			c.items.map(fields => {
				const domkeys = fields.domkey.map(k =>{return (bool ? k : `_${k}`)})
				items.push({
	                com:(<FormItem
	                    label={`${fields.label}`}
	                    labelCol={{span: `${fields.labelcol}`}}
	                    wrapperCol={{span: `${fields.fieldcol}`}}>
	                        { WeaTools.switchComponent(this.props, fields.key, domkeys, fields )}
	                    </FormItem>),
	                colSpan:1
	            })
			});
			group.push(<WeaSearchGroup needTigger={showTable} title={c.title} showGroup={c.defaultshow} items={items}/>)
		});
		return group
    }
    getTree() {
        const {leftTree,actions,searchParams,selectedTreeKeys} = this.props;
        return (
            <WeaLeftTree
                datas={leftTree && leftTree.toJS()}
                selectedKeys={selectedTreeKeys && selectedTreeKeys.toJS()}
                onFliterAll={()=>{
                	actions.setShowSearchAd(false);
                	actions.setSelectedTreeKeys();
                	actions.saveFields();
                    actions.doSearch();
                }}
                onSelect={(key)=>{
                	actions.setShowSearchAd(false);
                	actions.setSelectedTreeKeys([key]);

                	const workflowid = key.indexOf("wf_")===0 ? key.substring(3) : '';
                	const typeid = key.indexOf("type_")===0 ? key.substring(5) : '';
                	let workflowidShowName = '';
                	let typeidShowName = '';
                	leftTree && leftTree.map(l=>{
                		if(l.get('domid') == key) typeidShowName = l.get('name');
                		l.get('childs') && l.get('childs').map(c=>{
                			if(c.get('domid') == key) workflowidShowName = c.get('name');
                		})
                	})
                	const fieldsObj = {
                		workflowid:{name:'workflowid',value:workflowid,valueSpan:workflowidShowName},
                		_workflowid:{name:'_workflowid',value:workflowid,valueSpan:workflowidShowName},
                		typeid:{name:'typeid',value:typeid,valueSpan:typeidShowName},
                		_typeid:{name:'_typeid',value:typeid,valueSpan:typeidShowName}
                	};
                	actions.saveFields(fieldsObj);
                    actions.doSearch();
                }} />
        )
    }
    getTabButtonsAd() {
        const {actions,searchParamsAd} = this.props;
        return [
            (<Button type="primary" onClick={()=>{actions.doSearch();actions.setShowSearchAd(false)}}>搜索</Button>),
            (<Button type="ghost" onClick={()=>{actions.saveFields();actions.setSelectedTreeKeys();}}>重置</Button>),
            (<Button type="ghost" onClick={()=>{actions.setShowSearchAd(false)}}>取消</Button>)
        ]
    }
    getSearchButtons() {
        const { actions,searchParamsAd} = this.props;
        const btnStyle={
        	borderRadius: 3,
			height: 28,
			width: 80
        }
        return [
            (<Button type="primary" style={btnStyle} onClick={()=>{actions.doSearch(); actions.updateDisplayTable(true);actions.setShowSearchAd(false)}}>搜索</Button>),
            (<span style={{width:'15px', display:'inline-block'}}></span>),
            (<Button type="ghost" style={btnStyle} onClick={()=>{actions.saveFields();actions.setShowSearchAd(false)}}>重置</Button>)
        ]
    }
    getButtons() {
        const { dataKey, comsWeaTable, actions, showTable } = this.props;
		const tablekey = dataKey ? dataKey.split('_')[0] : 'init';
		const tableNow = comsWeaTable.get(tablekey);
    	const selectedRowKeys = tableNow.get('selectedRowKeys');
        let btns =[];
        showTable && btns.push(<Button type="primary" disabled={!(selectedRowKeys && `${selectedRowKeys.toJS()}`)} onClick={()=>{actions.batchShareWf(`${selectedRowKeys.toJS()}`)}}  >批量共享</Button>);
        return btns
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

QueryFlow = WeaTools.tryCatch(React, MyErrorHandler, {error: ""})(QueryFlow);

QueryFlow = createForm({
    onFieldsChange(props, fields) {
    	let __fields = {};
    	for(let k in fields){
    		let __obj = {...fields[k]};
    		__obj.name = fields[k].name.indexOf('_') < 0 ? `_${fields[k].name}` : fields[k].name.substring(1);
    		__obj.value = fields[k].value;
    		__obj.valueSpan = fields[k].valueSpan;
    		__fields[k.indexOf('_') < 0 ? `_${k}` : k.substring(1)] = {...__obj};
    	}
        props.actions.saveFields({...props.fields.toJS(), ...fields, ...__fields});
    },
    mapPropsToFields(props) {
        return props.fields.toJS();
    }
})(QueryFlow);

function mapStateToProps(state) {
    const {workflowqueryFlow,comsWeaTable} = state;
    return {
    	loading: workflowqueryFlow.get('loading'),
        title: workflowqueryFlow.get('title'),
        condition: workflowqueryFlow.get('condition'),
        fields: workflowqueryFlow.get('fields'),
        searchParamsAd: workflowqueryFlow.get('searchParamsAd'),
        dataKey: workflowqueryFlow.get('dataKey'),
        showTable: workflowqueryFlow.get('showTable'),
        showSearchAd: workflowqueryFlow.get('showSearchAd'),
        leftTree: workflowqueryFlow.get('leftTree'),
        searchParams:workflowqueryFlow.get('searchParams'),
        selectedTreeKeys:workflowqueryFlow.get('selectedTreeKeys'),
        //table
        comsWeaTable, //绑定整个table
    }
}

function mapDispatchToProps(dispatch) {
    return {
        actions: bindActionCreators({...QueryFlowAction,setNowRouterWfpath,...WeaTableAction}, dispatch)
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(QueryFlow);