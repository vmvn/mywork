import { Icon } from 'antd'
import { WeaTools ,WeaScroll} from 'ecCom'

export default class OGroup extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			showall: false,
			hrmgroups: [],
			allUserIds: '',
			allUserCount: 0,
			showAllUser: false
		};

		const _this = this;
		WeaTools.callApi('/api/workflow/hrmgroup/datas', 'GET', {}).then(data => {
			_this.setState({ hrmgroups: data.datas });
		});
	}
	
	componentWillReceiveProps(nextProps) {
		if(!nextProps.isshowoperategroup){
			this.setState({showall:false});
		}
	}

	shouldComponentUpdate(nextProps, nextState) {
		return this.state.hrmgroups !== nextState.hrmgroups ||
			this.state.showall !== nextState.showall ||
			this.state.showAllUser !== nextState.showAllUser ||
			this.state.allUserCount !== nextState.allUserCount;
	}

	//添加常用组
	addgroup(handleVisibleChange) {
		handleVisibleChange(false);
		const languageid = readCookie("languageidweaver");
		const dialog = new window.top.Dialog();
		dialog.currentWindow = window;
		dialog.Title = '新建自定义组';
		dialog.Width = 550;
		dialog.Height = 550;
		dialog.Drag = true;
		dialog.maxiumnable = true;
		dialog.URL = "/hrm/HrmDialogTab.jsp?_fromURL=hrmGroup&method=HrmGroupAdd&isdialog=1";
		dialog.show();
	}

	add(setOperatorIds, handleVisibleChange, groupobj) {
		//公共組
		let params = {isAllUser:false};
		if(groupobj.grouptype == '4') {
			params.datas = [groupobj];
		}

		if(groupobj.grouptype == '6') {
			params.datas = groupobj.users;
		}
		setOperatorIds(params);
		handleVisibleChange(false);
	}

	showAllOperators() {
		const { showAllUser, allUserIds, allUserCount } = this.state;
		if(showAllUser) {
			const { handleVisibleChange, setOperatorIds } = this.props;
			let params  = {ids:allUserIds,isAllUser:true,count:allUserCount};
			setOperatorIds(params);
			handleVisibleChange(false);
		} else {
			WeaTools.callApi('/api/workflow/hrmgroup/datas', 'GET', { isgetallres: '1' }).then(data => {
				this.setState({ showAllUser: true, allUserIds: data.ids, allUserCount: data.count });
			});
		}
	}
	render() {
		const { handleVisibleChange, setOperatorIds } = this.props;
		const { showall, hrmgroups, showAllUser, allUserCount } = this.state;
		
		return(
			<div className="wea-req-operate-group">
				<div className="wea-req-all-operators" onClick={this.showAllOperators.bind(this)}>
					<span>所有人{showAllUser && "（"+allUserCount+"）"}</span>
				</div>
				<div className="wea-req-operate-content">
					<WeaScroll className="wea-scroll" typeClass="scrollbar-macosx" >
						<ul>
							{hrmgroups && 
								hrmgroups.map((o,index)=>{
									if(index > 2 && !showall){
										return true;
									}
									const count = o.users.length;
									return  <li onClick={this.add.bind(this,setOperatorIds,handleVisibleChange,o)}>
												<span className='cg_title'>{`${o.lastname}（${count}人）`}</span>
												<span className='cg_detail'>{o.names}</span>
											</li>
								})
							}
						</ul>
					</WeaScroll>
				</div>
				{hrmgroups.length > 3 && !showall &&
					<div className="wea-req-operate-load-more" onClick={()=>this.setState({showall:true})}>
						<Icon type="down" />
						<span style={{'color':'#4397d3','margin-left':'10px'}}>显示全部</span>
					</div>
				}
				<div className="wea-req-operate-add" onClick={() => this.addgroup(handleVisibleChange)}>
					<span style={{'color':'#59b632'}}><Icon type="plus-square" /></span>
					<span style={{'color':'#323232','margin-left':'10px'}} >添加常用组</span>
				</div>
			</div>
		)
	}
}