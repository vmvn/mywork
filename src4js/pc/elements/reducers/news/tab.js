import { NEWS_STATE_TYPES } from '../../constants/ActionTypes';
const { NEWS_TAB_DATA, NEWS_TAB_REFRESH, NEWS_TAB_TABID } = NEWS_STATE_TYPES;
import Immutable from 'immutable';
const initialState = Immutable.fromJS({
    data: {},
    refresh:{},
    tabid: {}
});
export default function ersstab(state = initialState, action) {
    switch (action.type) {
        case NEWS_TAB_DATA:
            return state.merge({
                data: action.data,
                refresh: action.refresh,
                tabid: action.tabid
            })
        case NEWS_TAB_TABID:
            return state.merge({
                tabid: action.tabid
            })
        case NEWS_TAB_REFRESH:
            return state.merge({
                tabid: action.tabid,
                refresh: action.refresh
            })
        default:
            return state
    }
}