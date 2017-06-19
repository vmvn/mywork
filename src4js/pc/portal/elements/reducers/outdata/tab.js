import { OUTDATA_STATE_TYPES } from '../../constants/ActionTypes';
const { OUTDATA_TAB_DATA, OUTDATA_TAB_REFRESH, OUTDATA_TAB_TABID } = OUTDATA_STATE_TYPES;
import Immutable from 'immutable';
const initialState = Immutable.fromJS({
    data: {},
    refresh:{},
    tabid: {}
});
export default function eoutdatatab(state = initialState, action) {
    switch (action.type) {
        case OUTDATA_TAB_DATA:
            return state.merge({
                data: action.data,
                refresh: action.refresh,
                tabid: action.tabid
            })
        case OUTDATA_TAB_TABID:
            return state.merge({
                tabid: action.tabid
            })
        case OUTDATA_TAB_REFRESH:
            return state.merge({
                tabid: action.tabid,
                refresh: action.refresh
            })
        default:
            return state
    }
}