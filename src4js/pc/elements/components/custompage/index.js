import { Spin } from 'antd';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import * as CustomPageAction from '../../actions/custompage/';
import Immutable from 'immutable';
import EHeader from '../common/EHeader';
import EContent from './Tab';
import { NoRightCom } from '../Common';
//元素组件
class CustomPage extends React.Component {
    componentDidMount(){
        const { config, actions } = this.props;
        actions.initCustomPageConfig(config.toJSON());
    }
    shouldComponentUpdate(nextProps){
        const { config, data, refresh } = this.props;
        return !Immutable.is(config,nextProps.config) || !Immutable.is(data,nextProps.data) || nextProps.refresh !== refresh
    }
    render() {
        const { data, refresh, actions } = this.props;
        let config = this.props.config.toJSON();
        const { isHasRight, item } = config;
        const { eid, ebaseid, content, header, contentview } = item;
        const edata = data.toJSON();
        let EContentHtml = <div width = "100%"></div>;
        if(isHasRight === 'true'){
            if (!_isEmpty(edata)) {
                EContentHtml = <EContent config={config} handleRefresh={actions.handleRefresh.bind(this)} data={edata}/>;
            }
        }else{
            EContentHtml = <NoRightCom/>
        }
        const portalParams = window.store_e9_element.getState().portal.get("params").toJSON();
        if (refresh && portalParams.loadIframe === 'false') {
            EContentHtml = <Spin>{EContentHtml}</Spin>
        }
        return <div className = "item" style = { { marginTop: '10px' } } id = { `item_${eid}` } data-eid = { eid } data-ebaseid = { ebaseid } data-needRefresh = { item.needRefresh } data-cornerTop = { item.cornerTop } data-cornerTopRadian = { item.cornerTopRadian } data-cornerBottom = { item.cornerBottom } data-cornerBottomRadian = { item.cornerBottomRadian }>
            <EHeader config = {config} handleRefresh={actions.handleRefresh.bind(this)}/>
            <div className = "content" id = { `content_${eid}` } style = { { width: 'auto', _width: '100%' } }>
                <div className = "content_view" id = { `content_view_id_${eid}` } style = {contentview.style}>
                    {EContentHtml }
                </div>
                <div style = { { textAlign: 'right' }} id = { `footer_${eid}` }></div>
            </div>
            </div>;
    }
}
import { WeaErrorPage, WeaTools } from 'ecCom';

class MyErrorHandler extends React.Component {
    render() {
        const hasErrorMsg = this.props.error && this.props.error !== "";
            return ( <WeaErrorPage msg = { hasErrorMsg ? this.props.error : "对不起，自定义页面加载异常，请联系管理员！" }/>
        );
    }
}
CustomPage = WeaTools.tryCatch(React, MyErrorHandler, { error: "" })(CustomPage);
const mapStateToProps = state => {
    const { elements } = state;
    return ({
        data: elements.get("data"),
        refresh: elements.get("refresh"),
        config: elements.get("config")
    })
}

function mapDispatchToProps(dispatch) {
    return {
        actions: bindActionCreators(CustomPageAction, dispatch)
    }
}

function mergeProps(stateProps, dispatchProps, ownProps) {
    const { eid } = ownProps.config.item;
    return {
        refresh: stateProps.refresh.get(eid) || false,
        data: stateProps.data.get(eid) || Immutable.fromJS({}),
        config: stateProps.config.get(eid) || Immutable.fromJS(ownProps.config),
        actions: dispatchProps.actions
    };
}
export default connect(mapStateToProps, mapDispatchToProps, mergeProps)(CustomPage);



