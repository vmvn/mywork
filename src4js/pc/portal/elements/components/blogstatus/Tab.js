import { Spin } from 'antd';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import * as EContentAction from '../../actions/blogstatus/tab';
import TitleContainer from './Title';
import { Table } from 'antd';
import Immutable from 'immutable';
import BlogStatusCom from './BlogStatus';
//内容组件
class EContent extends React.Component {
    shouldComponentUpdate(nextProps){
        const { config, data, tabid, edata } = this.props;
        return !Immutable.is(config,nextProps.config) || !Immutable.is(data,nextProps.data) || tabid !== nextProps.tabid || !Immutable.is(edata,nextProps.edata)
    }
    render() {
        let contentHtml = <div width = "100%"></div>;
        const { config, data, tabid, edata, actions, handleRefresh } = this.props;
        const { eid } = config.item;
        const { tabids, titles, esetting, counts } = edata.toJSON();
        let tabdata = data.toJSON();
        if (!_isEmpty(tabdata)) {
            const list = tabdata;
            if(list.length > 0)
                contentHtml =  <BlogStatusCom list={list} currTab={tabid} esetting={esetting}/>
        }
        return <div>
            <TitleContainer counts={counts} config={config} tabid={tabid} titles={titles} tabids={tabids} handleRefresh={handleRefresh}/>
            <div className = "tabContant" id = { `tabcontant_${eid}` } >
            { contentHtml }
            </div>
         </div>;
    }
}


import { WeaErrorPage, WeaTools } from 'ecCom';
class MyErrorHandler extends React.Component {
    render() {
        const hasErrorMsg = this.props.error && this.props.error !== "";
            return ( <WeaErrorPage msg = { hasErrorMsg ? this.props.error : "对不起，该页面异常，请联系管理员！" }/>
        );
    }
}
EContent = WeaTools.tryCatch(React, MyErrorHandler, { error: "" })(EContent);
const mapStateToProps = state => {
    const { eblogstatustab } = state;
    return ({
        data: eblogstatustab.get("data"),
        tabid: eblogstatustab.get("tabid")
    })
}

function mapDispatchToProps(dispatch) {
    return { actions: bindActionCreators(EContentAction, dispatch) }
}

function mergeProps(stateProps, dispatchProps, ownProps) {
    const { data, config } = ownProps;
    const { eid } = config.item;
    const tabid = stateProps.tabid.get(eid) || data.tabids[0];
    const key = eid + "-" + tabid;
    let initdata = data.data[tabid];
    return {
        data: stateProps.data.get(key) || Immutable.fromJS(initdata),
        tabid: tabid,
        config: ownProps.config,
        edata: Immutable.fromJS(data),
        handleRefresh: ownProps.handleRefresh,
        actions: dispatchProps.actions
    };
}

export default connect(mapStateToProps, mapDispatchToProps, mergeProps)(EContent);
