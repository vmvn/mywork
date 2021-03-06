import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { REQ_URLS } from '../../constants/ActionTypes';
const { WORKFLOW_MORE_SPA_URL } = REQ_URLS;

import * as WorkflowTitleAction from '../../actions/workflow/title';
import OpToolbar from '../common/OpToolbar';
import Immutable from 'immutable';
import { initheight, backMarqueeDiv8, nextMarqueeDiv8 } from '../Common';

//回去老版本moreurl
let oldMoreHref="";
//tab的标题组件
class TitleContainer extends React.Component {
    componentDidMount() {
        const { config, tabids }=this.props;
        const { eid, header } = config.item;
        oldMoreHref = $("#more_" + eid).attr("data-morehref");
        this.setMoreHref();
        initheight(eid,tabids.length,header.canHeadbar);
    }
    componentDidUpdate() {
        const { config, tabids }=this.props;
        const { eid, header } = config.item;
        this.setMoreHref();
        initheight(eid,tabids.length,header.canHeadbar);
    }
    setMoreHref() {
        const { config, more } = this.props;
        const { eid } = config.item;
        if (more && JSON.parse(more).viewType !=="6") {
            $("#more_" + eid).attr("data-morehref", WORKFLOW_MORE_SPA_URL + escape(more));
        } else {
            $("#more_" + eid).attr("data-morehref", oldMoreHref);
        }
    }
    handleOnChangeTab(tabid) {
        const { params, actions, dispatch } = this.props;
        const { eid } = params;
        actions.onChangeTab(tabid,params);
    }
    render() {
        const tdStyle={ wordWrap: 'break-word', paddingTop: '5px', verticalAlign: 'top', height: '32px' };
        const { tabid, params, config, tabids, titles, counts, actions, handleRefresh } = this.props;
        const { eid, ebaseid } = params;
        let tHtml=titles.map((title, i)=> {
            if (!_isEmpty(counts) && '' !== counts[i]) {
                 title +=' (' + counts[i] + ')';  
            }
            const className = tabids[i] == tabid ? "tab2selected" : "tab2unselected";
            return <td title={title} data-tabid={tabids[i]} style={tdStyle} className={className} onClick={this.handleOnChangeTab.bind(this,tabids[i])}>{title}</td>
        });
        let className = tabids.length <= 1 ? 'nodisplay' : '';
        return <div id={ `titleContainer_${eid}` } className={`titlecontainer ${className}`}>
                {<div id={`tabnavprev_${eid}`} className="picturebackhp" onClick={backMarqueeDiv8.bind(this,eid)}></div>}
                <div id={ `tabContainer_${eid}` } className="tabcontainer tab2">
                    <table style={ { tableLayout: "fixed", borderCollapse: 'collapse'} } height="32">
                      <tbody height="32">
                        <tr height="32">
                          {tHtml} 
                        </tr> 
                      </tbody>
                    </table>
                </div> 
                {<div id={`tabnavnext_${eid}`} className="picturenexthp" onClick={nextMarqueeDiv8.bind(this,eid)}></div>} 
                {config.item.header.canHeadbar === 'false' ? <OpToolbar config={config} clsname={className} handleRefresh={handleRefresh}/> : null} 
              </div>
      }
}
import { WeaErrorPage, WeaTools } from 'ecCom';
class MyErrorHandler extends React.Component {
    render() {
        const hasErrorMsg=this.props.error && this.props.error !=="";
            return ( <WeaErrorPage msg={ hasErrorMsg ? this.props.error : "对不起，该页面异常，请联系管理员！" } />
        );
    }
}
TitleContainer = WeaTools.tryCatch(React, MyErrorHandler, { error: "" })(TitleContainer);

const mapStateToProps = state => {
    const { eworkflowtitle } = state;
    return ({
        tabid: eworkflowtitle.get("tabid")
    })
}

function mapDispatchToProps(dispatch) {
    return {
        actions: bindActionCreators(WorkflowTitleAction, dispatch)
    }
}

function mergeProps(stateProps, dispatchProps, ownProps) {
    const { eid } = ownProps.config.item;
    return {
        tabid: stateProps.tabid.get(eid) || ownProps.params.tabid,
        tabids: ownProps.tabids,
        params: ownProps.params,
        counts: ownProps.counts,
        config: ownProps.config,
        titles: ownProps.titles,
        more:ownProps.more,
        currCount:ownProps.currCount,
        handleRefresh:ownProps.handleRefresh,
        actions: dispatchProps.actions
    };
}
export default connect(mapStateToProps, mapDispatchToProps,mergeProps)(TitleContainer);
