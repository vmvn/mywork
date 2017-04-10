import { ELEMENT_URLS } from '../constants/ActionTypes';

import { WeaTools } from 'ecCom'; 

//获取门户信息api
const reqAddWfDatas = (params = {}) => {
    return WeaTools.callApi(ELEMENT_URLS.ADDWF_URL, 'POST', params);
}

const reqAddWfTabDatas = (params = {}) => {
    return WeaTools.callApi(ELEMENT_URLS.ADDWF_TAB_URL, 'POST', params);
}

module.exports = {
    reqAddWfDatas,
    reqAddWfTabDatas
};
