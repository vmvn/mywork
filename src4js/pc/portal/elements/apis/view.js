import { ELEMENT_URLS } from '../constants/ActionTypes';

import { WeaTools } from 'ecCom'; 

//获取门户信息api
const reqViewDatas = (params = {}) => {
    return WeaTools.callApi(ELEMENT_URLS.VIEW_URL, 'POST', params);
}
module.exports = {
    reqViewDatas
};
