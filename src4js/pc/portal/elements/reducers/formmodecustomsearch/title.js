import { FORMMODECUSTOMSEARCH_STATE_TYPES } from '../../constants/ActionTypes';
const { CHANGE_FORMMODECUSTOMSEARCH_TABID } = FORMMODECUSTOMSEARCH_STATE_TYPES;
import Immutable from 'immutable';
const initialState = Immutable.fromJS({
    tabid:{}
});
export default function eformmodecustomsearchtitle(state = initialState, action) {
    switch (action.type) {
        case CHANGE_FORMMODECUSTOMSEARCH_TABID:
            return state.merge({
                tabid: action.tabid
            })
        default:
            return state
    }
}
